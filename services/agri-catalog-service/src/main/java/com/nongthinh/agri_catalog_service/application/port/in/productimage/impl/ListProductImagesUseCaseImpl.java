package com.nongthinh.agri_catalog_service.application.port.in.productimage.impl;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import com.nongthinh.agri_catalog_service.application.port.in.productimage.ListProductImagesUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductImageRepository;
import com.nongthinh.agri_catalog_service.application.view.ProductImageView;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListProductImagesUseCaseImpl implements ListProductImagesUseCase {

    private final ProductImageUseCaseSupport support;
    private final ProductImageRepository productImageRepository;

    @Override
    public List<ProductImageView> execute(UUID productId, boolean publicOnly) {
        if (publicOnly) {
            support.requirePublicProduct(productId);
        } else {
            support.requireProduct(productId);
        }
        return productImageRepository.findAllByProductIdOrderByDisplayOrder(productId)
                .stream()
                .map(ProductImageView::from)
                .toList();
    }
}
