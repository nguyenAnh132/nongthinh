package com.nongthinh.profile_service.application.port.in.admin.impl;

import java.util.UUID;
import org.springframework.stereotype.Service;
import com.nongthinh.profile_service.application.port.in.admin.GetAdminBrandProfileDetailUseCase;
import com.nongthinh.profile_service.application.port.out.repository.BrandDocumentRepository;
import com.nongthinh.profile_service.application.port.out.repository.BrandLifecycleLogRepository;
import com.nongthinh.profile_service.application.port.out.repository.BrandProfileRepository;
import com.nongthinh.profile_service.application.port.out.repository.BrandVerificationLogRepository;
import com.nongthinh.profile_service.application.view.AdminBrandProfileDetailView;
import com.nongthinh.profile_service.application.view.BrandDocumentView;
import com.nongthinh.profile_service.application.view.BrandLifecycleLogView;
import com.nongthinh.profile_service.application.view.BrandProfileView;
import com.nongthinh.profile_service.application.view.BrandVerificationLogView;
import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.brandprofile.BrandProfile;
import com.nongthinh.profile_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetAdminBrandProfileDetailUseCaseImpl implements GetAdminBrandProfileDetailUseCase {

    private final BrandProfileRepository brandProfileRepository;
    private final BrandDocumentRepository brandDocumentRepository;
    private final BrandVerificationLogRepository brandVerificationLogRepository;
    private final BrandLifecycleLogRepository brandLifecycleLogRepository;

    @Override
    public AdminBrandProfileDetailView execute(UUID id) {
        BrandProfile profile = brandProfileRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));

        return new AdminBrandProfileDetailView(
                BrandProfileView.from(profile),
                brandDocumentRepository.findAllByBrandProfileIdOrderByCreatedAtDesc(id)
                        .stream()
                        .map(BrandDocumentView::from)
                        .toList(),
                brandVerificationLogRepository.findAllByBrandProfileIdOrderByVerifiedAtDesc(id)
                        .stream()
                        .map(BrandVerificationLogView::from)
                        .toList(),
                brandLifecycleLogRepository.findAllByBrandProfileIdOrderByCreatedAtDesc(id)
                        .stream()
                        .map(BrandLifecycleLogView::from)
                        .toList()
        );
    }
}
