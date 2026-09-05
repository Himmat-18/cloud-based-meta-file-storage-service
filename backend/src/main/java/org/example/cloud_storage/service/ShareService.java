package org.example.cloud_storage.service;

import org.example.cloud_storage.File;
import org.example.cloud_storage.Share;
import org.example.cloud_storage.User;
import org.example.cloud_storage.repository.FileRepository;
import org.example.cloud_storage.repository.ShareRepository;
import org.example.cloud_storage.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShareService {

    private final ShareRepository shareRepository;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;

    public ShareService(
            ShareRepository shareRepository,
            FileRepository fileRepository,
            UserRepository userRepository) {

        this.shareRepository = shareRepository;
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
    }

    // =========================
    // SHARE FILE
    // =========================

    public Share shareFile(
            Long fileId,
            Long userId,
            String role,
            Long ownerId) {

        File file = fileRepository.findById(fileId)
                .orElseThrow(() ->
                        new RuntimeException("File not found"));

        // Only file owner can share the file
        if (!file.getUser().getId().equals(ownerId)) {

            throw new RuntimeException(
                    "Only the file owner can share this file");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (file.isDeleted()) {

            throw new RuntimeException(
                    "Cannot share a deleted file");
        }

        if (role == null ||
                (!role.equalsIgnoreCase("VIEWER")
                        && !role.equalsIgnoreCase("EDITOR"))) {

            throw new RuntimeException(
                    "Role must be VIEWER or EDITOR");
        }

        Share share = new Share();

        share.setFile(file);
        share.setSharedWithUser(user);
        share.setRole(role.toUpperCase());

        return shareRepository.save(share);
    }

    // =========================
    // GET SHARES
    // =========================

    public List<Share> getSharesByFile(Long fileId) {

        return shareRepository.findByFileId(fileId);
    }
    // =========================
    // GET FILES SHARED WITH USER
    // =========================

    public List<Share> getSharesForUser(Long userId) {

        return shareRepository.findBySharedWithUser_Id(userId);
    }

    // =========================
    // DELETE SHARE
    // =========================

    public void deleteShare(
            Long shareId,
            Long ownerId) {

        Share share = shareRepository.findById(shareId)
                .orElseThrow(() ->
                        new RuntimeException("Share not found"));

        File file = share.getFile();

        // Only file owner can delete the share
        if (!file.getUser().getId().equals(ownerId)) {

            throw new RuntimeException(
                    "Only the file owner can delete this share");
        }

        shareRepository.delete(share);
    }

    // =========================
    // GET USER ROLE
    // =========================

    public String getUserRole(
            Long userId,
            Long fileId) {

        File file = fileRepository.findById(fileId)
                .orElseThrow(() ->
                        new RuntimeException("File not found"));

        // File owner
        if (file.getUser().getId().equals(userId)) {
            return "OWNER";
        }

        // Find all shares instead of expecting only one
        List<Share> shares =
                shareRepository.findBySharedWithUserIdAndFileId(
                        userId,
                        fileId
                );

        // No access
        if (shares.isEmpty()) {
            return "NO_ACCESS";
        }

        // Return the latest share's role
        return shares.get(shares.size() - 1).getRole();
    }
}