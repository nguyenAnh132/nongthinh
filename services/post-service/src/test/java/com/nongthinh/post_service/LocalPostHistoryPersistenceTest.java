package com.nongthinh.post_service;

import static org.assertj.core.api.Assertions.assertThat;

import com.nongthinh.post_service.application.model.PostHistoryFilter;
import com.nongthinh.post_service.application.port.out.repository.PostHistoryRepository;
import com.nongthinh.post_service.domain.history.PostHistory;
import com.nongthinh.post_service.domain.history.PostSnapshot;
import com.nongthinh.post_service.domain.history.valueobject.HistoryActorType;
import com.nongthinh.post_service.domain.history.valueobject.PostHistoryAction;
import com.nongthinh.post_service.domain.post.valueobject.PostStatus;
import com.nongthinh.post_service.domain.post.valueobject.PostVisibility;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@EnabledIfSystemProperty(named = "post.local.integration", matches = "true")
@SpringBootTest(properties = {
        "spring.profiles.active=dev",
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost/unused"
})
class LocalPostHistoryPersistenceTest {
    private static final UUID POST_ID = UUID.fromString("72cd8860-0ba9-435b-a247-aa86df2fa00e");
    private static final UUID HISTORY_ID = UUID.fromString("ef6590b4-1e5b-4493-a45a-75b70a047890");
    private static final UUID AUTHOR_ID = UUID.fromString("a4d8abde-8cd7-4ec5-87c8-d96074677c11");
    private static final UUID ACTOR_ID = UUID.fromString("aa44413e-0062-44df-8f9f-327646798d4e");
    private static final Instant NOW = Instant.parse("2026-08-26T00:00:00Z");

    @Autowired private JdbcTemplate jdbc;
    @Autowired private PostHistoryRepository histories;

    @Test
    @Transactional
    void persistsGetsAndFiltersImmutableHistorySnapshots() {
        UUID postTypeId = jdbc.queryForObject(
                "SELECT id FROM post_types WHERE code = 'QUESTION'",
                UUID.class
        );
        jdbc.update("""
                        INSERT INTO posts (
                            id, author_user_id, post_type_id, content, visibility, status,
                            published_at, created_at, updated_at
                        ) VALUES (?, ?, ?, ?, 'PUBLIC', 'PUBLISHED', ?, ?, ?)
                        """,
                POST_ID,
                AUTHOR_ID,
                postTypeId,
                "Current post content",
                Timestamp.from(NOW),
                Timestamp.from(NOW),
                Timestamp.from(NOW)
        );

        PostHistory history = histories.append(PostHistory.create(
                HISTORY_ID,
                POST_ID,
                AUTHOR_ID,
                ACTOR_ID,
                HistoryActorType.ADMIN,
                PostHistoryAction.HIDDEN,
                PostStatus.PUBLISHED,
                PostStatus.HIDDEN,
                PostVisibility.PUBLIC,
                PostVisibility.PUBLIC,
                "MISINFORMATION",
                "Confirmed inaccurate advice",
                null,
                new PostSnapshot(
                        "Current post content",
                        postTypeId,
                        null,
                        Set.of(),
                        "Đồng Tháp",
                        List.of(),
                        PostStatus.HIDDEN,
                        PostVisibility.PUBLIC
                ),
                NOW.plusSeconds(1)
        ));

        assertThat(histories.findById(HISTORY_ID))
                .get()
                .satisfies(persisted -> assertThat(persisted.getId()).isEqualTo(history.getId()));
        var page = histories.findAll(new PostHistoryFilter(
                POST_ID,
                AUTHOR_ID,
                ACTOR_ID,
                HistoryActorType.ADMIN,
                PostHistoryAction.HIDDEN
        ), 0, 20);

        assertThat(page.items()).singleElement().satisfies(persisted -> {
            assertThat(persisted.getId()).isEqualTo(HISTORY_ID);
            assertThat(persisted.getReasonCode()).isEqualTo("MISINFORMATION");
            assertThat(persisted.getSnapshot().content()).isEqualTo("Current post content");
            assertThat(persisted.getSnapshot().locationText()).isEqualTo("Đồng Tháp");
        });
        assertThat(page.totalElements()).isEqualTo(1);
    }
}
