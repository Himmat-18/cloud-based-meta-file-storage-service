package org.example.cloud_storage.controller;

import org.example.cloud_storage.Activity;
import org.example.cloud_storage.service.ActivityService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @PostMapping
    public Activity createActivity(
            @RequestParam Long userId,
            @RequestParam String action,
            @RequestParam String description) {

        return activityService.createActivity(
                userId,
                action,
                description
        );
    }

    @GetMapping
    public List<Activity> getActivities(
            @RequestParam Long userId) {

        return activityService.getActivities(userId);
    }
}
