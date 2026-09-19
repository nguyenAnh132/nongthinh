package com.nongthinh.auth_service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import com.nongthinh.auth_service.infra.CurrentUserProviderImpl;
import com.nongthinh.auth_service.infra.client.keycloak.*;
import com.nongthinh.auth_service.infra.client.keycloak.dto.KeycloakIdentityResponse;
import com.nongthinh.auth_service.infra.client.keycloak.dto.KeycloakUserUpdateNongThinhIdParam;
import com.nongthinh.auth_service.common.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nongthinh.auth_service.infra.client.profileservice.*;
import com.nongthinh.auth_service.domain.exception.BusinessException;
import com.nongthinh.auth_service.infra.exception.InfrastructureException;

class CompleteRegistrationAdaptersTest {
    private static final UUID USER = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID SUBJECT = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @AfterEach
    void clearSecurity() { SecurityContextHolder.clearContext(); }

    @Test
    void registrationPrincipalAllowsMissingLocalClaimButRequiresAuthenticatedSubjectAndRole() {
        var provider = new CurrentUserProviderImpl();
        assertThrows(BusinessException.class, provider::getRegistrationPrincipal);
        var jwt = Jwt.withTokenValue("test").header("alg", "RS256").subject(SUBJECT.toString()).claim("email", "admin@example.com").build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        var principal = provider.getRegistrationPrincipal();
        assertEquals(SUBJECT, principal.keycloakId());
        assertNull(principal.applicationUserId());
        assertEquals("ROLE_ADMIN", principal.role());
        assertThrows(BusinessException.class, provider::getCurrentUser);
    }

    @Test
    void malformedLocalClaimIsInvalidTokenInsteadOfRegistrationRequired() {
        var jwt = Jwt.withTokenValue("test").header("alg", "RS256").subject(SUBJECT.toString())
                .claim("email", "admin@example.com").claim("nongthinh_id", "not-a-uuid").build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        assertThrows(BusinessException.class, () -> new CurrentUserProviderImpl().getRegistrationPrincipal());
    }

    @Test
    void linkingPreservesOtherAttributesAndVerifiesPersistedClaim() throws Exception {
        var client = mock(KeycloakClient.class);
        when(client.getIdentity("token", SUBJECT.toString())).thenReturn(
                new KeycloakIdentityResponse(SUBJECT.toString(), "admin-login", "admin@example.com", "An", "Nguyen", true, Map.of("locale", List.of("vi"))),
                new KeycloakIdentityResponse(SUBJECT.toString(), "admin-login", "admin@example.com", "An", "Nguyen", true, Map.of("locale", List.of("vi"), "nongthinh_id", List.of(USER.toString()))));
        new KeycloakIdpImpl(client, Instant::now).updateNongThinhIdUser(SUBJECT, USER, "token");
        var payload = org.mockito.ArgumentCaptor.forClass(KeycloakUserUpdateNongThinhIdParam.class);
        var ordered = inOrder(client);
        ordered.verify(client).getIdentity("token", SUBJECT.toString());
        ordered.verify(client).updateNongThinhIdUser(eq("token"), eq(SUBJECT.toString()), payload.capture());
        ordered.verify(client).getIdentity("token", SUBJECT.toString());
        var json = new ObjectMapper();
        assertEquals(json.readTree("""
                {"username":"admin-login","email":"admin@example.com","firstName":"An","lastName":"Nguyen",
                 "attributes":{"locale":["vi"],"nongthinh_id":["00000000-0000-0000-0000-000000000001"]}}
                """), json.readTree(json.writeValueAsString(payload.getValue())));
    }

    @Test
    void existingDifferentLinkCannotBeOverwritten() {
        var client = mock(KeycloakClient.class);
        when(client.getIdentity(anyString(), anyString())).thenReturn(new KeycloakIdentityResponse(SUBJECT.toString(), "admin-login", "admin@example.com", "An", "Nguyen", true, Map.of("nongthinh_id", List.of(SUBJECT.toString()))));
        assertThrows(BusinessException.class, () -> new KeycloakIdpImpl(client, Instant::now).updateNongThinhIdUser(SUBJECT, USER, "token"));
        verify(client, never()).updateNongThinhIdUser(anyString(), anyString(), any());
    }

    @Test
    void missingAttributesCanBeLinked() {
        var client = mock(KeycloakClient.class);
        when(client.getIdentity("token", SUBJECT.toString())).thenReturn(
                new KeycloakIdentityResponse(SUBJECT.toString(), "admin-login", "admin@example.com", null, null, true, null),
                new KeycloakIdentityResponse(SUBJECT.toString(), "admin-login", "admin@example.com", null, null, true, Map.of("nongthinh_id", List.of(USER.toString()))));
        new KeycloakIdpImpl(client, Instant::now).updateNongThinhIdUser(SUBJECT, USER, "token");
        verify(client).updateNongThinhIdUser(eq("token"), eq(SUBJECT.toString()),
                argThat(body -> body.getAttributes().equals(Map.of("nongthinh_id", List.of(USER.toString())))
                        && "admin-login".equals(body.getUsername()) && "admin@example.com".equals(body.getEmail())
                        && body.getFirstName() == null && body.getLastName() == null));
    }

    @Test
    void repeatedLinkDoesNotWriteAgain() {
        var client = mock(KeycloakClient.class);
        when(client.getIdentity("token", SUBJECT.toString())).thenReturn(
                new KeycloakIdentityResponse(SUBJECT.toString(), "admin-login", "admin@example.com", "An", "Nguyen", true, Map.of("nongthinh_id", List.of(USER.toString()))));
        new KeycloakIdpImpl(client, Instant::now).updateNongThinhIdUser(SUBJECT, USER, "token");
        verify(client, never()).updateNongThinhIdUser(anyString(), anyString(), any());
    }

    @Test
    void updateNotPersistedByKeycloakIsReportedAsSyncFailure() {
        var client = mock(KeycloakClient.class);
        when(client.getIdentity("token", SUBJECT.toString())).thenReturn(
                new KeycloakIdentityResponse(SUBJECT.toString(), "admin-login", "admin@example.com", "An", "Nguyen", true, Map.of()));
        var error = assertThrows(InfrastructureException.class,
                () -> new KeycloakIdpImpl(client, Instant::now).updateNongThinhIdUser(SUBJECT, USER, "token"));
        assertEquals(ErrorCode.KEYCLOAK_IDENTITY_SYNC_FAILED, error.getErrorCode());
    }

    @Test
    void profile404IsAbsenceBut500RemainsInfrastructureFailure() {
        var client = mock(ProfileClient.class);
        var query = new ProfileQueryImpl(client, mock(ProfileViewMapper.class), "test-key");
        doThrow(httpError(404)).when(client).getAdminProfile(USER, "test-key");
        assertTrue(query.getAdminProfile(USER).isEmpty());
        doThrow(httpError(500)).when(client).getAdminProfile(USER, "test-key");
        assertThrows(InfrastructureException.class, () -> query.getAdminProfile(USER));
    }

    private feign.FeignException httpError(int status) {
        var request = feign.Request.create(feign.Request.HttpMethod.GET, "http://localhost/profile", Map.of(), null, java.nio.charset.StandardCharsets.UTF_8, null);
        return feign.FeignException.errorStatus("getAdminProfile", feign.Response.builder().status(status).reason("test").request(request).headers(Map.of()).build());
    }
}
