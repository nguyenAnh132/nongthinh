package com.nongthinh.post_service.infra.persistence.adapter;

import com.nongthinh.post_service.application.port.out.repository.PostTopicRepository;
import com.nongthinh.post_service.common.exception.ErrorCode;
import com.nongthinh.post_service.domain.exception.BusinessException;
import com.nongthinh.post_service.domain.post.PostTopic;
import com.nongthinh.post_service.infra.persistence.mapper.CatalogPersistenceMapper;
import com.nongthinh.post_service.infra.persistence.repository.JpaPostTopicRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.dao.DataIntegrityViolationException;

@Repository
public class PostTopicRepositoryImpl implements PostTopicRepository {
    private final JpaPostTopicRepository repository;
    private final CatalogPersistenceMapper mapper;

    public PostTopicRepositoryImpl(JpaPostTopicRepository repository, CatalogPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public Optional<PostTopic> findById(UUID id) { return repository.findById(id).map(mapper::toDomain); }
    public Optional<PostTopic> findBySlug(String slug) { return repository.findBySlug(slug).map(mapper::toDomain); }
    public List<PostTopic> findAll() { return repository.findAllByOrderByDisplayOrderAscIdAsc().stream().map(mapper::toDomain).toList(); }
    public List<PostTopic> findAllActive() { return repository.findAllByActiveTrueOrderByDisplayOrderAscIdAsc().stream().map(mapper::toDomain).toList(); }
    public PostTopic save(PostTopic value) {
        try {
            return mapper.toDomain(repository.saveAndFlush(mapper.toEntity(value)));
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(ErrorCode.POST_TOPIC_SLUG_ALREADY_EXISTS);
        }
    }
    public void deleteById(UUID id) {
        repository.deleteById(id);
        repository.flush();
    }
}
