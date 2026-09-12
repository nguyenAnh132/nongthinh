package com.nongthinh.location_service.application.port.in.province.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import com.nongthinh.location_service.application.port.in.province.ListProvincesUseCase;
import com.nongthinh.location_service.application.port.out.repository.ProvinceRepository;
import com.nongthinh.location_service.application.view.ProvinceView;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListProvincesUseCaseImpl implements ListProvincesUseCase {

    private final ProvinceRepository provinceRepository;

    @Override
    public List<ProvinceView> execute() {
        return provinceRepository.findAllOrderByCode().stream()
                .map(ProvinceView::from)
                .toList();
    }
}
