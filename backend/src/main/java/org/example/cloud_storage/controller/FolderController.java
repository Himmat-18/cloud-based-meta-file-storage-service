package org.example.cloud_storage.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.example.cloud_storage.Folder;
import org.example.cloud_storage.service.FolderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/folders")
public class FolderController {

    private final FolderService folderService;

    public FolderController(FolderService folderService) {
        this.folderService = folderService;
    }

    // =========================
    // CREATE FOLDER - JWT USER
    // =========================

    @PostMapping
    public Folder createFolder(
            @RequestParam String name,
            @RequestParam(required = false) Long parentId,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");

        return folderService.createFolder(
                name,
                userId,
                parentId
        );
    }

    // =========================
    // GET USER FOLDERS - JWT USER
    // =========================

    @GetMapping
    public List<Folder> getUserFolders(
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");

        return folderService.getUserFolders(userId);
    }

    // =========================
    // GET FOLDER BY ID
    // =========================

    @GetMapping("/{folderId}")
    public Folder getFolderById(
            @PathVariable Long folderId) {

        return folderService.getFolderById(folderId);
    }

    // =========================
    // DELETE FOLDER - JWT USER
    // =========================

    @DeleteMapping("/{folderId}")
    public String deleteFolder(
            @PathVariable Long folderId,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");

        folderService.deleteFolder(
                folderId,
                userId
        );

        return "Folder deleted successfully";
    }

    // =========================
    // RENAME FOLDER - JWT USER
    // =========================

    @PutMapping("/{folderId}/rename")
    public Folder renameFolder(
            @PathVariable Long folderId,
            @RequestParam String newName,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");

        return folderService.renameFolder(
                folderId,
                userId,
                newName
        );
    }

    // =========================
    // MOVE FOLDER - JWT USER
    // =========================

    @PutMapping("/{folderId}/move")
    public Folder moveFolder(
            @PathVariable Long folderId,
            @RequestParam Long targetParentId,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");

        return folderService.moveFolder(
                folderId,
                userId,
                targetParentId
        );
    }
}