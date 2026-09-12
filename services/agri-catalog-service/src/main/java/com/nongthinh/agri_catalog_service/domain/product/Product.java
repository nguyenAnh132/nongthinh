package com.nongthinh.agri_catalog_service.domain.product;

import java.time.Instant;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.PublicationStatus;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.ModerationStatus;

public class Product {

    private final UUID id;
    private final UUID brandId;
    private UUID categoryId;

    private String name;
    private String slug;
    private String sku;
    private String registrationNumber;

    private String manufacturerName;
    private String originCountry;

    private String shortDescription;
    private String description;

    private String ingredients;
    private String usageInstruction;
    private String dosageInstruction;
    private String safetyInstruction;
    private String storageInstruction;
    private String warning;

    private String form;
    private String unit;
    private String packageSpecification;

    private String thumbnailUrl;
    private String purchaseUrl;

    private PublicationStatus publicationStatus;
    private ModerationStatus moderationStatus;

    private String moderationReason;
    private Instant lockedAt;
    private UUID lockedBy;

    private Instant publishedAt;
    private Instant unpublishedAt;

    private boolean isFeatured;

    private final Instant createdAt;
    private final UUID createdBy;
    private Instant updatedAt;
    private UUID updatedBy;
    private Instant deletedAt;
    private UUID deletedBy;


    private Product (
        UUID id,
        UUID brandId,
        UUID categoryId,
        String name,
        String slug,
        String sku,
        String registrationNumber,
        String manufacturerName,
        String originCountry,
        String shortDescription,
        String description,
        String ingredients,
        String usageInstruction,
        String dosageInstruction,
        String safetyInstruction,
        String storageInstruction,
        String warning,
        String form,
        String unit,
        String packageSpecification,
        String thumbnailUrl,
        String purchaseUrl,
        PublicationStatus publicationStatus,
        ModerationStatus moderationStatus,
        String moderationReason,
        Instant lockedAt,
        UUID lockedBy,
        Instant publishedAt,
        Instant unpublishedAt,
        boolean isFeatured,
        Instant createdAt,
        UUID createdBy,
        Instant updatedAt,
        UUID updatedBy,
        Instant deletedAt,
        UUID deletedBy
    ) {
        this.id = id;
        this.brandId = brandId;
        this.categoryId = categoryId;
        this.name = name;
        this.slug = slug;
        this.sku = sku;
        this.registrationNumber = registrationNumber;
        this.manufacturerName = manufacturerName;
        this.originCountry = originCountry;
        this.shortDescription = shortDescription;
        this.description = description;
        this.ingredients = ingredients;
        this.usageInstruction = usageInstruction;
        this.dosageInstruction = dosageInstruction;
        this.safetyInstruction = safetyInstruction;
        this.storageInstruction = storageInstruction;
        this.warning = warning;
        this.form = form;
        this.unit = unit;
        this.packageSpecification = packageSpecification;
        this.thumbnailUrl = thumbnailUrl;
        this.purchaseUrl = purchaseUrl;
        this.publicationStatus = publicationStatus;
        this.moderationStatus = moderationStatus;
        this.moderationReason = moderationReason;
        this.lockedAt = lockedAt;
        this.lockedBy = lockedBy;
        this.publishedAt = publishedAt;
        this.unpublishedAt = unpublishedAt;
        this.isFeatured = isFeatured;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
        this.deletedAt = deletedAt;
        this.deletedBy = deletedBy;
    }

    public static Product create(
        UUID id,
        UUID brandId,
        UUID categoryId,
        String name,
        String slug,
        String sku,
        String registrationNumber,
        String manufacturerName,
        String originCountry,
        String shortDescription,
        String description,
        String ingredients,
        String usageInstruction,
        String dosageInstruction,
        String safetyInstruction,
        String storageInstruction,
        String warning,
        String form,
        String unit,
        String packageSpecification,
        String thumbnailUrl,
        String purchaseUrl,
        PublicationStatus publicationStatus,
        ModerationStatus moderationStatus,
        String moderationReason,
        UUID createdBy,
        Instant now
    ) {
        return new Product(
            id,
            brandId,
            categoryId,
            name,
            slug,
            sku,
            registrationNumber,
            manufacturerName,
            originCountry,
            shortDescription,
            description,
            ingredients,
            usageInstruction,
            dosageInstruction,
            safetyInstruction,
            storageInstruction,
            warning,
            form,
            unit,
            packageSpecification,
            thumbnailUrl,
            purchaseUrl,
            publicationStatus,
            moderationStatus,
            moderationReason,
            null,
            null,
            null,
            null,
            false,
            now,
            createdBy,  
            now,
            createdBy,  
            null,
            null
        );
    }
    public static Product reconstruct(
        UUID id,
        UUID brandId,
        UUID categoryId,
        String name,
        String slug,
        String sku,
        String registrationNumber,
        String manufacturerName,
        String originCountry,
        String shortDescription,
        String description,
        String ingredients,
        String usageInstruction,
        String dosageInstruction,
        String safetyInstruction,
        String storageInstruction,
        String warning,
        String form,
        String unit,
        String packageSpecification,
        String thumbnailUrl,
        String purchaseUrl,
        PublicationStatus publicationStatus,
        ModerationStatus moderationStatus,
        String moderationReason,
        Instant lockedAt,
        UUID lockedBy,
        Instant publishedAt,
        Instant unpublishedAt,
        boolean isFeatured,
        Instant createdAt,
        UUID createdBy,
        Instant updatedAt,
        UUID updatedBy,
        Instant deletedAt,
        UUID deletedBy

    ) {
        return new Product(id, brandId, categoryId, name, slug, sku, registrationNumber, manufacturerName, originCountry, shortDescription, description, ingredients, usageInstruction, dosageInstruction, safetyInstruction, storageInstruction, warning, form, unit, packageSpecification, thumbnailUrl, purchaseUrl, publicationStatus, moderationStatus, moderationReason, lockedAt, lockedBy, publishedAt, unpublishedAt, isFeatured, createdAt, createdBy, updatedAt, updatedBy, deletedAt, deletedBy);
    }

    public void update(
        UUID categoryId,
        String name,
        String slug,
        String sku,
        String registrationNumber,
        String manufacturerName,
        String originCountry,
        String shortDescription,
        String description,
        String ingredients,
        String usageInstruction,
        String dosageInstruction,
        String safetyInstruction,
        String storageInstruction,
        String warning,
        String form,
        String unit,
        String packageSpecification,
        String purchaseUrl,
        UUID updatedBy,
        Instant now
    ) {
        this.categoryId = categoryId;
        this.name = name;
        this.slug = slug;
        this.sku = sku;
        this.registrationNumber = registrationNumber;
        this.manufacturerName = manufacturerName;
        this.originCountry = originCountry;
        this.shortDescription = shortDescription;
        this.description = description;
        this.ingredients = ingredients;
        this.usageInstruction = usageInstruction;
        this.dosageInstruction = dosageInstruction;
        this.safetyInstruction = safetyInstruction;
        this.storageInstruction = storageInstruction;
        this.warning = warning;
        this.form = form;
        this.unit = unit;
        this.packageSpecification = packageSpecification;
        this.purchaseUrl = purchaseUrl;
        this.touch(now, updatedBy);
    }

    public void changeThumbnailUrl(String thumbnailUrl, UUID updatedBy, Instant now) {
        this.thumbnailUrl = thumbnailUrl;
        this.touch(now, updatedBy);
    }

    public void publish(UUID updatedBy, Instant now) {
        this.publicationStatus = PublicationStatus.PUBLISHED;
        this.publishedAt = now;
        this.unpublishedAt = null;
        this.touch(now, updatedBy);
    }
    
    public void unpublish(UUID updatedBy, Instant now) {
        this.publicationStatus = PublicationStatus.UNPUBLISHED;
        this.unpublishedAt = now;
        this.touch(now, updatedBy);
    }
    
    public void lock(
        String moderationReason,
        UUID lockedBy,
        Instant now
    ) {
        this.moderationStatus = ModerationStatus.LOCKED;
        this.moderationReason = moderationReason;
        this.lockedAt = now;
        this.lockedBy = lockedBy;
        this.touch(now, lockedBy);
    }

    public void changePurchaseUrl(String purchaseUrl, UUID updatedBy, Instant now) {
        this.purchaseUrl = purchaseUrl;
        this.touch(now, updatedBy);
    }
    
    public void unlock(UUID updatedBy, Instant now) {
        this.moderationStatus = ModerationStatus.NORMAL;
        this.moderationReason = null;
        this.lockedAt = null;
        this.lockedBy = null;
        this.touch(now, updatedBy);
    }
    
    public void changeFeatured(
        boolean featured,
        UUID updatedBy,
        Instant now
    ) {
        this.isFeatured = featured;
        this.touch(now, updatedBy);
    }
    
    public void delete(UUID deletedBy, Instant now) {
        this.deletedAt = now;
        this.deletedBy = deletedBy;
        this.touch(now, deletedBy);
    }
    
    public void restore(UUID restoredBy, Instant now) {
        this.deletedAt = null;
        this.deletedBy = null;
        this.touch(now, restoredBy);
    }

    private void touch(Instant now, UUID updatedBy) {
        this.updatedAt = now;
        this.updatedBy = updatedBy;
    }

    public UUID getId() {
        return id;
    }
    public UUID getBrandId() {
        return brandId;
    }
    public UUID getCategoryId() {
        return categoryId;
    }
    public String getName() {
        return name;
    }
    public String getSlug() {
        return slug;
    }
    public String getSku() {
        return sku;
    }
    public String getRegistrationNumber() {
        return registrationNumber;
    }
    public String getManufacturerName() {
        return manufacturerName;
    }
    public String getOriginCountry() {
        return originCountry;
    }
    public String getShortDescription() {
        return shortDescription;
    }
    public String getDescription() {
        return description;
    }
    public String getIngredients() {
        return ingredients;
    }
    public String getUsageInstruction() {
        return usageInstruction;
    }
    public String getDosageInstruction() {
        return dosageInstruction;
    }
    public String getSafetyInstruction() {
        return safetyInstruction;
    }
    public String getStorageInstruction() {
        return storageInstruction;
    }
    public String getWarning() {
        return warning;
    }
    public String getForm() {
        return form;
    }
    public String getUnit() {
        return unit;
    }
    public String getPackageSpecification() {
        return packageSpecification;
    }
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
    public PublicationStatus getPublicationStatus() {
        return publicationStatus;
    }
    public ModerationStatus getModerationStatus() {
        return moderationStatus;
    }
    public String getModerationReason() {
        return moderationReason;
    }
    public Instant getLockedAt() {
        return lockedAt;
    }
    public UUID getLockedBy() {
        return lockedBy;
    }
    public Instant getPublishedAt() {
        return publishedAt;
    }
    public Instant getUnpublishedAt() {
        return unpublishedAt;
    }
    public boolean isFeatured() {
        return isFeatured;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
    public UUID getCreatedBy() {
        return createdBy;
    }
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    public UUID getUpdatedBy() {
        return updatedBy;
    }
    public Instant getDeletedAt() {
        return deletedAt;
    }
    public UUID getDeletedBy() {
        return deletedBy;
    }
    public String getPurchaseUrl() {
        return purchaseUrl;
    }

    
}
