package org.example.cloud_storage.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.cloud_storage.Star;
import org.example.cloud_storage.service.StarService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stars")
public class StarController {

    private final StarService starService;

    public StarController(StarService starService) {
        this.starService = starService;
    }

    // STAR FILE
    @PostMapping
    public Star starFile(
            @RequestParam Long fileId,
            HttpServletRequest request) {

        Long userId = getUserId(request);

        return starService.starFile(
                fileId,
                userId
        );
    }

    // UNSTAR FILE
    @DeleteMapping("/{fileId}")
    public String unstarFile(
            @PathVariable Long fileId,
            HttpServletRequest request) {

        Long userId = getUserId(request);

        starService.unstarFile(
                fileId,
                userId
        );

        return "File unstarred successfully";
    }

    // GET STARRED FILES
    @GetMapping
    public List<Star> getStarredFiles(
            HttpServletRequest request) {

        Long userId = getUserId(request);

        return starService.getStarredFiles(userId);
    }

    // CHECK STAR STATUS
    @GetMapping("/{fileId}/status")
    public boolean isStarred(
            @PathVariable Long fileId,
            HttpServletRequest request) {

        Long userId = getUserId(request);

        return starService.isStarred(
                fileId,
                userId
        );
    }

    // GET JWT USER ID
    private Long getUserId(
            HttpServletRequest request) {

        Object userId =
                request.getAttribute("userId");

        if (userId == null) {
            throw new RuntimeException(
                    "User ID not found in JWT");
        }

        if (!(userId instanceof Long)) {
            throw new RuntimeException(
                    "Invalid JWT user ID");
        }

        return (Long) userId;
    }
}