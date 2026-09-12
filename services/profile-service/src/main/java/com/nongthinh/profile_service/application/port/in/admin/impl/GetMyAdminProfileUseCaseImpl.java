package com.nongthinh.profile_service.application.port.in.admin.impl;

import org.springframework.stereotype.Service;
import com.nongthinh.profile_service.application.port.in.admin.GetMyAdminProfileUseCase;
import com.nongthinh.profile_service.application.port.out.repository.AdminProfileRepository;
import com.nongthinh.profile_service.application.view.AdminProfileView;
import com.nongthinh.profile_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetMyAdminProfileUseCaseImpl implements GetMyAdminProfileUseCase {

    private final CurrentUserProvider currentUserProvider;
    private final AdminProfileRepository adminProfileRepository;

    @Override
    public AdminProfileView execute() {
        var userId = currentUserProvider.getCurrentUser().getUserId();
        return adminProfileRepository.findByUserId(userId)
                .map(AdminProfileView::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
    }
}
