# Email verification owned by Keycloak

Registration creates the local account and profile request, then displays
"Đăng ký thành công" with a login button. Keycloak owns email verification;
auth-service no longer stores or synchronizes verification status. The `/me`
response no longer includes `flags.requiresEmailVerification`.

## Deployment order

1. Configure and test SMTP on the application's Keycloak realm. Enable Verify
   Email and the built-in VERIFY_EMAIL required action. Confirm new accounts
   are created with `emailVerified=false` and a nonempty email.
2. Before applying auth migration V3, export any local verification data needed
   for existing accounts. Either reconcile verified accounts with Keycloak using
   matching user IDs and emails, or explicitly require them to verify again.
3. End existing unverified-user sessions and review enabled clients/grants.
   Enabling required actions does not invalidate access JWTs already issued;
   services validating JWTs locally may accept those until expiry. Include that
   lifetime in the cutover plan. Disable grants not used by the application.
4. Stop the old auth/notification instances and registration traffic before
   database cleanup. Do not run old OTP writers or consumers alongside the new
   schema. Build clean artifacts so removed classes are not packaged.
5. Deploy the three service migrations and matching frontend together. Keycloak
   SMTP, theme and realm configuration are external prerequisites; this code
   change does not configure the running Keycloak server.

## Irreversible cleanup

- auth-service V3 drops `email_otps` and `users.email_verified`.
- bo-portal-service V5 deletes the five named OTP parameters.
- notification-service V6 hard-deletes `REGISTER_OTP` and `EMAIL_VERIFICATION`
  purposes, all their templates and variables, and histories referencing those
  purposes or templates. This includes custom/inactive templates under them.
- Other purposes and histories are preserved. Historical Flyway files remain
  unchanged. Restoring deleted database data requires a pre-migration backup;
  rolling back application code alone cannot restore it.
- The application no longer publishes or consumes `register-otp`. Its Kafka
  topic and any backlog require separate operational cleanup after cutover.

Keycloak verification emails use its SMTP/theme, not notification-service's
template editor or email history. Brand approval remains a separate workflow.

## Verification

Run the migration regression script on a dedicated PostgreSQL database:

```shell
psql -X -v ON_ERROR_STOP=1 -h 127.0.0.1 -p 55449 -U postgres -d postgres -f ci/tests/email-verification-migrations.sql
```

The script creates isolated schemas in a transaction, applies the old schema
and seeded data, adds verification history, runs the cleanup and asserts that
unrelated records survive. It rolls back all test schemas and data on success.

End-to-end acceptance: register farmer and brand; choose login; verify that no
application session is issued before the email link is completed; test closing
and reopening login, link expiration/resend, and existing admin login. After
verification, check `/me` and the role/profile destination. A brand must still
respect approval restrictions.
