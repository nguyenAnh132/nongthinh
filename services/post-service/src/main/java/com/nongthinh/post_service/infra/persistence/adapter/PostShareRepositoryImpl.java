package com.nongthinh.post_service.infra.persistence.adapter;

import com.nongthinh.post_service.application.port.out.repository.PostShareRepository;
import com.nongthinh.post_service.domain.interaction.PostShare;
import com.nongthinh.post_service.infra.persistence.mapper.InteractionPersistenceMapper;
import com.nongthinh.post_service.infra.persistence.repository.JpaPostShareRepository;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class PostShareRepositoryImpl implements PostShareRepository {
    private final JpaPostShareRepository repository;
    private final InteractionPersistenceMapper mapper;

    public PostShareRepositoryImpl(JpaPostShareRepository repository, InteractionPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public PostShare save(PostShare value) {
        return mapper.toDomain(repository.save(mapper.toEntity(value)));
    }

    @Override
    public long countByPostId(UUID postId) {
        return repository.countByPostId(postId);
    }
}
