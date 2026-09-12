package com.nongthinh.auth_service.common.cookie;

import jakarta.servlet.http.HttpServletResponse;

public interface CookieWriter {

    void setTokenToCookie(HttpServletResponse response, TokenCookie tokenCookie);

    void clearTokenFromCookie(HttpServletResponse response);

}
