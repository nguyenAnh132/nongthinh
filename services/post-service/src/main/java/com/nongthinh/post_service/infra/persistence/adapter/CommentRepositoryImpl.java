package com.nongthinh.post_service.infra.persistence.adapter;

import com.nongthinh.post_service.application.model.CommentPage;
import com.nongthinh.post_service.application.model.CommentThread;
import com.nongthinh.post_service.application.port.out.repository.CommentRepository;
import com.nongthinh.post_service.domain.comment.Comment;
import com.nongthinh.post_service.infra.persistence.entity.JpaPostCommentEntity;
import com.nongthinh.post_service.infra.persistence.mapper.InteractionPersistenceMapper;
import com.nongthinh.post_service.infra.persistence.repository.JpaPostCommentRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class CommentRepositoryImpl implements CommentRepository {
    private final JpaPostCommentRepository repository;
    private final InteractionPersistenceMapper mapper;

    public CommentRepositoryImpl(JpaPostCommentRepository repository, InteractionPersistenceMapper mapper) {
        this.repository = repository; this.mapper = mapper;
    }
    public Optional<Comment> findByIdAndPostId(UUID id, UUID postId) {
        return repository.findByIdAndPostIdAndDeletedAtIsNull(id, postId)
                .map(mapper::toDomain);
    }

    @Transactional
    public Optional<Comment> findByIdAndPostIdForUpdate(UUID id, UUID postId) {
        return repository.findByIdAndPostIdForUpdate(id, postId).map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    public CommentPage findPublishedThreads(UUID postId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));
        Page<JpaPostCommentEntity> roots = repository.findPublishedRoots(postId, pageable);
        List<UUID> rootIds = roots.getContent().stream()
                .map(JpaPostCommentEntity::getId)
                .toList();
        Map<UUID, List<Comment>> repliesByParent = rootIds.isEmpty()
                ? Map.of()
                : repository.findPublishedReplies(postId, rootIds).stream()
                        .map(mapper::toDomain)
                        .collect(Collectors.groupingBy(Comment::getParentCommentId));
        List<CommentThread> threads = roots.getContent().stream()
                .map(mapper::toDomain)
                .map(root -> new CommentThread(
                        root, repliesByParent.getOrDefault(root.getId(), List.of())))
                .toList();
        return new CommentPage(threads, roots.getNumber(), roots.getSize(),
                roots.getTotalElements(), roots.getTotalPages(), roots.hasNext());
    }

    public Comment save(Comment value) { return mapper.toDomain(repository.save(mapper.toEntity(value))); }
}
