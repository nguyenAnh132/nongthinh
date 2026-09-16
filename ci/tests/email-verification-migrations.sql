\set ON_ERROR_STOP on
begin;

create schema test_email_auth;
set search_path to test_email_auth;
\ir ../../services/auth-service/src/main/resources/db/migration/V1__create_identity_tables.sql
\ir ../../services/auth-service/src/main/resources/db/migration/V2__create_email_otps.sql
insert into users (id, email, email_verified, created_at, updated_at)
values ('00000000-0000-0000-0000-000000000001', 'migration@example.com', true, now(), now());
insert into email_otps (id, user_id, email, otp_hash, last_sent_at, expires_at, created_at, updated_at)
values ('00000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000001',
        'migration@example.com', 'hash', now(), now() + interval '5 minutes', now(), now());
\ir ../../services/auth-service/src/main/resources/db/migration/V3__remove_local_email_verification.sql
do $$ begin
    assert to_regclass('email_otps') is null, 'OTP table still exists';
    assert not exists (select 1 from information_schema.columns
        where table_schema = 'test_email_auth' and table_name = 'users' and column_name = 'email_verified');
    assert (select count(*) from users) = 1, 'User data must be preserved';
end $$;

create schema test_email_bo;
set search_path to test_email_bo;
\ir ../../services/bo-portal-service/src/main/resources/db/migration/V1__create_system_params.sql
\ir ../../services/bo-portal-service/src/main/resources/db/migration/V2__seed_system_params.sql
\ir ../../services/bo-portal-service/src/main/resources/db/migration/V3__create_system_param_types.sql
\ir ../../services/bo-portal-service/src/main/resources/db/migration/V4__seed_otp_resend_cooldown.sql
insert into system_params (name, value, description, data_type, system_defined, type_id, created_at, updated_at)
select name, '5', 'Legacy attempt/resend limits', 'INTEGER', true,
       (select min(id) from system_param_types), now(), now()
from (values ('OTP_MAX_ATTEMPTS'), ('OTP_MAX_RESEND')) as params(name);
\ir ../../services/bo-portal-service/src/main/resources/db/migration/V5__remove_otp_parameters.sql
do $$ begin
    assert not exists (select 1 from system_params where name like 'OTP_%');
    assert (select count(*) from system_params where name = 'CORS_ALLOWED_ORIGINS') = 1;
    assert (select count(*) from system_param_types) = 1;
end $$;

create schema test_email_notifications;
set search_path to test_email_notifications;
\ir ../../services/notification-service/src/main/resources/db/migration/V1__create_email_template_table.sql
\ir ../../services/notification-service/src/main/resources/db/migration/V2__seed_email_template_data.sql
\ir ../../services/notification-service/src/main/resources/db/migration/V3__create_email_histories_table.sql
\ir ../../services/notification-service/src/main/resources/db/migration/V4__seed_brand_workflow_email_templates.sql
\ir ../../services/notification-service/src/main/resources/db/migration/V5__in_app_notifications.sql
-- Include inactive/custom OTP templates and history, not just seeded IDs.
insert into email_templates (purpose_id, name, subject, is_active, created_at, updated_at)
select id, 'Custom verification template', 'Custom subject', false, now(), now()
from email_template_purposes where code = 'REGISTER_OTP';
insert into email_histories (template_id, purpose_id, status, send_at)
select id, purpose_id, 'SENT', now() from email_templates;
create temporary table expected_history as
select h.* from email_histories h join email_template_purposes p on p.id = h.purpose_id
where p.code not in ('REGISTER_OTP', 'EMAIL_VERIFICATION');
create temporary table expected_templates as
select t.* from email_templates t join email_template_purposes p on p.id = t.purpose_id
where p.code not in ('REGISTER_OTP', 'EMAIL_VERIFICATION');
\ir ../../services/notification-service/src/main/resources/db/migration/V6__remove_otp_email_templates.sql
do $$ begin
    assert not exists (select 1 from email_template_purposes where code in ('REGISTER_OTP', 'EMAIL_VERIFICATION'));
    assert not exists (select 1 from email_template_variables where variable_name = 'otp_code');
    assert not exists ((select * from email_histories except select * from expected_history)
        union all (select * from expected_history except select * from email_histories));
    assert not exists ((select * from email_templates except select * from expected_templates)
        union all (select * from expected_templates except select * from email_templates));
    assert to_regclass('notifications') is not null;
end $$;

rollback;
\echo 'Email verification migration checks passed; all test data rolled back.'
