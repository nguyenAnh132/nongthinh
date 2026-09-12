create table stored_files (
    id uuid primary key,
    owner_user_id uuid not null,
    purpose varchar(50) not null,
    storage_provider varchar(20) not null default 'LOCAL',
    bucket varchar(100) not null,
    object_key varchar(500) not null,
    original_file_name varchar(255) not null,
    content_type varchar(100) not null,
    size_bytes bigint not null,
    public_url varchar(500) not null,
    status varchar(20) not null default 'UPLOADED',
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create index idx_stored_files_owner_user_id
    on stored_files (owner_user_id);

create index idx_stored_files_owner_purpose
    on stored_files (owner_user_id, purpose);

create unique index uk_stored_files_bucket_object_key
    on stored_files (bucket, object_key);
