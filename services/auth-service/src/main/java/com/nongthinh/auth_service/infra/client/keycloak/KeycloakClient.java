package com.nongthinh.auth_service.infra.client.keycloak;

import java.util.List;
import java.util.Map;

import com.nongthinh.auth_service.infra.client.keycloak.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import feign.QueryMap;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@FeignClient(name = "keycloak-client", url = "${keycloak.url}")
public interface KeycloakClient {

    @GetMapping("/admin/realms/${keycloak.realm}/users/{userId}")
    com.nongthinh.auth_service.infra.client.keycloak.dto.KeycloakIdentityResponse getIdentity(
            @RequestHeader("Authorization") String token, @PathVariable String userId);

    @PutMapping("/admin/realms/${keycloak.realm}/users/{userId}")
    void updateNongThinhIdUser(
            @RequestHeader("Authorization") String token,
            @PathVariable String userId,
            @RequestBody KeycloakUserUpdateNongThinhIdParam request
    );

    @PostMapping(value = "/admin/realms/${keycloak.realm}/users", 
        consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> createUser(
        @RequestHeader("Authorization") String token,
        @RequestBody KeycloakUserRegisterParam request
    );

    @PostMapping(value = "/realms/${keycloak.realm}/protocol/openid-connect/token", 
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    TokenExchangeResponse exchangeClientToken(@QueryMap TokenExchangeParam param);

    @PostMapping(value = "/realms/${keycloak.realm}/protocol/openid-connect/token", 
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    TokenExchangeResponse refreshToken(@QueryMap RefreshTokenParam param);

    @PostMapping(value = "/realms/${keycloak.realm}/protocol/openid-connect/logout", 
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    void logout(@QueryMap LogoutParam param);

    @GetMapping(value = "/admin/realms/${keycloak.realm}/roles/{name}", 
        consumes = MediaType.APPLICATION_JSON_VALUE)
    RoleResponse getRoleByName(
        @RequestHeader("Authorization") String token,
        @PathVariable String name
    );

    @PostMapping(value = "/admin/realms/${keycloak.realm}/users/{userId}/role-mappings/realm", 
        consumes = MediaType.APPLICATION_JSON_VALUE)
    void roleMapping(
        @RequestHeader("Authorization") String token,
        @PathVariable String userId, 
        @RequestBody List<RoleMapping> roles
        
    );
    @GetMapping("/admin/realms/${keycloak.realm}/users/{userId}/role-mappings/realm")
    List<RoleResponse> getRealmRoles(@RequestHeader("Authorization") String token, @PathVariable String userId);

    @org.springframework.web.bind.annotation.DeleteMapping("/admin/realms/${keycloak.realm}/users/{userId}/role-mappings/realm")
    void removeRealmRoles(@RequestHeader("Authorization") String token, @PathVariable String userId,
            @RequestBody List<RoleMapping> roles);

    @PostMapping("/admin/realms/${keycloak.realm}/roles")
    void createRole(@RequestHeader("Authorization") String token, @RequestBody java.util.Map<String, String> role);
}
