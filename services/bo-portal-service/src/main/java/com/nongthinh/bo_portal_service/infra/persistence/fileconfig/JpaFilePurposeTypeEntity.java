package com.nongthinh.bo_portal_service.infra.persistence.fileconfig;

import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "file_purpose_types", uniqueConstraints = @UniqueConstraint(
        name = "uq_file_purpose_type", columnNames = {"purpose", "file_type_code"}))
@Getter
@Setter
public class JpaFilePurposeTypeEntity {
    @Id
    private UUID id;

    @Column(nullable = false, length = 30)
    private String purpose;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "file_type_code", nullable = false)
    private JpaFileTypeEntity fileType;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
