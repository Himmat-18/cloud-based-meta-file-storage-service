package org.example.cloud_storage.repository;

import org.example.cloud_storage.LinkShare;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LinkShareRepository extends JpaRepository<LinkShare, Long> {

    Optional<LinkShare> findByToken(String token);
}