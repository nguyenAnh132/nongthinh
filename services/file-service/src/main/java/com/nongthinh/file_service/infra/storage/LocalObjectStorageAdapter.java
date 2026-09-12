package com.nongthinh.file_service.infra.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.springframework.stereotype.Component;
import com.nongthinh.file_service.application.port.out.storage.ObjectStoragePort;
import com.nongthinh.file_service.configuration.StorageProperties;
import com.nongthinh.file_service.domain.file.valueobject.StorageProvider;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LocalObjectStorageAdapter implements ObjectStoragePort {

    private final StorageProperties storageProperties;
    private Path basePath;

    @PostConstruct
    void init() throws IOException {
        basePath = Path.of(storageProperties.getBasePath()).toAbsolutePath().normalize();
        Files.createDirectories(basePath);
    }

    @Override
    public StorageProvider provider() {
        return StorageProvider.LOCAL;
    }

    @Override
    public void putObject(String bucket, String objectKey, String contentType, long sizeBytes, InputStream inputStream) {
        try {
            Path target = resolvePath(objectKey);
            Files.createDirectories(target.getParent());
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to store file locally", ex);
        }
    }

    @Override
    public void deleteObject(String bucket, String objectKey) {
        try {
            Files.deleteIfExists(resolvePath(objectKey));
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to delete local file", ex);
        }
    }

    @Override
    public InputStream getObject(String bucket, String objectKey) {
        try {
            return Files.newInputStream(resolvePath(objectKey));
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read local file", ex);
        }
    }

    @Override
    public String buildPublicUrl(String objectKey) {
        String fileId = extractFileIdFromObjectKey(objectKey);
        String baseUrl = storageProperties.getPublicBaseUrl();
        if (baseUrl.endsWith("/")) {
            return baseUrl + fileId;
        }
        return baseUrl + "/" + fileId;
    }

    private Path resolvePath(String objectKey) {
        Path resolvedPath = basePath.resolve(objectKey).normalize();
        if (!resolvedPath.startsWith(basePath)) {
            throw new IllegalArgumentException("Invalid object storage key");
        }
        return resolvedPath;
    }

    private String extractFileIdFromObjectKey(String objectKey) {
        String fileName = objectKey.substring(objectKey.lastIndexOf('/') + 1);
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            return fileName.substring(0, dotIndex);
        }
        return fileName;
    }
}
