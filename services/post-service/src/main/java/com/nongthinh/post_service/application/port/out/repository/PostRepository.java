package com.nongthinh.post_service.application.port.out.repository;

import com.nongthinh.post_service.application.model.PostPage;
import com.nongthinh.post_service.domain.post.Post;
import com.nongthinh.post_service.domain.post.valueobject.PostStatus;
import com.nongthinh.post_service.domain.post.valueobject.PostId;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface PostRepository {
    Optional<Post> findById(PostId id);
    Optional<Post> findByIdForUpdate(PostId id);
    Optional<Post> findByIdIncludingDeleted(PostId id);
    Optional<Post> findByIdForUpdateIncludingDeleted(PostId id);
    PostPage findPublic(UUID postTypeId, UUID topicId, UUID cropTypeId, String keyword,
                        int page, int size);
    PostPage findByAuthor(UUID authorUserId, PostStatus status, int page, int size);
    PostPage findAllForAdmin(UUID authorUserId, PostStatus status, String keyword,
                             Instant from, Instant to, int page, int size);
    PostPage findBookmarkedByUser(UUID userId, int page, int size);
    long countAll();
    Map<PostStatus, Long> countByStatus();
    List<Instant> findCreatedAtBetween(Instant from, Instant to);
    boolean existsByPostTypeId(UUID postTypeId);
    boolean existsByTopicId(UUID topicId);
    Post save(Post post);
}
