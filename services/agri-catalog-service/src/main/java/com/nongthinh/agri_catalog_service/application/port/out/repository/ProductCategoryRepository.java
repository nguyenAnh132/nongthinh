package com.nongthinh.agri_catalog_service.application.port.out.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.domain.category.ProductCategory;

public interface ProductCategoryRepository {

    boolean existsBySlug(String slug);

    Optional<ProductCategory> findById(UUID id);

    Optional<ProductCategory> findBySlug(String slug);

    List<ProductCategory> findAllOrderByDisplayOrder();

    List<ProductCategory> findAllActiveOrderByDisplayOrder();

    List<ProductCategory> findByParentIdOrderByDisplayOrder(UUID parentId);

    ProductCategory save(ProductCategory productCategory);
}
