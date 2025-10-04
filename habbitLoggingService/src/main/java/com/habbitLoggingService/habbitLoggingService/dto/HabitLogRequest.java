package com.habbitLoggingService.habbitLoggingService.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HabitLogRequest {
    private String habitId; // Reference to habit in activity service
    private String userId;  // User who owns the habit
    private LocalDateTime completionTime; // When the habit was completed
    private String notes; // Optional notes about completion
}