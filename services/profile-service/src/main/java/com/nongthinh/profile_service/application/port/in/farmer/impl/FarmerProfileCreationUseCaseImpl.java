package com.nongthinh.profile_service.application.port.in.farmer.impl;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;

import com.nongthinh.profile_service.application.command.farmer.FarmerProfileCreationCommand;
import com.nongthinh.profile_service.application.port.in.farmer.FarmerProfileCreationUseCase;
import com.nongthinh.profile_service.application.port.out.ClockProvider;
import com.nongthinh.profile_service.application.port.out.IdGenerator;
import com.nongthinh.profile_service.application.port.out.LocationServicePort;
import com.nongthinh.profile_service.application.port.out.repository.FarmerProfileRepository;
import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.exception.BusinessException;
import com.nongthinh.profile_service.domain.farmerprofile.FarmerProfile;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FarmerProfileCreationUseCaseImpl implements FarmerProfileCreationUseCase {

    private final IdGenerator idGenerator;
    private final ClockProvider clockProvider;
    private final FarmerProfileRepository farmerProfileRepository;
    private final LocationServicePort locationServicePort;

    @Override
    public void execute(FarmerProfileCreationCommand command) {
        Objects.requireNonNull(command, "command is required");

        if (farmerProfileRepository.existsByUserId(command.userId())) {
            throw new BusinessException(ErrorCode.PROFILE_ALREADY_EXISTS);
        }

        if (command.provinceId() != null || command.communeId() != null) {
            locationServicePort.validateAddress(command.provinceId(), command.communeId());
        }

        UUID id = idGenerator.generate();
        Instant now = clockProvider.now();

        FarmerProfile newFarmerProfile = FarmerProfileCreationCommand.toFarmerProfile(command, now, id);

        farmerProfileRepository.save(newFarmerProfile);
    }
}
