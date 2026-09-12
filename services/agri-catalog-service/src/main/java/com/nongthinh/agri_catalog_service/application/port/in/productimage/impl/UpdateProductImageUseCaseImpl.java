package com.nongthinh.agri_catalog_service.application.port.in.productimage.impl;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.command.ProductImageUpdateCommand;
import com.nongthinh.agri_catalog_service.application.model.FileMetadata;
import com.nongthinh.agri_catalog_service.application.port.in.productimage.UpdateProductImageUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.ClockProvider;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductImageRepository;
import com.nongthinh.agri_catalog_service.application.view.ProductImageView;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.agri_catalog_service.domain.product.Product;
import com.nongthinh.agri_catalog_service.domain.product.ProductImage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateProductImageUseCaseImpl implements UpdateProductImageUseCase {

    private final ProductImageUseCaseSupport support;
    private final ProductImageRepository productImageRepository;
    private final ClockProvider clockProvider;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public ProductImageView execute(
            UUID productId,
            UUID imageId,
            ProductImageUpdateCommand command
    ) {
        Objects.requireNonNull(command, "command is required");
        Product product = support.requireProduct(productId);
        ProductImage image = support.requireImage(productId, imageId);
        FileMetadata file = support.requireValidFile(command.fileId(), imageId);
        UUID actorId = currentUserProvider.getCurrentUser().getUserId();
        Instant now = clockProvider.now();

        image.update(
                file.id(),
                file.publicUrl(),
                command.altText(),
                command.displayOrder(),
                actorId,
                now
        );

        if (command.primary() && !image.isPrimary()) {
            support.demoteCurrentPrimary(productId, imageId, actorId, now);
            image.markAsPrimary(actorId, now);
        }

        ProductImage savedImage = productImageRepository.save(image);
        if (savedImage.isPrimary()) {
            support.syncThumbnail(product, savedImage.getImageUrl(), actorId, now);
        }
        return ProductImageView.from(savedImage);
    }
}
