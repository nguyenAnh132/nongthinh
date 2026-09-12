package com.nongthinh.agri_catalog_service.infra.persistence.producthistory;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductHistoryRepository;
import com.nongthinh.agri_catalog_service.domain.product.ProductHistory;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductHistoryRepositoryImpl implements ProductHistoryRepository {

    private final JpaProductHistoryRepository jpaRepository;
    private final ProductHistoryPersistenceMapper mapper;

    @Override
    public ProductHistory save(ProductHistory productHistory) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(productHistory)));
    }

    @Override
    public List<ProductHistory> findAllByProductIdOrderByCreatedAtDesc(UUID productId) {
        return jpaRepository.findAllByProductIdOrderByCreatedAtDesc(productId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
