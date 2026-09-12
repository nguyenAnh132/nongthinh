package com.nongthinh.agri_catalog_service.presentation.mapper;

import org.mapstruct.Mapper;
import com.nongthinh.agri_catalog_service.application.command.ProductDiseaseTreatmentCreationCommand;
import com.nongthinh.agri_catalog_service.application.command.ProductDiseaseTreatmentUpdateCommand;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.EffectivenessLevel;
import com.nongthinh.agri_catalog_service.presentation.dto.request.ProductDiseaseTreatmentCreationRequest;
import com.nongthinh.agri_catalog_service.presentation.dto.request.ProductDiseaseTreatmentUpdateRequest;

@Mapper(componentModel = "spring")
public interface ProductDiseaseTreatmentMapper {

    ProductDiseaseTreatmentCreationCommand toCreationCommand(
            ProductDiseaseTreatmentCreationRequest request
    );

    ProductDiseaseTreatmentUpdateCommand toUpdateCommand(
            ProductDiseaseTreatmentUpdateRequest request
    );

    default EffectivenessLevel toEffectivenessLevel(String value) {
        return value == null ? null : EffectivenessLevel.fromString(value);
    }
}
