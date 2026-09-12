package com.nongthinh.agri_catalog_service.infra.persistence.aimodeldeployment;

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

@Entity
@Table(name = "ai_model_deployments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JpaAiModelDeploymentEntity {

    @Id
    private UUID id;

    @Column(name = "model_version_id", nullable = false)
    private UUID modelVersionId;

    @Column(name = "crop_type_id")
    private UUID cropTypeId;

    @Column(name = "deployment_scope", nullable = false, length = 30)
    private String deploymentScope;

    @Column(name = "priority", nullable = false)
    private int priority;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "activated_at")
    private Instant activatedAt;

    @Column(name = "activated_by")
    private UUID activatedBy;
}
