package com.nongthinh.agri_catalog_service.application.port.in.croptype.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import com.nongthinh.agri_catalog_service.application.port.in.croptype.ListCropTypesUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.repository.CropTypeRepository;
import com.nongthinh.agri_catalog_service.application.view.CropTypeView;
import com.nongthinh.agri_catalog_service.domain.croptype.CropType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListCropTypesUseCaseImpl implements ListCropTypesUseCase {

    private final CropTypeRepository cropTypeRepository;

    @Override
    public List<CropTypeView> execute(boolean activeOnly) {
        List<CropType> cropTypes = activeOnly
                ? cropTypeRepository.findAllActive()
                : cropTypeRepository.findAll();
        return cropTypes.stream()
                .map(CropTypeView::from)
                .toList();
    }
}

