package org.example.cloud_storage.service;

import org.example.cloud_storage.File;
import org.example.cloud_storage.Folder;
import org.example.cloud_storage.Share;
import org.example.cloud_storage.User;
import org.example.cloud_storage.repository.FileRepository;
import org.example.cloud_storage.repository.FolderRepository;
import org.example.cloud_storage.repository.ShareRepository;
import org.example.cloud_storage.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class FileService {

    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final FolderRepository folderRepository;
    private final ShareRepository shareRepository;

    public FileService(
            FileRepository fileRepository,
            UserRepository userRepository,
            FolderRepository folderRepository,
            ShareRepository shareRepository) {

        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
        this.folderRepository = folderRepository;
        this.shareRepository = shareRepository;
    }

    // =========================
    // CREATE FILE
    // =========================

    public File createFile(
            String name,
            String storageKey,
            String contentType,
            Long size,
            Long userId,
            Long folderId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        File file = new File();

        file.setName(name);
        file.setStorageKey(storageKey);
        file.setContentType(contentType);
        file.setSize(size);
        file.setUser(user);

        if (folderId != null) {

            Folder folder = folderRepository.findById(folderId)
                    .orElseThrow(() ->
                            new RuntimeException("Folder not found"));

            file.setFolder(folder);
        }

        return fileRepository.save(file);
    }

    // =========================
    // GET FILES BY FOLDER
    // =========================

    public List<File> getFilesByFolder(Long folderId) {

       if (folderId ==null || folderId== 0) {
           return fileRepository.findByFolderIsNullAndDeletedFalse();
       }
       return fileRepository.findByFolderIdAndDeletedFalse(folderId);
    }

    // =========================
    // SEARCH FILES
    // =========================

    public List<File> searchFiles(String name) {

        return fileRepository
                .findByNameContainingIgnoreCaseAndDeletedFalse(name);
    }

    // =========================
    // FILTER BY CONTENT TYPE
    // =========================

    public List<File> filterByContentType(
            String contentType) {

        return fileRepository
                .findByContentTypeContainingIgnoreCaseAndDeletedFalse(
                        contentType
                );
    }

    // =========================
    // FILTER BY FOLDER + TYPE
    // =========================

    public List<File> filterByFolderAndContentType(
            Long folderId,
            String contentType) {

        return fileRepository
                .findByFolderIdAndContentTypeContainingIgnoreCaseAndDeletedFalse(
                        folderId,
                        contentType
                );
    }

    // =========================
    // DELETE FILE - SOFT DELETE
    // =========================

    public void deleteFile(
            Long fileId,
            Long userId) {

        File file = getFileById(fileId);

        checkModifyPermission(file, userId);

        file.setDeleted(true);

        fileRepository.save(file);
    }

    // =========================
    // RESTORE FILE
    // =========================

    public File restoreFile(
            Long fileId,
            Long userId) {

        File file = getFileById(fileId);

        checkModifyPermission(file, userId);

        file.setDeleted(false);

        return fileRepository.save(file);
    }

    // =========================
    // UPLOAD FILE
    // =========================

    public File uploadFile(
            MultipartFile multipartFile,
            Long userId,
            Long folderId)
            throws IOException {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Folder folder = null;

        if (folderId != null) {

            folder = folderRepository.findById(folderId)
                    .orElseThrow(() ->
                            new RuntimeException("Folder not found"));

            if (!folder.getUser().getId().equals(userId)) {

                throw new RuntimeException(
                        "You do not have permission to upload to this folder"
                );
            }
        }

        Path uploadPath = Paths.get("uploads");

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFileName =
                multipartFile.getOriginalFilename();

        if (originalFileName == null ||
                originalFileName.trim().isEmpty()) {

            throw new RuntimeException(
                    "File name cannot be empty"
            );
        }

        String storageKey =
                System.currentTimeMillis()
                        + "_" + originalFileName;

        Path filePath =
                uploadPath.resolve(storageKey);

        Files.copy(
                multipartFile.getInputStream(),
                filePath
        );

        File file = new File();

        file.setName(originalFileName);
        file.setStorageKey(storageKey);
        file.setContentType(multipartFile.getContentType());
        file.setSize(multipartFile.getSize());
        file.setUser(user);
        file.setFolder(folder);

        System.out.println(
                "UPLOAD: saving file " + file.getName()
        );

        return fileRepository.save(file);
    }

    // =========================
    // MODIFY FILE
    // =========================

    public File modifyFile(
            Long fileId,
            Long userId,
            MultipartFile multipartFile)
            throws IOException {

        File file = getFileById(fileId);

        checkModifyPermission(file, userId);

        if (file.isDeleted()) {

            throw new RuntimeException(
                    "Cannot modify a deleted file"
            );
        }

        String originalFileName =
                multipartFile.getOriginalFilename();

        if (originalFileName == null ||
                originalFileName.trim().isEmpty()) {

            throw new RuntimeException(
                    "File name cannot be empty"
            );
        }

        Path uploadPath = Paths.get("uploads");

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String oldStorageKey =
                file.getStorageKey();

        String newStorageKey =
                System.currentTimeMillis()
                        + "_" + originalFileName;

        Path newFilePath =
                uploadPath.resolve(newStorageKey);

        Files.copy(
                multipartFile.getInputStream(),
                newFilePath
        );

        Path oldFilePath =
                uploadPath.resolve(oldStorageKey);

        if (Files.exists(oldFilePath)) {
            Files.delete(oldFilePath);
        }

        file.setName(originalFileName);
        file.setStorageKey(newStorageKey);
        file.setContentType(
                multipartFile.getContentType()
        );
        file.setSize(
                multipartFile.getSize()
        );

        return fileRepository.save(file);
    }

    // =========================
    // RENAME FILE
    // =========================

    public File renameFile(
            Long fileId,
            Long userId,
            String newName) {

        File file = getFileById(fileId);

        checkModifyPermission(file, userId);

        if (file.isDeleted()) {

            throw new RuntimeException(
                    "Cannot rename a deleted file"
            );
        }

        if (newName == null ||
                newName.trim().isEmpty()) {

            throw new RuntimeException(
                    "File name cannot be empty"
            );
        }

        file.setName(newName.trim());

        return fileRepository.save(file);
    }

    // =========================
    // MOVE FILE
    // =========================

    public File moveFile(
            Long fileId,
            Long userId,
            Long targetFolderId) {

        File file = getFileById(fileId);

        checkModifyPermission(file, userId);

        if (file.isDeleted()) {

            throw new RuntimeException(
                    "Cannot move a deleted file"
            );
        }

        Folder targetFolder =
                folderRepository.findById(targetFolderId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Target folder not found"
                                ));

        if (!targetFolder.getUser().getId().equals(userId)) {

            throw new RuntimeException(
                    "You do not have permission to move file to this folder"
            );
        }

        file.setFolder(targetFolder);

        return fileRepository.save(file);
    }

    // =========================
    // GET FILE BY ID
    // =========================

    public File getFileById(Long fileId) {

        return fileRepository.findById(fileId)
                .orElseThrow(() ->
                        new RuntimeException("File not found"));
    }

    // =========================
    // DOWNLOAD FILE
    // =========================

    public byte[] downloadFile(
            Long fileId,
            Long userId)
            throws IOException {

        File file = getFileById(fileId);

        if (file.isDeleted()) {

            throw new RuntimeException(
                    "File has been deleted"
            );
        }

        checkReadPermission(file, userId);

        Path filePath =
                Paths.get("uploads")
                        .resolve(file.getStorageKey());

        if (!Files.exists(filePath)) {

            throw new RuntimeException(
                    "File not found on storage"
            );
        }

        return Files.readAllBytes(filePath);
    }

    // =========================
    // READ PERMISSION
    // =========================

    private void checkReadPermission(
            File file,
            Long userId) {

        // Owner
        if (file.getUser().getId().equals(userId)) {
            return;
        }

        List<Share> shares =
                shareRepository
                        .findBySharedWithUserIdAndFileId(
                                userId,
                                file.getId()
                        );

        if (shares.isEmpty()) {

            throw new RuntimeException(
                    "You do not have access to this file"
            );
        }

        // Any valid share gives read access
        boolean hasReadPermission =
                shares.stream()
                        .anyMatch(share ->
                                share.getRole()
                                        .equalsIgnoreCase("OWNER")
                                        || share.getRole()
                                        .equalsIgnoreCase("EDITOR")
                                        || share.getRole()
                                        .equalsIgnoreCase("VIEWER")
                        );

        if (!hasReadPermission) {

            throw new RuntimeException(
                    "You do not have permission to read this file"
            );
        }
    }

    // =========================
    // MODIFY PERMISSION
    // =========================

    private void checkModifyPermission(
            File file,
            Long userId) {

        // Owner
        if (file.getUser().getId().equals(userId)) {
            return;
        }

        List<Share> shares =
                shareRepository
                        .findBySharedWithUserIdAndFileId(
                                userId,
                                file.getId()
                        );

        if (shares.isEmpty()) {

            throw new RuntimeException(
                    "You do not have permission to modify this file"
            );
        }

        // EDITOR or OWNER can modify
        boolean hasModifyPermission =
                shares.stream()
                        .anyMatch(share ->
                                share.getRole()
                                        .equalsIgnoreCase("OWNER")
                                        || share.getRole()
                                        .equalsIgnoreCase("EDITOR")
                        );

        if (!hasModifyPermission) {

            throw new RuntimeException(
                    "Only OWNER or EDITOR can modify this file"
            );
        }
    }
    // =========================
// GET TRASH FILES
// =========================

    public List<File> getTrashFiles(Long userId) {

        return fileRepository.findByDeletedTrue()
                .stream()
                .filter(file ->
                        file.getUser().getId().equals(userId))
                .toList();
    }
}