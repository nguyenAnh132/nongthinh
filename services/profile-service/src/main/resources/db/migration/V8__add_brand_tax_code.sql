alter table brand_profiles
    add column tax_code varchar(20);

create index idx_brand_profiles_tax_code
    on brand_profiles (tax_code);
