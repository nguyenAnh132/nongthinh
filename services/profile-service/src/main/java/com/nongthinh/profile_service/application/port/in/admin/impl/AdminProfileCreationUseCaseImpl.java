package com.nongthinh.profile_service.application.port.in.admin.impl;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;

import com.nongthinh.profile_service.application.command.admin.AdminProfileCreationCommand;
import com.nongthinh.profile_service.application.port.in.admin.AdminProfileCreationUseCase;
import com.nongthinh.profile_service.application.port.out.ClockProvider;
import com.nongthinh.profile_service.application.port.out.IdGenerator;
import com.nongthinh.profile_service.application.port.out.repository.AdminProfileRepository;
import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.adminprofile.AdminProfile;
import com.nongthinh.profile_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminProfileCreationUseCaseImpl implements AdminProfileCreationUseCase {

    private final IdGenerator idGenerator;
    private final ClockProvider clockProvider;
    private final AdminProfileRepository adminProfileRepository;

    @Override
    public void execute(AdminProfileCreationCommand command) {
        Objects.requireNonNull(command, "command is required");

        if (adminProfileRepository.existsByUserId(command.userId())) {
            throw new BusinessException(ErrorCode.PROFILE_ALREADY_EXISTS);
        }

        UUID id = idGenerator.generate();
        Instant now = clockProvider.now();

        AdminProfile newAdminProfile = AdminProfileCreationCommand.toAdminProfile(command, now, id);

        adminProfileRepository.save(newAdminProfile);
    }
}
