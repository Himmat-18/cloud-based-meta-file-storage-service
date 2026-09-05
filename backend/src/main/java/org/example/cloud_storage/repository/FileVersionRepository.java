package org.example.cloud_storage.repository;

import org.example.cloud_storage.FileVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileVersionRepository extends JpaRepository<FileVersion, Long> {

    List<FileVersion> findByFileIdOrderByVersionNumberDesc(Long fileId);
}
