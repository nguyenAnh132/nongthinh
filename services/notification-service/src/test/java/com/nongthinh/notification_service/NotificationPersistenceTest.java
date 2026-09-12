package com.nongthinh.notification_service;

import static org.assertj.core.api.Assertions.*;
import com.nongthinh.notification_service.application.event.PostEngagementEvent;
import com.nongthinh.notification_service.application.port.in.notification.*;
import com.nongthinh.notification_service.application.port.out.repository.NotificationRepository;
import com.nongthinh.notification_service.common.exception.ErrorCode;
import com.nongthinh.notification_service.domain.exception.BusinessException;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;

@EnabledIfSystemProperty(named = "sse.local.integration", matches = "true")
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:postgresql://127.0.0.1:55433/sse_notification_test",
        "spring.datasource.username=postgres", "spring.datasource.password=unused",
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://127.0.0.1/unused",
        "spring.kafka.bootstrap-servers=127.0.0.1:1", "spring.kafka.listener.auto-startup=false",
        "in-app-notification.enabled=false", "post-realtime.enabled=false"
})
@Transactional
class NotificationPersistenceTest {
    private static final UUID ACTOR = UUID.fromString("90000000-0000-0000-0000-000000000001");
    private static final UUID RECIPIENT = UUID.fromString("90000000-0000-0000-0000-000000000002");
    private static final UUID POST = UUID.fromString("90000000-0000-0000-0000-000000000003");
    private static final Instant NOW = Instant.parse("2026-09-09T00:00:00Z");
    @Autowired HandlePostEngagementUseCase handle;
    @Autowired NotificationRepository repository;
    @Autowired MarkNotificationReadUseCase read;
    @Autowired MarkAllNotificationsReadUseCase readAll;
    @Autowired ListNotificationsUseCase list;
    @Autowired JdbcTemplate jdbc;

    @AfterEach
    void clearAuthentication() { SecurityContextHolder.clearContext(); }

    @Test
    void duplicateAndOutOfOrderReactionsCoalesceWithoutResettingReadState() {
        var first = event(1, RECIPIENT, "POST_REACTION");
        var second = event(2, RECIPIENT, "POST_REACTION");
        handle.execute(first);
        handle.execute(second);
        authenticate(RECIPIENT);
        var notification = list.execute(0, 20).items().getFirst();
        assertThat(repository.state(RECIPIENT).unreadCount()).isEqualTo(1);
        read.execute(notification.id());
        long revision = repository.state(RECIPIENT).version();
        handle.execute(first);
        handle.execute(second);
        handle.execute(event(1, RECIPIENT, "POST_REACTION"));
        assertThat(list.execute(0, 20).items()).hasSize(1);
        assertThat(repository.state(RECIPIENT).unreadCount()).isZero();
        assertThat(repository.state(RECIPIENT).version()).isEqualTo(revision);
    }

    @Test
    void skipsSelfNotificationsAndEditDeleteEvents() {
        handle.execute(event(1, ACTOR, "POST_REACTION"));
        handle.execute(event(2, RECIPIENT, null));
        handle.execute(event(3, RECIPIENT, null));
        assertThat(repository.list(ACTOR, 0, 20).items()).isEmpty();
        assertThat(repository.list(RECIPIENT, 0, 20).items()).isEmpty();
    }

    @Test
    void replyHasOneRecipientAndReadOperationsAreUserScoped() {
        handle.execute(event(1, RECIPIENT, "COMMENT_REPLY"));
        handle.execute(event(2, RECIPIENT, "POST_COMMENT"));
        var notification = repository.list(RECIPIENT, 0, 20).items().getFirst();
        authenticate(ACTOR);
        assertThat(list.execute(0, 20).items()).isEmpty();
        assertThatThrownBy(() -> read.execute(notification.id()))
                .isInstanceOfSatisfying(BusinessException.class, ex -> assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.NOTIFICATION_NOT_FOUND));
        assertThat(readAll.execute().unreadCount()).isZero();
        assertThat(repository.state(RECIPIENT).unreadCount()).isEqualTo(2);
        authenticate(RECIPIENT);
        assertThat(readAll.execute().unreadCount()).isZero();
        long revision = repository.state(RECIPIENT).version();
        assertThat(readAll.execute().version()).isEqualTo(revision);
    }

    @Test
    void validatesPagination() {
        authenticate(RECIPIENT);
        assertThatThrownBy(() -> list.execute(-1, 20)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> list.execute(0, 101)).isInstanceOf(BusinessException.class);
    }

    private PostEngagementEvent event(long version, UUID recipient, String type) {
        return new PostEngagementEvent(UUID.randomUUID(), "POST_REACTION".equals(type)
                ? "post.reaction.updated" : "post.comment.updated", 1, NOW.plusSeconds(version),
                POST, version, Map.of("postId", POST), ACTOR, recipient, type, true);
    }

    private void authenticate(UUID userId) {
        var jwt = Jwt.withTokenValue("test").header("alg", "none").claim("nongthinh_id", userId.toString())
                .issuedAt(NOW).expiresAt(NOW.plusSeconds(3600)).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, List.of()));
    }
}
