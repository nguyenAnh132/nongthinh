package com.nongthinh.auth_service.domain.user;

import com.nongthinh.auth_service.domain.user.valueobject.Email;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class User {

    private final UUID id;
    private final String keycloakId;
    private final Email email;
    // Last password change observed by this backend, not synchronized from Keycloak.
    private final Instant passwordUpdatedAt;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    private User(
            UUID id,
            String keycloakId,
            Email email,
            Instant passwordUpdatedAt,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.keycloakId = keycloakId;
        this.email = Objects.requireNonNull(email, "email is required");
        this.passwordUpdatedAt = passwordUpdatedAt;
        this.createdAt = Objects.requireNonNull(createdAt, "created at is required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updated at is required");
        this.deletedAt = deletedAt;
    }

    public static User reconstruct(
            UUID id,
            String keycloakId,
            Email email,
            Instant passwordUpdatedAt,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt) {
        return new User(
                id,
                keycloakId,
                email,
                passwordUpdatedAt,
                createdAt,
                updatedAt,
                deletedAt);
    }

    public static User create(
            UUID id,
            String keycloakId,
            Email email,
            Instant now) {
        Objects.requireNonNull(now, "now is required");

        return new User(
                id,
                keycloakId,
                email,
                null,
                now,
                now,
                null);
    }

    public void delete(Instant now) {
        this.deletedAt = Objects.requireNonNull(now, "now is required");
        touch(now);
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
