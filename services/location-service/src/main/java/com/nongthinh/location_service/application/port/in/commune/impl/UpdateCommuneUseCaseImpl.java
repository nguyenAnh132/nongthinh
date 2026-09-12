package com.nongthinh.location_service.application.port.in.commune.impl;

import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.location_service.application.command.CommuneUpdateCommand;
import com.nongthinh.location_service.application.port.in.commune.UpdateCommuneUseCase;
import com.nongthinh.location_service.application.port.out.repository.CommuneRepository;
import com.nongthinh.location_service.application.port.out.repository.ProvinceRepository;
import com.nongthinh.location_service.application.view.CommuneView;
import com.nongthinh.location_service.common.exception.ErrorCode;
import com.nongthinh.location_service.domain.commune.Commune;
import com.nongthinh.location_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateCommuneUseCaseImpl implements UpdateCommuneUseCase {

    private final CommuneRepository communeRepository;
    private final ProvinceRepository provinceRepository;

    @Override
    @Transactional
    public CommuneView execute(UUID id, CommuneUpdateCommand command) {
        Objects.requireNonNull(command, "command is required");

        Commune commune = communeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMUNE_NOT_FOUND));

        if (!commune.getProvinceId().equals(command.provinceId())
                && !provinceRepository.existsById(command.provinceId())) {
            throw new BusinessException(ErrorCode.PROVINCE_NOT_FOUND);
        }
        if (!commune.getCode().equals(command.code()) && communeRepository.existsByCode(command.code())) {
            throw new BusinessException(ErrorCode.COMMUNE_CODE_ALREADY_EXISTS);
        }

        commune.updateProvinceId(command.provinceId());
        commune.updateCode(command.code());
        commune.updateName(command.name());
        return CommuneView.from(communeRepository.save(commune));
    }
}
