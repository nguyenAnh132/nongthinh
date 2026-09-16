package com.nongthinh.file_service.infra.client.boportal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.nongthinh.file_service.application.port.out.FileUploadPolicyProvider;
import com.nongthinh.file_service.domain.file.valueobject.FilePurpose;
import com.nongthinh.file_service.infra.caching.RedisStringCache;
import com.nongthinh.file_service.infra.caching.upload.RedisCachedFileUploadPolicy;

@SpringBootTest
@ActiveProfiles("test")
class FileUploadPolicyHttpIntegrationTest {
    private static final AtomicInteger calls = new AtomicInteger();
    private static final AtomicInteger status = new AtomicInteger(200);
    private static final AtomicReference<String> body = new AtomicReference<>();
    private static final AtomicReference<String> apiKey = new AtomicReference<>();
    private static final HttpServer server = startServer();

    @Autowired
    private FileUploadPolicyProvider provider;
    @MockitoBean
    private RedisStringCache cache;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.security.api-key.clients.bo-portal-service.url",
                () -> "http://127.0.0.1:" + server.getAddress().getPort() + "/bo-portal");
        registry.add("spring.security.api-key.clients.bo-portal-service.api-key", () -> "test-bo-key");
        registry.add("app.upload.default.max-size-bytes.AVATAR", () -> "777");
    }

    @BeforeEach
    void resetServer() {
        calls.set(0);
        status.set(200);
        body.set("""
                {"code":"1000","result":{"purpose":"AVATAR","maxSizeBytes":123,"allowedContentTypes":[],"fileTypes":[]}}
                """);
        when(cache.get(anyString())).thenReturn(Optional.empty());
    }

    @AfterAll
    static void stopServer() {
        server.stop(0);
    }

    @Test
    void primaryDecoratorUsesAuthenticatedHttpResponseAndCachesIt() {
        assertInstanceOf(RedisCachedFileUploadPolicy.class, provider);
        var policy = provider.getPolicy(FilePurpose.AVATAR);
        assertEquals(123, policy.maxSizeBytes());
        assertEquals(Set.of(), policy.allowedContentTypes());
        assertEquals("test-bo-key", apiKey.get());
        assertEquals(1, calls.get());
        verify(cache).put(anyString(), contains("\"maxSizeBytes\":123"), eq(java.time.Duration.ofMinutes(1)));
    }

    @Test
    void httpNotFoundAndServerFailureUseBoundDefaults() {
        for (int code : new int[] {404, 503}) {
            status.set(code);
            body.set("{\"code\":\"SYSTEM_PARAM_NOT_FOUND\"}");
            var policy = provider.getPolicy(FilePurpose.AVATAR);
            assertEquals(777, policy.maxSizeBytes());
            assertEquals(Set.of("image/jpeg", "image/png", "image/webp"), policy.allowedContentTypes());
        }
    }

    @Test
    void malformedHttpResponseUsesDefaults() {
        body.set("broken");
        assertEquals(777, provider.getPolicy(FilePurpose.AVATAR).maxSizeBytes());
    }

    private static HttpServer startServer() {
        try {
            HttpServer stub = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            stub.createContext("/bo-portal/internal/file-upload-policies/AVATAR", exchange -> {
                calls.incrementAndGet();
                apiKey.set(exchange.getRequestHeaders().getFirst("X-API-KEY"));
                byte[] bytes = body.get().getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(status.get(), bytes.length);
                try (var stream = exchange.getResponseBody()) {
                    stream.write(bytes);
                }
            });
            stub.start();
            return stub;
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot start BO HTTP test server", ex);
        }
    }
}
