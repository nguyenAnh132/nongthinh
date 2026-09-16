package com.nongthinh.agri_catalog_service.infra.persistence.product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.PageRequest;
import com.nongthinh.agri_catalog_service.application.query.PublicProductQuery;
import com.nongthinh.agri_catalog_service.application.view.PageView;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductRepository;
import com.nongthinh.agri_catalog_service.domain.product.Product;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.ModerationStatus;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.PublicationStatus;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final JpaProductRepository jpaProductRepository;
    private final ProductPersistenceMapper productPersistenceMapper;

    @Override
    public PageView<Product> searchPublic(PublicProductQuery query) {
        var page = jpaProductRepository.searchPublic(containsPattern(query.name()),
                query.diseaseName().isEmpty() ? "" : containsPattern(query.diseaseName()),
                query.keyword().isEmpty() ? "" : containsPattern(query.keyword()),
                PageRequest.of(query.page(), PublicProductQuery.PAGE_SIZE));
        return new PageView<>(toDomains(page.getContent()), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.hasNext());
    }

    private static String containsPattern(String value) {
        return "%" + value.replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
    }

    @Override
    public boolean existsByBrandIdAndSlug(UUID brandId, String slug) {
        return jpaProductRepository.existsByBrandIdAndSlugIgnoreCaseAndDeletedAtIsNull(
                brandId,
                slug
        );
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return jpaProductRepository.findByIdAndDeletedAtIsNull(id)
                .map(productPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Product> findByBrandIdAndSlug(UUID brandId, String slug) {
        return jpaProductRepository.findByBrandIdAndSlugIgnoreCaseAndDeletedAtIsNull(brandId, slug)
                .map(productPersistenceMapper::toDomain);
    }

    @Override
    public List<Product> findAll() {
        return toDomains(jpaProductRepository.findAllByDeletedAtIsNullOrderByCreatedAtDesc());
    }

    @Override
    public List<Product> findAllByBrandId(UUID brandId) {
        return toDomains(
                jpaProductRepository.findAllByBrandIdAndDeletedAtIsNullOrderByCreatedAtDesc(brandId)
        );
    }

    @Override
    public List<Product> findAllByCategoryId(UUID categoryId) {
        return toDomains(
                jpaProductRepository
                        .findAllByCategoryIdAndDeletedAtIsNullOrderByCreatedAtDesc(categoryId)
        );
    }

    @Override
    public List<Product> findAllPublishedOrderByPublishedAtDesc() {
        return toDomains(
                jpaProductRepository
                        .findAllByPublicationStatusAndModerationStatusAndDeletedAtIsNullOrderByPublishedAtDesc(
                                PublicationStatus.PUBLISHED.name(),
                                ModerationStatus.NORMAL.name()
                        )
        );
    }

    @Override
    public Product save(Product product) {
        return productPersistenceMapper.toDomain(
                jpaProductRepository.save(productPersistenceMapper.toEntity(product))
        );
    }

    private List<Product> toDomains(List<JpaProductEntity> entities) {
        return entities.stream()
                .map(productPersistenceMapper::toDomain)
                .toList();
    }
}
