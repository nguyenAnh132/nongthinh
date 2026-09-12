package com.nongthinh.agri_catalog_service.application.port.out.repository;

import java.util.List;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.domain.product.ProductHistory;

public interface ProductHistoryRepository {

    ProductHistory save(ProductHistory productHistory);

    List<ProductHistory> findAllByProductIdOrderByCreatedAtDesc(UUID productId);
}
