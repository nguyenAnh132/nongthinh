package com.nongthinh.post_service.application.port.out.repository;

import com.nongthinh.post_service.domain.post.PostTopic;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostTopicRepository {
    Optional<PostTopic> findById(UUID id);
    Optional<PostTopic> findBySlug(String slug);
    List<PostTopic> findAll();
    List<PostTopic> findAllActive();
    PostTopic save(PostTopic postTopic);
    void deleteById(UUID id);
}
