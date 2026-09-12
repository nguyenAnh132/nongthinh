create table brand_verification_logs (
    id uuid primary key,
    brand_profile_id uuid not null
        references brand_profiles (id),
    admin_user_id uuid not null,
    phone_called varchar(20) not null,
    result varchar(30) not null,
    note varchar(1000),
    verified_at timestamptz not null,
    created_at timestamptz not null
);

create index idx_brand_verification_logs_brand_profile_id
    on brand_verification_logs (brand_profile_id);

create index idx_brand_verification_logs_verified_at
    on brand_verification_logs (verified_at);

create table brand_documents (
    id uuid primary key,
    brand_profile_id uuid not null
        references brand_profiles (id),
    business_license_url varchar(500) not null,
    review_status varchar(30) not null,
    reviewed_by uuid,
    reviewed_at timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create index idx_brand_documents_brand_profile_id
    on brand_documents (brand_profile_id);

create index idx_brand_documents_review_status
    on brand_documents (brand_profile_id, review_status);

create table brand_lifecycle_logs (
    id uuid primary key,
    brand_profile_id uuid not null
        references brand_profiles (id),
    action varchar(50) not null,
    actor_user_id uuid,
    from_status varchar(30),
    to_status varchar(30),
    payload_json jsonb,
    created_at timestamptz not null
);

create index idx_brand_lifecycle_logs_brand_profile_id
    on brand_lifecycle_logs (brand_profile_id);

create index idx_brand_lifecycle_logs_created_at
    on brand_lifecycle_logs (brand_profile_id, created_at);