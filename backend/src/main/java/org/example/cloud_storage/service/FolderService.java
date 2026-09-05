package org.example.cloud_storage.service;

import org.example.cloud_storage.Folder;
import org.example.cloud_storage.User;
import org.example.cloud_storage.repository.FolderRepository;
import org.example.cloud_storage.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FolderService {

    private final FolderRepository folderRepository;
    private final UserRepository userRepository;

    public FolderService(
            FolderRepository folderRepository,
            UserRepository userRepository) {

        this.folderRepository = folderRepository;
        this.userRepository = userRepository;
    }

    // =========================
    // CREATE FOLDER
    // =========================

    public Folder createFolder(
            String name,
            Long userId,
            Long parentId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException(
                    "Folder name cannot be empty");
        }

        Folder folder = new Folder();

        folder.setName(name.trim());
        folder.setUser(user);

        if (parentId != null) {

            Folder parent = folderRepository.findById(parentId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Parent folder not found"));

            // Parent folder must belong to same user
            if (!parent.getUser().getId().equals(userId)) {

                throw new RuntimeException(
                        "You do not have permission to use this parent folder");
            }

            folder.setParent(parent);
        }

        return folderRepository.save(folder);
    }

    // =========================
    // GET USER FOLDERS
    // =========================

    public List<Folder> getUserFolders(Long userId) {

        return folderRepository.findByUserId(userId);
    }

    // =========================
    // DELETE FOLDER
    // =========================

    public void deleteFolder(
            Long folderId,
            Long userId) {

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Folder not found"));

        checkOwner(folder, userId);

        folderRepository.delete(folder);
    }

    // =========================
    // RENAME FOLDER
    // =========================

    public Folder renameFolder(
            Long folderId,
            Long userId,
            String newName) {

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Folder not found"));

        checkOwner(folder, userId);

        if (newName == null ||
                newName.trim().isEmpty()) {

            throw new RuntimeException(
                    "Folder name cannot be empty");
        }

        folder.setName(newName.trim());

        return folderRepository.save(folder);
    }

    // =========================
    // MOVE FOLDER
    // =========================

    public Folder moveFolder(
            Long folderId,
            Long userId,
            Long targetParentId) {

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Folder not found"));

        checkOwner(folder, userId);

        Folder targetParent =
                folderRepository.findById(targetParentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Target parent folder not found"));

        // Target parent must belong to same user
        if (!targetParent.getUser().getId().equals(userId)) {

            throw new RuntimeException(
                    "You do not have permission to move folder here");
        }

        // Folder cannot be its own parent
        if (folder.getId().equals(targetParent.getId())) {

            throw new RuntimeException(
                    "A folder cannot be its own parent");
        }

        folder.setParent(targetParent);

        return folderRepository.save(folder);
    }

    // =========================
    // GET FOLDER BY ID
    // =========================

    public Folder getFolderById(Long folderId) {

        return folderRepository.findById(folderId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Folder not found"));
    }

    // =========================
    // OWNER CHECK
    // =========================

    private void checkOwner(
            Folder folder,
            Long userId) {

        if (!folder.getUser().getId().equals(userId)) {

            throw new RuntimeException(
                    "You do not have permission to modify this folder");
        }
    }
}