package com.nongthinh.agri_catalog_service.infra.persistence.productpurchaseclick;

import java.time.Instant;
import java.util.UUID;

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
@Table(name = "product_purchase_clicks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JpaProductPurchaseClickEntity {

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

    @Column(name = "farmer_id")
    private UUID farmerId;

    @Column(name = "brand_id", nullable = false)
    private UUID brandId;

    @Column(name = "purchase_url", nullable = false, columnDefinition = "TEXT")
    private String purchaseUrl;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "clicked_at", nullable = false)
    private Instant clickedAt;
}