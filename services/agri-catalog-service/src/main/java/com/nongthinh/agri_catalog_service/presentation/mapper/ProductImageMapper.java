package com.nongthinh.agri_catalog_service.presentation.mapper;

import org.mapstruct.Mapper;
import com.nongthinh.agri_catalog_service.application.command.ProductImageCreationCommand;
import com.nongthinh.agri_catalog_service.application.command.ProductImageUpdateCommand;
import com.nongthinh.agri_catalog_service.presentation.dto.request.ProductImageCreationRequest;
import com.nongthinh.agri_catalog_service.presentation.dto.request.ProductImageUpdateRequest;

@Mapper(componentModel = "spring")
public interface ProductImageMapper {

    ProductImageCreationCommand toProductImageCreationCommand(
            ProductImageCreationRequest request
    );

    ProductImageUpdateCommand toProductImageUpdateCommand(
            ProductImageUpdateRequest request
    );
}
