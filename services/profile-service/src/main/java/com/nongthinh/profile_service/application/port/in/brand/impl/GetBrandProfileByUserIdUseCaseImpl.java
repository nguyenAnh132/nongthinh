package com.nongthinh.profile_service.application.port.in.brand.impl;

import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.nongthinh.profile_service.application.port.in.brand.GetBrandProfileByUserIdUseCase;
import com.nongthinh.profile_service.application.port.out.repository.BrandProfileRepository;
import com.nongthinh.profile_service.application.view.BrandProfileView;
import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.brandprofile.BrandProfile;
import com.nongthinh.profile_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetBrandProfileByUserIdUseCaseImpl implements GetBrandProfileByUserIdUseCase {

    private final BrandProfileRepository brandProfileRepository;

    @Override
    public BrandProfileView execute(UUID userId) {
        BrandProfile profile = brandProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
        log.info("Profile: {}", profile);
        return BrandProfileView.from(profile);
    }
}
