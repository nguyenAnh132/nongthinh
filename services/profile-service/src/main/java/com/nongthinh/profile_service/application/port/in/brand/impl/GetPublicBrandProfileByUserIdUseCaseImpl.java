package com.nongthinh.profile_service.application.port.in.brand.impl;

import java.util.UUID;
import org.springframework.stereotype.Service;
import com.nongthinh.profile_service.application.port.in.brand.GetPublicBrandProfileByUserIdUseCase;
import com.nongthinh.profile_service.application.port.out.repository.BrandProfileRepository;
import com.nongthinh.profile_service.application.view.BrandProfilePublicView;
import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.brandprofile.BrandProfile;
import com.nongthinh.profile_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetPublicBrandProfileByUserIdUseCaseImpl implements GetPublicBrandProfileByUserIdUseCase {

    private final BrandProfileRepository brandProfileRepository;

    @Override
    public BrandProfilePublicView execute(UUID userId) {
        BrandProfile profile = brandProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
        profile.ensureActive();
        return BrandProfilePublicView.from(profile);
    }
}
