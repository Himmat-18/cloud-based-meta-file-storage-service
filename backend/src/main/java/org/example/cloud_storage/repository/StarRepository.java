package org.example.cloud_storage.repository;

import org.example.cloud_storage.Star;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StarRepository extends JpaRepository<Star, Long> {

    Optional<Star> findByUserIdAndFileId(Long userId, Long fileId);

    List<Star> findByUserId(Long userId);
}
