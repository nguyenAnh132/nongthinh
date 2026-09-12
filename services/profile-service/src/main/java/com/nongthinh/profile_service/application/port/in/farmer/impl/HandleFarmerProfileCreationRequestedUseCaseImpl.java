package com.nongthinh.profile_service.application.port.in.farmer.impl;

import java.util.Objects;
import org.springframework.stereotype.Service;

import com.nongthinh.profile_service.application.command.farmer.FarmerProfileCreationCommand;
import com.nongthinh.profile_service.application.event.FarmerProfileCreationRequestedEvent;
import com.nongthinh.profile_service.application.port.in.farmer.FarmerProfileCreationUseCase;
import com.nongthinh.profile_service.application.port.in.farmer.HandleFarmerProfileCreationRequestedUseCase;
import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class HandleFarmerProfileCreationRequestedUseCaseImpl implements HandleFarmerProfileCreationRequestedUseCase {

    private final FarmerProfileCreationUseCase farmerProfileCreationUseCase;

    @Override
    public void execute(FarmerProfileCreationRequestedEvent event) {
        Objects.requireNonNull(event, "event is required");

        try {
            farmerProfileCreationUseCase.execute(new FarmerProfileCreationCommand(
                    event.userId(),
                    event.firstName(),
                    event.lastName(),
                    event.gender(),
                    event.phone(),
                    event.provinceId(),
                    event.communeId(),
                    event.addressDetail(),
                    event.avatarUrl()
            ));
            log.info("Created farmer profile for userId={}", event.userId());
        } catch (BusinessException ex) {
            if (ex.getErrorCode() == ErrorCode.PROFILE_ALREADY_EXISTS) {
                log.warn("Farmer profile already exists for userId={}, skipping", event.userId());
                return;
            }
            throw ex;
        }
    }
}
