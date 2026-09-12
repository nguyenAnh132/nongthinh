package com.nongthinh.profile_service.application.port.in.brand.impl;

import java.util.UUID;
import org.springframework.stereotype.Service;
import com.nongthinh.profile_service.application.port.in.brand.GetPublicBrandProfileByIdUseCase;
import com.nongthinh.profile_service.application.port.out.repository.BrandProfileRepository;
import com.nongthinh.profile_service.application.view.BrandProfilePublicView;
import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.brandprofile.BrandProfile;
import com.nongthinh.profile_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetPublicBrandProfileByIdUseCaseImpl implements GetPublicBrandProfileByIdUseCase {

    private final BrandProfileRepository brandProfileRepository;

    @Override
    public BrandProfilePublicView execute(UUID id) {
        BrandProfile profile = brandProfileRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
        profile.ensureActive();
        return BrandProfilePublicView.from(profile);
    }
}
