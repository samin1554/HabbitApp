package com.habbitLoggingService.habbitLoggingService.service;

import com.habbitLoggingService.habbitLoggingService.dto.HabitActivityResponse;
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
    private HabitActivityResponse fetchHabitDetails(String habitId) {
        try {
            log.info("Fetching habit details for habitId: {}", habitId);
            HabitActivityResponse result = activityServiceWebClient
                    .get()
                    .uri("/{id}", habitId)
                    .retrieve()
                    .bodyToMono(HabitActivityResponse.class)
                    .doOnSuccess(data -> log.info("Successfully fetched habit details for habitId: {}, title: {}", habitId, data != null ? data.getTitle() : "null"))
                    .doOnError(error -> log.error("Error fetching habit details for habitId: {}, error: {}", habitId, error.getMessage()))
                    .onErrorReturn(null)
                    .block();
            
            if (result == null) {
                log.warn("Received null response from activity service for habitId: {}", habitId);
            }
            
            return result;
        } catch (Exception e) {
            log.error("Exception while fetching habit details for habitId: {}", habitId, e);
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
        log.info("Creating new habit log for habitId: {}, userId: {}", request.getHabitId(), request.getUserId());
        
        HabitLogs habitLog = new HabitLogs();
        habitLog.setId(request.getHabitId()); // Set the habit ID
        habitLog.setUserId(request.getUserId()); // Set the user ID
        
        // Try to fetch real habit details from Activity Service
        HabitActivityResponse habitData = fetchHabitDetails(request.getHabitId());
        if (habitData != null) {
            log.info("Successfully fetched habit data for habitId: {}, setting details", request.getHabitId());
            habitLog.setTitle(habitData.getTitle() != null ? habitData.getTitle() : "Habit " + request.getHabitId());
            habitLog.setDescription(habitData.getDescription());
            habitLog.setFrequency(habitData.getFrequency());
            habitLog.setDays(habitData.getDays());
            habitLog.setCreatedAt(habitData.getCreatedAt() != null ? habitData.getCreatedAt() : LocalDateTime.now());
            habitLog.setUpdatedAt(LocalDateTime.now());
            
            // Copy existing streak data if available
            habitLog.setStreak(habitData.getStreak() > 0 ? habitData.getStreak() : 1);
            habitLog.setLongestStreak(habitData.getLongestStreak() > 0 ? habitData.getLongestStreak() : 1);
        } else {
            log.warn("Could not fetch habit details for habitId: {}, using defaults", request.getHabitId());
            habitLog.setTitle("Habit " + request.getHabitId());
            habitLog.setDescription("Default description");
            habitLog.setFrequency("daily");
            habitLog.setCreatedAt(LocalDateTime.now());
            habitLog.setUpdatedAt(LocalDateTime.now());
            habitLog.setStreak(1);
            habitLog.setLongestStreak(1);
        }
        
        habitLog.setStatus(HabitStatus.ACTIVE);
        
        List<LocalDateTime> completions = new ArrayList<>();
        completions.add(request.getCompletionTime());
        habitLog.setCompletionLog(completions);
        habitLog.setLastCompletionDate(request.getCompletionTime());
        
        log.info("Created habit log with title: {}, description: {}, frequency: {}", 
                habitLog.getTitle(), habitLog.getDescription(), habitLog.getFrequency());
        
        return habitLog;
    }

    /**
     * Map entity to response DTO
     */
    private HabitLogResponse mapToResponse(HabitLogs habitLog) {
        log.debug("Mapping HabitLogs to response - ID: {}, Title: {}, Description: {}, Frequency: {}", 
                habitLog.getId(), habitLog.getTitle(), habitLog.getDescription(), habitLog.getFrequency());
        
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
        } else {
            response.setTotalCompletions(0);
            response.setSuccessRate(0.0);
        }
        
        log.debug("Mapped response - Title: {}, Description: {}, Status: {}", 
                response.getTitle(), response.getDescription(), response.getStatus());
        
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
     * Test method to check activity service connection
     */
    public String testActivityServiceConnection(String habitId) {
        try {
            log.info("Testing connection to activity service for habitId: {}", habitId);
            
            // First, let's get the raw JSON response to see what we're receiving
            String rawResponse = activityServiceWebClient
                    .get()
                    .uri("/{id}", habitId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnSuccess(data -> log.info("Raw JSON response: {}", data))
                    .block();
            
            log.info("Raw response from activity service: {}", rawResponse);
            
            // Now try to parse it as our DTO
            HabitActivityResponse result = activityServiceWebClient
                    .get()
                    .uri("/{id}", habitId)
                    .retrieve()
                    .bodyToMono(HabitActivityResponse.class)
                    .doOnSuccess(data -> log.info("Test successful - received data: {}", data))
                    .doOnError(error -> log.error("Test failed with error: {}", error.getMessage()))
                    .block();
            
            if (result != null) {
                return String.format("SUCCESS: Connected to activity service. Raw JSON: %s\nParsed - ID: %s, Title: %s, Description: %s, Frequency: %s", 
                        rawResponse, result.getId(), result.getTitle(), result.getDescription(), result.getFrequency());
            } else {
                return "FAILED: Received null response from activity service. Raw JSON: " + rawResponse;
            }
        } catch (Exception e) {
            log.error("Exception during activity service test: {}", e.getMessage(), e);
            return "ERROR: " + e.getMessage();
        }
    }

    /**
     * Refresh habit data from activity service for existing logs
     */
    public HabitLogResponse refreshHabitData(String userId, String habitId) {
        log.info("Refreshing habit data for habitId: {}, userId: {}", habitId, userId);
        
        Optional<HabitLogs> existingLog = repository.findByIdAndUserId(habitId, userId);
        if (existingLog.isEmpty()) {
            throw new RuntimeException("Habit log not found: " + habitId);
        }
        
        HabitLogs habitLog = existingLog.get();
        
        // Fetch fresh data from activity service
        HabitActivityResponse habitData = fetchHabitDetails(habitId);
        if (habitData != null) {
            log.info("Updating habit log with fresh data from activity service");
            habitLog.setTitle(habitData.getTitle());
            habitLog.setDescription(habitData.getDescription());
            habitLog.setFrequency(habitData.getFrequency());
            habitLog.setDays(habitData.getDays());
            habitLog.setUpdatedAt(LocalDateTime.now());
            
            HabitLogs savedLog = repository.save(habitLog);
            return mapToResponse(savedLog);
        } else {
            log.warn("Could not refresh habit data - activity service returned null");
            return mapToResponse(habitLog);
        }
    }

    /**
     * Test method to create a habit log without user validation (for debugging)
     */
    public HabitLogResponse testCreateHabitLog(HabitLogRequest request) {
        log.info("TEST: Creating habit log without user validation for habitId: {}, userId: {}", 
                request.getHabitId(), request.getUserId());
        
        // Skip user validation for testing
        HabitLogs habitLog = createNewHabitLog(request);
        HabitLogs savedLog = repository.save(habitLog);
        
        log.info("TEST: Saved habit log with title: {}, description: {}", 
                savedLog.getTitle(), savedLog.getDescription());
        
        return mapToResponse(savedLog);
    }
}