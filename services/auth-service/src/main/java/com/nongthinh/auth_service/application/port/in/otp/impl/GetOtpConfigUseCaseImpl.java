package com.nongthinh.auth_service.application.port.in.otp.impl;

import org.springframework.stereotype.Service;
import com.nongthinh.auth_service.application.port.in.otp.GetOtpConfigUseCase;
import com.nongthinh.auth_service.application.port.out.SystemParam;
import com.nongthinh.auth_service.application.view.OtpConfigView;
import com.nongthinh.auth_service.common.constant.DefaultParamValueConstant;
import com.nongthinh.auth_service.common.constant.SystemParamNameConstant;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetOtpConfigUseCaseImpl implements GetOtpConfigUseCase {

    private final SystemParam systemParam;

    @Override
    public OtpConfigView execute() {
        int otpLength = systemParam.getInt(
                SystemParamNameConstant.OTP_LENGTH,
                DefaultParamValueConstant.DEFAULT_OTP_LENGTH);
        int cooldownSeconds = systemParam.getInt(
                SystemParamNameConstant.OTP_RESEND_COOLDOWN_SECONDS,
                DefaultParamValueConstant.DEFAULT_OTP_RESEND_COOLDOWN_SECONDS);
        return new OtpConfigView(otpLength, cooldownSeconds);
    }
}
