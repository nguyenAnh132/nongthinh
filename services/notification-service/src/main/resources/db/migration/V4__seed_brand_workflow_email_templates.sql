insert into email_template_purposes (id, code, name, description, system_defined, created_at, updated_at) values
(7, 'BRAND_NEEDS_REVISION', 'Yêu cầu bổ sung hồ sơ Brand', 'Thông báo Brand cần bổ sung hồ sơ hoặc giấy phép kinh doanh', true, now(), now()),
(8, 'BRAND_DOCUMENTS_REQUESTED', 'Yêu cầu nộp giấy phép kinh doanh', 'Hướng dẫn Brand upload giấy phép kinh doanh sau xác minh điện thoại', true, now(), now());

insert into email_template_variables (id, purpose_id, variable_name, description, example_value, is_required, created_at, updated_at) values
(16, 5, 'can_re_register_at', 'Ngày được đăng ký lại', '2026-07-18', false, now(), now()),
(17, 7, 'brand_name', 'Tên thương hiệu', 'Nông Thịnh Organic', true, now(), now()),
(18, 7, 'user_name', 'Tên người đại diện', 'Nguyễn Văn A', true, now(), now()),
(19, 7, 'revision_reason', 'Lý do yêu cầu bổ sung', 'Giấy phép kinh doanh chưa rõ', true, now(), now()),
(20, 8, 'brand_name', 'Tên thương hiệu', 'Nông Thịnh Organic', true, now(), now()),
(21, 8, 'user_name', 'Tên người đại diện', 'Nguyễn Văn A', true, now(), now());

insert into email_templates (id, purpose_id, name, description, subject, html_content, text_content, is_active, created_at, updated_at) values
(7, 7, 'BRAND_NEEDS_REVISION v1', 'Template mặc định yêu cầu bổ sung hồ sơ Brand',
 'Nong Thinh - Yeu cau bo sung ho so thuong hieu',
 '<p>Xin chao {{user_name}},</p><p>Thuong hieu <strong>{{brand_name}}</strong> can bo sung ho so. Ly do: {{revision_reason}}</p><p>Vui long dang nhap portal de cap nhat.</p>',
 'Xin chao {{user_name}}, thuong hieu {{brand_name}} can bo sung ho so. Ly do: {{revision_reason}}.',
 true, now(), now()),

(8, 8, 'BRAND_DOCUMENTS_REQUESTED v1', 'Template mặc định yêu cầu nộp giấy phép kinh doanh',
 'Nong Thinh - Vui long nop giay phep kinh doanh',
 '<p>Xin chao {{user_name}},</p><p>Thuong hieu <strong>{{brand_name}}</strong> da duoc xac minh qua dien thoai. Vui long dang nhap portal de upload anh giay phep kinh doanh.</p>',
 'Xin chao {{user_name}}, thuong hieu {{brand_name}} da duoc xac minh. Vui long dang nhap portal de upload giay phep kinh doanh.',
 true, now(), now());

select setval(pg_get_serial_sequence('email_template_purposes', 'id'), (select max(id) from email_template_purposes));
select setval(pg_get_serial_sequence('email_template_variables', 'id'), (select max(id) from email_template_variables));
select setval(pg_get_serial_sequence('email_templates', 'id'), (select max(id) from email_templates));
