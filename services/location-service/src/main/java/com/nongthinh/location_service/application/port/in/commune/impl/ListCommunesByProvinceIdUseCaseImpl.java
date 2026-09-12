package com.nongthinh.location_service.application.port.in.commune.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import com.nongthinh.location_service.application.port.in.commune.ListCommunesByProvinceIdUseCase;
import com.nongthinh.location_service.application.port.out.repository.CommuneRepository;
import com.nongthinh.location_service.application.port.out.repository.ProvinceRepository;
import com.nongthinh.location_service.application.view.CommuneView;
import com.nongthinh.location_service.common.exception.ErrorCode;
import com.nongthinh.location_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListCommunesByProvinceIdUseCaseImpl implements ListCommunesByProvinceIdUseCase {

    private final CommuneRepository communeRepository;
    private final ProvinceRepository provinceRepository;

    @Override
    public List<CommuneView> execute(String provinceId) {
        if (!provinceRepository.existsById(provinceId)) {
            throw new BusinessException(ErrorCode.PROVINCE_NOT_FOUND);
        }
        return communeRepository.findByProvinceIdOrderByCode(provinceId).stream()
                .map(CommuneView::from)
                .toList();
    }
}
