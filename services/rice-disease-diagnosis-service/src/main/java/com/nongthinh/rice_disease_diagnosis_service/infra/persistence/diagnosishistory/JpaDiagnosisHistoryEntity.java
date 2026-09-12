package com.nongthinh.rice_disease_diagnosis_service.infra.persistence.diagnosishistory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "diagnosis_histories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JpaDiagnosisHistoryEntity {
    @Id
    private UUID id;
    @Column(name = "farmer_user_id", nullable = false)
    private UUID farmerUserId;
    @Column(name = "crop_type_id", nullable = false)
    private UUID cropTypeId;
    @Column(name = "model_id", nullable = false)
    private UUID modelId;
    @Column(name = "model_version_id", nullable = false)
    private UUID modelVersionId;
    @Column(name = "model_version", nullable = false, length = 128)
    private String modelVersion;
    @Column(name = "status", nullable = false, length = 20)
    private String status;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "result_snapshot", nullable = false, columnDefinition = "jsonb")
    private String resultSnapshot;
    @ElementCollection
    @CollectionTable(
            name = "diagnosis_history_files",
            joinColumns = @JoinColumn(name = "diagnosis_history_id"))
    @Column(name = "file_id", nullable = false)
    @Builder.Default
    private List<UUID> fileIds = new ArrayList<>();
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
