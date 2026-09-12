package com.nongthinh.profile_service.application.port.in.admin.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import com.nongthinh.profile_service.application.port.in.admin.ListBrandProfilesForAdminUseCase;
import com.nongthinh.profile_service.application.port.out.repository.BrandProfileRepository;
import com.nongthinh.profile_service.application.view.BrandProfileView;
import com.nongthinh.profile_service.domain.brandprofile.BrandProfile;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListBrandProfilesForAdminUseCaseImpl implements ListBrandProfilesForAdminUseCase {

    private final BrandProfileRepository brandProfileRepository;

    @Override
    public List<BrandProfileView> execute(List<String> statuses) {
        return brandProfileRepository.findAllByStatusIn(statuses)
                .stream()
                .map(BrandProfileView::from)
                .toList();
    }
}
