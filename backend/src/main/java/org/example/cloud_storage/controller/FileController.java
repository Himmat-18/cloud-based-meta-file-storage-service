package org.example.cloud_storage.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.example.cloud_storage.File;
import org.example.cloud_storage.service.FileService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    // =========================
    // CREATE FILE
    // =========================

    @PostMapping
    public File createFile(
            @RequestParam String name,
            @RequestParam String storageKey,
            @RequestParam String contentType,
            @RequestParam Long size,
            @RequestParam(required = false) Long folderId,
            HttpServletRequest request) {

        Long userId = getUserId(request);

        return fileService.createFile(
                name,
                storageKey,
                contentType,
                size,
                userId,
                folderId
        );
    }

    // =========================
    // GET FILES BY FOLDER
    // =========================

    @GetMapping
    public List<File> getFilesByFolder(
            @RequestParam Long folderId) {

        return fileService.getFilesByFolder(folderId);
    }
     // =========================
    // GET TRASH FILES
    // =========================

    @GetMapping("/trash")
    public List<File> getTrashFiles(
            HttpServletRequest request) {

        Long userId = getUserId(request);

        return fileService.getTrashFiles(userId);
    }

    // =========================
    // SEARCH FILES
    // =========================

    @GetMapping("/search")
    public List<File> searchFiles(
            @RequestParam String name) {

        return fileService.searchFiles(name);
    }

    // =========================
    // FILTER BY CONTENT TYPE
    // =========================

    @GetMapping("/filter")
    public List<File> filterByContentType(
            @RequestParam String contentType) {

        return fileService.filterByContentType(contentType);
    }

    // =========================
    // FILTER BY FOLDER + TYPE
    // =========================

    @GetMapping("/filter/folder")
    public List<File> filterByFolderAndContentType(
            @RequestParam Long folderId,
            @RequestParam String contentType) {

        return fileService.filterByFolderAndContentType(
                folderId,
                contentType
        );
    }

    // =========================
    // DELETE FILE
    // =========================

    @DeleteMapping("/{fileId}")
    public String deleteFile(
            @PathVariable Long fileId,
            HttpServletRequest request) {

        Long userId = getUserId(request);

        fileService.deleteFile(
                fileId,
                userId
        );

        return "File deleted successfully";
    }

    // =========================
    // RESTORE FILE
    // =========================

    @PutMapping("/{fileId}/restore")
    public File restoreFile(
            @PathVariable Long fileId,
            HttpServletRequest request) {

        Long userId = getUserId(request);

        return fileService.restoreFile(
                fileId,
                userId
        );
    }

    // =========================
    // UPLOAD FILE
    // =========================

    @PostMapping("/upload")
    public File uploadFile(
            @RequestParam MultipartFile file,
            @RequestParam(required = false) Long folderId,
            HttpServletRequest request)
            throws IOException {

        Long userId = getUserId(request);

        return fileService.uploadFile(
                file,
                userId,
                folderId
        );
    }

    // =========================
    // MODIFY FILE
    // =========================

    @PutMapping("/{fileId}/modify")
    public File modifyFile(
            @PathVariable Long fileId,
            @RequestParam MultipartFile file,
            HttpServletRequest request)
            throws IOException {

        Long userId = getUserId(request);

        return fileService.modifyFile(
                fileId,
                userId,
                file
        );
    }

    // =========================
    // RENAME FILE
    // =========================

    @PutMapping("/{fileId}/rename")
    public File renameFile(
            @PathVariable Long fileId,
            @RequestParam String newName,
            HttpServletRequest request) {

        Long userId = getUserId(request);

        return fileService.renameFile(
                fileId,
                userId,
                newName
        );
    }

    // =========================
    // MOVE FILE
    // =========================

    @PutMapping("/{fileId}/move")
    public File moveFile(
            @PathVariable Long fileId,
            @RequestParam Long targetFolderId,
            HttpServletRequest request) {

        Long userId = getUserId(request);

        return fileService.moveFile(
                fileId,
                userId,
                targetFolderId
        );
    }

    // =========================
    // DOWNLOAD FILE
    // =========================

    @GetMapping("/{fileId}/download")
    public ResponseEntity<byte[]> downloadFile(
            @PathVariable Long fileId,
            HttpServletRequest request)
            throws IOException {

        Long userId = getUserId(request);

        File file = fileService.getFileById(fileId);

        byte[] data = fileService.downloadFile(
                fileId,
                userId
        );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" +
                                file.getName() +
                                "\""
                )
                .contentType(
                        MediaType.parseMediaType(
                                file.getContentType()
                        )
                )
                .body(data);
    }

    // =========================
    // GET JWT USER ID
    // =========================

    private Long getUserId(
            HttpServletRequest request) {

        Object userId =
                request.getAttribute("userId");

        if (userId == null) {
            throw new RuntimeException(
                    "User ID not found in JWT"
            );
        }

        if (!(userId instanceof Long)) {
            throw new RuntimeException(
                    "Invalid JWT user ID"
            );
        }

        return (Long) userId;
    }
}