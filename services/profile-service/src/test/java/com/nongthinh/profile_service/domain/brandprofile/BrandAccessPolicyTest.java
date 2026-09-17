package com.nongthinh.profile_service.domain.brandprofile;

import com.nongthinh.profile_service.domain.brandprofile.valueobject.BrandProfileStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class BrandAccessPolicyTest {
    @ParameterizedTest
    @EnumSource(BrandProfileStatus.class)
    void exposesOnlyRegistrationCapabilitiesForAllowedStates(BrandProfileStatus status) {
        boolean blocked = Set.of(BrandProfileStatus.REJECTED, BrandProfileStatus.LOCKED,
                BrandProfileStatus.DISABLED, BrandProfileStatus.DELETED).contains(status);
        assertEquals(!blocked, BrandAccessPolicy.canEditProfile(status));
        assertEquals(status == BrandProfileStatus.UNDER_REVIEW || status == BrandProfileStatus.NEEDS_REVISION,
                BrandAccessPolicy.canSubmitDocuments(status));
    }

    @Test
    void missingProfileAllowsCompletionButNotDocumentSubmission() {
        assertTrue(BrandAccessPolicy.canEditProfile(null));
        assertFalse(BrandAccessPolicy.canSubmitDocuments(null));
    }
}
