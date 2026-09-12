package com.nongthinh.post_service.infra.persistence.mapper;

import com.nongthinh.post_service.domain.post.PostTopic;
import com.nongthinh.post_service.domain.post.PostType;
import com.nongthinh.post_service.infra.persistence.entity.JpaPostTopicEntity;
import com.nongthinh.post_service.infra.persistence.entity.JpaPostTypeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface CatalogPersistenceMapper {
    default PostType toDomain(JpaPostTypeEntity entity) {
        return PostType.reconstruct(entity.getId(), entity.getCode(), entity.getName(),
                entity.getDescription(), entity.getDisplayOrder(), entity.isActive(),
                entity.getCreatedAt(), entity.getUpdatedAt());
    }

    JpaPostTypeEntity toEntity(PostType domain);

    default PostTopic toDomain(JpaPostTopicEntity entity) {
        return PostTopic.reconstruct(entity.getId(), entity.getName(), entity.getSlug(),
                entity.getDescription(), entity.getDisplayOrder(), entity.isActive(),
                entity.getCreatedAt(), entity.getUpdatedAt());
    }

    JpaPostTopicEntity toEntity(PostTopic domain);
}
