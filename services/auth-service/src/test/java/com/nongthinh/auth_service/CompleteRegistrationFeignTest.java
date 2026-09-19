package com.nongthinh.auth_service;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.http.converter.autoconfigure.ClientHttpMessageConvertersCustomizer;
import org.springframework.cloud.openfeign.support.FeignHttpMessageConverters;
import org.springframework.cloud.openfeign.support.HttpMessageConverterCustomizer;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.support.GenericApplicationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nongthinh.auth_service.infra.client.keycloak.KeycloakClient;
import com.nongthinh.auth_service.infra.client.keycloak.KeycloakIdpImpl;
import feign.Feign;
import feign.Request;
import feign.Response;
import feign.Retryer;

class CompleteRegistrationFeignTest {
    @Test
    void springFeignSendsSingleAttributesObjectAsJsonAndReadsUpdatedIdentity() throws Exception {
        var requests = new ArrayList<Request>();
        var json = new ObjectMapper();
        var subject = UUID.fromString("00000000-0000-0000-0000-000000000002");
        var user = UUID.fromString("00000000-0000-0000-0000-000000000001");
        var beans = new DefaultListableBeanFactory();
        beans.registerSingleton("converters", new FeignHttpMessageConverters(
                beans.getBeanProvider(ClientHttpMessageConvertersCustomizer.class),
                beans.getBeanProvider(HttpMessageConverterCustomizer.class)));
        var converters = beans.getBeanProvider(FeignHttpMessageConverters.class);
        try (var context = new GenericApplicationContext()) {
            context.getEnvironment().getPropertySources().addFirst(
                    new org.springframework.core.env.MapPropertySource("test", Map.of("keycloak.realm", "test-realm")));
            context.refresh();
            var contract = new SpringMvcContract();
            contract.setResourceLoader(context);
            var client = Feign.builder().contract(contract)
                    .encoder(new SpringEncoder(converters)).decoder(new SpringDecoder(converters))
                    .retryer(Retryer.NEVER_RETRY)
                    .client((request, options) -> {
                        requests.add(request);
                        if (request.httpMethod() == Request.HttpMethod.PUT) {
                            // Keycloak validates root profile fields when attributes are supplied.
                            if (!json.readTree(request.body()).hasNonNull("username")) {
                                return Response.builder().request(request).status(400)
                                        .headers(Map.of("Content-Type", List.of("application/json")))
                                        .body("{\"errorMessage\":\"User name is missing\"}", StandardCharsets.UTF_8).build();
                            }
                            return Response.builder().request(request).status(204).headers(Map.of()).build();
                        }
                        var attributes = requests.size() == 1
                                ? Map.of("locale", List.of("vi"))
                                : Map.of("locale", List.of("vi"), "nongthinh_id", List.of(user.toString()));
                        return Response.builder().request(request).status(200)
                                .headers(Map.of("Content-Type", List.of("application/json")))
                                .body(json.writeValueAsString(Map.of("id", subject.toString(), "username", "admin-login",
                                        "email", "admin@example.com", "firstName", "An", "lastName", "Nguyen",
                                        "enabled", true, "attributes", attributes)), StandardCharsets.UTF_8).build();
                    }).target(KeycloakClient.class, "http://keycloak.test");
            new KeycloakIdpImpl(client, Instant::now).updateNongThinhIdUser(subject, user, "Bearer test-token");
        }
        assertEquals(List.of(Request.HttpMethod.GET, Request.HttpMethod.PUT, Request.HttpMethod.GET),
                requests.stream().map(Request::httpMethod).toList());
        var update = requests.get(1);
        assertEquals("http://keycloak.test/admin/realms/test-realm/users/" + subject, update.url());
        assertTrue(update.headers().get("Content-Type").stream().anyMatch(value -> value.startsWith("application/json")));
        assertEquals(List.of("Bearer test-token"), List.copyOf(update.headers().get("Authorization")));
        assertEquals(json.readTree("""
                {"username":"admin-login","email":"admin@example.com","firstName":"An","lastName":"Nguyen",
                 "attributes":{"locale":["vi"],"nongthinh_id":["00000000-0000-0000-0000-000000000001"]}}
                """), json.readTree(update.body()));
    }
}
