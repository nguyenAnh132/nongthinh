create table brand_approval_processes (
    id uuid primary key,
    brand_profile_id uuid not null,
    camunda_process_instance_id varchar(64) not null,
    camunda_business_key varchar(128) not null,
    status varchar(30) not null,
    assigned_reviewer_id uuid,
    started_at timestamptz not null,
    completed_at timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create unique index uk_brand_approval_processes_brand_profile_id
    on brand_approval_processes (brand_profile_id);

create unique index uk_brand_approval_processes_instance_id
    on brand_approval_processes (camunda_process_instance_id);

create unique index uk_brand_approval_processes_business_key
    on brand_approval_processes (camunda_business_key);

create index idx_brand_approval_processes_status
    on brand_approval_processes (status);
