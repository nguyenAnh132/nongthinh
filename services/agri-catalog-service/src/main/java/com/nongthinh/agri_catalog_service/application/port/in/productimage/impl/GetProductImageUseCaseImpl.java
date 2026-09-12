package com.nongthinh.agri_catalog_service.application.port.in.productimage.impl;

import java.util.UUID;
import org.springframework.stereotype.Service;
import com.nongthinh.agri_catalog_service.application.port.in.productimage.GetProductImageUseCase;
import com.nongthinh.agri_catalog_service.application.view.ProductImageView;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetProductImageUseCaseImpl implements GetProductImageUseCase {

    private final ProductImageUseCaseSupport support;

    @Override
    public ProductImageView execute(UUID productId, UUID imageId) {
        support.requireProduct(productId);
        return ProductImageView.from(support.requireImage(productId, imageId));
    }
}
