package com.nongthinh.profile_service.application.port.in.admin;

import java.util.List;
import com.nongthinh.profile_service.application.view.BrandProfileView;

public interface ListBrandProfilesForAdminUseCase {

    List<BrandProfileView> execute(List<String> statuses);
}
