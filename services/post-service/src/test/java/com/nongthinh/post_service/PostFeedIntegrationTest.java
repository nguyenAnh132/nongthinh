package com.nongthinh.post_service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.nongthinh.post_service.application.command.GetPostFeedCommand;
import com.nongthinh.post_service.application.port.in.post.GetPostFeedUseCase;
import com.nongthinh.post_service.application.port.out.ClockProvider;
import com.nongthinh.post_service.application.port.out.CurrentUserProvider;
import com.nongthinh.post_service.application.port.out.FollowingQuery;
import com.nongthinh.post_service.application.view.PostView;
import com.nongthinh.post_service.common.exception.ErrorCode;
import com.nongthinh.post_service.domain.exception.BusinessException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:post_feed_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop", "spring.flyway.enabled=false",
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://127.0.0.1/unused",
        "post-outbox.enabled=false"
})
@ActiveProfiles("test")
@Transactional
class PostFeedIntegrationTest {
    private static final Instant NOW = Instant.parse("2026-09-16T00:00:00.123456Z");
    private static final UUID VIEWER = new UUID(1, 1);
    private static final UUID AUTHOR = new UUID(1, 2);
    @Autowired GetPostFeedUseCase feed;
    @Autowired JdbcTemplate jdbc;
    @MockitoBean ClockProvider clock;
    @MockitoBean CurrentUserProvider currentUser;
    @MockitoBean FollowingQuery following;

    @BeforeEach
    void setup() {
        when(clock.now()).thenReturn(NOW);
    }

    @Test
    void pagesTenPostsWithoutDuplicatesAcrossTiesInsertionsAndDeletionUntilRefresh() {
        for (int i = 1; i <= 25; i++) insert(i, AUTHOR, "PUBLIC", "PUBLISHED", NOW.minusSeconds(1));
        var first = feed.execute(command(null, false, null));
        assertThat(first.items()).hasSize(10);
        assertThat(first.items().getFirst().id()).isEqualTo(new UUID(0, 25));
        assertThat(first.hasNext()).isTrue();

        when(clock.now()).thenReturn(NOW.plusSeconds(10));
        insert(100, AUTHOR, "PUBLIC", "PUBLISHED", NOW.plusSeconds(1));
        jdbc.update("delete from posts where id = ?", new UUID(0, 14));
        // The anchor can itself disappear; pagination never needs to look it up.
        jdbc.update("delete from posts where id = ?", first.items().getLast().id());
        var second = feed.execute(command(first.nextCursor(), false, null));
        var retry = feed.execute(command(first.nextCursor(), false, null));
        assertThat(retry).isEqualTo(second);
        assertThat(second.items()).hasSize(10);
        var last = feed.execute(command(second.nextCursor(), false, null));
        assertThat(last.items()).hasSize(4);
        assertThat(last.hasNext()).isFalse();
        assertThat(last.nextCursor()).isNull();
        List<UUID> received = new ArrayList<>();
        for (var page : List.of(first, second, last)) received.addAll(page.items().stream().map(PostView::id).toList());
        assertThat(received).hasSize(24).doesNotHaveDuplicates().doesNotContain(new UUID(0, 100), new UUID(0, 14));
        assertThat(feed.execute(command(null, false, null)).items().getFirst().id()).isEqualTo(new UUID(0, 100));
    }

    @Test
    void usesPublicationTimeAndExcludesPrivateHiddenDraftAndDeletedPosts() {
        insert(1, AUTHOR, "PUBLIC", "PUBLISHED", NOW.minusSeconds(5));
        insert(2, AUTHOR, "PRIVATE", "PUBLISHED", NOW.minusSeconds(4));
        insert(3, AUTHOR, "PUBLIC", "HIDDEN", NOW.minusSeconds(3));
        insert(4, AUTHOR, "PUBLIC", "DRAFT", null);
        insert(5, AUTHOR, "PUBLIC", "PUBLISHED", NOW.minusSeconds(2));
        jdbc.update("update posts set deleted_at = ? where id = ?", Timestamp.from(NOW), new UUID(0, 5));
        insert(6, AUTHOR, "PUBLIC", "PUBLISHED", NOW.minusSeconds(1));
        assertThat(feed.execute(command(null, false, null)).items()).extracting(PostView::id)
                .containsExactly(new UUID(0, 6), new UUID(0, 1));
    }

    @Test
    void appliesFollowingFilterAndEmptyFollowingNeverFallsBackToPublicFeed() {
        when(currentUser.getCurrentUserId()).thenReturn(VIEWER);
        when(following.findFollowingUserIds(VIEWER)).thenReturn(Set.of(AUTHOR));
        insert(1, AUTHOR, "PUBLIC", "PUBLISHED", NOW.minusSeconds(1));
        insert(2, VIEWER, "PUBLIC", "PUBLISHED", NOW.minusSeconds(1));
        assertThat(feed.execute(command(null, true, null)).items()).extracting(PostView::id)
                .containsExactly(new UUID(0, 1));
        when(following.findFollowingUserIds(VIEWER)).thenReturn(Set.of());
        assertThat(feed.execute(command(null, true, null)).items()).isEmpty();
    }

    @Test
    void rejectsMalformedAndMismatchedCursorsAndAnonymousFollowing() {
        assertThatThrownBy(() -> feed.execute(command("invalid", false, null)))
                .isInstanceOfSatisfying(BusinessException.class,
                        error -> assertThat(error.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST_PARAMETER));
        for (int i = 1; i <= 11; i++) insert(i, AUTHOR, "PUBLIC", "PUBLISHED", NOW.minusSeconds(1));
        var first = feed.execute(command(null, false, null));
        assertThatThrownBy(() -> feed.execute(command(first.nextCursor(), false, AUTHOR)))
                .isInstanceOf(BusinessException.class);
        when(currentUser.getCurrentUserId()).thenThrow(new BusinessException(ErrorCode.UNAUTHENTICATED));
        assertThatThrownBy(() -> feed.execute(command(null, true, null)))
                .isInstanceOfSatisfying(BusinessException.class,
                        error -> assertThat(error.getErrorCode()).isEqualTo(ErrorCode.UNAUTHENTICATED));
    }

    @Test
    void authorFilterAndExactlyTenPostsHaveNoNextPage() {
        for (int i = 1; i <= 10; i++) insert(i, AUTHOR, "PUBLIC", "PUBLISHED", NOW.minusSeconds(1));
        insert(20, VIEWER, "PUBLIC", "PUBLISHED", NOW.minusSeconds(1));
        var page = feed.execute(command(null, false, AUTHOR));
        assertThat(page.items()).hasSize(10);
        assertThat(page.hasNext()).isFalse();
        assertThat(page.nextCursor()).isNull();
    }

    private GetPostFeedCommand command(String cursor, boolean followingOnly, UUID author) {
        return new GetPostFeedCommand(cursor, null, null, null, author, null, followingOnly);
    }

    private void insert(long id, UUID author, String visibility, String status, Instant publishedAt) {
        jdbc.update("""
                insert into posts(id, author_user_id, content, visibility, status, published_at, created_at, updated_at,
                                  reaction_version, comment_version)
                values (?, ?, 'Feed test', ?, ?, ?, ?, ?, 0, 0)
                """, new UUID(0, id), author, visibility, status,
                publishedAt == null ? null : Timestamp.from(publishedAt), Timestamp.from(NOW.minusSeconds(100)),
                Timestamp.from(NOW.plusSeconds(10)));
    }
}
