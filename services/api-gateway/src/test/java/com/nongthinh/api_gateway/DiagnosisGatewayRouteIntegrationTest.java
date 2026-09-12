package com.nongthinh.api_gateway;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("gateway-test")
class DiagnosisGatewayRouteIntegrationTest {

    private static final CompletableFuture<UpstreamRequest> UPSTREAM_REQUEST = new CompletableFuture<>();
    private static final HttpServer UPSTREAM = startUpstream();

    @LocalServerPort
    private int gatewayPort;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("test.upstream-url", () -> "http://localhost:" + UPSTREAM.getAddress().getPort());
    }

    @AfterAll
    static void stopUpstream() {
        UPSTREAM.stop(0);
    }

    @Test
    void stripsBrowserApiKeyAndForwardsBearerTokenToDiagnosisService() throws Exception {
        HttpResponse<String> response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + gatewayPort + "/api/v1/diagnoses/history"))
                        .header("Authorization", "Bearer farmer-jwt")
                        .header("X-API-KEY", "browser-must-not-reach-upstream")
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        UpstreamRequest upstream = UPSTREAM_REQUEST.get(5, TimeUnit.SECONDS);
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(upstream.path()).isEqualTo("/diagnosis/diagnoses/history");
        assertThat(upstream.headers()).doesNotContainKey("x-api-key");
        assertThat(upstream.headers()).containsEntry("authorization", "Bearer farmer-jwt");
    }

    private static HttpServer startUpstream() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            server.createContext("/", DiagnosisGatewayRouteIntegrationTest::respond);
            server.start();
            return server;
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot start gateway test upstream", ex);
        }
    }

    private static void respond(HttpExchange exchange) throws IOException {
        Map<String, String> headers = exchange.getRequestHeaders().entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        entry -> entry.getKey().toLowerCase(),
                        entry -> entry.getValue().getFirst()));
        UPSTREAM_REQUEST.complete(new UpstreamRequest(exchange.getRequestURI().getPath(), headers));
        byte[] response = "{\"ok\":true}".getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    private record UpstreamRequest(String path, Map<String, String> headers) {
    }
}
