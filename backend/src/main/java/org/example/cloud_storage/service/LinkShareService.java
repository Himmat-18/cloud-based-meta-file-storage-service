package org.example.cloud_storage.service;

import org.example.cloud_storage.File;
import org.example.cloud_storage.LinkShare;
import org.example.cloud_storage.repository.FileRepository;
import org.example.cloud_storage.repository.LinkShareRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class LinkShareService {

    private final LinkShareRepository linkShareRepository;
    private final FileRepository fileRepository;
    private final PasswordEncoder passwordEncoder;

    public LinkShareService(
            LinkShareRepository linkShareRepository,
            FileRepository fileRepository,
            PasswordEncoder passwordEncoder) {

        this.linkShareRepository = linkShareRepository;
        this.fileRepository = fileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // =========================
    // CREATE PUBLIC SHARE LINK
    // =========================

    public LinkShare createLink(
            Long fileId,
            Integer expiryMinutes,
            String password) {

        File file = fileRepository.findById(fileId)
                .orElseThrow(() ->
                        new RuntimeException("File not found"));

        LinkShare linkShare = new LinkShare();

        linkShare.setFile(file);

        linkShare.setToken(
                UUID.randomUUID().toString()
        );

        linkShare.setActive(true);

        // =========================
        // SET LINK EXPIRY
        // =========================

        if (expiryMinutes != null &&
                expiryMinutes > 0) {

            linkShare.setExpiresAt(
                    LocalDateTime.now()
                            .plusMinutes(expiryMinutes)
            );
        }

        // =========================
        // SET PASSWORD
        // =========================

        if (password != null &&
                !password.trim().isEmpty()) {

            linkShare.setPassword(
                    passwordEncoder.encode(password)
            );
        }

        return linkShareRepository.save(linkShare);
    }

    // =========================
    // DOWNLOAD USING PUBLIC LINK
    // =========================

    public byte[] downloadByToken(
            String token,
            String password) throws IOException {

        LinkShare linkShare =
                linkShareRepository.findByToken(token)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Link not found"));

        // =========================
        // CHECK ACTIVE
        // =========================

        if (!linkShare.isActive()) {

            throw new RuntimeException(
                    "Link is inactive");
        }

        // =========================
        // CHECK EXPIRY
        // =========================

        if (linkShare.getExpiresAt() != null &&
                linkShare.getExpiresAt()
                        .isBefore(LocalDateTime.now())) {

            throw new RuntimeException(
                    "Link has expired");
        }

        // =========================
        // CHECK PASSWORD
        // =========================

        if (linkShare.getPassword() != null) {

            if (password == null ||
                    !passwordEncoder.matches(
                            password,
                            linkShare.getPassword())) {

                throw new RuntimeException(
                        "Incorrect password");
            }
        }

        // =========================
        // GET FILE
        // =========================

        File file = linkShare.getFile();

        // =========================
        // CHECK DELETED FILE
        // =========================

        if (file.isDeleted()) {

            throw new RuntimeException(
                    "File has been deleted");
        }

        // =========================
        // FILE STORAGE PATH
        // =========================

        Path filePath =
                Paths.get("uploads")
                        .resolve(file.getStorageKey());

        // =========================
        // CHECK FILE EXISTS
        // =========================

        if (!Files.exists(filePath)) {

            throw new RuntimeException(
                    "File not found on storage");
        }

        return Files.readAllBytes(filePath);
    }

    // =========================
    // DEACTIVATE PUBLIC LINK
    // =========================

    public void deactivateLink(
            Long linkId) {

        LinkShare linkShare =
                linkShareRepository.findById(linkId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Link not found"));

        linkShare.setActive(false);

        linkShareRepository.save(linkShare);
    }
}