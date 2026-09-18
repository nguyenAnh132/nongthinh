insert into system_param_types (name, description, system_defined, created_at, updated_at)
values ('Cấu hình tệp', 'Giới hạn kích thước tệp tải lên theo mục đích sử dụng', true, now(), now())
on conflict (name) do nothing;

insert into system_params (name, value, description, data_type, system_defined, type_id, created_at, updated_at)
select seed.name, seed.value, seed.description, 'INTEGER', true, param_type.id, now(), now()
from (values
    ('FILE_UPLOAD_MAX_BYTES_AVATAR', '2097152', 'Kích thước tối đa (byte) cho AVATAR'),
    ('FILE_UPLOAD_MAX_BYTES_BRAND_LOGO', '2097152', 'Kích thước tối đa (byte) cho BRAND_LOGO'),
    ('FILE_UPLOAD_MAX_BYTES_BRAND_BANNER', '5242880', 'Kích thước tối đa (byte) cho BRAND_BANNER'),
    ('FILE_UPLOAD_MAX_BYTES_BUSINESS_LICENSE', '10485760', 'Kích thước tối đa (byte) cho BUSINESS_LICENSE'),
    ('FILE_UPLOAD_MAX_BYTES_PRODUCT_IMAGE', '5242880', 'Kích thước tối đa (byte) cho PRODUCT_IMAGE'),
    ('FILE_UPLOAD_MAX_BYTES_DISEASE_IMAGE', '5242880', 'Kích thước tối đa (byte) cho DISEASE_IMAGE'),
    ('FILE_UPLOAD_MAX_BYTES_DIAGNOSIS_IMAGE', '5242880', 'Kích thước tối đa (byte) cho DIAGNOSIS_IMAGE'),
    ('FILE_UPLOAD_MAX_BYTES_POST_IMAGE', '5242880', 'Kích thước tối đa (byte) cho POST_IMAGE'),
    ('FILE_UPLOAD_MAX_BYTES_POST_VIDEO', '52428800', 'Kích thước tối đa (byte) cho POST_VIDEO'),
    ('FILE_UPLOAD_MAX_BYTES_MODEL_ARTIFACT', '52428800', 'Kích thước tối đa (byte) cho MODEL_ARTIFACT')
) as seed(name, value, description)
cross join system_param_types param_type
where param_type.name = 'Cấu hình tệp'
on conflict (name) do nothing;

alter table system_params add constraint ck_file_upload_max_bytes
check (name not in ('FILE_UPLOAD_MAX_BYTES_AVATAR', 'FILE_UPLOAD_MAX_BYTES_BRAND_LOGO', 'FILE_UPLOAD_MAX_BYTES_BRAND_BANNER', 'FILE_UPLOAD_MAX_BYTES_BUSINESS_LICENSE', 'FILE_UPLOAD_MAX_BYTES_PRODUCT_IMAGE', 'FILE_UPLOAD_MAX_BYTES_DISEASE_IMAGE', 'FILE_UPLOAD_MAX_BYTES_DIAGNOSIS_IMAGE', 'FILE_UPLOAD_MAX_BYTES_POST_IMAGE', 'FILE_UPLOAD_MAX_BYTES_POST_VIDEO', 'FILE_UPLOAD_MAX_BYTES_MODEL_ARTIFACT')
    or (data_type = 'INTEGER' and case when value ~ '^[0-9]{1,10}$'
        then cast(value as bigint) between 1 and 2147483647 else false end));

create table file_types (
    code varchar(20) primary key,
    content_type varchar(100) not null unique,
    extension varchar(10) not null
);

create table file_purpose_types (
    id uuid primary key,
    purpose varchar(30) not null,
    file_type_code varchar(20) not null references file_types(code),
    enabled boolean not null default true,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint uq_file_purpose_type unique (purpose, file_type_code),
    constraint ck_file_purpose check (purpose in ('AVATAR', 'BRAND_LOGO', 'BRAND_BANNER', 'BUSINESS_LICENSE', 'PRODUCT_IMAGE', 'DISEASE_IMAGE', 'DIAGNOSIS_IMAGE', 'POST_IMAGE', 'POST_VIDEO', 'MODEL_ARTIFACT'))
);

insert into file_types (code, content_type, extension) values
    ('JPEG', 'image/jpeg', 'jpg'),
    ('PNG', 'image/png', 'png'),
    ('WEBP', 'image/webp', 'webp'),
    ('PDF', 'application/pdf', 'pdf'),
    ('MP4', 'video/mp4', 'mp4'),
    ('WEBM', 'video/webm', 'webm'),
    ('MOV', 'video/quicktime', 'mov'),
    ('ONNX', 'application/octet-stream', 'onnx');

-- Only supported purpose/type pairs can be toggled through the API.
insert into file_purpose_types (id, purpose, file_type_code, enabled, created_at, updated_at) values
    ('60000000-0000-0000-0000-000000000001', 'AVATAR', 'JPEG', true, now(), now()),
    ('60000000-0000-0000-0000-000000000002', 'AVATAR', 'PNG', true, now(), now()),
    ('60000000-0000-0000-0000-000000000003', 'AVATAR', 'WEBP', true, now(), now()),
    ('60000000-0000-0000-0000-000000000004', 'BRAND_LOGO', 'JPEG', true, now(), now()),
    ('60000000-0000-0000-0000-000000000005', 'BRAND_LOGO', 'PNG', true, now(), now()),
    ('60000000-0000-0000-0000-000000000006', 'BRAND_LOGO', 'WEBP', true, now(), now()),
    ('60000000-0000-0000-0000-000000000007', 'BRAND_BANNER', 'JPEG', true, now(), now()),
    ('60000000-0000-0000-0000-000000000008', 'BRAND_BANNER', 'PNG', true, now(), now()),
    ('60000000-0000-0000-0000-000000000009', 'BRAND_BANNER', 'WEBP', true, now(), now()),
    ('60000000-0000-0000-0000-000000000010', 'BUSINESS_LICENSE', 'JPEG', true, now(), now()),
    ('60000000-0000-0000-0000-000000000011', 'BUSINESS_LICENSE', 'PNG', true, now(), now()),
    ('60000000-0000-0000-0000-000000000012', 'BUSINESS_LICENSE', 'WEBP', true, now(), now()),
    ('60000000-0000-0000-0000-000000000013', 'BUSINESS_LICENSE', 'PDF', true, now(), now()),
    ('60000000-0000-0000-0000-000000000014', 'PRODUCT_IMAGE', 'JPEG', true, now(), now()),
    ('60000000-0000-0000-0000-000000000015', 'PRODUCT_IMAGE', 'PNG', true, now(), now()),
    ('60000000-0000-0000-0000-000000000016', 'PRODUCT_IMAGE', 'WEBP', true, now(), now()),
    ('60000000-0000-0000-0000-000000000017', 'DISEASE_IMAGE', 'JPEG', true, now(), now()),
    ('60000000-0000-0000-0000-000000000018', 'DISEASE_IMAGE', 'PNG', true, now(), now()),
    ('60000000-0000-0000-0000-000000000019', 'DISEASE_IMAGE', 'WEBP', true, now(), now()),
    ('60000000-0000-0000-0000-000000000020', 'DIAGNOSIS_IMAGE', 'JPEG', true, now(), now()),
    ('60000000-0000-0000-0000-000000000021', 'DIAGNOSIS_IMAGE', 'PNG', true, now(), now()),
    ('60000000-0000-0000-0000-000000000022', 'DIAGNOSIS_IMAGE', 'WEBP', true, now(), now()),
    ('60000000-0000-0000-0000-000000000023', 'POST_IMAGE', 'JPEG', true, now(), now()),
    ('60000000-0000-0000-0000-000000000024', 'POST_IMAGE', 'PNG', true, now(), now()),
    ('60000000-0000-0000-0000-000000000025', 'POST_IMAGE', 'WEBP', true, now(), now()),
    ('60000000-0000-0000-0000-000000000026', 'POST_VIDEO', 'MP4', true, now(), now()),
    ('60000000-0000-0000-0000-000000000027', 'POST_VIDEO', 'WEBM', true, now(), now()),
    ('60000000-0000-0000-0000-000000000028', 'POST_VIDEO', 'MOV', true, now(), now()),
    ('60000000-0000-0000-0000-000000000029', 'MODEL_ARTIFACT', 'ONNX', true, now(), now());
