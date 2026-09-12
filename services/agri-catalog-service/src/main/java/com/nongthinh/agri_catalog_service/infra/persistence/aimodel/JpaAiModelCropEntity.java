package com.nongthinh.agri_catalog_service.infra.persistence.aimodel;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ai_model_crops")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class JpaAiModelCropEntity {

    @EmbeddedId
    private JpaAiModelCropId id;
}
