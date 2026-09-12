package com.nongthinh.post_service.infra.persistence.repository;

import com.nongthinh.post_service.infra.persistence.entity.JpaPostTypeEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPostTypeRepository extends JpaRepository<JpaPostTypeEntity, UUID> {
    Optional<JpaPostTypeEntity> findByCode(String code);
    List<JpaPostTypeEntity> findAllByOrderByDisplayOrderAscIdAsc();
    List<JpaPostTypeEntity> findAllByActiveTrueOrderByDisplayOrderAscIdAsc();
}
