package com.nongthinh.post_service.application.port.out.repository;

import com.nongthinh.post_service.domain.post.PostType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostTypeRepository {
    Optional<PostType> findById(UUID id);
    Optional<PostType> findByCode(String code);
    List<PostType> findAll();
    List<PostType> findAllActive();
    PostType save(PostType postType);
    void deleteById(UUID id);
}
