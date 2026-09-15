package com.nongthinh.auth_service.application.port.out.repository;

import java.util.UUID;
import com.nongthinh.auth_service.domain.user.User;
import java.util.Optional;

public interface UserRepository {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);


    Optional<User> findById(UUID id);

    User save(User user);
}
