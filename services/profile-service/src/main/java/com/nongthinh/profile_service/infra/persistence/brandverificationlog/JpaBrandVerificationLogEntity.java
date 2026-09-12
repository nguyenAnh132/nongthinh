package com.nongthinh.profile_service.infra.persistence.brandverificationlog;

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
@Table(name = "brand_verification_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JpaBrandVerificationLogEntity {

    @Id
    private UUID id;

    @Column(name = "brand_profile_id", nullable = false)
    private UUID brandProfileId;

    @Column(name = "admin_user_id", nullable = false)
    private UUID adminUserId;

    @Column(name = "phone_called", nullable = false, length = 20)
    private String phoneCalled;

    @Column(name = "result", nullable = false, length = 30)
    private String result;

    @Column(name = "note", length = 1000)
    private String note;

    @Column(name = "verified_at", nullable = false)
    private Instant verifiedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
