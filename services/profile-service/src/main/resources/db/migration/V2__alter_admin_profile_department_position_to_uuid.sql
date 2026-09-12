alter table admin_profiles
    drop column if exists department;

alter table admin_profiles
    drop column if exists position;

alter table admin_profiles
    add column department_id uuid;

alter table admin_profiles
    add column position_id uuid;

create index idx_admin_profiles_department_id
    on admin_profiles(department_id);

create index idx_admin_profiles_position_id
    on admin_profiles(position_id);
