package com.nongthinh.post_service.infra.persistence.adapter;

import com.nongthinh.post_service.application.port.out.repository.PostTypeRepository;
import com.nongthinh.post_service.common.exception.ErrorCode;
import com.nongthinh.post_service.domain.exception.BusinessException;
import com.nongthinh.post_service.domain.post.PostType;
import com.nongthinh.post_service.infra.persistence.mapper.CatalogPersistenceMapper;
import com.nongthinh.post_service.infra.persistence.repository.JpaPostTypeRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.dao.DataIntegrityViolationException;

@Repository
public class PostTypeRepositoryImpl implements PostTypeRepository {
    private final JpaPostTypeRepository repository;
    private final CatalogPersistenceMapper mapper;

    public PostTypeRepositoryImpl(JpaPostTypeRepository repository, CatalogPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public Optional<PostType> findById(UUID id) { return repository.findById(id).map(mapper::toDomain); }
    public Optional<PostType> findByCode(String code) { return repository.findByCode(code).map(mapper::toDomain); }
    public List<PostType> findAll() { return repository.findAllByOrderByDisplayOrderAscIdAsc().stream().map(mapper::toDomain).toList(); }
    public List<PostType> findAllActive() { return repository.findAllByActiveTrueOrderByDisplayOrderAscIdAsc().stream().map(mapper::toDomain).toList(); }
    public PostType save(PostType value) {
        try {
            return mapper.toDomain(repository.saveAndFlush(mapper.toEntity(value)));
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(ErrorCode.POST_TYPE_CODE_ALREADY_EXISTS);
        }
    }
    public void deleteById(UUID id) {
        try {
            repository.deleteById(id);
            repository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(ErrorCode.POST_TYPE_IN_USE);
        }
    }
}
