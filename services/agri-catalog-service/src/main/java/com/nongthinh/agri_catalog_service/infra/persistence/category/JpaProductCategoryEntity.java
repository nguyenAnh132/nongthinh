package com.nongthinh.agri_catalog_service.infra.persistence.category;

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
@Table(name = "product_categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JpaProductCategoryEntity {

    @Id
    private UUID id;

    @Column(name = "parent_id", nullable = true)
    private UUID parentId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "slug", nullable = false)
    private String slug;

    @Column(name = "description", nullable = true)
    private String description;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

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
