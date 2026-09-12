package com.nongthinh.profile_service.infra.persistence.brandprofile;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaBrandProfileRepository extends JpaRepository<JpaBrandProfileEntity, UUID> {

    boolean existsByUserId(UUID userId);

    Optional<JpaBrandProfileEntity> findByUserId(UUID userId);

    List<JpaBrandProfileEntity> findAllByStatusInOrderByCreatedAtDesc(Collection<String> statuses);
}
