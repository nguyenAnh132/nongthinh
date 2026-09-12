package com.nongthinh.post_service.infra.persistence.repository;

import com.nongthinh.post_service.application.model.TrendingPostTopic;
import com.nongthinh.post_service.infra.persistence.entity.JpaPostTopicEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JpaPostTopicRepository extends JpaRepository<JpaPostTopicEntity, UUID> {
    Optional<JpaPostTopicEntity> findBySlug(String slug);
    List<JpaPostTopicEntity> findAllByOrderByDisplayOrderAscIdAsc();
    List<JpaPostTopicEntity> findAllByActiveTrueOrderByDisplayOrderAscIdAsc();

    @Query("""
            select new com.nongthinh.post_service.application.model.TrendingPostTopic(
                topic.id, topic.name, topic.slug, count(post.id)
            )
            from JpaPostTopicEntity topic, JpaPostEntity post
            where post.topicId = topic.id
              and topic.active = true
              and post.deletedAt is null
              and post.status = 'PUBLISHED'
              and post.visibility = 'PUBLIC'
            group by topic.id, topic.name, topic.slug, topic.displayOrder
            order by count(post.id) desc, topic.displayOrder asc, topic.id asc
            """)
    List<TrendingPostTopic> findTrending(Pageable pageable);
}
