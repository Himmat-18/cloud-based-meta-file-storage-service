package org.example.cloud_storage.service;

import org.example.cloud_storage.Activity;
import org.example.cloud_storage.User;
import org.example.cloud_storage.repository.ActivityRepository;
import org.example.cloud_storage.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;

    public ActivityService(ActivityRepository activityRepository,
                           UserRepository userRepository) {
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
    }

    public Activity createActivity(Long userId,
                                   String action,
                                   String description) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Activity activity = new Activity();

        activity.setUser(user);
        activity.setAction(action);
        activity.setDescription(description);
        activity.setCreatedAt(LocalDateTime.now());

        return activityRepository.save(activity);
    }

    public List<Activity> getActivities(Long userId) {

        return activityRepository
                .findByUserIdOrderByCreatedAtDesc(userId);
    }
}
