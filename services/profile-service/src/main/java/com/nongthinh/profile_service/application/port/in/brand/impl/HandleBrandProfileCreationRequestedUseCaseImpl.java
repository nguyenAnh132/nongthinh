package com.nongthinh.profile_service.application.port.in.brand.impl;

import java.util.Objects;
import org.springframework.stereotype.Service;

import com.nongthinh.profile_service.application.command.brand.BrandProfileCreationCommand;
import com.nongthinh.profile_service.application.event.BrandProfileCreationRequestedEvent;
import com.nongthinh.profile_service.application.port.in.brand.BrandProfileCreationUseCase;
import com.nongthinh.profile_service.application.port.in.brand.HandleBrandProfileCreationRequestedUseCase;
import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class HandleBrandProfileCreationRequestedUseCaseImpl implements HandleBrandProfileCreationRequestedUseCase {

    private final BrandProfileCreationUseCase brandProfileCreationUseCase;

    @Override
    public void execute(BrandProfileCreationRequestedEvent event) {
        Objects.requireNonNull(event, "event is required");

        try {
            brandProfileCreationUseCase.execute(new BrandProfileCreationCommand(
                    event.userId(),
                    event.brandName(),
                    event.taxCode(),
                    event.description(),
                    event.phone(),
                    event.officeProvinceId(),
                    event.officeCommuneId(),
                    event.officeAddressDetail(),
                    event.representativeName(),
                    event.representativePhone(),
                    event.representativeEmail(),
                    event.logoUrl(),
                    event.bannerUrl(),
                    event.websiteUrl()
            ));
            log.info("Created brand profile for userId={}", event.userId());
        } catch (BusinessException ex) {
            if (ex.getErrorCode() == ErrorCode.PROFILE_ALREADY_EXISTS) {
                log.warn("Brand profile already exists for userId={}, skipping", event.userId());
                return;
            }
            throw ex;
        }
    }
}
