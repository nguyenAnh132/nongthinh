package com.nongthinh.agri_catalog_service.infra.persistence.category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductCategoryRepository;
import com.nongthinh.agri_catalog_service.domain.category.ProductCategory;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductCategoryRepositoryImpl implements ProductCategoryRepository {

    private final JpaProductCategoryRepository jpaProductCategoryRepository;
    private final ProductCategoryPersistenceMapper productCategoryPersistenceMapper;

    @Override
    public boolean existsBySlug(String slug) {
        return jpaProductCategoryRepository.existsBySlugIgnoreCaseAndDeletedAtIsNull(slug);
    }

    @Override
    public Optional<ProductCategory> findById(UUID id) {
        return jpaProductCategoryRepository.findByIdAndDeletedAtIsNull(id)
                .map(productCategoryPersistenceMapper::toDomain);
    }

    @Override
    public Optional<ProductCategory> findBySlug(String slug) {
        return jpaProductCategoryRepository.findBySlugIgnoreCaseAndDeletedAtIsNull(slug)
                .map(productCategoryPersistenceMapper::toDomain);
    }

    @Override
    public List<ProductCategory> findAllOrderByDisplayOrder() {
        return toDomains(jpaProductCategoryRepository.findAllByDeletedAtIsNullOrderByDisplayOrderAscNameAsc());
    }

    @Override
    public List<ProductCategory> findAllActiveOrderByDisplayOrder() {
        return toDomains(
                jpaProductCategoryRepository
                        .findAllByDeletedAtIsNullAndIsActiveTrueOrderByDisplayOrderAscNameAsc()
        );
    }

    @Override
    public List<ProductCategory> findByParentIdOrderByDisplayOrder(UUID parentId) {
        return toDomains(
                jpaProductCategoryRepository
                        .findAllByParentIdAndDeletedAtIsNullOrderByDisplayOrderAscNameAsc(parentId)
        );
    }

    @Override
    public ProductCategory save(ProductCategory productCategory) {
        JpaProductCategoryEntity entity = productCategoryPersistenceMapper.toEntity(productCategory);
        return productCategoryPersistenceMapper.toDomain(jpaProductCategoryRepository.save(entity));
    }

    private List<ProductCategory> toDomains(List<JpaProductCategoryEntity> entities) {
        return entities.stream()
                .map(productCategoryPersistenceMapper::toDomain)
                .toList();
    }
}
