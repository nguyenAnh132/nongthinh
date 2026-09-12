alter table brand_profiles
    add column scheduled_deletion_at timestamptz;

alter table brand_profiles
    add column rejected_at timestamptz;

alter table brand_profiles
    add column approved_at timestamptz;

alter table brand_profiles
    add column approved_by uuid;

alter table brand_profiles
    add column rejected_by uuid;

create index idx_brand_profiles_scheduled_deletion_at
    on brand_profiles (scheduled_deletion_at)
    where scheduled_deletion_at is not null;
