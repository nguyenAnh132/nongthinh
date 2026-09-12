package com.nongthinh.agri_catalog_service.infra.persistence.producthistory;

import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.fasterxml.jackson.databind.JsonNode;
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
@Table(name = "product_histories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JpaProductHistoryEntity {

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

    @Column(name = "action", nullable = false, length = 30)
    private String action;

    @Column(name = "actor_id", nullable = false)
    private UUID actorId;

    @Column(name = "actor_type", nullable = false, length = 20)
    private String actorType;

    @Column(name = "previous_publication_status", length = 20)
    private String previousPublicationStatus;

    @Column(name = "new_publication_status", length = 20)
    private String newPublicationStatus;

    @Column(name = "previous_moderation_status", length = 20)
    private String previousModerationStatus;

    @Column(name = "new_moderation_status", length = 20)
    private String newModerationStatus;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "change_summary", columnDefinition = "TEXT")
    private String changeSummary;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "snapshot_data", columnDefinition = "jsonb")
    private JsonNode snapshotData;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}