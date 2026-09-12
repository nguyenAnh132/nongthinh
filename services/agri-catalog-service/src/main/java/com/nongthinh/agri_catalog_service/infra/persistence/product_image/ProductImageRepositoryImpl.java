package com.nongthinh.agri_catalog_service.infra.persistence.product_image;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductImageRepository;
import com.nongthinh.agri_catalog_service.domain.product.ProductImage;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductImageRepositoryImpl implements ProductImageRepository {

    private final JpaProductImageRepository jpaProductImageRepository;
    private final ProductImagePersistenceMapper productImagePersistenceMapper;

    @Override
    public Optional<ProductImage> findById(UUID id) {
        return jpaProductImageRepository.findByIdAndDeletedAtIsNull(id)
                .map(productImagePersistenceMapper::toDomain);
    }

    @Override
    public Optional<ProductImage> findByFileId(UUID fileId) {
        return jpaProductImageRepository.findByFileIdAndDeletedAtIsNull(fileId)
                .map(productImagePersistenceMapper::toDomain);
    }

    @Override
    public Optional<ProductImage> findPrimaryByProductId(UUID productId) {
        return jpaProductImageRepository
                .findByProductIdAndIsPrimaryTrueAndDeletedAtIsNull(productId)
                .map(productImagePersistenceMapper::toDomain);
    }

    @Override
    public List<ProductImage> findAllByProductIdOrderByDisplayOrder(UUID productId) {
        return jpaProductImageRepository
                .findAllByProductIdAndDeletedAtIsNullOrderByDisplayOrderAscCreatedAtAsc(productId)
                .stream()
                .map(productImagePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public ProductImage save(ProductImage productImage) {
        return productImagePersistenceMapper.toDomain(
                jpaProductImageRepository.save(productImagePersistenceMapper.toEntity(productImage))
        );
    }

    @Override
    public ProductImage saveAndFlush(ProductImage productImage) {
        return productImagePersistenceMapper.toDomain(
                jpaProductImageRepository.saveAndFlush(
                        productImagePersistenceMapper.toEntity(productImage)
                )
        );
    }
}
