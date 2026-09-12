package com.nongthinh.agri_catalog_service.presentation.mapper;

import org.mapstruct.Mapper;
import com.nongthinh.agri_catalog_service.application.command.ProductCategoryCreationCommand;
import com.nongthinh.agri_catalog_service.application.command.ProductCategoryUpdateCommand;
import com.nongthinh.agri_catalog_service.presentation.dto.request.ProductCategoryCreationRequest;
import com.nongthinh.agri_catalog_service.presentation.dto.request.ProductCategoryUpdateRequest;

@Mapper(componentModel = "spring")
public interface ProductCategoryMapper {

    ProductCategoryCreationCommand toProductCategoryCreationCommand(
            ProductCategoryCreationRequest request
    );

    ProductCategoryUpdateCommand toProductCategoryUpdateCommand(
            ProductCategoryUpdateRequest request
    );
}
