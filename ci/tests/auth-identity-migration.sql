\set ON_ERROR_STOP on
begin;

create schema test_auth_identity;
set search_path to test_auth_identity;
\ir ../../services/auth-service/src/main/resources/db/migration/V1__create_identity_tables.sql
\ir ../../services/auth-service/src/main/resources/db/migration/V2__create_email_otps.sql
\ir ../../services/auth-service/src/main/resources/db/migration/V3__remove_local_email_verification.sql

-- Simulate an already migrated database, including disabled/deleted accounts
-- and both known and unknown historical password update times.
insert into users (id, keycloak_id, email, password_hash, enabled, auth_provider,
                   password_updated_at, created_at, updated_at, deleted_at)
values
('00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001',
 'farmer@example.com', 'legacy-hash', true, 'LOCAL',
 '2026-09-01 10:00:00+00', '2026-08-01 10:00:00+00', '2026-09-01 10:00:00+00', null),
('00000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002',
 'brand@example.com', 'legacy-hash', false, 'LOCAL',
 '2026-08-02 10:00:00+00', '2026-08-02 10:00:00+00', '2026-09-02 10:00:00+00', '2026-09-02 10:00:00+00'),
('00000000-0000-0000-0000-000000000003', null,
 'external@example.com', null, true, 'GOOGLE',
 null, '2026-08-03 10:00:00+00', '2026-08-03 10:00:00+00', null);

create temporary table expected_users as
select id, keycloak_id, email, password_updated_at, created_at, updated_at, deleted_at from users;

\ir ../../services/auth-service/src/main/resources/db/migration/V4__remove_local_credentials_and_account_status.sql

do $$ begin
    assert not exists (
        select 1 from information_schema.columns
        where table_schema = 'test_auth_identity' and table_name = 'users'
          and column_name in ('password_hash', 'enabled', 'email_verified', 'auth_provider')
    ), 'Local credential/status columns still exist';
    assert not exists (
        (select id, keycloak_id, email, password_updated_at, created_at, updated_at, deleted_at
         from users except select * from expected_users)
        union all (select * from expected_users except
         select id, keycloak_id, email, password_updated_at, created_at, updated_at, deleted_at from users)
    ), 'Identity, email, or historical timestamp data changed';
    assert to_regclass('idx_users_enabled') is null;
    assert to_regclass('idx_users_email') is not null;
    assert to_regclass('idx_users_keycloak_id') is not null;
end $$;

-- New registrations need no local credential or account-status fields.
insert into users (id, keycloak_id, email, created_at, updated_at)
values ('00000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000004',
        'new@example.com', now(), now());
do $$ begin
    assert (select password_updated_at is null from users where email = 'new@example.com');
    begin
        insert into users (id, email, created_at, updated_at)
        values ('00000000-0000-0000-0000-000000000005', 'new@example.com', now(), now());
        raise exception 'Email uniqueness constraint was lost';
    exception when unique_violation then null;
    end;
end $$;

rollback;
\echo 'Auth identity migration checks passed; all test data rolled back.'
