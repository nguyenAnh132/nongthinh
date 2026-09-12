package com.nongthinh.location_service.application.port.in.province.impl;

import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.location_service.application.command.ProvinceCreationCommand;
import com.nongthinh.location_service.application.port.in.province.CreateProvinceUseCase;
import com.nongthinh.location_service.application.port.out.repository.ProvinceRepository;
import com.nongthinh.location_service.application.view.ProvinceView;
import com.nongthinh.location_service.common.exception.ErrorCode;
import com.nongthinh.location_service.domain.exception.BusinessException;
import com.nongthinh.location_service.domain.province.Province;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateProvinceUseCaseImpl implements CreateProvinceUseCase {

    private final ProvinceRepository provinceRepository;

    @Override
    @Transactional
    public ProvinceView execute(ProvinceCreationCommand command) {
        Objects.requireNonNull(command, "command is required");

        if (provinceRepository.existsById(command.id())) {
            throw new BusinessException(ErrorCode.PROVINCE_ALREADY_EXISTS);
        }
        if (provinceRepository.existsByCode(command.code())) {
            throw new BusinessException(ErrorCode.PROVINCE_CODE_ALREADY_EXISTS);
        }

        Province province = Province.create(command.id(), command.code(), command.name());
        return ProvinceView.from(provinceRepository.save(province));
    }
}
