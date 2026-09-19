package com.nongthinh.auth_service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.nongthinh.auth_service.application.command.CompleteRegistrationCommand;
import com.nongthinh.auth_service.application.exception.RegistrationRequiredException;
import com.nongthinh.auth_service.application.port.in.auth.impl.GetMeUseCaseImpl;
import com.nongthinh.auth_service.application.port.in.user.SynchronizeBrandRoleUseCase;
import com.nongthinh.auth_service.application.port.in.user.impl.CompleteRegistrationUseCaseImpl;
import com.nongthinh.auth_service.application.port.out.ProfileQuery;
import com.nongthinh.auth_service.application.port.out.ProfileRegistration;
import com.nongthinh.auth_service.application.port.out.keycloak.KeycloakIdentity;
import com.nongthinh.auth_service.application.port.out.keycloak.KeycloakIdp;
import com.nongthinh.auth_service.application.port.out.repository.UserRepository;
import com.nongthinh.auth_service.application.view.ProfileView;
import com.nongthinh.auth_service.application.view.RegistrationPrincipal;
import com.nongthinh.auth_service.common.exception.ErrorCode;
import com.nongthinh.auth_service.domain.exception.BusinessException;
import com.nongthinh.auth_service.domain.user.User;
import com.nongthinh.auth_service.domain.user.valueobject.Email;
import com.nongthinh.auth_service.infra.exception.InfrastructureException;

class CompleteRegistrationUseCaseTest {
    private static final UUID USER = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID SUBJECT = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final Instant NOW = Instant.parse("2026-09-19T00:00:00Z");
    private static final String EMAIL = "admin@example.com";
    private final UserRepository users = mock(UserRepository.class);
    private final KeycloakIdp keycloak = mock(KeycloakIdp.class);
    private final ProfileRegistration profiles = mock(ProfileRegistration.class);
    private final ProfileQuery queries = mock(ProfileQuery.class);
    private final SynchronizeBrandRoleUseCase brands = mock(SynchronizeBrandRoleUseCase.class);
    private final AtomicReference<User> saved = new AtomicReference<>();
    private final CompleteRegistrationUseCaseImpl useCase = new CompleteRegistrationUseCaseImpl(users, keycloak, profiles, brands, () -> NOW, () -> USER);
    private final GetMeUseCaseImpl me = new GetMeUseCaseImpl(users, queries, brands);

    @BeforeEach
    void prepare() {
        when(keycloak.exchangeClientToken()).thenReturn("service-token");
        when(keycloak.getIdentity(SUBJECT, "Bearer service-token")).thenReturn(new KeycloakIdentity(SUBJECT, EMAIL, null, true));
        when(users.findByKeycloakId(SUBJECT)).thenAnswer(invocation -> Optional.ofNullable(saved.get()));
        when(users.findById(USER)).thenAnswer(invocation -> Optional.ofNullable(saved.get()));
        doAnswer(invocation -> { saved.compareAndSet(null, invocation.getArgument(0)); return null; }).when(users).insertIfAbsent(any());
    }

    @Test
    void adminWithoutLocalIdentityCanCompleteWithoutGrantingRoles() {
        var result = useCase.execute(principal(null, "ROLE_ADMIN"), person());
        assertEquals(USER, result.userId());
        assertTrue(result.refreshRequired());
        var ordered = inOrder(users, keycloak, profiles);
        ordered.verify(users).insertIfAbsent(any());
        ordered.verify(keycloak).updateNongThinhIdUser(SUBJECT, USER, "Bearer service-token");
        ordered.verify(profiles).complete(USER, "ROLE_ADMIN", person());
        verify(keycloak, never()).createUser(any(), anyString(), anyString(), anyBoolean(), anyBoolean(), anyString());
        verify(keycloak, never()).assignRealmRoles(anyString(), anyCollection(), anyString());
        verifyNoInteractions(brands);
    }

    @Test
    void profileFailureCanRetrySameIdentityWithoutAnotherInsert() {
        doThrow(new InfrastructureException(ErrorCode.PROFILE_REGISTRATION_FAILED)).doNothing()
                .when(profiles).complete(USER, "ROLE_ADMIN", person());
        assertThrows(InfrastructureException.class, () -> useCase.execute(principal(null, "ROLE_ADMIN"), person()));
        assertNotNull(saved.get());
        assertEquals(USER, useCase.execute(principal(null, "ROLE_ADMIN"), person()).userId());
        verify(users, times(1)).insertIfAbsent(any());
        verify(profiles, times(2)).complete(USER, "ROLE_ADMIN", person());
    }

    @Test
    void failureLinkingKeycloakDoesNotCreateProfileAndRemainsRetryable() {
        doThrow(new InfrastructureException(ErrorCode.KEYCLOAK_IDENTITY_SYNC_FAILED)).doNothing()
                .when(keycloak).updateNongThinhIdUser(SUBJECT, USER, "Bearer service-token");
        assertThrows(InfrastructureException.class, () -> useCase.execute(principal(null, "ROLE_ADMIN"), person()));
        verifyNoInteractions(profiles);
        useCase.execute(principal(null, "ROLE_ADMIN"), person());
        verify(users, times(1)).insertIfAbsent(any());
    }

    @Test
    void emailCollisionNeverAttachesAnotherAccount() {
        doNothing().when(users).insertIfAbsent(any());
        assertEquals(ErrorCode.REGISTRATION_IDENTITY_CONFLICT,
                assertThrows(BusinessException.class, () -> useCase.execute(principal(null, "ROLE_ADMIN"), person())).getErrorCode());
        verify(keycloak, never()).updateNongThinhIdUser(any(), any(), anyString());
        verifyNoInteractions(profiles);
    }

    @Test
    void existingIdentityCannotBeReboundToDifferentClaim() {
        saved.set(User.create(USER, SUBJECT.toString(), Email.of(EMAIL), NOW));
        when(keycloak.getIdentity(any(), anyString())).thenReturn(new KeycloakIdentity(SUBJECT, EMAIL, SUBJECT, true));
        assertThrows(BusinessException.class, () -> useCase.execute(principal(null, "ROLE_ADMIN"), person()));
        verifyNoInteractions(profiles);
    }

    @Test
    void deletedIdentityIsNotRecreated() {
        var user = User.create(USER, SUBJECT.toString(), Email.of(EMAIL), NOW);
        user.delete(NOW);
        saved.set(user);
        assertEquals(ErrorCode.FORBIDDEN, assertThrows(BusinessException.class,
                () -> useCase.execute(principal(null, "ROLE_ADMIN"), person())).getErrorCode());
        verifyNoInteractions(profiles);
    }

    @Test
    void brandCompletionRetainsApprovalFlowAndReconcilesExistingRole() {
        var command = new CompleteRegistrationCommand(null, null, null, "0900000000", "Brand", "Nguyen Van An", "0900000001", "representative@example.com");
        useCase.execute(principal(null, "ROLE_BRAND_PENDING"), command);
        verify(profiles).complete(USER, "ROLE_BRAND_PENDING", command);
        verify(brands).execute(USER);
    }

    @Test
    void wrongRoleAndInvalidFieldsFailBeforeSideEffects() {
        assertThrows(BusinessException.class, () -> useCase.execute(principal(null, "ROLE_UNKNOWN"), person()));
        assertThrows(BusinessException.class, () -> useCase.execute(principal(null, "ROLE_ADMIN"),
                new CompleteRegistrationCommand(null, null, null, "bad", null, null, null, null)));
        verifyNoInteractions(keycloak, profiles, users);
    }

    @Test
    void meReturnsRegistrationRequiredForMissingIdentityOrClaim() {
        assertThrows(RegistrationRequiredException.class, () -> me.execute(principal(null, "ROLE_ADMIN")));
        saved.set(User.create(USER, SUBJECT.toString(), Email.of(EMAIL), NOW));
        assertThrows(RegistrationRequiredException.class, () -> me.execute(principal(null, "ROLE_ADMIN")));
        verifyNoInteractions(queries);
    }

    @Test
    void meTreatsMissingAdminProfileAsRegistrationButNotDownstreamFailures() {
        saved.set(User.create(USER, SUBJECT.toString(), Email.of(EMAIL), NOW));
        assertThrows(RegistrationRequiredException.class, () -> me.execute(principal(USER, "ROLE_ADMIN")));
        when(queries.getAdminProfile(USER)).thenThrow(new InfrastructureException(ErrorCode.PROFILE_SERVICE_GET_ADMIN_PROFILE_FAILED));
        assertThrows(InfrastructureException.class, () -> me.execute(principal(USER, "ROLE_ADMIN")));
    }

    @Test
    void lockedProfileDoesNotTriggerRegistration() {
        saved.set(User.create(USER, SUBJECT.toString(), Email.of(EMAIL), NOW));
        when(queries.getAdminProfile(USER)).thenReturn(Optional.of(new ProfileView(USER, "ADMIN", "Name", null, null, "LOCKED", null, null)));
        assertEquals(ErrorCode.FORBIDDEN, assertThrows(BusinessException.class, () -> me.execute(principal(USER, "ROLE_ADMIN"))).getErrorCode());
    }

    private RegistrationPrincipal principal(UUID claim, String role) {
        return new RegistrationPrincipal(SUBJECT, EMAIL, claim, role, "SUPER_ADMIN", Set.of("admin:role:manage"));
    }

    private CompleteRegistrationCommand person() {
        return new CompleteRegistrationCommand("A", "B", "OTHER", "0900000000", null, null, null, null);
    }
}
