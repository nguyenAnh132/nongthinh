package com.nongthinh.rice_disease_diagnosis_service.application.port.in.modelvalidation.impl;

import ai.onnxruntime.NodeInfo;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import ai.onnxruntime.TensorInfo;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import com.nongthinh.rice_disease_diagnosis_service.application.command.ModelArtifactValidationCommand;
import com.nongthinh.rice_disease_diagnosis_service.application.command.ModelClassManifestItem;
import com.nongthinh.rice_disease_diagnosis_service.application.model.ModelArtifactValidationResult;
import com.nongthinh.rice_disease_diagnosis_service.application.port.in.modelvalidation.ValidateModelArtifactUseCase;
import com.nongthinh.rice_disease_diagnosis_service.application.port.out.FileArtifactPort;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ValidateModelArtifactUseCaseImpl implements ValidateModelArtifactUseCase {

    private final FileArtifactPort fileArtifactPort;

    @Override
    public ModelArtifactValidationResult execute(ModelArtifactValidationCommand command) {
        if (!hasValidManifest(command)) {
            return failed("MANIFEST_INVALID");
        }

        byte[] artifact;
        try {
            artifact = fileArtifactPort.getArtifactContent(command.artifactFileId());
        } catch (RuntimeException ex) {
            return failed("ARTIFACT_UNAVAILABLE");
        }

        if (!sha256(artifact).equalsIgnoreCase(command.artifactSha256())) {
            return failed("CHECKSUM_MISMATCH");
        }

        try (OrtSession.SessionOptions options = new OrtSession.SessionOptions();
                OrtSession session = OrtEnvironment.getEnvironment().createSession(artifact, options)) {
            if (!hasExpectedInput(session.getInputInfo(), command.inputWidth(), command.inputHeight())) {
                return failed("INPUT_DIMENSION_MISMATCH");
            }
            if (!hasCompatibleOutput(session.getOutputInfo(), command.classes().size())) {
                return failed("TENSOR_CLASS_MISMATCH");
            }
            return new ModelArtifactValidationResult(
                    true,
                    "{\"status\":\"VALIDATED\",\"checks\":{\"checksum\":\"PASSED\",\"onnx\":\"PASSED\",\"input\":\"PASSED\",\"classes\":\"PASSED\"}}"
            );
        } catch (OrtException | RuntimeException ex) {
            return failed("ONNX_INVALID");
        }
    }

    private boolean hasValidManifest(ModelArtifactValidationCommand command) {
        if (command == null
                || command.artifactFileId() == null
                || command.artifactSha256() == null
                || !command.artifactSha256().matches("(?i)^[a-f0-9]{64}$")
                || command.inputWidth() <= 0
                || command.inputHeight() <= 0
                || command.classes() == null
                || command.classes().isEmpty()) {
            return false;
        }

        Set<Integer> indexes = new HashSet<>();
        Set<String> codes = new HashSet<>();
        for (ModelClassManifestItem item : command.classes()) {
            if (item == null
                    || item.classIndex() < 0
                    || item.classCode() == null
                    || item.classCode().isBlank()
                    || !indexes.add(item.classIndex())
                    || !codes.add(item.classCode())
                    || !("DISEASE".equals(item.classKind()) || "HEALTHY".equals(item.classKind()))) {
                return false;
            }
        }
        return indexes.size() == command.classes().size()
                && indexes.stream().allMatch(index -> index < command.classes().size());
    }

    private boolean hasExpectedInput(Map<String, NodeInfo> inputInfo, int width, int height) {
        if (inputInfo.size() != 1) {
            return false;
        }
        NodeInfo input = inputInfo.values().iterator().next();
        if (!(input.getInfo() instanceof TensorInfo tensorInfo)) {
            return false;
        }
        long[] shape = tensorInfo.getShape();
        if (shape.length != 4) {
            return false;
        }
        boolean nchw = matches(shape[2], height) && matches(shape[3], width);
        boolean nhwc = matches(shape[1], height) && matches(shape[2], width);
        return nchw || nhwc;
    }

    private boolean hasCompatibleOutput(Map<String, NodeInfo> outputInfo, int classCount) {
        return outputInfo.values().stream()
                .map(NodeInfo::getInfo)
                .filter(TensorInfo.class::isInstance)
                .map(TensorInfo.class::cast)
                .map(TensorInfo::getShape)
                .anyMatch(shape -> {
                    if (shape.length < 2) {
                        return false;
                    }
                    for (int index = 1; index < shape.length; index++) {
                        long dimension = shape[index];
                        if (dimension == classCount
                                || dimension == classCount + 4L
                                || dimension == classCount + 5L) {
                            return true;
                        }
                    }
                    return false;
                });
    }

    private boolean matches(long modelDimension, int expectedDimension) {
        return modelDimension == -1 || modelDimension == expectedDimension;
    }

    private String sha256(byte[] content) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(content);
            StringBuilder value = new StringBuilder(digest.length * 2);
            for (byte item : digest) {
                value.append(String.format("%02x", item));
            }
            return value.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }

    private ModelArtifactValidationResult failed(String code) {
        return new ModelArtifactValidationResult(
                false,
                "{\"status\":\"FAILED\",\"code\":\"" + code + "\"}"
        );
    }
}
