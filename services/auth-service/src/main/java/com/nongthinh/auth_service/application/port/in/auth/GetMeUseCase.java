package com.nongthinh.auth_service.application.port.in.auth;

import com.nongthinh.auth_service.application.view.MeView;
import java.util.Set;
import java.util.UUID;

public interface GetMeUseCase {

    MeView execute(com.nongthinh.auth_service.application.view.RegistrationPrincipal principal);

    MeView execute(UUID userId, Set<String> roles, String adminGroup, Set<String> permissions);

}
