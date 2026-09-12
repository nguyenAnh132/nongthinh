package com.nongthinh.profile_service.domain.brandverificationlog;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import com.nongthinh.profile_service.domain.brandverificationlog.valueobject.VerificationResult;

public final class BrandVerificationLog {

    private final UUID id;
    private final UUID brandProfileId;
    private final UUID adminUserId;
    private final String phoneCalled;
    private final VerificationResult result;
    private final String note;
    private final Instant verifiedAt;
    private final Instant createdAt;

    private BrandVerificationLog(
        UUID id,
        UUID brandProfileId,
        UUID adminUserId,
        String phoneCalled,
        VerificationResult result,
        String note,
        Instant verifiedAt,
        Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.brandProfileId = Objects.requireNonNull(brandProfileId, "brandProfileId is required");
        this.adminUserId = Objects.requireNonNull(adminUserId, "adminUserId is required");
        this.phoneCalled = Objects.requireNonNull(phoneCalled, "phoneCalled is required");
        this.result = Objects.requireNonNull(result, "result is required");
        this.note = note;
        this.verifiedAt = Objects.requireNonNull(verifiedAt, "verifiedAt is required");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
    }

    public static BrandVerificationLog create(
        UUID id,
        UUID brandProfileId,
        UUID adminUserId,
        String phoneCalled,
        VerificationResult result,
        String note,
        Instant verifiedAt,
        Instant createdAt
    ) {
        return new BrandVerificationLog(
            id,
            brandProfileId,
            adminUserId,
            phoneCalled,
            result,
            note,
            verifiedAt,
            createdAt
        );
    }

    public static BrandVerificationLog reconstruct(
        UUID id,
        UUID brandProfileId,
        UUID adminUserId,
        String phoneCalled,
        VerificationResult result,
        String note,
        Instant verifiedAt,
        Instant createdAt
    ) {
        return new BrandVerificationLog(
            id,
            brandProfileId,
            adminUserId,
            phoneCalled,
            result,
            note,
            verifiedAt,
            createdAt
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getBrandProfileId() {
        return brandProfileId;
    }

    public UUID getAdminUserId() {
        return adminUserId;
    }

    public String getPhoneCalled() {
        return phoneCalled;
    }

    public VerificationResult getResult() {
        return result;
    }

    public String getNote() {
        return note;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
