package com.nongthinh.auth_service.infra.persistence.otp;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaOtpRepository extends JpaRepository<JpaOtpEntity, UUID> {
    Optional<JpaOtpEntity> findByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from JpaOtpEntity o where o.email = :email")
    Optional<JpaOtpEntity> findByEmailForUpdate(@Param("email") String email);
}
