package com.nongthinh.location_service.application.port.in.province.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.location_service.application.port.in.province.DeleteProvinceUseCase;
import com.nongthinh.location_service.application.port.out.repository.CommuneRepository;
import com.nongthinh.location_service.application.port.out.repository.ProvinceRepository;
import com.nongthinh.location_service.common.exception.ErrorCode;
import com.nongthinh.location_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteProvinceUseCaseImpl implements DeleteProvinceUseCase {

    private final ProvinceRepository provinceRepository;
    private final CommuneRepository communeRepository;

    @Override
    @Transactional
    public void execute(String id) {
        if (!provinceRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.PROVINCE_NOT_FOUND);
        }
        if (communeRepository.existsByProvinceId(id)) {
            throw new BusinessException(ErrorCode.PROVINCE_HAS_COMMUNES);
        }
        provinceRepository.deleteById(id);
    }
}
