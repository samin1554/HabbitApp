package com.habbitLoggingService.habbitLoggingService.controller;

import com.habbitLoggingService.habbitLoggingService.dto.HabitLogRequest;
import com.habbitLoggingService.habbitLoggingService.dto.HabitLogResponse;
import com.habbitLoggingService.habbitLoggingService.service.HabitLoggingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/habit-logs")
@RequiredArgsConstructor
@Slf4j
public class HabitLoggingController {

    private final HabitLoggingService habitLoggingService;

    /**
     * Log a habit completion
     * POST /api/habit-logs/complete
     */
    @PostMapping("/complete")
    public ResponseEntity<HabitLogResponse> logHabitCompletion(@RequestBody HabitLogRequest request) {
        log.info("Received habit completion request for user: {}", request.getUserId());
        
        try {
            HabitLogResponse response = habitLoggingService.logHabitCompletion(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error logging habit completion: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get all habit analytics for a user
     * GET /api/habit-logs/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<HabitLogResponse>> getUserHabitAnalytics(@PathVariable String userId) {
        log.info("Getting habit analytics for user: {}", userId);
        
        try {
            List<HabitLogResponse> analytics = habitLoggingService.getUserHabitAnalytics(userId);
            return ResponseEntity.ok(analytics);
        } catch (RuntimeException e) {
            log.error("Error getting user analytics: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get specific habit analytics
     * GET /api/habit-logs/user/{userId}/habit/{habitTitle}
     */
    @GetMapping("/user/{userId}/habit/{habitTitle}")
    public ResponseEntity<HabitLogResponse> getHabitAnalytics(
            @PathVariable String userId, 
            @PathVariable String habitTitle) {
        log.info("Getting analytics for habit: {} of user: {}", habitTitle, userId);
        
        try {
            HabitLogResponse analytics = habitLoggingService.getHabitAnalytics(userId, habitTitle);
            return ResponseEntity.ok(analytics);
        } catch (RuntimeException e) {
            log.error("Error getting habit analytics: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Health check endpoint
     * GET /api/habit-logs/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Habit Logging Service is running!");
    }
}