package com.nongthinh.auth_service.infra.persistence.user;

import com.nongthinh.auth_service.domain.user.User;
import com.nongthinh.auth_service.domain.user.valueobject.AuthProvider;
import com.nongthinh.auth_service.domain.user.valueobject.Email;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UserPersistenceMapper {

    public User toDomain(JpaUserEntity entity) {
        return User.reconstruct(
                entity.getId(),
                entity.getKeycloakId() != null ? entity.getKeycloakId().toString() : null,
                Email.of(entity.getEmail()),
                entity.getPasswordHash(),
                entity.isEnabled(),
                AuthProvider.fromDbValue(entity.getAuthProvider()),
                entity.getPasswordUpdatedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    public JpaUserEntity toEntity(User user) {
        JpaUserEntity entity = new JpaUserEntity();
        entity.setId(user.getId());
        entity.setKeycloakId(
                user.getKeycloakId() != null ? UUID.fromString(user.getKeycloakId()) : null);
        entity.setEmail(user.getEmail().getValue());
        entity.setPasswordHash(user.getPasswordHash());
        entity.setEnabled(user.isEnabled());
        entity.setAuthProvider(user.getAuthProvider().name());
        entity.setPasswordUpdatedAt(user.getPasswordUpdatedAt());
        entity.setDeletedAt(user.getDeletedAt());
        entity.setCreatedAt(user.getCreatedAt());
        entity.setUpdatedAt(user.getUpdatedAt());
        return entity;
    }
}
