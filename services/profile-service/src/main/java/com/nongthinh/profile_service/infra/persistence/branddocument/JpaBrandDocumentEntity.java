package com.nongthinh.profile_service.infra.persistence.branddocument;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "brand_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JpaBrandDocumentEntity {

    @Id
    private UUID id;

    @Column(name = "brand_profile_id", nullable = false)
    private UUID brandProfileId;

    @Column(name = "business_license_url", nullable = false, length = 500)
    private String businessLicenseUrl;

    @Column(name = "review_status", nullable = false, length = 30)
    private String reviewStatus;

    @Column(name = "reviewed_by")
    private UUID reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
