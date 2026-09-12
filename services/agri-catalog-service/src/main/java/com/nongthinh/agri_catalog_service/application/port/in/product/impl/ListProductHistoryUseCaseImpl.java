package com.nongthinh.agri_catalog_service.application.port.in.product.impl;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import com.nongthinh.agri_catalog_service.application.port.in.product.ListProductHistoryUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.repository.ProductHistoryRepository;
import com.nongthinh.agri_catalog_service.application.view.ProductHistoryView;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListProductHistoryUseCaseImpl implements ListProductHistoryUseCase {

    private final ProductUseCaseSupport support;
    private final ProductHistoryRepository historyRepository;

    @Override
    public List<ProductHistoryView> execute(UUID productId) {
        support.requireAccessibleProduct(productId);
        return historyRepository.findAllByProductIdOrderByCreatedAtDesc(productId)
                .stream()
                .map(ProductHistoryView::from)
                .toList();
    }
}
