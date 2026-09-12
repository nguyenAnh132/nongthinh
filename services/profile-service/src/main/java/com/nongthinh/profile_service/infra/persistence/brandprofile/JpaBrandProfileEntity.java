package com.nongthinh.profile_service.infra.persistence.brandprofile;

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
@Table(name = "brand_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JpaBrandProfileEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "brand_name", nullable = false)
    private String brandName;

    @Column(name = "tax_code", length = 20)
    private String taxCode;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "office_province_id", length = 10)
    private String officeProvinceId;

    @Column(name = "office_commune_id")
    private UUID officeCommuneId;

    @Column(name = "office_address_detail", length = 500)
    private String officeAddressDetail;

    @Column(name = "representative_name", nullable = false)
    private String representativeName;

    @Column(name = "representative_phone", nullable = false, length = 20)
    private String representativePhone;

    @Column(name = "representative_email", nullable = false)
    private String representativeEmail;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "banner_url")
    private String bannerUrl;

    @Column(name = "website_url")
    private String websiteUrl;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    @Column(name = "scheduled_deletion_at")
    private Instant scheduledDeletionAt;

    @Column(name = "rejected_at")
    private Instant rejectedAt;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "rejected_by")
    private UUID rejectedBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
