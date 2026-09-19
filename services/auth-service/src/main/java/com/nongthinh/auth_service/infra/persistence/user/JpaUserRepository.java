package com.nongthinh.auth_service.infra.persistence.user;

import java.util.Optional;
import java.util.UUID;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface JpaUserRepository extends JpaRepository<JpaUserEntity, UUID> {

    boolean existsByEmail(String email);

    Optional<JpaUserEntity> findByEmail(String email);

    Optional<JpaUserEntity> findByKeycloakId(UUID keycloakId);

    @Transactional
    @Modifying
    @Query(value = """
            insert into users (id, keycloak_id, email, created_at, updated_at)
            values (:id, :keycloakId, :email, :now, :now)
            on conflict do nothing
            """, nativeQuery = true)
    void insertIfAbsent(UUID id, UUID keycloakId, String email, Instant now);


}
