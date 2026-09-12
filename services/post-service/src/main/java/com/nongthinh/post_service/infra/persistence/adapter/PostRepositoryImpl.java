package com.nongthinh.post_service.infra.persistence.adapter;

import com.nongthinh.post_service.application.model.PostPage;
import com.nongthinh.post_service.application.port.out.repository.PostRepository;
import com.nongthinh.post_service.configuration.PostLimitsProperties;
import com.nongthinh.post_service.domain.post.Post;
import com.nongthinh.post_service.domain.post.valueobject.PostId;
import com.nongthinh.post_service.domain.post.valueobject.PostStatus;
import com.nongthinh.post_service.infra.persistence.entity.JpaPostCropTypeEntity;
import com.nongthinh.post_service.infra.persistence.entity.JpaPostEntity;
import com.nongthinh.post_service.infra.persistence.entity.JpaPostMediaEntity;
import com.nongthinh.post_service.infra.persistence.mapper.PostPersistenceMapper;
import com.nongthinh.post_service.infra.persistence.repository.JpaPostCropTypeRepository;
import com.nongthinh.post_service.infra.persistence.repository.JpaPostMediaRepository;
import com.nongthinh.post_service.infra.persistence.repository.JpaPostRepository;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class PostRepositoryImpl implements PostRepository {
    private final JpaPostRepository posts;
    private final JpaPostMediaRepository media;
    private final JpaPostCropTypeRepository cropTypes;
    private final PostPersistenceMapper mapper;
    private final PostLimitsProperties limits;

    public PostRepositoryImpl(JpaPostRepository posts, JpaPostMediaRepository media,
                              JpaPostCropTypeRepository cropTypes, PostPersistenceMapper mapper,
                              PostLimitsProperties limits) {
        this.posts = posts;
        this.media = media;
        this.cropTypes = cropTypes;
        this.mapper = mapper;
        this.limits = limits;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Post> findById(PostId id) {
        return posts.findByIdAndDeletedAtIsNull(id.value()).map(entity -> mapper.toDomain(
                entity,
                media.findAllByPostIdOrderByDisplayOrderAsc(entity.getId()),
                cropTypes.findAllByIdPostId(entity.getId()),
                limits
        ));
    }

    @Override
    @Transactional
    public Optional<Post> findByIdForUpdate(PostId id) {
        return posts.findByIdForUpdate(id.value()).map(entity -> mapper.toDomain(
                entity,
                media.findAllByPostIdOrderByDisplayOrderAsc(entity.getId()),
                cropTypes.findAllByIdPostId(entity.getId()),
                limits
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Post> findByIdIncludingDeleted(PostId id) {
        return posts.findById(id.value()).map(entity -> mapper.toDomain(
                entity,
                media.findAllByPostIdOrderByDisplayOrderAsc(entity.getId()),
                cropTypes.findAllByIdPostId(entity.getId()),
                limits
        ));
    }

    @Override
    @Transactional
    public Optional<Post> findByIdForUpdateIncludingDeleted(PostId id) {
        return posts.findByIdForUpdateIncludingDeleted(id.value()).map(entity -> mapper.toDomain(
                entity,
                media.findAllByPostIdOrderByDisplayOrderAsc(entity.getId()),
                cropTypes.findAllByIdPostId(entity.getId()),
                limits
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public PostPage findPublic(UUID postTypeId, UUID topicId, UUID cropTypeId, String keyword,
                               int page, int size) {
        PageRequest pageable = PageRequest.of(page, size,
                Sort.by(Sort.Order.desc("publishedAt"), Sort.Order.desc("id")));
        Page<JpaPostEntity> result = keyword == null
                ? posts.findPublic(postTypeId, topicId, cropTypeId, pageable)
                : posts.findPublicByKeyword(
                        postTypeId, topicId, cropTypeId, keyword, pageable);
        return toPage(result);
    }

    @Override
    @Transactional(readOnly = true)
    public PostPage findByAuthor(UUID authorUserId, PostStatus status, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));
        return toPage(posts.findByAuthor(
                authorUserId, status == null ? null : status.name(), pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public PostPage findAllForAdmin(UUID authorUserId, PostStatus status, String keyword,
                                    Instant from, Instant to, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));
        Specification<JpaPostEntity> specification = (root, query, builder) ->
                builder.isNull(root.get("deletedAt"));
        if (authorUserId != null) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("authorUserId"), authorUserId));
        }
        if (status != null) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("status"), status.name()));
        }
        if (keyword != null) {
            String pattern = "%" + escapeLike(keyword.toLowerCase(Locale.ROOT)) + "%";
            specification = specification.and((root, query, builder) ->
                    builder.like(builder.lower(root.get("content")), pattern, '\\'));
        }
        if (from != null) {
            specification = specification.and((root, query, builder) ->
                    builder.greaterThanOrEqualTo(root.get("createdAt"), from));
        }
        if (to != null) {
            specification = specification.and((root, query, builder) ->
                    builder.lessThan(root.get("createdAt"), to));
        }
        Page<JpaPostEntity> result = posts.findAll(specification, pageable);
        return toPage(result);
    }

    @Override
    @Transactional(readOnly = true)
    public PostPage findBookmarkedByUser(UUID userId, int page, int size) {
        return toPage(posts.findBookmarkedByUser(userId, PageRequest.of(page, size)));
    }

    @Override
    @Transactional(readOnly = true)
    public long countAll() {
        return posts.countByDeletedAtIsNull();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<PostStatus, Long> countByStatus() {
        Map<PostStatus, Long> result = new EnumMap<>(PostStatus.class);
        for (PostStatus status : PostStatus.values()) {
            result.put(status, 0L);
        }
        for (Object[] row : posts.countActivePostsGroupedByStatus()) {
            result.put(PostStatus.valueOf((String) row[0]), (Long) row[1]);
        }
        return Map.copyOf(result);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Instant> findCreatedAtBetween(Instant from, Instant to) {
        return List.copyOf(posts.findCreatedAtBetween(from, to));
    }

    @Override
    public boolean existsByPostTypeId(java.util.UUID postTypeId) {
        return posts.existsByPostTypeId(postTypeId);
    }

    @Override
    public boolean existsByTopicId(java.util.UUID topicId) {
        return posts.existsByTopicId(topicId);
    }

    @Override
    @Transactional
    public Post save(Post post) {
        posts.saveAndFlush(mapper.toEntity(post));
        media.deleteAllByPostId(post.getId().value());
        media.flush();
        media.saveAll(mapper.toMediaEntities(post));
        synchronizeCropTypes(post);
        return post;
    }

    private void synchronizeCropTypes(Post post) {
        UUID postId = post.getId().value();
        List<JpaPostCropTypeEntity> existing = cropTypes.findAllByIdPostId(postId);
        Set<UUID> requestedIds = post.getCropTypeIds().stream()
                .map(item -> item.value())
                .collect(Collectors.toSet());

        List<JpaPostCropTypeEntity> removed = existing.stream()
                .filter(item -> !requestedIds.contains(item.getId().getCropTypeId()))
                .toList();
        if (!removed.isEmpty()) {
            cropTypes.deleteAllInBatch(removed);
        }

        Set<UUID> existingIds = existing.stream()
                .map(item -> item.getId().getCropTypeId())
                .collect(Collectors.toSet());
        List<JpaPostCropTypeEntity> added = post.getCropTypeIds().stream()
                .filter(item -> !existingIds.contains(item.value()))
                .map(item -> mapper.toCropTypeEntity(item, postId, post.getUpdatedAt()))
                .toList();
        if (!added.isEmpty()) {
            cropTypes.saveAll(added);
        }
    }

    private PostPage toPage(Page<JpaPostEntity> result) {
        List<JpaPostEntity> entities = result.getContent();
        List<UUID> postIds = entities.stream().map(JpaPostEntity::getId).toList();
        if (postIds.isEmpty()) {
            return new PostPage(List.of(), result.getNumber(), result.getSize(),
                    result.getTotalElements(), result.getTotalPages(), result.hasNext());
        }

        Map<UUID, List<JpaPostMediaEntity>> mediaByPost = media
                .findAllByPostIdInOrderByPostIdAscDisplayOrderAsc(postIds)
                .stream()
                .collect(Collectors.groupingBy(JpaPostMediaEntity::getPostId));
        Map<UUID, List<JpaPostCropTypeEntity>> cropTypesByPost = cropTypes
                .findAllByIdPostIdIn(postIds)
                .stream()
                .collect(Collectors.groupingBy(item -> item.getId().getPostId()));

        List<Post> domainPosts = entities.stream()
                .map(entity -> mapper.toDomain(
                        entity,
                        mediaByPost.getOrDefault(entity.getId(), List.of()),
                        cropTypesByPost.getOrDefault(entity.getId(), List.of()),
                        limits
                ))
                .toList();
        return new PostPage(domainPosts, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages(), result.hasNext());
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
