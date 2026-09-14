create table email_otps (
    id uuid primary key,
    user_id uuid not null unique references users (id),
    email varchar(255) not null unique,
    otp_hash varchar(255) not null,
    attempt_count integer not null default 0 check (attempt_count >= 0),
    resend_count integer not null default 0 check (resend_count >= 0),
    last_sent_at timestamptz not null,
    expires_at timestamptz not null,
    consumed_at timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint chk_email_otp_expiry check (expires_at > last_sent_at),
    constraint chk_email_otp_consumed check (consumed_at is null or consumed_at >= last_sent_at)
);

create index idx_email_otps_expires_at on email_otps (expires_at);
