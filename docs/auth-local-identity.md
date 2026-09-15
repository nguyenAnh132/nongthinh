# Local identity and Keycloak credentials

The auth-service `users` table retains `id`, `keycloak_id`, `email`,
`password_updated_at`, `created_at`, `updated_at`, and `deleted_at`.
Application user IDs and Keycloak IDs remain distinct. Existing
`nongthinh_id` mappings and profile creation events are unchanged.

Keycloak owns passwords, account enablement and email verification.
Registration still accepts a password and the Keycloak `enabled`/`temporary`
options, but stores only the local identity. There is no local password hash,
password verification, password synchronization, or local enabled flag.
`GET /me` still reads the local identity and the business profile, and no longer
returns `isEnabled`. Frontend consumers must deploy with the matching API.

`password_updated_at` is nullable. New registrations leave it null. Existing
values are preserved as historical backend observations, including values
previously initialized at registration; they are not a complete or current
Keycloak password audit log. Resetting a password directly in Keycloak does not
update this field. A future backend password-change endpoint should record a
timestamp only after Keycloak confirms success. That endpoint is not part of
this change.

Local email lookup and uniqueness checks remain. A future email-change flow
must keep this local email consistent with Keycloak.

## Migration and deployment

V1-V3 remain unchanged. Apply the new Flyway migration
`V4__remove_local_credentials_and_account_status.sql` to an existing V3 database.
It drops only `password_hash`, `enabled`, and `auth_provider`; V3 already removed
`email_verified`. PostgreSQL also removes the index on the dropped enabled column.
User rows, IDs, email constraints and retained timestamps are preserved.

Before migration, reconcile any local disabled accounts with their Keycloak
accounts if that status must be retained. This migration does not change
Keycloak account settings or invalidate issued tokens. Stop old auth-service
instances before migrating because their entity mappings require the dropped
columns. Deleted credential/status data can only be restored from a backup;
the old application version cannot run against the new schema.

## Validation

Run the regression checks on a dedicated PostgreSQL database:

```shell
psql -X -v ON_ERROR_STOP=1 -h 127.0.0.1 -p 55449 -U postgres -d postgres -f ci/tests/auth-identity-migration.sql
```

The script applies V1-V3, seeds existing accounts, applies V4 and checks retained
data, nullable password timestamps, removed columns and email uniqueness.
It rolls back all test schemas and data.

For acceptance, register farmer/brand/admin, check local identity/profile IDs,
complete Keycloak login and call `/me`. Test Keycloak password reset and account
disablement separately. These operations do not synchronize password metadata
or status to the application database.
