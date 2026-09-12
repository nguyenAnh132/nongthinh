package com.nongthinh.rice_disease_diagnosis_service.application.service;

import ai.onnxruntime.NodeInfo;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import ai.onnxruntime.TensorInfo;
import java.nio.FloatBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;
import com.nongthinh.rice_disease_diagnosis_service.application.command.ModelRuntimeWarmupCommand;
import com.nongthinh.rice_disease_diagnosis_service.application.port.out.FileArtifactPort;
import com.nongthinh.rice_disease_diagnosis_service.configuration.ModelValidationProperties;

@Component
public class ModelRuntimeManager {

    private final FileArtifactPort fileArtifactPort;
    private final ModelValidationProperties properties;
    private final ConcurrentHashMap<UUID, RuntimeHandle> runtimes = new ConcurrentHashMap<>();

    public ModelRuntimeManager(FileArtifactPort fileArtifactPort, ModelValidationProperties properties) {
        this.fileArtifactPort = fileArtifactPort;
        this.properties = properties;
    }

    public boolean warm(ModelRuntimeWarmupCommand command) {
        if (!isValid(command)) {
            return false;
        }
        try {
            byte[] artifact = fileArtifactPort.getArtifactContent(command.artifactFileId());
            if (!sha256(artifact).equalsIgnoreCase(command.artifactSha256())) {
                return false;
            }
            OrtSession session = createAndValidateSession(artifact, command.inputWidth(), command.inputHeight());
            RuntimeHandle replacement = new RuntimeHandle(session);
            RuntimeHandle previous = runtimes.put(command.modelVersionId(), replacement);
            if (previous != null) {
                previous.retire();
            }
            evictIdleRuntimes(command.modelVersionId());
            return true;
        } catch (RuntimeException | OrtException ex) {
            return false;
        }
    }

    public Optional<ModelRuntimeLease> acquire(UUID modelVersionId) {
        RuntimeHandle handle = runtimes.get(modelVersionId);
        return handle != null && handle.tryRetain()
                ? Optional.of(new ModelRuntimeLease(handle))
                : Optional.empty();
    }

    public int cachedVersionCount() {
        return runtimes.size();
    }

    private OrtSession createAndValidateSession(byte[] artifact, int width, int height) throws OrtException {
        OrtSession.SessionOptions options = new OrtSession.SessionOptions();
        try {
            OrtSession session = OrtEnvironment.getEnvironment().createSession(artifact, options);
            if (!hasExpectedInput(session.getInputInfo(), width, height)) {
                session.close();
                throw new OrtException("Input dimension mismatch");
            }
            runWarmupInference(session, width, height);
            return session;
        } finally {
            options.close();
        }
    }

    private void runWarmupInference(OrtSession session, int width, int height) throws OrtException {
        Map.Entry<String, NodeInfo> input = session.getInputInfo().entrySet().iterator().next();
        if (!(input.getValue().getInfo() instanceof TensorInfo tensorInfo)) {
            throw new OrtException("Model input must be a tensor");
        }
        long[] tensorShape = resolvedInputShape(tensorInfo.getShape(), width, height);
        long elementCount = 1;
        for (int index = 0; index < tensorShape.length; index++) {
            long dimension = tensorShape[index] <= 0 ? 1 : tensorShape[index];
            if (dimension > Integer.MAX_VALUE || elementCount > Integer.MAX_VALUE / dimension) {
                throw new OrtException("Model input is too large to warm up");
            }
            tensorShape[index] = dimension;
            elementCount *= dimension;
        }
        try (OnnxTensor tensor = OnnxTensor.createTensor(
                OrtEnvironment.getEnvironment(), FloatBuffer.wrap(new float[(int) elementCount]), tensorShape);
                OrtSession.Result ignored = session.run(Map.of(input.getKey(), tensor))) {
            // A complete zero-value run verifies native session initialization before it is published.
        }
    }

    static long[] resolvedInputShape(long[] inputShape, int width, int height) throws OrtException {
        long[] shape = inputShape.clone();
        if (shape.length != 4) {
            throw new OrtException("Model input must have four dimensions");
        }
        boolean nhwc = shape[3] == 3 && shape[1] != 3;
        if (shape[0] <= 0) shape[0] = 1;
        if (nhwc) {
            if (shape[1] <= 0) shape[1] = height;
            if (shape[2] <= 0) shape[2] = width;
            if (shape[3] <= 0) shape[3] = 3;
            return shape;
        }
        if (shape[1] <= 0) shape[1] = 3;
        if (shape[2] <= 0) shape[2] = height;
        if (shape[3] <= 0) shape[3] = width;
        return shape;
    }

    private boolean hasExpectedInput(Map<String, NodeInfo> inputInfo, int width, int height) {
        if (inputInfo.size() != 1) {
            return false;
        }
        if (!(inputInfo.values().iterator().next().getInfo() instanceof TensorInfo tensorInfo)) {
            return false;
        }
        long[] shape = tensorInfo.getShape();
        return shape.length == 4
                && ((matches(shape[2], height) && matches(shape[3], width))
                || (matches(shape[1], height) && matches(shape[2], width)));
    }

    private void evictIdleRuntimes(UUID protectedVersionId) {
        while (runtimes.size() > properties.maxCachedModelVersions()) {
            Map.Entry<UUID, RuntimeHandle> candidate = runtimes.entrySet().stream()
                    .filter(entry -> !entry.getKey().equals(protectedVersionId) && entry.getValue().isIdle())
                    .min(Map.Entry.comparingByValue((left, right) -> left.lastUsed().compareTo(right.lastUsed())))
                    .orElse(null);
            if (candidate == null || !runtimes.remove(candidate.getKey(), candidate.getValue())) {
                return;
            }
            candidate.getValue().retire();
        }
    }

    private boolean isValid(ModelRuntimeWarmupCommand command) {
        return command != null
                && command.modelVersionId() != null
                && command.artifactFileId() != null
                && command.artifactSha256() != null
                && command.artifactSha256().matches("(?i)^[a-f0-9]{64}$")
                && command.inputWidth() > 0
                && command.inputHeight() > 0;
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

    public static final class ModelRuntimeLease implements AutoCloseable {
        private final RuntimeHandle handle;
        private final AtomicBoolean closed = new AtomicBoolean();

        private ModelRuntimeLease(RuntimeHandle handle) {
            this.handle = handle;
        }

        public OrtSession session() {
            return handle.session();
        }

        @Override
        public void close() {
            if (closed.compareAndSet(false, true)) {
                handle.release();
            }
        }
    }

    private static final class RuntimeHandle {
        private final OrtSession session;
        private final AtomicInteger references = new AtomicInteger();
        private final AtomicBoolean retired = new AtomicBoolean();
        private volatile Instant lastUsed = Instant.now();

        private RuntimeHandle(OrtSession session) {
            this.session = session;
        }

        private boolean tryRetain() {
            if (retired.get()) {
                return false;
            }
            references.incrementAndGet();
            if (retired.get()) {
                release();
                return false;
            }
            lastUsed = Instant.now();
            return true;
        }

        private void release() {
            references.decrementAndGet();
            lastUsed = Instant.now();
            closeIfRetiredAndIdle();
        }

        private void retire() {
            retired.set(true);
            closeIfRetiredAndIdle();
        }

        private boolean isIdle() {
            return references.get() == 0;
        }

        private Instant lastUsed() {
            return lastUsed;
        }

        private OrtSession session() {
            return session;
        }

        private void closeIfRetiredAndIdle() {
            if (retired.get() && references.get() == 0) {
                try {
                    session.close();
                } catch (OrtException ignored) {
                    // Native resources are already being retired; there is no safe recovery action.
                }
            }
        }
    }
}
