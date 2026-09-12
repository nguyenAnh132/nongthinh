package com.nongthinh.auth_service.infra.client.keycloak;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.nongthinh.auth_service.infra.client.keycloak.dto.KeycloakUserRegisterParam;
import com.nongthinh.auth_service.infra.client.keycloak.dto.TokenExchangeParam;
import feign.QueryMap;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import com.nongthinh.auth_service.infra.client.keycloak.dto.RefreshTokenParam;
import com.nongthinh.auth_service.infra.client.keycloak.dto.RoleMapping;
import com.nongthinh.auth_service.infra.client.keycloak.dto.TokenExchangeResponse;
import com.nongthinh.auth_service.infra.client.keycloak.dto.LogoutParam;
import com.nongthinh.auth_service.infra.client.keycloak.dto.RoleResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "keycloak-client", url = "${keycloak.url}")
public interface KeycloakClient {

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
}
