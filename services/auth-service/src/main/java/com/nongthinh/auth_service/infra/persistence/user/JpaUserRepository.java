package com.nongthinh.auth_service.infra.persistence.user;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaUserRepository extends JpaRepository<JpaUserEntity, UUID> {

    boolean existsByEmail(String email);

    Optional<JpaUserEntity> findByEmail(String email);
}
