package com.nongthinh.profile_service.domain.brandprofile;

import java.util.Set;
import com.nongthinh.profile_service.domain.brandprofile.valueobject.BrandProfileStatus;

public final class BrandAccessPolicy {
    private static final Set<BrandProfileStatus> EDITABLE = Set.of(
            BrandProfileStatus.PENDING_APPROVAL, BrandProfileStatus.UNDER_REVIEW,
            BrandProfileStatus.NEEDS_REVISION, BrandProfileStatus.READY_FOR_FINAL_REVIEW,
            BrandProfileStatus.ACTIVE);

    private BrandAccessPolicy() { }

    public static boolean canEditProfile(BrandProfileStatus status) {
        return status == null || EDITABLE.contains(status);
    }

    public static boolean canSubmitDocuments(BrandProfileStatus status) {
        return status == BrandProfileStatus.UNDER_REVIEW || status == BrandProfileStatus.NEEDS_REVISION;
    }
}
