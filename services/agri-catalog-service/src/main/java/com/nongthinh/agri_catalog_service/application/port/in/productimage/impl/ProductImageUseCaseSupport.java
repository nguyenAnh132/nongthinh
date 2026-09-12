package com.nongthinh.agri_catalog_service.application.port.in.productimage.impl;

import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;
import com.nongthinh.agri_catalog_service.application.model.FileMetadata;
import com.nongthinh.agri_catalog_service.application.port.out.FileServicePort;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductImageRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductRepository;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.common.constant.RoleConstant;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUser;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;
import com.nongthinh.agri_catalog_service.domain.product.Product;
import com.nongthinh.agri_catalog_service.domain.product.ProductImage;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.ModerationStatus;
import com.nongthinh.agri_catalog_service.domain.product.valueobject.PublicationStatus;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
class ProductImageUseCaseSupport {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final FileServicePort fileServicePort;
    private final CurrentUserProvider currentUserProvider;

    Product requireProduct(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    Product requireWritableProduct(UUID productId) {
        Product product = requireProduct(productId);
        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        if (currentUser.hasRole(RoleConstant.ROLE_BRAND)
                && !currentUser.getUserId().equals(product.getCreatedBy())) {
            throw new BusinessException(ErrorCode.BRAND_RESOURCE_ACCESS_DENIED);
        }
        return product;
    }

    Product requirePublicProduct(UUID productId) {
        Product product = requireProduct(productId);
        if (product.getPublicationStatus() != PublicationStatus.PUBLISHED
                || product.getModerationStatus() != ModerationStatus.NORMAL) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return product;
    }

    ProductImage requireImage(UUID productId, UUID imageId) {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_IMAGE_NOT_FOUND));
        if (!productId.equals(image.getProductId())) {
            throw new BusinessException(ErrorCode.PRODUCT_IMAGE_NOT_FOUND);
        }
        return image;
    }

    FileMetadata requireValidFile(UUID fileId, UUID currentImageId) {
        productImageRepository.findByFileId(fileId)
                .filter(image -> currentImageId == null || !image.getId().equals(currentImageId))
                .ifPresent(image -> {
                    throw new BusinessException(ErrorCode.PRODUCT_IMAGE_FILE_ALREADY_USED);
                });

        FileMetadata file = fileServicePort.getActiveFile(fileId);
        if (!file.isActiveProductImage()) {
            throw new BusinessException(ErrorCode.PRODUCT_IMAGE_FILE_INVALID);
        }
        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        if (currentUser.hasRole(RoleConstant.ROLE_BRAND)
                && !currentUser.getUserId().equals(file.ownerUserId())) {
            throw new BusinessException(ErrorCode.BRAND_RESOURCE_ACCESS_DENIED);
        }
        return file;
    }

    void demoteCurrentPrimary(UUID productId, UUID excludedImageId, UUID actorId, Instant now) {
        productImageRepository.findPrimaryByProductId(productId)
                .filter(image -> excludedImageId == null || !image.getId().equals(excludedImageId))
                .ifPresent(image -> {
                    image.removePrimary(actorId, now);
                    productImageRepository.saveAndFlush(image);
                });
    }

    void syncThumbnail(Product product, String imageUrl, UUID actorId, Instant now) {
        product.changeThumbnailUrl(imageUrl, actorId, now);
        productRepository.save(product);
    }
}
