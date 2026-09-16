-- Keycloak owns credentials and account status. Keep local identity/email and
-- password_updated_at for password changes observed by the backend in future.
-- Existing timestamps are preserved; they are not a current Keycloak audit log.
alter table users
    drop column password_hash,
    drop column enabled,
    drop column auth_provider;

comment on column users.password_updated_at is
    'Last password change observed by auth-service; nullable and not synchronized from Keycloak. Legacy values are preserved.';
