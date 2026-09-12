package com.nongthinh.agri_catalog_service.application.port.in.productimage.impl;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.port.in.productimage.DeleteProductImageUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.ClockProvider;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductImageRepository;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.agri_catalog_service.domain.product.Product;
import com.nongthinh.agri_catalog_service.domain.product.ProductImage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteProductImageUseCaseImpl implements DeleteProductImageUseCase {

    private final ProductImageUseCaseSupport support;
    private final ProductImageRepository productImageRepository;
    private final ClockProvider clockProvider;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public void execute(UUID productId, UUID imageId) {
        Product product = support.requireProduct(productId);
        ProductImage image = support.requireImage(productId, imageId);
        UUID actorId = currentUserProvider.getCurrentUser().getUserId();
        Instant now = clockProvider.now();
        boolean wasPrimary = image.isPrimary();

        image.delete(actorId, now);
        productImageRepository.saveAndFlush(image);

        if (wasPrimary) {
            List<ProductImage> remainingImages =
                    productImageRepository.findAllByProductIdOrderByDisplayOrder(productId);
            if (remainingImages.isEmpty()) {
                support.syncThumbnail(product, null, actorId, now);
            } else {
                ProductImage newPrimary = remainingImages.getFirst();
                newPrimary.markAsPrimary(actorId, now);
                productImageRepository.save(newPrimary);
                support.syncThumbnail(product, newPrimary.getImageUrl(), actorId, now);
            }
        }
    }
}
