package com.nongthinh.location_service.application.port.in.address.impl;

import java.util.UUID;
import org.springframework.stereotype.Service;
import com.nongthinh.location_service.application.port.in.address.ValidateAddressUseCase;
import com.nongthinh.location_service.application.port.out.repository.CommuneRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ValidateAddressUseCaseImpl implements ValidateAddressUseCase {

    private final CommuneRepository communeRepository;

    @Override
    public boolean execute(String provinceId, UUID communeId) {
        if (provinceId == null || provinceId.isBlank() || communeId == null) {
            return false;
        }
        return communeRepository.findById(communeId)
                .map(commune -> provinceId.equals(commune.getProvinceId()))
                .orElse(false);
    }
}
