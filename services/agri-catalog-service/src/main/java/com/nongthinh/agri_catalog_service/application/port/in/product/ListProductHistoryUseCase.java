package com.nongthinh.agri_catalog_service.application.port.in.product;

import java.util.List;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.application.view.ProductHistoryView;

public interface ListProductHistoryUseCase {

    List<ProductHistoryView> execute(UUID productId);
}
