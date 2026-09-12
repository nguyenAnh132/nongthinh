package com.nongthinh.auth_service.application.view;

import java.time.Instant;

public record MeFlags(
    boolean requiresEmailVerification,
    boolean requiresProfileCompletion,
    boolean brandRejected,
    Instant canReRegisterAt
) {

}
