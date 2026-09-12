package com.nongthinh.profile_service.application.port.in.brand.impl;

import org.springframework.stereotype.Service;
import com.nongthinh.profile_service.application.port.in.brand.GetMyBrandProfileUseCase;
import com.nongthinh.profile_service.application.port.out.repository.BrandProfileRepository;
import com.nongthinh.profile_service.application.view.BrandProfileView;
import com.nongthinh.profile_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetMyBrandProfileUseCaseImpl implements GetMyBrandProfileUseCase {

    private final CurrentUserProvider currentUserProvider;
    private final BrandProfileRepository brandProfileRepository;

    @Override
    public BrandProfileView execute() {
        var userId = currentUserProvider.getCurrentUser().getUserId();
        return brandProfileRepository.findByUserId(userId)
                .map(BrandProfileView::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
    }
}
