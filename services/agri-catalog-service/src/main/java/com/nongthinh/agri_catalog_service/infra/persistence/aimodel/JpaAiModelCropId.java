package com.nongthinh.agri_catalog_service.infra.persistence.aimodel;

import java.io.Serializable;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class JpaAiModelCropId implements Serializable {

    @Column(name = "model_id", nullable = false)
    private UUID modelId;

    @Column(name = "crop_type_id", nullable = false)
    private UUID cropTypeId;
}
