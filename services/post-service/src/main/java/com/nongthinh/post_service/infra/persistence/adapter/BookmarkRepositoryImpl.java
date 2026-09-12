package com.nongthinh.post_service.infra.persistence.adapter;

import com.nongthinh.post_service.application.port.out.repository.BookmarkRepository;
import com.nongthinh.post_service.domain.interaction.Bookmark;
import com.nongthinh.post_service.infra.persistence.entity.JpaPostBookmarkId;
import com.nongthinh.post_service.infra.persistence.repository.JpaPostBookmarkRepository;
import java.sql.Timestamp;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BookmarkRepositoryImpl implements BookmarkRepository {
    private final JpaPostBookmarkRepository repository;
    private final JdbcTemplate jdbc;

    public BookmarkRepositoryImpl(JpaPostBookmarkRepository repository, JdbcTemplate jdbc) {
        this.repository = repository;
        this.jdbc = jdbc;
    }

    @Override
    public boolean exists(UUID postId, UUID userId) {
        return repository.existsById(new JpaPostBookmarkId(postId, userId));
    }

    @Override
    public Bookmark save(Bookmark value) {
        return jdbc.queryForObject("""
                        INSERT INTO post_bookmarks (post_id, user_id, created_at)
                        VALUES (?, ?, ?)
                        ON CONFLICT (post_id, user_id) DO UPDATE
                        SET created_at = post_bookmarks.created_at
                        RETURNING post_id, user_id, created_at
                        """,
                (resultSet, rowNumber) -> new Bookmark(
                        resultSet.getObject("post_id", UUID.class),
                        resultSet.getObject("user_id", UUID.class),
                        resultSet.getTimestamp("created_at").toInstant()
                ),
                value.postId(), value.userId(), Timestamp.from(value.createdAt()));
    }

    @Override
    public void delete(UUID postId, UUID userId) {
        repository.deleteById(new JpaPostBookmarkId(postId, userId));
    }
}
