package com.nongthinh.rice_disease_diagnosis_service.infra.persistence.diagnosishistory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaDiagnosisHistoryRepository extends JpaRepository<JpaDiagnosisHistoryEntity, UUID> {
    List<JpaDiagnosisHistoryEntity> findAllByFarmerUserIdOrderByCreatedAtDescIdDesc(UUID farmerUserId);
    Optional<JpaDiagnosisHistoryEntity> findByIdAndFarmerUserId(UUID id, UUID farmerUserId);
}
