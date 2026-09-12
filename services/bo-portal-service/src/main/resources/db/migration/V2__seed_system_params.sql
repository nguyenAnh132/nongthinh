insert into system_params (name, value, description, data_type, system_defined, created_at, updated_at) values
('OTP_EXPIRE_MINUTES', '5', 'Thoi gian hieu luc cua ma OTP dang ky (phut)', 'INTEGER', true, now(), now()),
('OTP_LENGTH', '6', 'Do dai cua ma OTP', 'INTEGER', true, now(), now()),
('CORS_ALLOWED_ORIGINS', 'http://localhost:3000,http://localhost:5173', 'Danh sach origin duoc phep goi qua api-gateway, ngan cach boi dau phay', 'STRING', true, now(), now());

select setval(pg_get_serial_sequence('system_params', 'id'), (select max(id) from system_params));
