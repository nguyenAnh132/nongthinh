package com.nongthinh.agri_catalog_service.infra.persistence.product_disease_treatment;

import java.time.Instant;
import java.util.UUID;

import com.nongthinh.agri_catalog_service.infra.persistence.disease.JpaDiseaseEntity;
import com.nongthinh.agri_catalog_service.infra.persistence.product.JpaProductEntity;
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
@Table(name = "product_disease_treatments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JpaProductDiseaseTreatmentEntity {

    @Id
    private UUID id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "product_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false,
            nullable = false
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private JpaProductEntity product;

    @Column(name = "disease_id", nullable = false)
    private UUID diseaseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "disease_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false,
            nullable = false
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private JpaDiseaseEntity disease;

    @Column(name = "brand_id", nullable = false)
    private UUID brandId;

    @Column(name = "effectiveness_level", nullable = true, length = 20)
    private String effectivenessLevel;

    @Column(name = "priority", nullable = false)
    private int priority;

    @Column(name = "dosage", nullable = true, length = 255)
    private String dosage;

    @Column(name = "application_method", nullable = true, columnDefinition = "TEXT")
    private String applicationMethod;

    @Column(name = "application_timing", nullable = true, columnDefinition = "TEXT")
    private String applicationTiming;

    @Column(name = "frequency_instruction", nullable = true, length = 255)
    private String frequencyInstruction;

    @Column(name = "treatment_note", nullable = true, columnDefinition = "TEXT")
    private String treatmentNote;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "updated_at", nullable = true)
    private Instant updatedAt;

    @Column(name = "updated_by", nullable = true)
    private UUID updatedBy;

    @Column(name = "deleted_at", nullable = true)
    private Instant deletedAt;

    @Column(name = "deleted_by", nullable = true)
    private UUID deletedBy;
}