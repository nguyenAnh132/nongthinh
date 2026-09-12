package com.nongthinh.profile_service.application.port.in.brand.impl;

import org.springframework.stereotype.Service;

import com.nongthinh.profile_service.application.command.brand.BrandProfileCreationCommand;
import com.nongthinh.profile_service.application.port.in.brand.BrandProfileCreationUseCase;
import com.nongthinh.profile_service.application.port.in.brand.CreateMyBrandProfileUseCase;
import com.nongthinh.profile_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.profile_service.presentation.dto.request.me.MyBrandProfileCreationRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateMyBrandProfileUseCaseImpl implements CreateMyBrandProfileUseCase {

    private final CurrentUserProvider currentUserProvider;
    private final BrandProfileCreationUseCase brandProfileCreationUseCase;

    @Override
    public void execute(MyBrandProfileCreationRequest request) {
        var userId = currentUserProvider.getCurrentUser().getUserId();
        brandProfileCreationUseCase.execute(new BrandProfileCreationCommand(
                userId,
                request.brandName(),
                request.taxCode(),
                request.description(),
                request.phone(),
                request.officeProvinceId(),
                request.officeCommuneId(),
                request.officeAddressDetail(),
                request.representativeName(),
                request.representativePhone(),
                request.representativeEmail(),
                request.logoUrl(),
                request.bannerUrl(),
                request.websiteUrl()
        ));
    }
}
