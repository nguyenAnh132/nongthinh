package com.nongthinh.auth_service.application.port.in.auth;

import com.nongthinh.auth_service.application.view.RefreshTokenView;

public interface RefreshToken {

    RefreshTokenView execute(String refreshToken);

}
