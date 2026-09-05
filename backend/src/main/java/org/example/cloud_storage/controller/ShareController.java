package org.example.cloud_storage.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.example.cloud_storage.Share;
import org.example.cloud_storage.service.ShareService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shares")
public class ShareController {

    private final ShareService shareService;

    public ShareController(ShareService shareService) {
        this.shareService = shareService;
    }

    // =========================
    // SHARE FILE
    // =========================

    @PostMapping
    public Share shareFile(
            @RequestParam Long fileId,
            @RequestParam Long userId,
            @RequestParam String role,
            HttpServletRequest request) {

        Long ownerId = getUserId(request);

        return shareService.shareFile(
                fileId,
                userId,
                role,
                ownerId
        );
    }

    // =========================
    // GET SHARES
    // =========================

    @GetMapping
    public List<Share> getSharesByFile(
            @RequestParam Long fileId) {

        return shareService.getSharesByFile(fileId);
    }
    // =========================
    // GET FILES SHARED WITH CURRENT USER
    // =========================

    @GetMapping("/shared-with-me")
    public List<Share> getSharedFilesWithMe(
            HttpServletRequest request) {

        Long userId = getUserId(request);

        return shareService.getSharesForUser(userId);
    }

    // =========================
    // DELETE SHARE
    // =========================

    @DeleteMapping("/{shareId}")
    public String deleteShare(
            @PathVariable Long shareId,
            HttpServletRequest request) {

        Long ownerId = getUserId(request);

        shareService.deleteShare(
                shareId,
                ownerId
        );

        return "Share deleted successfully";
    }

    // =========================
    // GET USER ROLE
    // =========================

    @GetMapping("/role")
    public String getUserRole(
            @RequestParam Long userId,
            @RequestParam Long fileId) {

        return shareService.getUserRole(
                userId,
                fileId
        );
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