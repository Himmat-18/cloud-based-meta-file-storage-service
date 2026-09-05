package org.example.cloud_storage.repository;

import org.example.cloud_storage.Share;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShareRepository
        extends JpaRepository<Share, Long> {

    // Get all shares of a file
    List<Share> findByFileId(Long fileId);

    // Find all shares for a particular user and file
    List<Share> findBySharedWithUserIdAndFileId(
            Long userId,
            Long fileId
    );

    // Get all files shared with a user
    List<Share> findBySharedWithUser_Id(Long userId);
}