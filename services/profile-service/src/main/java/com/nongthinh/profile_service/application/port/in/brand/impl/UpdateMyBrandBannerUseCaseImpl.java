package com.nongthinh.profile_service.application.port.in.brand.impl;

import org.springframework.stereotype.Service;
import com.nongthinh.profile_service.application.port.in.brand.UpdateMyBrandBannerUseCase;
import com.nongthinh.profile_service.application.port.out.ClockProvider;
import com.nongthinh.profile_service.application.port.out.repository.BrandProfileRepository;
import com.nongthinh.profile_service.application.view.BrandProfileView;
import com.nongthinh.profile_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.brandprofile.BrandProfile;
import com.nongthinh.profile_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateMyBrandBannerUseCaseImpl implements UpdateMyBrandBannerUseCase {

    private final CurrentUserProvider currentUserProvider;
    private final BrandProfileRepository brandProfileRepository;
    private final ClockProvider clockProvider;

    @Override
    public BrandProfileView execute(String bannerUrl) {
        var userId = currentUserProvider.getCurrentUser().getUserId();
        BrandProfile profile = brandProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
        profile.updateBannerUrl(bannerUrl, clockProvider.now());
        return BrandProfileView.from(brandProfileRepository.save(profile));
    }
}
