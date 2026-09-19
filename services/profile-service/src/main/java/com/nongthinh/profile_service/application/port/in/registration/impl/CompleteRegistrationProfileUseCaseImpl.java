package com.nongthinh.profile_service.application.port.in.registration.impl;

import java.util.Objects;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import com.nongthinh.profile_service.application.command.CompleteRegistrationProfileCommand;
import com.nongthinh.profile_service.application.command.admin.AdminProfileCreationCommand;
import com.nongthinh.profile_service.application.command.brand.BrandProfileCreationCommand;
import com.nongthinh.profile_service.application.command.farmer.FarmerProfileCreationCommand;
import com.nongthinh.profile_service.application.port.in.admin.AdminProfileCreationUseCase;
import com.nongthinh.profile_service.application.port.in.brand.BrandProfileCreationUseCase;
import com.nongthinh.profile_service.application.port.in.farmer.FarmerProfileCreationUseCase;
import com.nongthinh.profile_service.application.port.in.registration.CompleteRegistrationProfileUseCase;
import com.nongthinh.profile_service.application.port.out.repository.AdminProfileRepository;
import com.nongthinh.profile_service.application.port.out.repository.BrandProfileRepository;
import com.nongthinh.profile_service.application.port.out.repository.FarmerProfileRepository;
import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompleteRegistrationProfileUseCaseImpl implements CompleteRegistrationProfileUseCase {
    private final AdminProfileCreationUseCase createAdmin;
    private final FarmerProfileCreationUseCase createFarmer;
    private final BrandProfileCreationUseCase createBrand;
    private final AdminProfileRepository admins;
    private final FarmerProfileRepository farmers;
    private final BrandProfileRepository brands;

    @Override
    public void execute(CompleteRegistrationProfileCommand command) {
        Objects.requireNonNull(command, "command is required");
        Objects.requireNonNull(command.userId(), "userId is required");
        if (exists(command)) return; // A retry never edits, reactivates or replaces an existing profile.
        try {
            switch (command.role()) {
                case "ROLE_ADMIN" -> createAdmin.execute(new AdminProfileCreationCommand(command.userId(),
                        command.firstName(), command.lastName(), command.phone(), null));
                case "ROLE_FARMER" -> createFarmer.execute(new FarmerProfileCreationCommand(command.userId(),
                        command.firstName(), command.lastName(), command.gender(), command.phone(), null, null, null, null));
                case "ROLE_BRAND", "ROLE_BRAND_PENDING" -> createBrand.execute(new BrandProfileCreationCommand(command.userId(),
                        command.brandName(), null, null, command.phone(), null, null, null,
                        command.representativeName(), command.representativePhone(), command.representativeEmail(), null, null, null));
                default -> throw new BusinessException(ErrorCode.FORBIDDEN);
            }
        } catch (BusinessException ex) {
            if (ex.getErrorCode() != ErrorCode.PROFILE_ALREADY_EXISTS || !exists(command)) throw ex;
        } catch (DataIntegrityViolationException ex) {
            // The repository transaction has rolled back. Only a concurrent insertion for this user is a success.
            if (!exists(command)) throw ex;
        }
    }

    private boolean exists(CompleteRegistrationProfileCommand command) {
        return switch (command.role()) {
            case "ROLE_ADMIN" -> admins.existsByUserId(command.userId());
            case "ROLE_FARMER" -> farmers.existsByUserId(command.userId());
            case "ROLE_BRAND", "ROLE_BRAND_PENDING" -> brands.existsByUserId(command.userId());
            default -> throw new BusinessException(ErrorCode.FORBIDDEN);
        };
    }
}
