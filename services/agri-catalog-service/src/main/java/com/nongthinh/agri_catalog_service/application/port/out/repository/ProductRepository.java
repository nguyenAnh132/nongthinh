package com.nongthinh.agri_catalog_service.application.port.out.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.domain.product.Product;

public interface ProductRepository {

    boolean existsByBrandIdAndSlug(UUID brandId, String slug);

    Optional<Product> findById(UUID id);

    Optional<Product> findByBrandIdAndSlug(UUID brandId, String slug);

    List<Product> findAll();

    List<Product> findAllByBrandId(UUID brandId);

    List<Product> findAllByCategoryId(UUID categoryId);

    List<Product> findAllPublishedOrderByPublishedAtDesc();

    Product save(Product product);
}
