package com.nongthinh.auth_service.domain.user;

import com.nongthinh.auth_service.common.exception.ErrorCode;
import com.nongthinh.auth_service.domain.exception.BusinessException;
import com.nongthinh.auth_service.domain.user.valueobject.AuthProvider;
import com.nongthinh.auth_service.domain.user.valueobject.Email;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class User {

    private final UUID id;
    private final String keycloakId;
    private final Email email;
    private String passwordHash;
    private boolean enabled;
    private final AuthProvider authProvider;
    private Instant passwordUpdatedAt;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    private User(
            UUID id,
            String keycloakId,
            Email email,
            String passwordHash,
            boolean enabled,
            AuthProvider authProvider,
            Instant passwordUpdatedAt,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.keycloakId = keycloakId;
        this.email = Objects.requireNonNull(email, "email is required");
        this.passwordHash = passwordHash;
        this.enabled = enabled;
        this.authProvider = Objects.requireNonNull(authProvider, "authProvider is required");
        this.passwordUpdatedAt = passwordUpdatedAt;
        this.createdAt = Objects.requireNonNull(createdAt, "created at is required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updated at is required");
        this.deletedAt = deletedAt;
    }

    public static User reconstruct(
            UUID id,
            String keycloakId,
            Email email,
            String passwordHash,
            boolean enabled,
            AuthProvider authProvider,
            Instant passwordUpdatedAt,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt) {
        return new User(
                id,
                keycloakId,
                email,
                passwordHash,
                enabled,
                authProvider,
                passwordUpdatedAt,
                createdAt,
                updatedAt,
                deletedAt);
    }

    public static User create(
            UUID id,
            String keycloakId,
            Email email,
            String passwordHash,
            boolean enabled,
            Instant now) {
        return create(
                id,
                keycloakId,
                email,
                passwordHash,
                enabled,
                AuthProvider.LOCAL,
                now);
    }

    public static User create(
            UUID id,
            String keycloakId,
            Email email,
            String passwordHash,
            boolean enabled,
            AuthProvider authProvider,
            Instant now) {
        Objects.requireNonNull(now, "now is required");
        Objects.requireNonNull(authProvider, "authProvider is required");

        Instant passwordUpdatedAt = authProvider == AuthProvider.LOCAL ? now : null;

        return new User(
                id,
                keycloakId,
                email,
                passwordHash,
                enabled,
                authProvider,
                passwordUpdatedAt,
                now,
                now,
                null);
    }

    public void setEnabled(boolean enabled, Instant now) {
        this.enabled = enabled;
        touch(now);
    }

    public void updatePassword(String passwordHash, Instant now) {
        Objects.requireNonNull(passwordHash, "passwordHash is required");
        Objects.requireNonNull(now, "now is required");

        if (authProvider != AuthProvider.LOCAL) {
            throw new BusinessException(ErrorCode.INVALID_AUTH_PROVIDER);
        }

        this.passwordHash = passwordHash;
        this.passwordUpdatedAt = now;
        touch(now);
    }

    public void delete(Instant now) {
        this.deletedAt = Objects.requireNonNull(now, "now is required");
        touch(now);
    }

    public void ensureCanLogin() {
        if (!enabled) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }
    }

    private void touch(Instant now) {
        this.updatedAt = Objects.requireNonNull(now, "updated at is required");
    }

    public UUID getId() {
        return id;
    }

    public String getKeycloakId() {
        return keycloakId;
    }

    public Email getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public AuthProvider getAuthProvider() {
        return authProvider;
    }

    public Instant getPasswordUpdatedAt() {
        return passwordUpdatedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
