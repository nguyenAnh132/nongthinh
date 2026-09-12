alter table admin_profiles
    drop column if exists person_name;

alter table admin_profiles
    add column first_name varchar(255) not null;

alter table admin_profiles
    add column last_name varchar(255) not null;
