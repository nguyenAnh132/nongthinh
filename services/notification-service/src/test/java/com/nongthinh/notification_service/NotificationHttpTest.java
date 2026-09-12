package com.nongthinh.notification_service;

import static org.assertj.core.api.Assertions.assertThat;
import com.nongthinh.notification_service.application.event.PostEngagementEvent;
import com.nongthinh.notification_service.application.port.in.notification.HandlePostEngagementUseCase;
import com.nongthinh.notification_service.infra.realtime.SseConnectionRegistry;
import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.*;
import tools.jackson.databind.ObjectMapper;

@EnabledIfSystemProperty(named = "sse.local.integration", matches = "true")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:postgresql://127.0.0.1:55433/sse_notification_test",
        "spring.datasource.username=postgres", "spring.datasource.password=unused",
        "spring.kafka.bootstrap-servers=127.0.0.1:1", "spring.kafka.listener.auto-startup=false",
        "in-app-notification.enabled=true", "post-realtime.enabled=true"
})
@Import(NotificationHttpTest.JwtConfig.class)
class NotificationHttpTest {
    @Value("${local.server.port}") int port;
    @Autowired HandlePostEngagementUseCase handle;
    @Autowired SseConnectionRegistry registry;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper json;

    @TestConfiguration
    static class JwtConfig {
        @Bean @Primary
        JwtDecoder testDecoder() {
            return token -> Jwt.withTokenValue(token).header("alg", "none").claim("nongthinh_id", token)
                    .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build();
        }
    }

    @Test
    void cookieAuthenticationPrivateNotificationsAndTwoTabDelivery() throws Exception {
        UUID actor = UUID.randomUUID(), recipient = UUID.randomUUID(), post = UUID.randomUUID(), eventId = UUID.randomUUID();
        try (var client = HttpClient.newHttpClient(); var pool = Executors.newFixedThreadPool(2)) {
            assertThat(client.send(request("/notifications", null).GET().build(), HttpResponse.BodyHandlers.ofString()).statusCode()).isEqualTo(401);
            var first = client.send(request("/events/stream?channels=notifications", recipient).GET().build(), HttpResponse.BodyHandlers.ofInputStream());
            var second = client.send(request("/events/stream?channels=notifications,post-engagement", recipient).GET().build(), HttpResponse.BodyHandlers.ofInputStream());
            assertThat(first.statusCode()).isEqualTo(200);
            try (var a = new BufferedReader(new InputStreamReader(first.body()));
                 var b = new BufferedReader(new InputStreamReader(second.body()))) {
                handle.execute(new PostEngagementEvent(eventId, "post.comment.updated", 1, Instant.now(),
                        post, 1, Map.of("postId", post, "commentRootTotal", 1, "commentTotal", 1, "commentVersion", 1),
                        actor, recipient, "POST_COMMENT", true));
                registry.notificationChanges();
                var eventA = pool.submit(() -> readEvent(a, "notification.created"));
                var eventB = pool.submit(() -> readEvent(b, "notification.created"));
                assertThat(eventA.get(4, TimeUnit.SECONDS)).contains("\"unreadCount\":1");
                assertThat(eventB.get(4, TimeUnit.SECONDS)).contains("\"unreadCount\":1");
                var list = client.send(request("/notifications", recipient).GET().build(), HttpResponse.BodyHandlers.ofString());
                var items = json.readTree(list.body()).path("result").path("items");
                assertThat(items.size()).isEqualTo(1);
                String id = items.get(0).path("id").asText();
                var foreignRead = client.send(request("/notifications/" + id + "/read", actor)
                        .method("PATCH", HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofString());
                assertThat(foreignRead.statusCode()).isEqualTo(404);
                var ownRead = client.send(request("/notifications/" + id + "/read", recipient)
                        .method("PATCH", HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofString());
                assertThat(ownRead.statusCode()).isEqualTo(200);
                registry.notificationChanges();
                assertThat(pool.submit(() -> readEvent(a, "notification.read")).get(4, TimeUnit.SECONDS)).contains("\"unreadCount\":0");
                assertThat(pool.submit(() -> readEvent(b, "notification.read")).get(4, TimeUnit.SECONDS)).contains("\"unreadCount\":0");
                registry.broadcast(new PostEngagementEvent(UUID.randomUUID(), "post.reaction.updated", 1,
                        Instant.now(), post, 2, Map.of("postId", post, "reactionTotal", 2, "reactionVersion", 2),
                        actor, recipient, "POST_REACTION", true));
                String metrics = pool.submit(() -> readEvent(b, "post.reaction.updated")).get(4, TimeUnit.SECONDS);
                assertThat(metrics).contains("\"reactionTotal\":2").doesNotContain("recipientUserId", "actorUserId", "notificationType");
            } finally { registry.shutdown(); }
        } finally {
            jdbc.update("DELETE FROM notifications WHERE recipient_user_id=?", recipient);
            jdbc.update("DELETE FROM notification_user_state WHERE user_id=?", recipient);
            jdbc.update("DELETE FROM notification_processed_events WHERE event_id=?", eventId);
        }
    }

    private HttpRequest.Builder request(String path, UUID userId) {
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/notification" + path)).timeout(Duration.ofSeconds(10));
        if (userId != null) request.header("Cookie", "access_token=" + userId);
        return request;
    }

    private String readEvent(BufferedReader reader, String name) throws IOException {
        StringBuilder frame = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) {
                if (frame.toString().contains("event:" + name) || frame.toString().contains("event: " + name)) return frame.toString();
                frame.setLength(0);
            } else frame.append(line).append('\n');
        }
        throw new EOFException("SSE closed before expected event");
    }
}
