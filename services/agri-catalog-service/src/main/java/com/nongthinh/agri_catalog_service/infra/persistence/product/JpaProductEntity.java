package com.nongthinh.agri_catalog_service.infra.persistence.product;

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
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JpaProductEntity {

    @Id
    private UUID id;

    @Column(name = "brand_id", nullable = false)
    private UUID brandId;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "slug", nullable = false, length = 280)
    private String slug;

    @Column(name = "sku", length = 100)
    private String sku;

    @Column(name = "registration_number", length = 100)
    private String registrationNumber;

    @Column(name = "manufacturer_name", length = 255)
    private String manufacturerName;

    @Column(name = "origin_country", length = 100)
    private String originCountry;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "ingredients", columnDefinition = "TEXT")
    private String ingredients;

    @Column(name = "usage_instruction", columnDefinition = "TEXT")
    private String usageInstruction;

    @Column(name = "dosage_instruction", columnDefinition = "TEXT")
    private String dosageInstruction;

    @Column(name = "safety_instruction", columnDefinition = "TEXT")
    private String safetyInstruction;

    @Column(name = "storage_instruction", columnDefinition = "TEXT")
    private String storageInstruction;

    @Column(name = "warning", columnDefinition = "TEXT")
    private String warning;

    @Column(name = "form", length = 100)
    private String form;

    @Column(name = "unit", length = 50)
    private String unit;

    @Column(name = "package_specification", length = 255)
    private String packageSpecification;

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Column(name = "purchase_url", columnDefinition = "TEXT")
    private String purchaseUrl;

    @Column(name = "publication_status", nullable = false, length = 20)
    private String publicationStatus;

    @Column(name = "moderation_status", nullable = false, length = 20)
    private String moderationStatus;

    @Column(name = "moderation_reason", columnDefinition = "TEXT")
    private String moderationReason;

    @Column(name = "locked_at")
    private Instant lockedAt;

    @Column(name = "locked_by")
    private UUID lockedBy;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "unpublished_at")
    private Instant unpublishedAt;

    @Column(name = "is_featured", nullable = false)
    private boolean isFeatured;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by")
    private UUID deletedBy;
}
