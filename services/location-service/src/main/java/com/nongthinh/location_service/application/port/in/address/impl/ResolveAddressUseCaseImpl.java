package com.nongthinh.location_service.application.port.in.address.impl;

import java.util.UUID;
import org.springframework.stereotype.Service;
import com.nongthinh.location_service.application.port.in.address.ResolveAddressUseCase;
import com.nongthinh.location_service.application.port.out.repository.CommuneRepository;
import com.nongthinh.location_service.application.port.out.repository.ProvinceRepository;
import com.nongthinh.location_service.application.view.AddressView;
import com.nongthinh.location_service.common.exception.ErrorCode;
import com.nongthinh.location_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResolveAddressUseCaseImpl implements ResolveAddressUseCase {

    private final ProvinceRepository provinceRepository;
    private final CommuneRepository communeRepository;

    @Override
    public AddressView execute(String provinceId, UUID communeId) {
        var province = provinceRepository.findById(provinceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROVINCE_NOT_FOUND));
        var commune = communeRepository.findById(communeId)
                .filter(item -> provinceId.equals(item.getProvinceId()))
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMUNE_NOT_FOUND));

        return new AddressView(
                province.getId(),
                province.getName(),
                commune.getId(),
                commune.getName()
        );
    }
}
