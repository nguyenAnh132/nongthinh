package com.nongthinh.rice_disease_diagnosis_service.infra.persistence.diagnosishistory;

import java.util.UUID;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "farmer_diagnosis_history_locks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JpaFarmerDiagnosisHistoryLockEntity {
    @Id
    private UUID farmerUserId;
}
