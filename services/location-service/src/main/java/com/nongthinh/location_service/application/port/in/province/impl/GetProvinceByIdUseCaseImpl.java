package com.nongthinh.location_service.application.port.in.province.impl;

import org.springframework.stereotype.Service;
import com.nongthinh.location_service.application.port.in.province.GetProvinceByIdUseCase;
import com.nongthinh.location_service.application.port.out.repository.ProvinceRepository;
import com.nongthinh.location_service.application.view.ProvinceView;
import com.nongthinh.location_service.common.exception.ErrorCode;
import com.nongthinh.location_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetProvinceByIdUseCaseImpl implements GetProvinceByIdUseCase {

    private final ProvinceRepository provinceRepository;

    @Override
    public ProvinceView execute(String id) {
        return provinceRepository.findById(id)
                .map(ProvinceView::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROVINCE_NOT_FOUND));
    }
}
