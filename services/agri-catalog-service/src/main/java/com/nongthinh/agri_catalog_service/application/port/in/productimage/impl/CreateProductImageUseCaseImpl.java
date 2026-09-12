package com.nongthinh.agri_catalog_service.application.port.in.productimage.impl;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.command.ProductImageCreationCommand;
import com.nongthinh.agri_catalog_service.application.model.FileMetadata;
import com.nongthinh.agri_catalog_service.application.port.in.productimage.CreateProductImageUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.ClockProvider;
import com.nongthinh.agri_catalog_service.application.port.out.IdGenerator;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductImageRepository;
import com.nongthinh.agri_catalog_service.application.view.ProductImageView;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.agri_catalog_service.domain.product.Product;
import com.nongthinh.agri_catalog_service.domain.product.ProductImage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateProductImageUseCaseImpl implements CreateProductImageUseCase {

    private final ProductImageUseCaseSupport support;
    private final ProductImageRepository productImageRepository;
    private final IdGenerator idGenerator;
    private final ClockProvider clockProvider;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public ProductImageView execute(UUID productId, ProductImageCreationCommand command) {
        Objects.requireNonNull(command, "command is required");
        Product product = support.requireWritableProduct(productId);
        FileMetadata file = support.requireValidFile(command.fileId(), null);
        UUID actorId = currentUserProvider.getCurrentUser().getUserId();
        Instant now = clockProvider.now();

        boolean firstImage = productImageRepository
                .findAllByProductIdOrderByDisplayOrder(productId)
                .isEmpty();
        boolean primary = firstImage || command.primary();
        if (primary) {
            support.demoteCurrentPrimary(productId, null, actorId, now);
        }

        ProductImage image = ProductImage.create(
                idGenerator.generate(),
                productId,
                file.id(),
                file.publicUrl(),
                command.altText(),
                command.displayOrder(),
                primary,
                actorId,
                now
        );
        ProductImage savedImage = productImageRepository.save(image);
        if (primary) {
            support.syncThumbnail(product, savedImage.getImageUrl(), actorId, now);
        }
        return ProductImageView.from(savedImage);
    }
}
