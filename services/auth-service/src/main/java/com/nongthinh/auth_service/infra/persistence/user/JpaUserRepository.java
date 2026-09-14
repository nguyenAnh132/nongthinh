package com.nongthinh.auth_service.infra.persistence.user;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaUserRepository extends JpaRepository<JpaUserEntity, UUID> {

    boolean existsByEmail(String email);

    Optional<JpaUserEntity> findByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from JpaUserEntity u where u.email = :email")
    Optional<JpaUserEntity> findByEmailForUpdate(@Param("email") String email);
}
