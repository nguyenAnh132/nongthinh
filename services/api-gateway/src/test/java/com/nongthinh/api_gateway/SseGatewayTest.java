package com.nongthinh.api_gateway;

import static org.assertj.core.api.Assertions.assertThat;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SseGatewayTest {
    static final CountDownLatch release = new CountDownLatch(1);
    static final AtomicReference<String> cookie = new AtomicReference<>();
    static final AtomicReference<String> apiKey = new AtomicReference<>();
    static final HttpServer upstream = upstream();
    @org.springframework.beans.factory.annotation.Value("${local.server.port}") int port;

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry properties) {
        String root = "spring.cloud.gateway.server.webmvc.routes[0].";
        properties.add(root + "id", () -> "sse-test");
        properties.add(root + "uri", () -> "http://127.0.0.1:" + upstream.getAddress().getPort());
        properties.add(root + "predicates[0]", () -> "Path=/api/v1/notification/events/stream");
        properties.add(root + "filters[0]", () -> "StripPrefix=2");
        properties.add(root + "filters[1]", () -> "RemoveRequestHeader=X-API-KEY");
    }

    @AfterAll
    static void stop() { release.countDown(); upstream.stop(0); }

    @Test
    void flushesSseBeforeUpstreamClosesAndForwardsOnlyUserCredentials() throws Exception {
        try (var client = HttpClient.newHttpClient(); var pool = Executors.newSingleThreadExecutor()) {
            var response = client.sendAsync(HttpRequest.newBuilder(
                    URI.create("http://127.0.0.1:" + port + "/api/v1/notification/events/stream?channels=notifications"))
                    .header("Cookie", "access_token=test-session").header("X-API-KEY", "untrusted")
                    .timeout(Duration.ofSeconds(10)).build(), HttpResponse.BodyHandlers.ofInputStream())
                    .get(5, TimeUnit.SECONDS);
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.headers().firstValue("X-Accel-Buffering")).contains("no");
            try (var reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                var line = pool.submit(reader::readLine);
                try { assertThat(line.get(3, TimeUnit.SECONDS)).isEqualTo("event: connected"); }
                finally { release.countDown(); }
            }
            assertThat(cookie.get()).isEqualTo("access_token=test-session");
            assertThat(apiKey.get()).isNull();
        }
    }

    private static HttpServer upstream() {
        try {
            var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/notification/events/stream", exchange -> {
                cookie.set(exchange.getRequestHeaders().getFirst("Cookie"));
                apiKey.set(exchange.getRequestHeaders().getFirst("X-API-KEY"));
                exchange.getResponseHeaders().set("Content-Type", "text/event-stream");
                exchange.getResponseHeaders().set("X-Accel-Buffering", "no");
                exchange.sendResponseHeaders(200, 0);
                try (var body = exchange.getResponseBody()) {
                    body.write("event: connected\ndata: {}\n\n".getBytes(StandardCharsets.UTF_8));
                    body.flush();
                    try { release.await(8, TimeUnit.SECONDS); }
                    catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
                }
            });
            server.start();
            return server;
        } catch (IOException ex) { throw new UncheckedIOException(ex); }
    }
}
