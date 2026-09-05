package org.example.cloud_storage.controller;

import org.example.cloud_storage.LinkShare;
import org.example.cloud_storage.service.LinkShareService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/link-shares")
public class LinkShareController {

    private final LinkShareService linkShareService;

    public LinkShareController(
            LinkShareService linkShareService) {

        this.linkShareService = linkShareService;
    }

    // =========================
    // CREATE PUBLIC SHARE LINK
    // =========================

    @PostMapping
    public LinkShare createLink(
            @RequestParam Long fileId,
            @RequestParam(required = false) Integer expiryMinutes,
            @RequestParam(required = false) String password) {

        return linkShareService.createLink(
                fileId,
                expiryMinutes,
                password
        );
    }

    // =========================
    // DOWNLOAD USING PUBLIC LINK
    // =========================

    @GetMapping("/{token}/download")
    public ResponseEntity<?> downloadByToken(
            @PathVariable String token,
            @RequestParam(required = false) String password)
            throws IOException {

        try {

            byte[] data =
                    linkShareService.downloadByToken(
                            token,
                            password
                    );

            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment"
                    )
                    .contentType(
                            MediaType.APPLICATION_OCTET_STREAM
                    )
                    .body(data);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.GONE)
                    .body(e.getMessage());
        }
    }

    // =========================
    // DEACTIVATE PUBLIC LINK
    // =========================

    @DeleteMapping("/{linkId}")
    public String deactivateLink(
            @PathVariable Long linkId) {

        linkShareService.deactivateLink(linkId);

        return "Link deactivated successfully";
    }
}