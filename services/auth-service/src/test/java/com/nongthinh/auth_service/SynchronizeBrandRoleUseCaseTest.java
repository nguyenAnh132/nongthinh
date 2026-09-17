package com.nongthinh.auth_service;

import com.nongthinh.auth_service.application.port.in.user.impl.SynchronizeBrandRoleUseCaseImpl;
import com.nongthinh.auth_service.application.port.out.ProfileQuery;
import com.nongthinh.auth_service.application.port.out.keycloak.KeycloakIdp;
import com.nongthinh.auth_service.application.port.out.repository.UserRepository;
import com.nongthinh.auth_service.application.view.ProfileView;
import com.nongthinh.auth_service.domain.user.User;
import com.nongthinh.auth_service.domain.user.valueobject.Email;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SynchronizeBrandRoleUseCaseTest {
    private static final UUID USER = UUID.fromString("00000000-0000-0000-0000-000000000123");
    private final UserRepository users = mock(UserRepository.class);
    private final ProfileQuery profiles = mock(ProfileQuery.class);
    private final KeycloakIdp keycloak = mock(KeycloakIdp.class);
    private final SynchronizeBrandRoleUseCaseImpl sync = new SynchronizeBrandRoleUseCaseImpl(users, profiles, keycloak);

    private void setup(String status, Set<String> roles) {
        when(users.findById(USER)).thenReturn(Optional.of(User.create(USER, "keycloak-user",
                Email.of("brand@example.com"), Instant.parse("2026-09-16T00:00:00Z"))));
        when(profiles.getBrandProfile(USER)).thenReturn(Optional.of(new ProfileView(USER, "BRAND",
                "Brand", null, null, status, null, null)));
        when(keycloak.exchangeClientToken()).thenReturn("token");
        when(keycloak.getRealmRoleNames("keycloak-user", "Bearer token")).thenReturn(roles);
    }

    @Test
    void approvalGrantsBrandAndRemovesPending() {
        setup("ACTIVE", Set.of("ROLE_BRAND_PENDING"));
        sync.execute(USER);
        verify(keycloak).assignRealmRoles("keycloak-user", List.of("ROLE_BRAND"), "Bearer token");
        verify(keycloak).removeRealmRoles("keycloak-user", List.of("ROLE_BRAND_PENDING"), "Bearer token");
    }

    @ParameterizedTest
    @ValueSource(strings = {"PENDING_APPROVAL", "UNDER_REVIEW", "NEEDS_REVISION", "READY_FOR_FINAL_REVIEW",
            "REJECTED", "LOCKED", "DISABLED", "DELETED"})
    void allNonActiveStatesRevokeBrand(String status) {
        setup(status, Set.of("ROLE_BRAND"));
        sync.execute(USER);
        verify(keycloak).assignRealmRoles("keycloak-user", List.of("ROLE_BRAND_PENDING"), "Bearer token");
        verify(keycloak).removeRealmRoles("keycloak-user", List.of("ROLE_BRAND"), "Bearer token");
    }

    @Test
    void duplicateDeliveryDoesNotWriteRolesAgain() {
        setup("ACTIVE", Set.of("ROLE_BRAND"));
        sync.execute(USER);
        sync.execute(USER);
        verify(keycloak, never()).assignRealmRoles(any(), any(), any());
        verify(keycloak, never()).removeRealmRoles(any(), any(), any());
    }

    @Test
    void retryFinishesPartiallyCompletedRoleChange() {
        setup("REJECTED", Set.of("ROLE_BRAND", "ROLE_BRAND_PENDING"));
        doThrow(new IllegalStateException("Keycloak unavailable")).doNothing()
                .when(keycloak).removeRealmRoles("keycloak-user", List.of("ROLE_BRAND"), "Bearer token");
        assertThrows(IllegalStateException.class, () -> sync.execute(USER));
        sync.execute(USER);
        verify(keycloak, never()).assignRealmRoles(any(), any(), any());
        verify(keycloak, times(2)).removeRealmRoles("keycloak-user", List.of("ROLE_BRAND"), "Bearer token");
    }

    @Test
    void delayedApprovalUsesCurrentRejectedState() {
        setup("REJECTED", Set.of("ROLE_BRAND"));
        sync.execute(USER);
        verify(keycloak, never()).assignRealmRoles("keycloak-user", List.of("ROLE_BRAND"), "Bearer token");
    }
}
