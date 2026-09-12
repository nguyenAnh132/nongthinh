package com.nongthinh.location_service.application.port.in.commune.impl;

import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.location_service.application.command.CommuneCreationCommand;
import com.nongthinh.location_service.application.port.in.commune.CreateCommuneUseCase;
import com.nongthinh.location_service.application.port.out.IdGenerator;
import com.nongthinh.location_service.application.port.out.repository.CommuneRepository;
import com.nongthinh.location_service.application.port.out.repository.ProvinceRepository;
import com.nongthinh.location_service.application.view.CommuneView;
import com.nongthinh.location_service.common.exception.ErrorCode;
import com.nongthinh.location_service.domain.commune.Commune;
import com.nongthinh.location_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateCommuneUseCaseImpl implements CreateCommuneUseCase {

    private final CommuneRepository communeRepository;
    private final ProvinceRepository provinceRepository;
    private final IdGenerator idGenerator;

    @Override
    @Transactional
    public CommuneView execute(CommuneCreationCommand command) {
        Objects.requireNonNull(command, "command is required");

        if (!provinceRepository.existsById(command.provinceId())) {
            throw new BusinessException(ErrorCode.PROVINCE_NOT_FOUND);
        }
        if (communeRepository.existsByCode(command.code())) {
            throw new BusinessException(ErrorCode.COMMUNE_CODE_ALREADY_EXISTS);
        }

        UUID id = idGenerator.generate();
        Commune commune = Commune.create(id, command.provinceId(), command.code(), command.name());
        return CommuneView.from(communeRepository.save(commune));
    }
}
