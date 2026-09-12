package com.nongthinh.rice_disease_diagnosis_service.application.port.in.modelvalidation.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import com.nongthinh.rice_disease_diagnosis_service.application.command.ModelArtifactValidationCommand;
import com.nongthinh.rice_disease_diagnosis_service.application.command.ModelClassManifestItem;
import com.nongthinh.rice_disease_diagnosis_service.application.port.out.FileArtifactPort;

class ValidateModelArtifactUseCaseImplTest {

    @Test
    void rejectsChecksumMismatchBeforeTryingToLoadOnnx() {
        FileArtifactPort artifactPort = ignored -> "model-bytes".getBytes(StandardCharsets.UTF_8);
        ValidateModelArtifactUseCaseImpl useCase = new ValidateModelArtifactUseCaseImpl(artifactPort);

        var result = useCase.execute(command("a".repeat(64)));

        assertFalse(result.valid());
        assertTrue(result.report().contains("CHECKSUM_MISMATCH"));
    }

    @Test
    void rejectsMalformedManifestBeforeReadingArtifact() {
        FileArtifactPort artifactPort = ignored -> { throw new AssertionError("artifact must not be read"); };
        ValidateModelArtifactUseCaseImpl useCase = new ValidateModelArtifactUseCaseImpl(artifactPort);

        var result = useCase.execute(new ModelArtifactValidationCommand(
                UUID.randomUUID(), UUID.randomUUID(), "a".repeat(64), 640, 640,
                List.of(new ModelClassManifestItem(1, "Brown_Spot", "DISEASE"))));

        assertFalse(result.valid());
        assertTrue(result.report().contains("MANIFEST_INVALID"));
    }

    private ModelArtifactValidationCommand command(String checksum) {
        return new ModelArtifactValidationCommand(
                UUID.randomUUID(),
                UUID.randomUUID(),
                checksum,
                640,
                640,
                List.of(new ModelClassManifestItem(0, "Brown_Spot", "DISEASE"))
        );
    }
}
