package com.nongthinh.location_service.application.port.in.commune.impl;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.location_service.application.port.in.commune.DeleteCommuneUseCase;
import com.nongthinh.location_service.application.port.out.repository.CommuneRepository;
import com.nongthinh.location_service.common.exception.ErrorCode;
import com.nongthinh.location_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteCommuneUseCaseImpl implements DeleteCommuneUseCase {

    private final CommuneRepository communeRepository;

    @Override
    @Transactional
    public void execute(UUID id) {
        if (!communeRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.COMMUNE_NOT_FOUND);
        }
        communeRepository.deleteById(id);
    }
}
