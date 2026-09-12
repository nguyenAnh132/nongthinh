package com.nongthinh.location_service.application.port.in.commune.impl;

import java.util.UUID;
import org.springframework.stereotype.Service;
import com.nongthinh.location_service.application.port.in.commune.GetCommuneByIdUseCase;
import com.nongthinh.location_service.application.port.out.repository.CommuneRepository;
import com.nongthinh.location_service.application.view.CommuneView;
import com.nongthinh.location_service.common.exception.ErrorCode;
import com.nongthinh.location_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetCommuneByIdUseCaseImpl implements GetCommuneByIdUseCase {

    private final CommuneRepository communeRepository;

    @Override
    public CommuneView execute(UUID id) {
        return communeRepository.findById(id)
                .map(CommuneView::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMUNE_NOT_FOUND));
    }
}
