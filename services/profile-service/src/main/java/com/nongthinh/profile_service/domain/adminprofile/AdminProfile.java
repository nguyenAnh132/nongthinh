package com.nongthinh.profile_service.domain.adminprofile;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import com.nongthinh.profile_service.domain.shared.valueobject.PersonName;
import com.nongthinh.profile_service.domain.shared.valueobject.StandardProfileStatus;
import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.exception.BusinessException;

public final class AdminProfile {

    private final UUID id;

    private final UUID userId;

    private PersonName firstName;

    private PersonName lastName;

    private String phone;

    private String avatarUrl;

    private StandardProfileStatus status;

    private final Instant createdAt;

    private Instant updatedAt;

    private AdminProfile(
        UUID id,
        UUID userId,
        PersonName firstName,
        PersonName lastName,
        String phone,
        String avatarUrl,
        StandardProfileStatus status,
        Instant createdAt,
        Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.userId = Objects.requireNonNull(userId, "userId is required");
        this.firstName = Objects.requireNonNull(firstName, "firstName is required");
        this.lastName = Objects.requireNonNull(lastName, "lastName is required");
        this.phone = Objects.requireNonNull(phone, "phone is required");
        this.avatarUrl = avatarUrl;
        this.status = Objects.requireNonNull(status, "status is required");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt is required");
    }

    public static AdminProfile reconstruct(
        UUID id,
        UUID userId,
        PersonName firstName,
        PersonName lastName,
        String phone,
        String avatarUrl,
        StandardProfileStatus status,
        Instant createdAt,
        Instant updatedAt
    ) {
        return new AdminProfile(
            id, userId, firstName, lastName, phone, avatarUrl, status, createdAt, updatedAt
        );
    }

    public static AdminProfile create(
        UUID id,
        UUID userId,
        PersonName firstName,
        PersonName lastName,
        String phone,
        String avatarUrl,
        Instant now
    ) {
        return new AdminProfile(
            id,
            userId,
            firstName,
            lastName,
            phone,
            avatarUrl,
            StandardProfileStatus.ACTIVE,
            now,
            now
        );
    }

    public void activate(Instant now) {
        this.status = StandardProfileStatus.ACTIVE;
        touch(now);
    }

    public void lock(Instant now) {
        this.status = StandardProfileStatus.LOCKED;
        touch(now);
    }

    public void disable(Instant now) {
        this.status = StandardProfileStatus.DISABLED;
        touch(now);
    }

    public void markDeleted(Instant now) {
        this.status = StandardProfileStatus.DELETED;
        touch(now);
    }

    public void ensureActive() {
        if (status != StandardProfileStatus.ACTIVE) {
            throw new BusinessException(status.toAccessDeniedErrorCode());
        }
    }

    public void updateFirstName(PersonName firstName, Instant now) {
        this.firstName = Objects.requireNonNull(firstName, "firstName is required");
        touch(now);
    }

    public void updateLastName(PersonName lastName, Instant now) {
        this.lastName = Objects.requireNonNull(lastName, "lastName is required");
        touch(now);
    }

    public void updatePhone(String phone, Instant now) {
        this.phone = Objects.requireNonNull(phone, "phone is required");
        touch(now);
    }

    public void updateAvatarUrl(String avatarUrl, Instant now) {
        this.avatarUrl = avatarUrl;
        touch(now);
    }

    private void touch(Instant now) {
        this.updatedAt = Objects.requireNonNull(now, "now is required");
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public PersonName getFirstName() {
        return firstName;
    }

    public PersonName getLastName() {
        return lastName;
    }

    public String getPhone() {
        return phone;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public StandardProfileStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
