package org.example.cloud_storage.controller;

import org.example.cloud_storage.FileVersion;
import org.example.cloud_storage.service.FileVersionService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/file-versions")
public class FileVersionController {

    private final FileVersionService fileVersionService;

    public FileVersionController(FileVersionService fileVersionService) {
        this.fileVersionService = fileVersionService;
    }

    @PostMapping
    public FileVersion createVersion(
            @RequestParam Long fileId,
            @RequestParam MultipartFile file) throws IOException {

        return fileVersionService.createVersion(fileId, file);
    }

    @GetMapping
    public List<FileVersion> getVersions(
            @RequestParam Long fileId) {

        return fileVersionService.getVersions(fileId);
    }

    @GetMapping("/{versionId}/download")
    public ResponseEntity<ByteArrayResource> downloadVersion(
            @PathVariable Long versionId) throws IOException {

        FileVersion version =
                fileVersionService.getVersionById(versionId);

        byte[] data =
                fileVersionService.downloadVersion(versionId);

        ByteArrayResource resource =
                new ByteArrayResource(data);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" +
                                version.getStorageKey() + "\""
                )
                .contentType(
                        MediaType.parseMediaType(
                                version.getContentType()
                        )
                )
                .contentLength(data.length)
                .body(resource);
    }
}