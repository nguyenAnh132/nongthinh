package com.nongthinh.agri_catalog_service.application.port.out.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.domain.product.ProductImage;

public interface ProductImageRepository {

    Optional<ProductImage> findById(UUID id);

    Optional<ProductImage> findByFileId(UUID fileId);

    Optional<ProductImage> findPrimaryByProductId(UUID productId);

    List<ProductImage> findAllByProductIdOrderByDisplayOrder(UUID productId);

    ProductImage save(ProductImage productImage);

    ProductImage saveAndFlush(ProductImage productImage);
}
