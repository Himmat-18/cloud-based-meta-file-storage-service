package org.example.cloud_storage.repository;

import org.example.cloud_storage.File;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileRepository extends JpaRepository<File, Long> {

    List<File> findByFolderIdAndDeletedFalse(Long folderId);

    List<File>findByFolderIsNullAndDeletedFalse();

    List<File> findByNameContainingIgnoreCaseAndDeletedFalse(String name);

    List<File> findByContentTypeContainingIgnoreCaseAndDeletedFalse(
            String contentType);

    List<File> findByFolderIdAndContentTypeContainingIgnoreCaseAndDeletedFalse(
            Long folderId,
            String contentType);
    List<File> findByDeletedTrue();
}