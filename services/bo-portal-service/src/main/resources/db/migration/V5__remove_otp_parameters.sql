delete from system_params
where name in (
    'OTP_LENGTH',
    'OTP_EXPIRE_MINUTES',
    'OTP_RESEND_COOLDOWN_SECONDS',
    'OTP_MAX_RESEND',
    'OTP_MAX_ATTEMPTS'
);
