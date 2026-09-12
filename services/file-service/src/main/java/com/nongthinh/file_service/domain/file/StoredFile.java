package com.nongthinh.file_service.domain.file;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import com.nongthinh.file_service.domain.file.valueobject.FilePurpose;
import com.nongthinh.file_service.domain.file.valueobject.FileStatus;
import com.nongthinh.file_service.domain.file.valueobject.FileVisibility;
import com.nongthinh.file_service.domain.file.valueobject.StorageProvider;

public final class StoredFile {

    private final UUID id;
    private final UUID ownerUserId;
    private final FilePurpose purpose;
    private final StorageProvider storageProvider;
    private final String bucket;
    private final String objectKey;
    private final String originalFileName;
    private final String contentType;
    private final long sizeBytes;
    private final String publicUrl;
    private final FileVisibility visibility;
    private FileStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    private StoredFile(
            UUID id,
            UUID ownerUserId,
            FilePurpose purpose,
            StorageProvider storageProvider,
            String bucket,
            String objectKey,
            String originalFileName,
            String contentType,
            long sizeBytes,
            String publicUrl,
            FileVisibility visibility,
            FileStatus status,
            Instant createdAt,
            Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.ownerUserId = Objects.requireNonNull(ownerUserId, "ownerUserId is required");
        this.purpose = Objects.requireNonNull(purpose, "purpose is required");
        this.storageProvider = Objects.requireNonNull(storageProvider, "storageProvider is required");
        this.bucket = Objects.requireNonNull(bucket, "bucket is required");
        this.objectKey = Objects.requireNonNull(objectKey, "objectKey is required");
        this.originalFileName = Objects.requireNonNull(originalFileName, "originalFileName is required");
        this.contentType = Objects.requireNonNull(contentType, "contentType is required");
        this.sizeBytes = sizeBytes;
        this.visibility = Objects.requireNonNull(visibility, "visibility is required");
        this.publicUrl = visibility == FileVisibility.PUBLIC
                ? Objects.requireNonNull(publicUrl, "publicUrl is required for a public file")
                : null;
        this.status = Objects.requireNonNull(status, "status is required");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt is required");
    }

    public static StoredFile create(
            UUID id,
            UUID ownerUserId,
            FilePurpose purpose,
            StorageProvider storageProvider,
            String bucket,
            String objectKey,
            String originalFileName,
            String contentType,
            long sizeBytes,
            String publicUrl,
            Instant now) {
        return new StoredFile(
                id,
                ownerUserId,
                purpose,
                storageProvider,
                bucket,
                objectKey,
                originalFileName,
                contentType,
                sizeBytes,
                publicUrl,
                FileVisibility.forPurpose(purpose),
                FileStatus.UPLOADED,
                now,
                now);
    }

    public static StoredFile reconstruct(
            UUID id,
            UUID ownerUserId,
            FilePurpose purpose,
            StorageProvider storageProvider,
            String bucket,
            String objectKey,
            String originalFileName,
            String contentType,
            long sizeBytes,
            String publicUrl,
            FileVisibility visibility,
            FileStatus status,
            Instant createdAt,
            Instant updatedAt) {
        return new StoredFile(
                id,
                ownerUserId,
                purpose,
                storageProvider,
                bucket,
                objectKey,
                originalFileName,
                contentType,
                sizeBytes,
                publicUrl,
                visibility,
                status,
                createdAt,
                updatedAt);
    }

    public void markDeleted(Instant now) {
        this.status = FileStatus.DELETED;
        this.updatedAt = now;
    }

    public boolean isOwnedBy(UUID userId) {
        return ownerUserId.equals(userId);
    }

    public boolean isActive() {
        return status == FileStatus.UPLOADED;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerUserId() {
        return ownerUserId;
    }

    public FilePurpose getPurpose() {
        return purpose;
    }

    public StorageProvider getStorageProvider() {
        return storageProvider;
    }

    public String getBucket() {
        return bucket;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public String getPublicUrl() {
        return publicUrl;
    }

    public FileVisibility getVisibility() {
        return visibility;
    }

    public FileStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public boolean isPubliclyAccessible() {
        return visibility == FileVisibility.PUBLIC;
    }
}
