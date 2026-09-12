package com.nongthinh.location_service.application.port.in.province.impl;

import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.location_service.application.command.ProvinceUpdateCommand;
import com.nongthinh.location_service.application.port.in.province.UpdateProvinceUseCase;
import com.nongthinh.location_service.application.port.out.repository.ProvinceRepository;
import com.nongthinh.location_service.application.view.ProvinceView;
import com.nongthinh.location_service.common.exception.ErrorCode;
import com.nongthinh.location_service.domain.exception.BusinessException;
import com.nongthinh.location_service.domain.province.Province;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateProvinceUseCaseImpl implements UpdateProvinceUseCase {

    private final ProvinceRepository provinceRepository;

    @Override
    @Transactional
    public ProvinceView execute(String id, ProvinceUpdateCommand command) {
        Objects.requireNonNull(command, "command is required");

        Province province = provinceRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROVINCE_NOT_FOUND));

        if (!province.getCode().equals(command.code()) && provinceRepository.existsByCode(command.code())) {
            throw new BusinessException(ErrorCode.PROVINCE_CODE_ALREADY_EXISTS);
        }

        province.updateCode(command.code());
        province.updateName(command.name());
        return ProvinceView.from(provinceRepository.save(province));
    }
}
