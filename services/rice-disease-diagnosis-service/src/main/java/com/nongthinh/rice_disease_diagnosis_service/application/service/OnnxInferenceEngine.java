package com.nongthinh.rice_disease_diagnosis_service.application.service;

import ai.onnxruntime.NodeInfo;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OnnxValue;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import ai.onnxruntime.TensorInfo;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import org.springframework.stereotype.Component;
import com.nongthinh.rice_disease_diagnosis_service.application.command.ModelRuntimeWarmupCommand;
import com.nongthinh.rice_disease_diagnosis_service.application.model.ActiveModelDeployment;
import com.nongthinh.rice_disease_diagnosis_service.application.model.InferenceImageResult;
import com.nongthinh.rice_disease_diagnosis_service.common.exception.DiagnosisException;
import com.nongthinh.rice_disease_diagnosis_service.common.exception.ErrorCode;
import com.nongthinh.rice_disease_diagnosis_service.configuration.DiagnosisProperties;

@Component
public class OnnxInferenceEngine {

    private final ModelRuntimeManager modelRuntimeManager;
    private final ImagePreprocessor imagePreprocessor;
    private final DetectionPostProcessor postProcessor;
    private final DiagnosisProperties properties;
    private final Semaphore inferenceSlots;

    public OnnxInferenceEngine(
            ModelRuntimeManager modelRuntimeManager,
            ImagePreprocessor imagePreprocessor,
            DetectionPostProcessor postProcessor,
            DiagnosisProperties properties) {
        this.modelRuntimeManager = modelRuntimeManager;
        this.imagePreprocessor = imagePreprocessor;
        this.postProcessor = postProcessor;
        this.properties = properties;
        this.inferenceSlots = new Semaphore(properties.maxConcurrentInferences(), true);
    }

    public InferenceImageResult infer(ActiveModelDeployment deployment, byte[] imageBytes) {
        if (!inferenceSlots.tryAcquire()) {
            throw new DiagnosisException(ErrorCode.DIAGNOSIS_CAPACITY_EXCEEDED);
        }
        try {
            return runInference(deployment, imageBytes);
        } finally {
            inferenceSlots.release();
        }
    }

    private InferenceImageResult runInference(ActiveModelDeployment deployment, byte[] imageBytes) {
        ModelRuntimeManager.ModelRuntimeLease lease = modelRuntimeManager.acquire(deployment.modelVersionId())
                .orElseGet(() -> warmAndAcquire(deployment));
        try (lease) {
            OrtSession session = lease.session();
            String inputName = session.getInputNames().stream().findFirst()
                    .orElseThrow(() -> new DiagnosisException(ErrorCode.MODEL_NOT_READY));
            boolean nhwc = isNhwc(session.getInputInfo().get(inputName));
            ImagePreprocessor.PreprocessedImage image = imagePreprocessor.preprocess(
                    imageBytes, deployment.inputWidth(), deployment.inputHeight(), nhwc, properties.maxImagePixels());
            try (OnnxTensor input = OnnxTensor.createTensor(
                    OrtEnvironment.getEnvironment(), FloatBuffer.wrap(image.values()), image.tensorShape());
                    OrtSession.Result result = session.run(Map.of(inputName, input))) {
                Object output = firstOutput(result);
                return new InferenceImageResult(
                        image.originalWidth(), image.originalHeight(), postProcessor.parse(output, deployment, image));
            } catch (OrtException | RuntimeException ex) {
                if (ex instanceof DiagnosisException diagnosisException) throw diagnosisException;
                throw new DiagnosisException(ErrorCode.MODEL_NOT_READY, ex);
            }
        } catch (OrtException ex) {
            throw new DiagnosisException(ErrorCode.MODEL_NOT_READY, ex);
        }
    }

    private ModelRuntimeManager.ModelRuntimeLease warmAndAcquire(ActiveModelDeployment deployment) {
        boolean warmed = modelRuntimeManager.warm(new ModelRuntimeWarmupCommand(
                deployment.modelVersionId(), deployment.artifactFileId(), deployment.artifactSha256(),
                deployment.inputWidth(), deployment.inputHeight()));
        if (!warmed) throw new DiagnosisException(ErrorCode.MODEL_NOT_READY);
        return modelRuntimeManager.acquire(deployment.modelVersionId())
                .orElseThrow(() -> new DiagnosisException(ErrorCode.MODEL_NOT_READY));
    }

    private boolean isNhwc(NodeInfo inputInfo) {
        if (!(inputInfo.getInfo() instanceof TensorInfo tensorInfo)) return false;
        long[] shape = tensorInfo.getShape();
        return shape.length == 4 && shape[1] != 3 && shape[3] == 3;
    }

    private Object firstOutput(OrtSession.Result result) throws OrtException {
        for (Map.Entry<String, OnnxValue> entry : result) {
            return entry.getValue().getValue();
        }
        throw new DiagnosisException(ErrorCode.MODEL_NOT_READY);
    }
}
