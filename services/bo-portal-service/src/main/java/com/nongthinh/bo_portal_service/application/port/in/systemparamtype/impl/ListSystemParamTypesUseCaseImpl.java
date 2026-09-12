package com.nongthinh.bo_portal_service.application.port.in.systemparamtype.impl;

import com.nongthinh.bo_portal_service.application.port.in.systemparamtype.ListSystemParamTypesUseCase;
import com.nongthinh.bo_portal_service.application.port.out.repository.SystemParamTypeRepository;
import com.nongthinh.bo_portal_service.application.view.SystemParamTypeView;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListSystemParamTypesUseCaseImpl implements ListSystemParamTypesUseCase {

    private final SystemParamTypeRepository systemParamTypeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SystemParamTypeView> execute() {
        return systemParamTypeRepository.findAll().stream()
                .map(SystemParamTypeView::fromSystemParamType)
                .toList();
    }
}
