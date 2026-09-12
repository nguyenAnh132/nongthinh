package com.nongthinh.agri_catalog_service.infra.persistence.aimodelversion;

import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ai_model_version_classes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JpaAiModelVersionClassEntity {

    @Id
    private UUID id;

    @Column(name = "model_version_id", nullable = false)
    private UUID modelVersionId;

    @Column(name = "class_index", nullable = false)
    private int classIndex;

    @Column(name = "class_code", nullable = false, length = 100)
    private String classCode;

    @Column(name = "display_name", nullable = false, length = 255)
    private String displayName;

    @Column(name = "class_kind", nullable = false, length = 30)
    private String classKind;
}
