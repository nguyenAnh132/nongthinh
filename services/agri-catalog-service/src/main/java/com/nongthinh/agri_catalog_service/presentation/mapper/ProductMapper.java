package com.nongthinh.agri_catalog_service.presentation.mapper;

import org.mapstruct.Mapper;
import com.nongthinh.agri_catalog_service.application.command.ProductCreationCommand;
import com.nongthinh.agri_catalog_service.application.command.ProductUpdateCommand;
import com.nongthinh.agri_catalog_service.presentation.dto.request.ProductCreationRequest;
import com.nongthinh.agri_catalog_service.presentation.dto.request.ProductUpdateRequest;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductCreationCommand toProductCreationCommand(ProductCreationRequest request);

    ProductUpdateCommand toProductUpdateCommand(ProductUpdateRequest request);
}
