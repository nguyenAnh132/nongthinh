alter table stored_files
    add column visibility varchar(20);

update stored_files
set visibility = 'PUBLIC'
where visibility is null;

alter table stored_files
    alter column visibility set default 'PUBLIC';

alter table stored_files
    alter column visibility set not null;

alter table stored_files
    alter column public_url drop not null;

alter table stored_files
    add constraint chk_stored_files_visibility
        check (visibility in ('PUBLIC', 'PRIVATE'));

alter table stored_files
    add constraint chk_stored_files_visibility_public_url
        check (
            (visibility = 'PUBLIC' and public_url is not null)
            or (visibility = 'PRIVATE' and public_url is null)
        );

alter table stored_files
    add constraint chk_stored_files_private_purposes
        check (
            purpose not in ('DIAGNOSIS_IMAGE', 'MODEL_ARTIFACT')
            or visibility = 'PRIVATE'
        );
