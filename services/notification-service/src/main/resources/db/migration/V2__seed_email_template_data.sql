
insert into email_template_purposes (id, code, name, description, system_defined, created_at, updated_at) values
(1, 'REGISTER_OTP', 'OTP đăng ký', 'Gửi mã OTP khi đăng ký tài khoản', true, now(), now()),
(2, 'RESET_PASSWORD', 'Đặt lại mật khẩu', 'Gửi hướng dẫn hoặc OTP đặt lại mật khẩu', true, now(), now()),
(3, 'EMAIL_VERIFICATION', 'Xác thực email', 'Gửi mã OTP xác thực email', true, now(), now()),
(4, 'BRAND_APPROVED', 'Duyệt thương hiệu', 'Thông báo thương hiệu được duyệt', true, now(), now()),
(5, 'BRAND_REJECTED', 'Từ chối thương hiệu', 'Thông báo thương hiệu bị từ chối', true, now(), now()),
(6, 'WELCOME', 'Chào mừng', 'Email chào mừng người dùng mới', true, now(), now());


insert into email_template_variables (id, purpose_id, variable_name, description, example_value, is_required, created_at, updated_at) values
(1, 1, 'user_name', 'Tên người dùng', 'Nguyễn Văn A', true, now(), now()),
(2, 1, 'otp_code', 'Mã OTP', '123456', true, now(), now()),
(3, 1, 'expire_minutes', 'Số phút hết hạn', '5', true, now(), now()),

(4, 2, 'user_name', 'Tên người dùng', 'Nguyễn Văn A', true, now(), now()),
(5, 2, 'reset_link', 'Liên kết đặt lại mật khẩu', 'https://nongthinh.vn/reset?token=abc', true, now(), now()),
(6, 2, 'expire_minutes', 'Số phút hết hạn', '30', true, now(), now()),

(7, 3, 'user_name', 'Tên người dùng', 'Nguyễn Văn A', false, now(), now()),
(8, 3, 'otp_code', 'Mã OTP', '123456', true, now(), now()),
(9, 3, 'expire_minutes', 'Số phút hết hạn', '5', true, now(), now()),

(10, 4, 'brand_name', 'Tên thương hiệu', 'Nông Thịnh Organic', true, now(), now()),
(11, 4, 'user_name', 'Tên người đại diện', 'Nguyễn Văn A', true, now(), now()),

(12, 5, 'brand_name', 'Tên thương hiệu', 'Nông Thịnh Organic', true, now(), now()),
(13, 5, 'user_name', 'Tên người đại diện', 'Nguyễn Văn A', true, now(), now()),
(14, 5, 'reject_reason', 'Lý do từ chối', 'Hồ sơ chưa đầy đủ', true, now(), now()),

(15, 6, 'user_name', 'Tên người dùng', 'Nguyễn Văn A', true, now(), now());


insert into email_templates (id, purpose_id, name, description, subject, html_content, text_content, is_active, created_at, updated_at) values
(1, 1, 'REGISTER_OTP v1', 'Template mặc định OTP đăng ký',
 'Nong Thinh - Ma OTP dang ky',
 '<h2>Xac thuc dang ky</h2><p>Xin chao {{user_name}},</p><p>Ma OTP cua ban la:</p><h1>{{otp_code}}</h1><p>Ma co hieu luc trong {{expire_minutes}} phut.</p>',
 'Xin chao {{user_name}}, Ma OTP cua ban la: {{otp_code}}. Ma co hieu luc trong {{expire_minutes}} phut.',
 true, now(), now()),

(2, 2, 'RESET_PASSWORD v1', 'Template mặc định đặt lại mật khẩu',
 'Nong Thinh - Dat lai mat khau',
 '<p>Xin chao {{user_name}},</p><p>Nhan vao lien ket sau de dat lai mat khau: <a href="{{reset_link}}">{{reset_link}}</a></p><p>Lien ket co hieu luc trong {{expire_minutes}} phut.</p>',
 'Xin chao {{user_name}}, truy cap {{reset_link}} de dat lai mat khau. Lien ket co hieu luc trong {{expire_minutes}} phut.',
 true, now(), now()),

(3, 3, 'EMAIL_VERIFICATION v1', 'Template mặc định xác thực email',
 'Nong Thinh - Ma OTP xac thuc email',
 '<h2>Xac thuc email</h2><p>Ma OTP cua ban la:</p><h1>{{otp_code}}</h1><p>Ma co hieu luc trong {{expire_minutes}} phut. Vui long khong chia se ma nay.</p>',
 'Ma OTP xac thuc email cua ban la: {{otp_code}}. Ma co hieu luc trong {{expire_minutes}} phut.',
 true, now(), now()),

(4, 4, 'BRAND_APPROVED v1', 'Template mặc định duyệt thương hiệu',
 'Nong Thinh - Thuong hieu da duoc duyet',
 '<p>Xin chao {{user_name}},</p><p>Thuong hieu <strong>{{brand_name}}</strong> da duoc duyet.</p>',
 'Xin chao {{user_name}}, thuong hieu {{brand_name}} da duoc duyet.',
 true, now(), now()),

(5, 5, 'BRAND_REJECTED v1', 'Template mặc định từ chối thương hiệu',
 'Nong Thinh - Thuong hieu bi tu choi',
 '<p>Xin chao {{user_name}},</p><p>Thuong hieu <strong>{{brand_name}}</strong> bi tu choi. Ly do: {{reject_reason}}</p>',
 'Xin chao {{user_name}}, thuong hieu {{brand_name}} bi tu choi. Ly do: {{reject_reason}}.',
 true, now(), now()),

(6, 6, 'WELCOME v1', 'Template mặc định chào mừng',
 'Chao mung den voi Nong Thinh',
 '<p>Xin chao {{user_name}},</p><p>Chao mung ban den voi Nong Thinh!</p>',
 'Xin chao {{user_name}}, chao mung ban den voi Nong Thinh!',
 true, now(), now());

select setval(pg_get_serial_sequence('email_template_purposes', 'id'), (select max(id) from email_template_purposes));
select setval(pg_get_serial_sequence('email_template_variables', 'id'), (select max(id) from email_template_variables));
select setval(pg_get_serial_sequence('email_templates', 'id'), (select max(id) from email_templates));
