package com.nongthinh.post_service.infra.persistence.adapter;

import com.nongthinh.post_service.application.model.PostHistoryFilter;
import com.nongthinh.post_service.application.model.PostHistoryPage;
import com.nongthinh.post_service.application.port.out.repository.PostHistoryRepository;
import com.nongthinh.post_service.domain.history.PostHistory;
import com.nongthinh.post_service.infra.persistence.entity.JpaPostHistoryEntity;
import com.nongthinh.post_service.infra.persistence.mapper.PostHistoryPersistenceMapper;
import com.nongthinh.post_service.infra.persistence.repository.JpaPostHistoryRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
public class PostHistoryRepositoryImpl implements PostHistoryRepository {
    private final JpaPostHistoryRepository repository;
    private final PostHistoryPersistenceMapper mapper;

    public PostHistoryRepositoryImpl(JpaPostHistoryRepository repository,
                                     PostHistoryPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public PostHistory append(PostHistory history) {
        return mapper.toDomain(repository.save(mapper.toEntity(history)));
    }

    @Override
    public Optional<PostHistory> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public PostHistoryPage findAll(PostHistoryFilter filter, int page, int size) {
        Specification<JpaPostHistoryEntity> specification =
                (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        if (filter.postId() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("postId"), filter.postId()));
        }
        if (filter.postAuthorUserId() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("postAuthorUserId"), filter.postAuthorUserId()));
        }
        if (filter.actorUserId() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("actorUserId"), filter.actorUserId()));
        }
        if (filter.actorType() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("actorType"), filter.actorType().name()));
        }
        if (filter.action() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("action"), filter.action().name()));
        }

        PageRequest pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))
        );
        Page<JpaPostHistoryEntity> result = repository.findAll(specification, pageable);
        return new PostHistoryPage(
                result.getContent().stream().map(mapper::toDomain).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.hasNext()
        );
    }
}
