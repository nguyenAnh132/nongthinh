package com.nongthinh.post_service;

import static org.assertj.core.api.Assertions.assertThat;

import com.nongthinh.post_service.application.port.out.repository.BookmarkRepository;
import com.nongthinh.post_service.application.port.out.repository.PostRepository;
import com.nongthinh.post_service.application.port.out.repository.PostShareRepository;
import com.nongthinh.post_service.domain.interaction.Bookmark;
import com.nongthinh.post_service.domain.interaction.PostShare;
import java.sql.Timestamp;
import java.time.Instant;
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
class LocalPostInteractionPersistenceTest {
    private static final UUID POST_ID = UUID.fromString("6fa8b3ec-e787-407f-a7f4-6d28485f0df1");
    private static final UUID AUTHOR_ID = UUID.fromString("b9185c03-a4ca-44c0-a46c-2ce09ef99b24");
    private static final UUID USER_ID = UUID.fromString("2a16f7bf-cfa3-4777-92bf-d0f2243d0baf");
    private static final UUID FIRST_SHARE_ID = UUID.fromString("ba02d1ec-086b-4a99-99c2-cd59ac1a546f");
    private static final UUID SECOND_SHARE_ID = UUID.fromString("6616d11b-5fc7-4973-b427-c767cd5a8184");
    private static final Instant NOW = Instant.parse("2026-08-25T00:00:00Z");

    @Autowired private JdbcTemplate jdbc;
    @Autowired private BookmarkRepository bookmarks;
    @Autowired private PostShareRepository shares;
    @Autowired private PostRepository posts;

    @Test
    @Transactional
    void persistsBookmarksIdempotentlyAndCountsShares() {
        UUID postTypeId = jdbc.queryForObject(
                "SELECT id FROM post_types WHERE code = 'QUESTION'", UUID.class);
        jdbc.update("""
                        INSERT INTO posts (
                            id, author_user_id, post_type_id, content, visibility, status,
                            published_at, created_at, updated_at
                        ) VALUES (?, ?, ?, ?, 'PUBLIC', 'PUBLISHED', ?, ?, ?)
                        """,
                POST_ID, AUTHOR_ID, postTypeId, "Bookmarked post",
                Timestamp.from(NOW), Timestamp.from(NOW), Timestamp.from(NOW));

        Bookmark first = bookmarks.save(new Bookmark(POST_ID, USER_ID, NOW.plusSeconds(1)));
        Bookmark repeated = bookmarks.save(new Bookmark(POST_ID, USER_ID, NOW.plusSeconds(2)));

        assertThat(repeated).isEqualTo(first);
        assertThat(posts.findBookmarkedByUser(USER_ID, 0, 20).items())
                .singleElement()
                .satisfies(post -> assertThat(post.getId().value()).isEqualTo(POST_ID));

        shares.save(new PostShare(FIRST_SHARE_ID, POST_ID, USER_ID, NOW.plusSeconds(3)));
        shares.save(new PostShare(SECOND_SHARE_ID, POST_ID, USER_ID, NOW.plusSeconds(4)));

        assertThat(shares.countByPostId(POST_ID)).isEqualTo(2);
    }
}
