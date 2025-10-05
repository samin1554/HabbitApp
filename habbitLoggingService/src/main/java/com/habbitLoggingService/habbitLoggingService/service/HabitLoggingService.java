package com.habbitLoggingService.habbitLoggingService.service;

import com.habbitLoggingService.habbitLoggingService.dto.HabitLogRequest;
import com.habbitLoggingService.habbitLoggingService.dto.HabitLogResponse;
import com.habbitLoggingService.habbitLoggingService.model.HabitLogs;
import com.habbitLoggingService.habbitLoggingService.model.HabitStatus;
import com.habbitLoggingService.habbitLoggingService.repository.HabitLoggingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class HabitLoggingService {

    private final HabitLoggingRepository repository;
    private final UserValidationService userValidationService;
    private final WebClient activityServiceWebClient;

    /**
     * Log a habit completion
     */
    public HabitLogResponse logHabitCompletion(HabitLogRequest request) {
        log.info("Logging habit completion for user: {}, habit: {}", request.getUserId(), request.getHabitId());
        
        // Validate user
        if (!userValidationService.validateUser(request.getUserId())) {
            throw new RuntimeException("Invalid user: " + request.getUserId());
        }

        // Find or create habit log
        Optional<HabitLogs> existingLog = repository.findByIdAndUserId(request.getHabitId(), request.getUserId());
        HabitLogs habitLog;
        
        if (existingLog.isPresent()) {
            habitLog = existingLog.get();
            updateHabitCompletion(habitLog, request.getCompletionTime());
        } else {
            // Create new habit log (fetch habit details from activity service)
            habitLog = createNewHabitLog(request);
        }

        HabitLogs savedLog = repository.save(habitLog);
        return mapToResponse(savedLog);
    }

    /**
     * Get habit analytics for a user
     */
    public List<HabitLogResponse> getUserHabitAnalytics(String userId) {
        log.info("Getting habit analytics for user: {}", userId);
        
        if (!userValidationService.validateUser(userId)) {
            throw new RuntimeException("Invalid user: " + userId);
        }

        List<HabitLogs> userHabits = repository.findByUserId(userId);
        return userHabits.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Get specific habit analytics
     */
    public HabitLogResponse getHabitAnalytics(String userId, String habitId) {
        log.info("Getting analytics for habit: {} of user: {}", habitId, userId);
        
        Optional<HabitLogs> habitLog = repository.findByIdAndUserId(habitId, userId);
        if (habitLog.isEmpty()) {
            throw new RuntimeException("Habit not found: " + habitId);
        }

        return mapToResponse(habitLog.get());
    }

    /**
     * Fetch habit details from Activity Service
     */
    private HabitActivityData fetchHabitDetails(String habitId) {
        try {
            log.info("Fetching habit details for habitId: {}", habitId);
            return activityServiceWebClient
                    .get()
                    .uri("/{id}", habitId)
                    .retrieve()
                    .bodyToMono(HabitActivityData.class)
                    .doOnError(error -> log.warn("Error fetching habit details: {}", error.getMessage()))
                    .onErrorReturn(null)
                    .block();
        } catch (Exception e) {
            log.error("Error fetching habit details for habitId: {}", habitId, e);
            return null;
        }
    }

    /**
     * Update habit completion and calculate streaks
     */
    private void updateHabitCompletion(HabitLogs habitLog, LocalDateTime completionTime) {
        // Add completion to log
        if (habitLog.getCompletionLog() == null) {
            habitLog.setCompletionLog(new ArrayList<>());
        }
        habitLog.getCompletionLog().add(completionTime);
        habitLog.setLastCompletionDate(completionTime);
        habitLog.setUpdatedAt(LocalDateTime.now());

        // Calculate streak
        calculateStreak(habitLog);
    }

    /**
     * Calculate current and longest streak
     */
    private void calculateStreak(HabitLogs habitLog) {
        List<LocalDateTime> completions = habitLog.getCompletionLog();
        if (completions == null || completions.isEmpty()) {
            habitLog.setStreak(0);
            return;
        }

        // Sort completions by date
        completions.sort(LocalDateTime::compareTo);
        
        int currentStreak = 1;
        int longestStreak = 1;
        int tempStreak = 1;

        // Calculate streaks based on consecutive days
        for (int i = 1; i < completions.size(); i++) {
            LocalDateTime current = completions.get(i);
            LocalDateTime previous = completions.get(i - 1);
            
            long daysBetween = ChronoUnit.DAYS.between(previous.toLocalDate(), current.toLocalDate());
            
            if (daysBetween == 1) {
                tempStreak++;
            } else {
                longestStreak = Math.max(longestStreak, tempStreak);
                tempStreak = 1;
            }
        }
        
        longestStreak = Math.max(longestStreak, tempStreak);
        
        // Current streak is from the last completion to today
        LocalDateTime lastCompletion = completions.get(completions.size() - 1);
        long daysSinceLastCompletion = ChronoUnit.DAYS.between(lastCompletion.toLocalDate(), LocalDateTime.now().toLocalDate());
        
        if (daysSinceLastCompletion <= 1) {
            currentStreak = tempStreak;
        } else {
            currentStreak = 0;
        }

        habitLog.setStreak(currentStreak);
        habitLog.setLongestStreak(longestStreak);
    }

    /**
     * Create new habit log entry
     */
    private HabitLogs createNewHabitLog(HabitLogRequest request) {
        HabitLogs habitLog = new HabitLogs();
        habitLog.setId(request.getHabitId()); // Set the habit ID
        habitLog.setUserId(request.getUserId()); // Set the user ID
        habitLog.setTitle("Habit " + request.getHabitId()); // Default title
        
        // Try to fetch real habit details from Activity Service
        HabitActivityData habitData = fetchHabitDetails(request.getHabitId());
        if (habitData != null) {
            habitLog.setTitle(habitData.getTitle() != null ? habitData.getTitle() : "Habit " + request.getHabitId());
            habitLog.setDescription(habitData.getDescription());
            habitLog.setFrequency(habitData.getFrequency());
            habitLog.setDays(habitData.getDays());
            habitLog.setCreatedAt(habitData.getCreatedAt());
            habitLog.setUpdatedAt(habitData.getUpdatedAt());
        } else {
            log.warn("Could not fetch habit details for habitId: {}", request.getHabitId());
            habitLog.setCreatedAt(LocalDateTime.now());
            habitLog.setUpdatedAt(LocalDateTime.now());
        }
        
        habitLog.setStatus(HabitStatus.ACTIVE);
        habitLog.setStreak(1);
        habitLog.setLongestStreak(1);
        
        List<LocalDateTime> completions = new ArrayList<>();
        completions.add(request.getCompletionTime());
        habitLog.setCompletionLog(completions);
        habitLog.setLastCompletionDate(request.getCompletionTime());
        
        return habitLog;
    }

    /**
     * Map entity to response DTO
     */
    private HabitLogResponse mapToResponse(HabitLogs habitLog) {
        HabitLogResponse response = new HabitLogResponse();
        response.setId(habitLog.getId());
        response.setUserId(habitLog.getUserId());
        response.setTitle(habitLog.getTitle());
        response.setDescription(habitLog.getDescription());
        response.setFrequency(habitLog.getFrequency());
        response.setDays(habitLog.getDays());
        response.setStreak(habitLog.getStreak());
        response.setLongestStreak(habitLog.getLongestStreak());
        response.setCreatedAt(habitLog.getCreatedAt());
        response.setUpdatedAt(habitLog.getUpdatedAt());
        response.setCompletionLog(habitLog.getCompletionLog());
        response.setLastCompletionDate(habitLog.getLastCompletionDate());
        response.setTargetCount(habitLog.getTargetCount());
        response.setStatus(habitLog.getStatus());
        
        // Calculate additional metrics
        if (habitLog.getCompletionLog() != null) {
            response.setTotalCompletions(habitLog.getCompletionLog().size());
            response.setSuccessRate(calculateSuccessRate(habitLog));
        }
        
        return response;
    }

    /**
     * Calculate success rate based on expected vs actual completions
     */
    private double calculateSuccessRate(HabitLogs habitLog) {
        if (habitLog.getCompletionLog() == null || habitLog.getCompletionLog().isEmpty()) {
            return 0.0;
        }
        
        // Simple calculation - you can make this more sophisticated
        long daysSinceCreation = ChronoUnit.DAYS.between(habitLog.getCreatedAt().toLocalDate(), LocalDateTime.now().toLocalDate());
        if (daysSinceCreation == 0) daysSinceCreation = 1;
        
        double expectedCompletions = daysSinceCreation; // Assuming daily habit
        double actualCompletions = habitLog.getCompletionLog().size();
        
        return Math.min(100.0, (actualCompletions / expectedCompletions) * 100.0);
    }
    
    /**
     * Data class to hold habit details from Activity Service
     */
    public static class HabitActivityData {
        private String id;
        private String userId;
        private String title;
        private String description;
        private String frequency;
        private List<String> days;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private int streak;
        private int longestStreak;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getFrequency() { return frequency; }
        public void setFrequency(String frequency) { this.frequency = frequency; }
        
        public List<String> getDays() { return days; }
        public void setDays(List<String> days) { this.days = days; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
        
        public int getStreak() { return streak; }
        public void setStreak(int streak) { this.streak = streak; }
        
        public int getLongestStreak() { return longestStreak; }
        public void setLongestStreak(int longestStreak) { this.longestStreak = longestStreak; }
    }
}