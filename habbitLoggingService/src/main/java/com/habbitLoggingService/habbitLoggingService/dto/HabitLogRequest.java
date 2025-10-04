package com.habbitLoggingService.habbitLoggingService.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HabitLogRequest {
    private String userId; // This is the habit ID
    private LocalDateTime completionTime; // When the habit was completed
    private String notes; // Optional notes about completion
}