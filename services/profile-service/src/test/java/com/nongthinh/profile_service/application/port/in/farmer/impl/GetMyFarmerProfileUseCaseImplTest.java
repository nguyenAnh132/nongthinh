package com.nongthinh.profile_service.application.port.in.farmer.impl;

import static org.assertj.core.api.Assertions.assertThat;
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
import com.nongthinh.profile_service.application.port.out.LocationServicePort;
import com.nongthinh.profile_service.application.port.out.repository.FarmerProfileRepository;
import com.nongthinh.profile_service.application.view.AddressNamesView;
import com.nongthinh.profile_service.common.currentuser.CurrentUser;
import com.nongthinh.profile_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.profile_service.domain.farmerprofile.FarmerProfile;
import com.nongthinh.profile_service.domain.farmerprofile.valueobject.Gender;
import com.nongthinh.profile_service.domain.shared.valueobject.Address;
import com.nongthinh.profile_service.domain.shared.valueobject.PersonName;
import com.nongthinh.profile_service.domain.shared.valueobject.StandardProfileStatus;

@ExtendWith(MockitoExtension.class)
class GetMyFarmerProfileUseCaseImplTest {

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private FarmerProfileRepository farmerProfileRepository;

    @Mock
    private LocationServicePort locationServicePort;

    @InjectMocks
    private GetMyFarmerProfileUseCaseImpl useCase;

    @Test
    void returnsResolvedProvinceAndCommuneNames() {
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000010");
        UUID communeId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        FarmerProfile profile = FarmerProfile.reconstruct(
                UUID.fromString("00000000-0000-0000-0000-000000000020"),
                userId,
                PersonName.of("Nguyễn"),
                PersonName.of("Văn An"),
                Gender.fromString("MALE"),
                "0912345678",
                Address.of("01", communeId, "Số 1"),
                null,
                StandardProfileStatus.ACTIVE,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z")
        );
        when(currentUserProvider.getCurrentUser()).thenReturn(new CurrentUser(
                userId,
                "keycloak-user",
                "farmer@example.com",
                Set.of("ROLE_FARMER"),
                Set.of()
        ));
        when(farmerProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(locationServicePort.resolveAddress("01", communeId))
                .thenReturn(new AddressNamesView("Thành phố Hà Nội", "Phường Ba Đình"));

        var result = useCase.execute();

        assertThat(result.provinceName()).isEqualTo("Thành phố Hà Nội");
        assertThat(result.communeName()).isEqualTo("Phường Ba Đình");
    }
}
