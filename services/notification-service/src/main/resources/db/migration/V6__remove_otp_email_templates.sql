
delete from email_histories
where purpose_id in (
    select id from email_template_purposes
    where code in ('REGISTER_OTP', 'EMAIL_VERIFICATION')
)
or template_id in (
    select t.id from email_templates t
    join email_template_purposes p on p.id = t.purpose_id
    where p.code in ('REGISTER_OTP', 'EMAIL_VERIFICATION')
);

delete from email_template_variables
where purpose_id in (
    select id from email_template_purposes
    where code in ('REGISTER_OTP', 'EMAIL_VERIFICATION')
);

delete from email_templates
where purpose_id in (
    select id from email_template_purposes
    where code in ('REGISTER_OTP', 'EMAIL_VERIFICATION')
);

delete from email_template_purposes
where code in ('REGISTER_OTP', 'EMAIL_VERIFICATION');
