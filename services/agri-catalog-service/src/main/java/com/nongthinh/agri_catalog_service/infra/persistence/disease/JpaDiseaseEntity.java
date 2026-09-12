package com.nongthinh.agri_catalog_service.infra.persistence.disease;

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
@Table(name = "diseases")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JpaDiseaseEntity {

    @Id
    private UUID id;

    @Column(name = "created_source", nullable = false, length = 20)
    private String createdSource;

    @Column(name = "brand_id", nullable = true)
    private UUID brandId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "slug", nullable = false, length = 280)
    private String slug;

    @Column(name = "scientific_name", nullable = true, length = 255)
    private String scientificName;

    @Column(name = "crop_type_id", nullable = false)
    private UUID cropTypeId;

    @Column(name = "affected_part", nullable = true, length = 100)
    private String affectedPart;

    @Column(name = "pathogen_type", nullable = true, length = 50)
    private String pathogenType;

    @Column(name = "short_description", nullable = true, length = 500)
    private String shortDescription;

    @Column(name = "description", nullable = true, columnDefinition = "TEXT")
    private String description;

    @Column(name = "symptoms", nullable = true, columnDefinition = "TEXT")
    private String symptoms;

    @Column(name = "causes", nullable = true, columnDefinition = "TEXT")
    private String causes;

    @Column(name = "favorable_conditions", nullable = true, columnDefinition = "TEXT")
    private String favorableConditions;

    @Column(name = "prevention_method", nullable = true, columnDefinition = "TEXT")
    private String preventionMethod;

    @Column(name = "treatment_guideline", nullable = true, columnDefinition = "TEXT")
    private String treatmentGuideline;

    @Column(name = "thumbnail_url", nullable = true, columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Column(name = "review_status", nullable = false, length = 30)
    private String reviewStatus;

    @Column(name = "rejection_reason", nullable = true, columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "submitted_at", nullable = true)
    private Instant submittedAt;

    @Column(name = "reviewed_at", nullable = true)
    private Instant reviewedAt;

    @Column(name = "reviewed_by", nullable = true)
    private UUID reviewedBy;

    @Column(name = "published_at", nullable = true)
    private Instant publishedAt;

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
