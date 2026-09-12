package com.nongthinh.file_service.infra.persistence.file;

import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "stored_files")
@Getter
@Setter
@NoArgsConstructor
public class JpaStoredFileEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "owner_user_id", nullable = false)
    private UUID ownerUserId;

    @Column(name = "purpose", length = 50, nullable = false)
    private String purpose;

    @Column(name = "storage_provider", length = 20, nullable = false)
    private String storageProvider;

    @Column(name = "bucket", length = 100, nullable = false)
    private String bucket;

    @Column(name = "object_key", length = 500, nullable = false)
    private String objectKey;

    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @Column(name = "content_type", length = 100, nullable = false)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(name = "public_url", length = 500)
    private String publicUrl;

    @Column(name = "visibility", length = 20, nullable = false)
    private String visibility;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
