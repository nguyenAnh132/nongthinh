package com.nongthinh.agri_catalog_service.infra.persistence.aimodeldiseasemapping;

import java.time.Instant;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.infra.persistence.aimodelversion.JpaAiModelVersionClassEntity;
import com.nongthinh.agri_catalog_service.infra.persistence.disease.JpaDiseaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "ai_model_disease_mappings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JpaAiModelDiseaseMappingEntity {

    @Id
    private UUID id;

    @Column(name = "model_version_class_id", nullable = false)
    private UUID modelVersionClassId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_version_class_id", referencedColumnName = "id", insertable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private JpaAiModelVersionClassEntity modelVersionClass;

    @Column(name = "crop_type_id", nullable = false)
    private UUID cropTypeId;

    @Column(name = "disease_id", nullable = false)
    private UUID diseaseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disease_id", referencedColumnName = "id", insertable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private JpaDiseaseEntity disease;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "updated_by")
    private UUID updatedBy;
}
