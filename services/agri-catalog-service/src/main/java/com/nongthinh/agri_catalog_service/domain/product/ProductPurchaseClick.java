package com.nongthinh.agri_catalog_service.domain.product;

import java.time.Instant;
import java.util.UUID;

public class ProductPurchaseClick {

    private final UUID id;
    private final UUID productId;
    private final UUID farmerId;
    private final UUID brandId;

    private final String purchaseUrl;
    private final String userAgent;
    private final Instant clickedAt;

    private ProductPurchaseClick(
        UUID id,
        UUID productId,
        UUID farmerId,
        UUID brandId,
        String purchaseUrl,
        String userAgent,
        Instant clickedAt
    ) {
        this.id = id;
        this.productId = productId;
        this.farmerId = farmerId;
        this.brandId = brandId;
        this.purchaseUrl = purchaseUrl;
        this.userAgent = userAgent;
        this.clickedAt = clickedAt;
    }

    public static ProductPurchaseClick create(
        UUID id,
        UUID productId,
        UUID farmerId,
        UUID brandId,
        String purchaseUrl,
        String userAgent,
        Instant now
    ) {
        validateRequiredFields(
            id,
            productId,
            brandId,
            purchaseUrl,
            now
        );

        return new ProductPurchaseClick(
            id,
            productId,
            farmerId,
            brandId,
            purchaseUrl,
            userAgent,
            now
        );
    }

    public static ProductPurchaseClick reconstruct(
        UUID id,
        UUID productId,
        UUID farmerId,
        UUID brandId,
        String purchaseUrl,
        String userAgent,
        Instant clickedAt
    ) {
        return new ProductPurchaseClick(
            id,
            productId,
            farmerId,
            brandId,
            purchaseUrl,
            userAgent,
            clickedAt
        );
    }

    private static void validateRequiredFields(
        UUID id,
        UUID productId,
        UUID brandId,
        String purchaseUrl,
        Instant clickedAt
    ) {
        if (id == null) {
            throw new IllegalArgumentException(
                "Product purchase click ID must not be null"
            );
        }

        if (productId == null) {
            throw new IllegalArgumentException(
                "Product ID must not be null"
            );
        }

        if (brandId == null) {
            throw new IllegalArgumentException(
                "Brand ID must not be null"
            );
        }

        if (purchaseUrl == null || purchaseUrl.isBlank()) {
            throw new IllegalArgumentException(
                "Purchase URL must not be blank"
            );
        }

        if (clickedAt == null) {
            throw new IllegalArgumentException(
                "Clicked time must not be null"
            );
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getProductId() {
        return productId;
    }

    public UUID getFarmerId() {
        return farmerId;
    }

    public UUID getBrandId() {
        return brandId;
    }

    public String getPurchaseUrl() {
        return purchaseUrl;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public Instant getClickedAt() {
        return clickedAt;
    }

    public boolean isAnonymous() {
        return farmerId == null;
    }
}