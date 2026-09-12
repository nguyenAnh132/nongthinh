package com.nongthinh.profile_service.application.port.in.farmer.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nongthinh.profile_service.application.port.out.ClockProvider;
import com.nongthinh.profile_service.application.port.out.repository.FarmerProfileRepository;
import com.nongthinh.profile_service.common.currentuser.CurrentUser;
import com.nongthinh.profile_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.profile_service.domain.farmerprofile.FarmerProfile;
import com.nongthinh.profile_service.domain.farmerprofile.valueobject.Gender;
import com.nongthinh.profile_service.domain.shared.valueobject.Address;
import com.nongthinh.profile_service.domain.shared.valueobject.PersonName;
import com.nongthinh.profile_service.domain.shared.valueobject.StandardProfileStatus;

@ExtendWith(MockitoExtension.class)
class UpdateMyFarmerAvatarUseCaseImplTest {

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private FarmerProfileRepository farmerProfileRepository;

    @Mock
    private ClockProvider clockProvider;

    @InjectMocks
    private UpdateMyFarmerAvatarUseCaseImpl useCase;

    @Test
    void updatesAndPersistsCurrentFarmersAvatarUrl() {
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000010");
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        Instant updatedAt = Instant.parse("2026-09-03T12:00:00Z");
        String avatarUrl = "https://files.example.test/avatars/farmer.webp";
        FarmerProfile profile = FarmerProfile.reconstruct(
                UUID.fromString("00000000-0000-0000-0000-000000000020"),
                userId,
                PersonName.of("Nguyễn"),
                PersonName.of("Văn An"),
                Gender.fromString("MALE"),
                "0912345678",
                Address.empty(),
                null,
                StandardProfileStatus.ACTIVE,
                createdAt,
                createdAt
        );

        when(currentUserProvider.getCurrentUser()).thenReturn(new CurrentUser(
                userId,
                "keycloak-user",
                "farmer@example.com",
                Set.of("ROLE_FARMER"),
                Set.of()
        ));
        when(farmerProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(clockProvider.now()).thenReturn(updatedAt);
        when(farmerProfileRepository.save(profile)).thenReturn(profile);

        var result = useCase.execute(avatarUrl);

        assertThat(result.avatarUrl()).isEqualTo(avatarUrl);
        assertThat(result.updatedAt()).isEqualTo(updatedAt);
        verify(farmerProfileRepository).save(profile);
    }
}
