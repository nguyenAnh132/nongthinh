package com.nongthinh.agri_catalog_service.infra.persistence.diseasereviewhistory;

import java.time.Instant;
import java.util.UUID;
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
@Table(name = "disease_review_histories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JpaDiseaseReviewHistoryEntity {

    @Id
    private UUID id;

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

    @Column(name = "action", nullable = false, length = 30)
    private String action;

    @Column(name = "previous_status", length = 30)
    private String previousStatus;

    @Column(name = "new_status", nullable = false, length = 30)
    private String newStatus;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "actor_id", nullable = false)
    private UUID actorId;

    @Column(name = "actor_type", nullable = false, length = 20)
    private String actorType;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}