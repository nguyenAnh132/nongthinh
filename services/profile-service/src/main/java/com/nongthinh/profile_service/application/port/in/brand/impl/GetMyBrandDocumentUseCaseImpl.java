package com.nongthinh.profile_service.application.port.in.brand.impl;

import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import com.nongthinh.profile_service.application.port.in.brand.GetMyBrandDocumentUseCase;
import com.nongthinh.profile_service.application.port.out.repository.BrandDocumentRepository;
import com.nongthinh.profile_service.application.port.out.repository.BrandProfileRepository;
import com.nongthinh.profile_service.application.view.BrandDocumentView;
import com.nongthinh.profile_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.brandprofile.BrandProfile;
import com.nongthinh.profile_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetMyBrandDocumentUseCaseImpl implements GetMyBrandDocumentUseCase {

    private final CurrentUserProvider currentUserProvider;
    private final BrandProfileRepository brandProfileRepository;
    private final BrandDocumentRepository brandDocumentRepository;

    @Override
    public Optional<BrandDocumentView> execute() {
        UUID userId = currentUserProvider.getCurrentUser().getUserId();
        BrandProfile profile = brandProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));

        return brandDocumentRepository.findLatestByBrandProfileId(profile.getId())
                .map(BrandDocumentView::from);
    }
}
