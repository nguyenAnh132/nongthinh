package com.nongthinh.profile_service.application.port.in.admin.impl;

import java.util.Objects;
import org.springframework.stereotype.Service;

import com.nongthinh.profile_service.application.command.admin.AdminProfileCreationCommand;
import com.nongthinh.profile_service.application.event.AdminProfileCreationRequestedEvent;
import com.nongthinh.profile_service.application.port.in.admin.AdminProfileCreationUseCase;
import com.nongthinh.profile_service.application.port.in.admin.HandleAdminProfileCreationRequestedUseCase;
import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class HandleAdminProfileCreationRequestedUseCaseImpl implements HandleAdminProfileCreationRequestedUseCase {

    private final AdminProfileCreationUseCase adminProfileCreationUseCase;

    @Override
    public void execute(AdminProfileCreationRequestedEvent event) {
        Objects.requireNonNull(event, "event is required");

        try {
            adminProfileCreationUseCase.execute(new AdminProfileCreationCommand(
                    event.userId(),
                    event.firstName(),
                    event.lastName(),
                    event.phone(),
                    event.avatarUrl()
            ));
            log.info("Created admin profile for userId={}", event.userId());
        } catch (BusinessException ex) {
            if (ex.getErrorCode() == ErrorCode.PROFILE_ALREADY_EXISTS) {
                log.warn("Admin profile already exists for userId={}, skipping", event.userId());
                return;
            }
            throw ex;
        }
    }
}
