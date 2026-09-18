package com.nongthinh.auth_service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nongthinh.auth_service.application.command.RegisterAdminCommand;
import com.nongthinh.auth_service.application.command.RegisterBrandCommand;
import com.nongthinh.auth_service.application.command.RegisterFarmerCommand;
import com.nongthinh.auth_service.application.event.DomainEvent;
import com.nongthinh.auth_service.application.port.in.auth.impl.GetMeUseCaseImpl;
import com.nongthinh.auth_service.application.port.in.user.impl.RegisterAdminUseCaseImpl;
import com.nongthinh.auth_service.application.port.in.user.impl.RegisterBrandUseCaseImpl;
import com.nongthinh.auth_service.application.port.in.user.impl.RegisterFarmerUseCaseImpl;
import com.nongthinh.auth_service.application.port.out.ClockProvider;
import com.nongthinh.auth_service.application.port.out.EventPublisher;
import com.nongthinh.auth_service.application.port.out.IdGenerator;
import com.nongthinh.auth_service.application.port.out.ProfileQuery;
import com.nongthinh.auth_service.application.port.out.keycloak.KeycloakIdp;
import com.nongthinh.auth_service.application.port.out.keycloak.RoleRecord;
import com.nongthinh.auth_service.application.port.out.repository.UserRepository;
import com.nongthinh.auth_service.application.view.ProfileView;
import com.nongthinh.auth_service.common.constant.AdminGroupConstant;
import com.nongthinh.auth_service.common.exception.ErrorCode;
import com.nongthinh.auth_service.domain.exception.BusinessException;
import com.nongthinh.auth_service.domain.user.User;
import com.nongthinh.auth_service.domain.user.valueobject.Email;
import com.nongthinh.auth_service.infra.exception.InfrastructureException;
import com.nongthinh.auth_service.infra.persistence.user.UserPersistenceMapper;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;

class AuthIdentityUseCasesTest {
    enum AccountType { FARMER, BRAND, ADMIN }

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final String KEYCLOAK_ID = "10000000-0000-0000-0000-000000000001";
    private static final String EMAIL = "identity@example.com";
    private static final String PASSWORD = "test-registration-password";
    private static final Instant NOW = Instant.parse("2026-09-15T00:00:00Z");
    private final UserRepository users = mock(UserRepository.class);
    private final KeycloakIdp keycloak = mock(KeycloakIdp.class);
    private final IdGenerator ids = mock(IdGenerator.class);
    private final ClockProvider clock = mock(ClockProvider.class);
    private final EventPublisher events = mock(EventPublisher.class);
    private final ProfileQuery profiles = mock(ProfileQuery.class);
    private final ObjectMapper json = new ObjectMapper().registerModule(new JavaTimeModule());

    @ParameterizedTest
    @EnumSource(AccountType.class)
    void registrationRetainsIdentityAndProfileLinkWhileSendingCredentialsOnlyToKeycloak(AccountType type) {
        prepareKeycloak(type);

        register(type);
        if (type == AccountType.BRAND) {
            verify(keycloak).getRoleByName("ROLE_BRAND_PENDING", "Bearer client-token");
            verify(keycloak).assignRealmRoles(KEYCLOAK_ID, java.util.List.of("ROLE_BRAND_PENDING"), "Bearer client-token");
        }

        // Non-default flags ensure removal of the local enabled column does not
        // accidentally change the account settings forwarded to Keycloak.
        verify(keycloak).createUser(USER_ID, EMAIL, PASSWORD, true, false, "Bearer client-token");
        var saved = ArgumentCaptor.forClass(User.class);
        verify(users).save(saved.capture());
        User identity = saved.getValue();
        assertEquals(USER_ID, identity.getId());
        assertEquals(KEYCLOAK_ID, identity.getKeycloakId());
        assertEquals(EMAIL, identity.getEmail().getValue());
        assertNull(identity.getPasswordUpdatedAt());

        var event = ArgumentCaptor.forClass(DomainEvent.class);
        verify(events).publish(event.capture());
        var payload = json.valueToTree(event.getValue());
        assertEquals(USER_ID.toString(), payload.path("userId").asText());
        assertEquals(EMAIL, payload.path("email").asText());
        assertFalse(payload.has("password"));
        assertFalse(payload.has("passwordHash"));
    }

    @ParameterizedTest
    @EnumSource(AccountType.class)
    void duplicateLocalEmailStillPreventsRegistration(AccountType type) {
        when(users.existsByEmail(EMAIL)).thenReturn(true);

        assertThrows(BusinessException.class, () -> register(type));

        verify(users, never()).save(any());
        verifyNoInteractions(keycloak, events);
    }

    @ParameterizedTest
    @EnumSource(AccountType.class)
    void failedKeycloakCreationDoesNotPersistIdentityOrRequestProfile(AccountType type) {
        prepareKeycloak(type);
        when(keycloak.createUser(any(), anyString(), anyString(), anyBoolean(), anyBoolean(), anyString()))
                .thenThrow(new InfrastructureException(ErrorCode.KEYCLOAK_USER_CREATION_FAILED));

        assertThrows(InfrastructureException.class, () -> register(type));

        verify(users, never()).save(any());
        verifyNoInteractions(events);
    }

    @Test
    void meKeepsIdentityAndBrandApprovalFlagsWithoutLocalAuthenticationStatus() {
        User user = User.create(USER_ID, KEYCLOAK_ID, Email.of(EMAIL), NOW);
        when(users.findById(USER_ID)).thenReturn(Optional.of(user));
        Instant deletionAt = NOW.plusSeconds(86400);
        var profile = new ProfileView(UUID.randomUUID(), "BRAND", "Brand", null, null,
                "REJECTED", "Missing documents", deletionAt);
        when(profiles.getBrandProfile(USER_ID)).thenReturn(Optional.of(profile));

        var me = new GetMeUseCaseImpl(users, profiles, mock(com.nongthinh.auth_service.application.port.in.user.SynchronizeBrandRoleUseCase.class))
                .execute(USER_ID, Set.of("ROLE_BRAND"), null, Set.of());

        assertEquals(USER_ID, me.userId());
        assertEquals(EMAIL, me.email());
        assertEquals(profile, me.profile());
        assertTrue(me.flags().brandRejected());
        assertFalse(me.flags().requiresProfileCompletion());
        assertEquals(deletionAt, me.flags().canReRegisterAt());
        var response = json.valueToTree(me);
        assertFalse(response.has("isEnabled"));
        assertFalse(response.has("enabled"));
        assertFalse(response.has("passwordHash"));
        assertFalse(response.has("authProvider"));
        assertFalse(response.path("flags").has("requiresEmailVerification"));
        verifyNoInteractions(keycloak);
    }

    @Test
    void meStillRequiresProfileCompletionWhenProfileHasNotArrived() {
        when(users.findById(USER_ID)).thenReturn(Optional.of(User.create(USER_ID, KEYCLOAK_ID, Email.of(EMAIL), NOW)));
        when(profiles.getFarmerProfile(USER_ID)).thenReturn(Optional.empty());

        var me = new GetMeUseCaseImpl(users, profiles, mock(com.nongthinh.auth_service.application.port.in.user.SynchronizeBrandRoleUseCase.class))
                .execute(USER_ID, Set.of("ROLE_FARMER"), null, Set.of());

        assertTrue(me.flags().requiresProfileCompletion());
        assertNull(me.profile());
    }

    @Test
    void persistencePreservesLegacyPasswordTimestampAndIdentity() {
        Instant passwordUpdatedAt = NOW.minusSeconds(86400);
        User original = User.reconstruct(USER_ID, KEYCLOAK_ID, Email.of(EMAIL), passwordUpdatedAt,
                NOW.minusSeconds(172800), NOW, NOW);
        var mapper = new UserPersistenceMapper();

        User restored = mapper.toDomain(mapper.toEntity(original));

        assertEquals(USER_ID, restored.getId());
        assertEquals(KEYCLOAK_ID, restored.getKeycloakId());
        assertEquals(EMAIL, restored.getEmail().getValue());
        assertEquals(passwordUpdatedAt, restored.getPasswordUpdatedAt());
        assertEquals(original.getCreatedAt(), restored.getCreatedAt());
        assertEquals(NOW, restored.getUpdatedAt());
        assertEquals(NOW, restored.getDeletedAt());
    }

    private void prepareKeycloak(AccountType type) {
        when(ids.generate()).thenReturn(USER_ID, UUID.randomUUID());
        when(clock.now()).thenReturn(NOW);
        when(keycloak.exchangeClientToken()).thenReturn("client-token");
        when(keycloak.createUser(any(), anyString(), anyString(), anyBoolean(), anyBoolean(), anyString()))
                .thenReturn(KEYCLOAK_ID);
        when(keycloak.getRoleByName(anyString(), anyString()))
                .thenReturn(new RoleRecord(UUID.randomUUID(), (type == AccountType.BRAND ? "ROLE_BRAND_PENDING" : "ROLE_" + type), false, false, null));
    }

    private void register(AccountType type) {
        switch (type) {
            case FARMER -> new RegisterFarmerUseCaseImpl(users, ids, clock, keycloak, events).execute(
                    new RegisterFarmerCommand(EMAIL, PASSWORD, true, false, "First", "Last", "MALE",
                            "0900000000", null, null, null, null));
            case BRAND -> new RegisterBrandUseCaseImpl(users, ids, clock, keycloak, events).execute(
                    new RegisterBrandCommand(EMAIL, PASSWORD, true, false, "Brand", null, null,
                            "0900000000", null, null, null, "Representative", "0900000000", EMAIL,
                            null, null, null));
            case ADMIN -> new RegisterAdminUseCaseImpl(users, ids, clock, keycloak, events).execute(
                    new RegisterAdminCommand(EMAIL, PASSWORD, true, false, "First", "Last", "0900000000",
                            AdminGroupConstant.SUPER_ADMIN, null));
        }
    }
}
