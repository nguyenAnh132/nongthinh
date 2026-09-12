package com.nongthinh.agri_catalog_service.infra.persistence.aimodelversion;

import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "ai_model_versions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JpaAiModelVersionEntity {

    @Id
    private UUID id;

    @Column(name = "model_id", nullable = false)
    private UUID modelId;

    @Column(name = "version", nullable = false, length = 128)
    private String version;

    @Column(name = "artifact_file_id", nullable = false)
    private UUID artifactFileId;

    @Column(name = "artifact_sha256", nullable = false, length = 64)
    private String artifactSha256;

    @Column(name = "input_width", nullable = false)
    private int inputWidth;

    @Column(name = "input_height", nullable = false)
    private int inputHeight;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "validation_report", columnDefinition = "jsonb")
    private String validationReport;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "validated_at")
    private Instant validatedAt;

    @Column(name = "validated_by")
    private UUID validatedBy;

    @Column(name = "retired_at")
    private Instant retiredAt;

    @Column(name = "retired_by")
    private UUID retiredBy;
}
