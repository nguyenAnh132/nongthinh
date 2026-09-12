package com.nongthinh.rice_disease_diagnosis_service.infra.client.fileservice;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.nongthinh.rice_disease_diagnosis_service.application.port.out.FileArtifactPort;
import com.nongthinh.rice_disease_diagnosis_service.application.port.out.DiagnosisFilePort;
import com.nongthinh.rice_disease_diagnosis_service.application.model.DiagnosisFileMetadata;
import com.nongthinh.rice_disease_diagnosis_service.configuration.DiagnosisProperties;
import com.nongthinh.rice_disease_diagnosis_service.configuration.ModelValidationProperties;
import feign.FeignException;
import feign.Response;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FileServiceArtifactAdapter implements FileArtifactPort, DiagnosisFilePort {

    private final FileServiceClient fileServiceClient;
    private final ModelValidationProperties modelValidationProperties;
    private final DiagnosisProperties diagnosisProperties;

    @Value("${spring.security.api-key.clients.file-service.api-key}")
    private String fileServiceApiKey;

    @Override
    public byte[] getArtifactContent(UUID artifactFileId) {
        return getContent(artifactFileId, modelValidationProperties.maxArtifactBytes());
    }

    @Override
    public byte[] getFileContent(UUID fileId) {
        return getContent(fileId, diagnosisProperties.maxImageBytes());
    }

    @Override
    public DiagnosisFileMetadata getFileMetadata(UUID fileId) {
        try {
            FileServiceResponse<FileMetadataDto> response = fileServiceClient.getMetadata(fileId, fileServiceApiKey);
            if (response == null || response.result() == null) {
                throw new IllegalStateException("File metadata is unavailable");
            }
            FileMetadataDto file = response.result();
            return new DiagnosisFileMetadata(
                    file.id(), file.ownerUserId(), file.purpose(), file.originalFileName(),
                    file.contentType(), file.sizeBytes(), file.status(), file.createdAt(), file.updatedAt());
        } catch (RuntimeException ex) {
            throw new IllegalStateException("File metadata is unavailable", ex);
        }
    }

    @Override
    public void deleteDiagnosisImage(UUID fileId) {
        try {
            fileServiceClient.deleteDiagnosisImage(fileId, fileServiceApiKey);
        } catch (RuntimeException ex) {
            throw new IllegalStateException("Diagnosis image cleanup is unavailable", ex);
        }
    }

    private byte[] getContent(UUID artifactFileId, long maxBytes) {
        try (Response response = fileServiceClient.getContent(artifactFileId, fileServiceApiKey)) {
            if (response.status() != 200 || response.body() == null) {
                throw new IllegalStateException("Artifact content is unavailable");
            }
            validateContentLength(response.headers().get("content-length"), maxBytes);
            try (InputStream input = response.body().asInputStream()) {
                return readBounded(input, maxBytes);
            }
        } catch (FeignException ex) {
            throw new IllegalStateException("Artifact content is unavailable", ex);
        } catch (IOException ex) {
            throw new IllegalStateException("Artifact content cannot be read", ex);
        }
    }

    private void validateContentLength(Collection<String> values, long maxBytes) {
        if (values == null || values.isEmpty()) {
            return;
        }
        long size = Long.parseLong(values.iterator().next());
        if (size > maxBytes) {
            throw new IllegalStateException("Artifact exceeds configured size limit");
        }
    }

    private byte[] readBounded(InputStream input, long maxBytes) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        long total = 0;
        while ((read = input.read(buffer)) != -1) {
            total += read;
            if (total > maxBytes) {
                throw new IllegalStateException("Artifact exceeds configured size limit");
            }
            output.write(buffer, 0, read);
        }
        return output.toByteArray();
    }
}
