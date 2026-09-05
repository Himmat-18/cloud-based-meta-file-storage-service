package org.example.cloud_storage.service;

import org.example.cloud_storage.File;
import org.example.cloud_storage.FileVersion;
import org.example.cloud_storage.repository.FileRepository;
import org.example.cloud_storage.repository.FileVersionRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class FileVersionService {

    private final FileVersionRepository fileVersionRepository;
    private final FileRepository fileRepository;

    public FileVersionService(FileVersionRepository fileVersionRepository,
                              FileRepository fileRepository) {
        this.fileVersionRepository = fileVersionRepository;
        this.fileRepository = fileRepository;
    }

    public FileVersion createVersion(Long fileId,
                                     MultipartFile multipartFile) throws IOException {

        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        List<FileVersion> versions =
                fileVersionRepository.findByFileIdOrderByVersionNumberDesc(fileId);

        int nextVersion = versions.isEmpty()
                ? 1
                : versions.get(0).getVersionNumber() + 1;

        Path uploadPath = Paths.get("uploads");

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String storageKey = System.currentTimeMillis()
                + "_v" + nextVersion
                + "_" + multipartFile.getOriginalFilename();

        Path filePath = uploadPath.resolve(storageKey);

        Files.copy(multipartFile.getInputStream(), filePath);

        FileVersion version = new FileVersion();

        version.setStorageKey(storageKey);
        version.setSize(multipartFile.getSize());
        version.setContentType(multipartFile.getContentType());
        version.setVersionNumber(nextVersion);
        version.setFile(file);

        return fileVersionRepository.save(version);
    }

    public List<FileVersion> getVersions(Long fileId) {

        return fileVersionRepository
                .findByFileIdOrderByVersionNumberDesc(fileId);
    }

    public FileVersion getVersionById(Long versionId) {

        return fileVersionRepository.findById(versionId)
                .orElseThrow(() ->
                        new RuntimeException("File version not found"));
    }

    public byte[] downloadVersion(Long versionId) throws IOException {

        FileVersion version = getVersionById(versionId);

        Path filePath = Paths.get("uploads")
                .resolve(version.getStorageKey());

        if (!Files.exists(filePath)) {
            throw new RuntimeException(
                    "File version not found on storage"
            );
        }

        return Files.readAllBytes(filePath);
    }
}