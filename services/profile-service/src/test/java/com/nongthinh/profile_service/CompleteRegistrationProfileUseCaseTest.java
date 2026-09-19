package com.nongthinh.profile_service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.dao.DataIntegrityViolationException;
import com.nongthinh.profile_service.application.command.CompleteRegistrationProfileCommand;
import com.nongthinh.profile_service.application.port.in.admin.AdminProfileCreationUseCase;
import com.nongthinh.profile_service.application.port.in.brand.BrandProfileCreationUseCase;
import com.nongthinh.profile_service.application.port.in.farmer.FarmerProfileCreationUseCase;
import com.nongthinh.profile_service.application.port.in.registration.impl.CompleteRegistrationProfileUseCaseImpl;
import com.nongthinh.profile_service.application.port.out.repository.*;
import com.nongthinh.profile_service.domain.exception.BusinessException;

class CompleteRegistrationProfileUseCaseTest {
    private final UUID user = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final AdminProfileCreationUseCase admin = mock(AdminProfileCreationUseCase.class);
    private final FarmerProfileCreationUseCase farmer = mock(FarmerProfileCreationUseCase.class);
    private final BrandProfileCreationUseCase brand = mock(BrandProfileCreationUseCase.class);
    private final AdminProfileRepository admins = mock(AdminProfileRepository.class);
    private final FarmerProfileRepository farmers = mock(FarmerProfileRepository.class);
    private final BrandProfileRepository brands = mock(BrandProfileRepository.class);
    private final CompleteRegistrationProfileUseCaseImpl useCase = new CompleteRegistrationProfileUseCaseImpl(admin, farmer, brand, admins, farmers, brands);

    @ParameterizedTest
    @ValueSource(strings = {"ROLE_ADMIN", "ROLE_FARMER", "ROLE_BRAND", "ROLE_BRAND_PENDING"})
    void createsOnlyTheProfileSelectedByTrustedRole(String role) {
        useCase.execute(command(role));
        switch (role) {
            case "ROLE_ADMIN" -> { verify(admin).execute(argThat(c -> c.userId().equals(user))); verifyNoInteractions(farmer, brand); }
            case "ROLE_FARMER" -> { verify(farmer).execute(argThat(c -> c.userId().equals(user))); verifyNoInteractions(admin, brand); }
            default -> { verify(brand).execute(argThat(c -> c.userId().equals(user))); verifyNoInteractions(admin, farmer); }
        }
    }

    @Test
    void repeatRequestNeverUpdatesOrReactivatesExistingProfile() {
        when(admins.existsByUserId(user)).thenReturn(true);
        useCase.execute(command("ROLE_ADMIN"));
        verifyNoInteractions(admin, farmer, brand);
    }

    @Test
    void concurrentInsertIsSuccessfulOnlyIfRequestedProfileNowExists() {
        when(admins.existsByUserId(user)).thenReturn(false, true);
        doThrow(new DataIntegrityViolationException("duplicate user")).when(admin).execute(any());
        assertDoesNotThrow(() -> useCase.execute(command("ROLE_ADMIN")));
    }

    @Test
    void unrelatedDatabaseFailureIsNotReportedAsSuccess() {
        doThrow(new DataIntegrityViolationException("database constraint")).when(admin).execute(any());
        assertThrows(DataIntegrityViolationException.class, () -> useCase.execute(command("ROLE_ADMIN")));
    }

    @Test
    void unknownRoleCannotCreateProfile() {
        assertThrows(BusinessException.class, () -> useCase.execute(command("SUPER_ADMIN")));
        verifyNoInteractions(admin, farmer, brand);
    }

    private CompleteRegistrationProfileCommand command(String role) {
        return new CompleteRegistrationProfileCommand(user, role, "An", "Nguyen Van", "OTHER", "0900000000", "Brand", "Nguyen Van An", "0900000001", "representative@example.com");
    }
}
