package com.nongthinh.profile_service.application.port.in.farmer.impl;

import java.util.UUID;
import org.springframework.stereotype.Service;
import com.nongthinh.profile_service.application.port.in.farmer.GetFarmerProfileByUserIdUseCase;
import com.nongthinh.profile_service.application.port.out.repository.FarmerProfileRepository;
import com.nongthinh.profile_service.application.view.FarmerProfileView;
import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.exception.BusinessException;
import com.nongthinh.profile_service.domain.farmerprofile.FarmerProfile;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetFarmerProfileByUserIdUseCaseImpl implements GetFarmerProfileByUserIdUseCase {

    private final FarmerProfileRepository farmerProfileRepository;

    @Override
    public FarmerProfileView execute(UUID userId) {
        FarmerProfile profile = farmerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
        profile.ensureActive();
        return FarmerProfileView.from(profile);
    }
}
