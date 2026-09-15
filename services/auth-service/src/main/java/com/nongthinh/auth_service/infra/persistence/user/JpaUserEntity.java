package com.nongthinh.auth_service.infra.persistence.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JpaUserEntity {

    @Id
    private UUID id;

    @Column(name = "keycloak_id", unique = true)
    private UUID keycloakId;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_updated_at")
    private Instant passwordUpdatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
