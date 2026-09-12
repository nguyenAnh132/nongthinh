create table farmer_profiles (
    id uuid primary key,
    user_id uuid not null,

    first_name varchar(255) not null,
    last_name varchar(255) not null,
    gender varchar(20) not null,
    phone varchar(20) not null,

    province_id varchar(10),
    commune_id uuid,
    address_detail varchar(500),

    avatar_url varchar(255),

    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table admin_profiles (
    id uuid primary key,
    user_id uuid not null,

    person_name varchar(255) not null,
    phone varchar(20) not null,
    department varchar(255),
    position varchar(255),
    avatar_url varchar(255),

    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table brand_profiles (
    id uuid primary key,
    user_id uuid not null,

    brand_name varchar(255) not null,
    description varchar(1000),
    phone varchar(20) not null,

    office_province_id varchar(10),
    office_commune_id uuid,
    office_address_detail varchar(500),

    representative_name varchar(255) not null,
    representative_phone varchar(20) not null,
    representative_email varchar(255) not null,

    logo_url varchar(255),
    banner_url varchar(255),
    website_url varchar(255),

    created_at timestamptz not null,
    updated_at timestamptz not null
);

create unique index uk_farmer_profiles_user_id
    on farmer_profiles(user_id);

create index idx_farmer_profiles_province_id
    on farmer_profiles(province_id);

create index idx_farmer_profiles_commune_id
    on farmer_profiles(commune_id);

create unique index uk_admin_profiles_user_id
    on admin_profiles(user_id);

create unique index uk_brand_profiles_user_id
    on brand_profiles(user_id);

create index idx_brand_profiles_office_province_id
    on brand_profiles(office_province_id);

create index idx_brand_profiles_office_commune_id
    on brand_profiles(office_commune_id);
