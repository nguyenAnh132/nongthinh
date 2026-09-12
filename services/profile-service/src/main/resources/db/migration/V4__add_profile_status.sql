alter table farmer_profiles
    add column status varchar(20) not null default 'ACTIVE';

alter table admin_profiles
    add column status varchar(20) not null default 'ACTIVE';

alter table brand_profiles
    add column status varchar(30) not null default 'PENDING_APPROVAL';

alter table brand_profiles
    add column rejection_reason varchar(1000);

create index idx_farmer_profiles_status on farmer_profiles (status);
create index idx_admin_profiles_status on admin_profiles (status);
create index idx_brand_profiles_status on brand_profiles (status);
