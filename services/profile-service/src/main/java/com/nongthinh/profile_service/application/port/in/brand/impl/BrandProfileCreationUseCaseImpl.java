package com.nongthinh.profile_service.application.port.in.brand.impl;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;

import com.nongthinh.profile_service.application.command.brand.BrandProfileCreationCommand;
import com.nongthinh.profile_service.application.event.BrandProfileCreatedEvent;
import com.nongthinh.profile_service.application.port.in.brand.BrandProfileCreationUseCase;
import com.nongthinh.profile_service.application.port.out.ClockProvider;
import com.nongthinh.profile_service.application.port.out.EventPublisher;
import com.nongthinh.profile_service.application.port.out.IdGenerator;
import com.nongthinh.profile_service.application.port.out.LocationServicePort;
import com.nongthinh.profile_service.application.port.out.repository.BrandProfileRepository;
import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.brandprofile.BrandProfile;
import com.nongthinh.profile_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BrandProfileCreationUseCaseImpl implements BrandProfileCreationUseCase {

    private final IdGenerator idGenerator;
    private final ClockProvider clockProvider;
    private final BrandProfileRepository brandProfileRepository;
    private final LocationServicePort locationServicePort;
    private final EventPublisher eventPublisher;

    @Override
    public void execute(BrandProfileCreationCommand command) {
        Objects.requireNonNull(command, "command is required");

        if (brandProfileRepository.existsByUserId(command.userId())) {
            throw new BusinessException(ErrorCode.PROFILE_ALREADY_EXISTS);
        }

        if (command.officeProvinceId() != null || command.officeCommuneId() != null) {
            locationServicePort.validateAddress(command.officeProvinceId(), command.officeCommuneId());
        }

        UUID id = idGenerator.generate();
        Instant now = clockProvider.now();

        BrandProfile newBrandProfile = BrandProfileCreationCommand.toBrandProfile(command, now, id);

        brandProfileRepository.save(newBrandProfile);

        BrandProfileCreatedEvent event = new BrandProfileCreatedEvent(
            idGenerator.generate(),
            now,
            newBrandProfile.getId(),
            newBrandProfile.getUserId(),
            newBrandProfile.getBrandName().getValue()
        );

        eventPublisher.publish(event);

    }
}
