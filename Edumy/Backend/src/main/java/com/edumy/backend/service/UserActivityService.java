package com.edumy.backend.service;

import com.edumy.backend.model.SearchHistory;
import com.edumy.backend.model.UserActivity;
import com.edumy.backend.repository.SearchHistoryRepository;
import com.edumy.backend.repository.UserActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserActivityService {

    @Autowired
    private UserActivityRepository userActivityRepository;

    @Autowired
    private SearchHistoryRepository searchHistoryRepository;

    public void logActivity(Integer userId, String activityType, String resourceId) {
        try {
            UserActivity activity = UserActivity.builder()
                    .userId(userId)
                    .activityType(activityType)
                    .resourceId(resourceId)
                    .timestamp(LocalDateTime.now())
                    .build();
            userActivityRepository.save(activity);
        } catch (Exception ex) {
            // Non-blocking logging
        }
    }

    public void logSearch(Integer userId, String query) {
        if (query == null || query.trim().isEmpty()) return;
        try {
            SearchHistory history = SearchHistory.builder()
                    .userId(userId)
                    .searchQuery(query.trim())
                    .searchedAt(LocalDateTime.now())
                    .build();
            searchHistoryRepository.save(history);
        } catch (Exception ex) {
            // Non-blocking logging
        }
    }

    public List<UserActivity> getUserActivities(Integer userId) {
        return userActivityRepository.findAll().stream()
                .filter(a -> userId != null && userId.equals(a.getUserId()))
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(50)
                .toList();
    }
}
