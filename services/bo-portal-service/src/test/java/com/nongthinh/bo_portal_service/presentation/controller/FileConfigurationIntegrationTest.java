package com.nongthinh.bo_portal_service.presentation.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:file_config_test;MODE=PostgreSQL;NON_KEYWORDS=VALUE;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE"
})
class FileConfigurationIntegrationTest {
    @LocalServerPort
    private int port;
    @Autowired
    private JdbcTemplate jdbc;
    @MockitoBean
    private JwtDecoder jwtDecoder;

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        jdbc.update("delete from file_purpose_types");
        jdbc.update("delete from file_types");
        jdbc.update("delete from system_params");
        jdbc.update("delete from system_param_types");
        jdbc.update("""
                insert into system_param_types (id, name, system_defined, created_at, updated_at)
                values (900, 'File configuration', true, current_timestamp, current_timestamp)
                """);
        jdbc.update("""
                insert into system_params (id, name, value, data_type, system_defined, type_id, created_at, updated_at)
                values (900, 'FILE_UPLOAD_MAX_BYTES_AVATAR', '2097152', 'INTEGER', true, 900,
                    current_timestamp, current_timestamp)
                """);
        jdbc.update("insert into file_types (code, content_type, extension) values ('PNG', 'image/png', 'png')");
        jdbc.update("""
                insert into file_purpose_types (id, purpose, file_type_code, enabled, created_at, updated_at)
                values ('60000000-0000-0000-0000-000000000001', 'AVATAR', 'PNG', true,
                    current_timestamp, current_timestamp)
                """);
        when(jwtDecoder.decode("reader")).thenReturn(jwt("reader", List.of("system:config:read")));
        when(jwtDecoder.decode("user")).thenReturn(jwt("user", List.of()));
        when(jwtDecoder.decode("writer")).thenReturn(jwt("writer", List.of("system:config:read", "system:config:write")));
    }

    @Test
    void internalPolicyRequiresApiKeyAndReturnsConfiguredSizeAndTypes() throws Exception {
        assertEquals(401, send("GET", "/internal/file-upload-policies/AVATAR", null, null).statusCode());
        var response = send("GET", "/internal/file-upload-policies/AVATAR", "internal", null);
        assertEquals(200, response.statusCode(), response.body());
        var result = mapper.readTree(response.body()).path("result");
        assertEquals(2097152, result.path("maxSizeBytes").asLong());
        assertEquals("image/png", result.path("allowedContentTypes").get(0).asText());
    }

    @Test
    void readerCannotChangePolicyButWriterCanDisableAndReenableType() throws Exception {
        assertEquals(200, send("GET", "/file-types", "reader", null).statusCode());
        assertEquals(200, send("GET", "/file-upload-policies/AVATAR", "reader", null).statusCode());
        String path = "/file-upload-policies/AVATAR/file-types/PNG";
        assertEquals(403, send("PUT", path, "reader", "{\"enabled\":false}").statusCode());
        var response = send("PUT", path, "writer", "{\"enabled\":false}");
        assertEquals(200, response.statusCode(), response.body());
        assertFalse(jdbc.queryForObject("select enabled from file_purpose_types", Boolean.class));
        var policy = mapper.readTree(send("GET", "/internal/file-upload-policies/AVATAR", "internal", null).body());
        assertEquals(0, policy.path("result").path("allowedContentTypes").size());
        assertEquals(200, send("PUT", path, "writer", "{\"enabled\":true}").statusCode());
        assertTrue(jdbc.queryForObject("select enabled from file_purpose_types", Boolean.class));
    }

    @Test
    void existingSystemParamApiUpdatesSizeAndRejectsNonPositiveValues() throws Exception {
        String path = "/system-params/FILE_UPLOAD_MAX_BYTES_AVATAR";
        var response = send("PUT", path, "writer", "{\"value\":\" +01024 \"}");
        assertEquals(200, response.statusCode(), response.body());
        var policy = mapper.readTree(send("GET", "/internal/file-upload-policies/AVATAR", "internal", null).body());
        assertEquals(1024, policy.path("result").path("maxSizeBytes").asLong());
        for (String value : List.of("0", "-1", "invalid", "2147483648")) {
            assertEquals(400, send("PUT", path, "writer", "{\"value\":\"" + value + "\"}").statusCode());
        }
        assertEquals("1024", jdbc.queryForObject("select value from system_params where id=900", String.class));
    }

    @Test
    void invalidPurposeTypeOrMissingEnabledFlagCannotModifyConfiguration() throws Exception {
        assertEquals(400, send("GET", "/file-upload-policies/UNKNOWN", "reader", null).statusCode());
        assertEquals(404, send("PUT", "/file-upload-policies/AVATAR/file-types/EXE", "writer",
                "{\"enabled\":true}").statusCode());
        assertEquals(400, send("PUT", "/file-upload-policies/AVATAR/file-types/PNG", "writer", "{}").statusCode());
        assertTrue(jdbc.queryForObject("select enabled from file_purpose_types", Boolean.class));
    }

    @Test
    void missingConfigurationIsDistinctFromExplicitlyDisabledTypes() throws Exception {
        jdbc.update("delete from system_params");
        jdbc.update("update file_purpose_types set enabled=false");
        var result = mapper.readTree(send("GET", "/internal/file-upload-policies/AVATAR", "internal", null).body())
                .path("result");
        assertTrue(result.path("maxSizeBytes").isNull());
        assertTrue(result.path("allowedContentTypes").isArray());
        assertEquals(0, result.path("allowedContentTypes").size());
        jdbc.update("delete from file_purpose_types");
        result = mapper.readTree(send("GET", "/internal/file-upload-policies/AVATAR", "internal", null).body())
                .path("result");
        assertTrue(result.path("allowedContentTypes").isNull());
    }

    @Test
    void authenticatedUserCanReadUploadConstraintsButCannotAccessAdminConfiguration() throws Exception {
        assertEquals(401, send("GET", "/upload-policies/AVATAR", null, null).statusCode());
        var response = send("GET", "/upload-policies/AVATAR", "user", null);
        assertEquals(200, response.statusCode(), response.body());
        var result = mapper.readTree(response.body()).path("result");
        assertEquals(2097152, result.path("maxSizeBytes").asLong());
        assertEquals("png", result.path("allowedExtensions").get(0).asText());
        assertFalse(result.has("fileTypes"));
        assertEquals(403, send("GET", "/file-upload-policies/AVATAR", "user", null).statusCode());
        assertEquals(403, send("PUT", "/file-upload-policies/AVATAR/file-types/PNG", "user",
                "{\"enabled\":false}").statusCode());
        jdbc.update("update file_purpose_types set enabled=false");
        result = mapper.readTree(send("GET", "/upload-policies/AVATAR", "user", null).body()).path("result");
        assertEquals(0, result.path("allowedContentTypes").size());
        assertEquals(0, result.path("allowedExtensions").size());
    }

    private Jwt jwt(String token, List<String> authorities) {
        return Jwt.withTokenValue(token).header("alg", "RS256").subject("test-admin")
                .claim("realm_access", Map.of("roles", authorities)).build();
    }

    private HttpResponse<String> send(String method, String path, String identity, String body) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json");
        if ("internal".equals(identity)) {
            builder.header("X-API-KEY", "test-bo-portal-key");
        } else if (identity != null) {
            builder.header("Authorization", "Bearer " + identity);
        }
        builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        try (var client = HttpClient.newHttpClient()) {
            return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        }
    }
}
