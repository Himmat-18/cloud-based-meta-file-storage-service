package org.example.cloud_storage.service;

import org.example.cloud_storage.File;
import org.example.cloud_storage.Star;
import org.example.cloud_storage.User;
import org.example.cloud_storage.repository.FileRepository;
import org.example.cloud_storage.repository.StarRepository;
import org.example.cloud_storage.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StarService {

    private final StarRepository starRepository;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;

    public StarService(
            StarRepository starRepository,
            FileRepository fileRepository,
            UserRepository userRepository) {

        this.starRepository = starRepository;
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
    }

    // =========================
    // STAR FILE
    // =========================

    public Star starFile(Long fileId, Long userId) {

        File file = fileRepository.findById(fileId)
                .orElseThrow(() ->
                        new RuntimeException("File not found"));

        if (file.isDeleted()) {
            throw new RuntimeException(
                    "Cannot star a deleted file");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (starRepository
                .findByUserIdAndFileId(userId, fileId)
                .isPresent()) {

            throw new RuntimeException(
                    "File is already starred");
        }

        Star star = new Star();

        star.setUser(user);
        star.setFile(file);

        return starRepository.save(star);
    }

    // =========================
    // UNSTAR FILE
    // =========================

    public void unstarFile(Long fileId, Long userId) {

        Star star = starRepository
                .findByUserIdAndFileId(userId, fileId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "File is not starred"));

        starRepository.delete(star);
    }

    // =========================
    // GET STARRED FILES
    // =========================

    public List<Star> getStarredFiles(Long userId) {

        return starRepository.findByUserId(userId);
    }

    // =========================
    // CHECK STAR STATUS
    // =========================

    public boolean isStarred(
            Long fileId,
            Long userId) {

        return starRepository
                .findByUserIdAndFileId(
                        userId,
                        fileId
                )
                .isPresent();
    }
}