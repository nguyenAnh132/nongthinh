package com.nongthinh.profile_service.application.port.in.farmer.impl;

import java.util.UUID;
import org.springframework.stereotype.Service;
import com.nongthinh.profile_service.application.port.in.farmer.GetPublicFarmerProfileByIdUseCase;
import com.nongthinh.profile_service.application.port.out.repository.FarmerProfileRepository;
import com.nongthinh.profile_service.application.view.FarmerProfilePublicView;
import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.exception.BusinessException;
import com.nongthinh.profile_service.domain.farmerprofile.FarmerProfile;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetPublicFarmerProfileByIdUseCaseImpl implements GetPublicFarmerProfileByIdUseCase {

    private final FarmerProfileRepository farmerProfileRepository;

    @Override
    public FarmerProfilePublicView execute(UUID id) {
        FarmerProfile profile = farmerProfileRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
        profile.ensureActive();
        return FarmerProfilePublicView.from(profile);
    }
}
