package com.nongthinh.rice_disease_diagnosis_service.infra.persistence.diagnosishistory;

import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaFarmerDiagnosisHistoryLockRepository
        extends JpaRepository<JpaFarmerDiagnosisHistoryLockEntity, UUID> {

    @Modifying
    @Query(value = "INSERT INTO farmer_diagnosis_history_locks (farmer_user_id) VALUES (:farmerUserId) ON CONFLICT DO NOTHING", nativeQuery = true)
    void insertIfAbsent(@Param("farmerUserId") UUID farmerUserId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<JpaFarmerDiagnosisHistoryLockEntity> findByFarmerUserId(UUID farmerUserId);
}
