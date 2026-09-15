package com.nongthinh.auth_service.infra.persistence.user;

import com.nongthinh.auth_service.application.port.out.repository.UserRepository;
import com.nongthinh.auth_service.domain.user.User;
import com.nongthinh.auth_service.domain.user.valueobject.Email;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final JpaUserRepository jpaUserRepository;
    private final UserPersistenceMapper userPersistenceMapper;

    @Override
    public boolean existsByEmail(String email) {
        return jpaUserRepository.existsByEmail(Email.normalize(email));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaUserRepository.findByEmail(Email.normalize(email))
                .map(userPersistenceMapper::toDomain);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaUserRepository.findById(id)
                .map(userPersistenceMapper::toDomain);
    }

    @Override
    public User save(User user) {
        JpaUserEntity entity = userPersistenceMapper.toEntity(user);
        JpaUserEntity saved = jpaUserRepository.save(entity);
        return userPersistenceMapper.toDomain(saved);
    }
}
