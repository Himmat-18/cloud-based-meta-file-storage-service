package org.example.cloud_storage.repository;

import org.example.cloud_storage.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
Optional<User>findByEmail(String email);
}
