create table users (
    id uuid primary key,

    keycloak_id uuid unique,

    email varchar(255) not null unique,

    password_hash varchar(255),

    enabled boolean not null default true,

    email_verified boolean not null default false,

    auth_provider varchar(30) not null default 'LOCAL',

    password_updated_at timestamptz,

    deleted_at timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create index idx_users_email on users (email);
create index idx_users_keycloak_id on users (keycloak_id);
create index idx_users_enabled on users (enabled);
create index idx_users_deleted_at on users (deleted_at);