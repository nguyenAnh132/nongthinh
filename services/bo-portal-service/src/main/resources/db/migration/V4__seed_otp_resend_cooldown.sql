insert into system_params (name, value, description, data_type, system_defined, type_id, created_at, updated_at)
values (
    'OTP_RESEND_COOLDOWN_SECONDS',
    '60',
    'Thoi gian cho phep nguoi dung bam gui lai ma OTP (giay)',
    'INTEGER',
    true,
    (select id from system_param_types where name = 'Khác'),
    now(),
    now()
);

select setval(pg_get_serial_sequence('system_params', 'id'), (select max(id) from system_params));
