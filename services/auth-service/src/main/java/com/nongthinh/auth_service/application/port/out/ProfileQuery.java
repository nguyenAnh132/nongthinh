package com.nongthinh.auth_service.application.port.out;

import java.util.Optional;
import java.util.UUID;
import com.nongthinh.auth_service.application.view.ProfileView;

public interface ProfileQuery {

    Optional<ProfileView> getFarmerProfile(UUID userId);

    Optional<ProfileView> getBrandProfile(UUID userId);

    Optional<ProfileView> getAdminProfile(UUID userId);
}
