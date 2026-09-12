package com.nongthinh.profile_service.application.port.in.admin.impl;

import org.springframework.stereotype.Service;

import com.nongthinh.profile_service.application.command.admin.AdminProfileCreationCommand;
import com.nongthinh.profile_service.application.port.in.admin.AdminProfileCreationUseCase;
import com.nongthinh.profile_service.application.port.in.admin.CreateMyAdminProfileUseCase;
import com.nongthinh.profile_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.profile_service.presentation.dto.request.me.MyAdminProfileCreationRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateMyAdminProfileUseCaseImpl implements CreateMyAdminProfileUseCase {

    private final CurrentUserProvider currentUserProvider;
    private final AdminProfileCreationUseCase adminProfileCreationUseCase;

    @Override
    public void execute(MyAdminProfileCreationRequest request) {
        var userId = currentUserProvider.getCurrentUser().getUserId();
        adminProfileCreationUseCase.execute(new AdminProfileCreationCommand(
                userId,
                request.firstName(),
                request.lastName(),
                request.phone(),
                request.avatarUrl()
        ));
    }
}
