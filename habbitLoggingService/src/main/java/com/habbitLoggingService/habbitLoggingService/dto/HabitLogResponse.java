package com.habbitLoggingService.habbitLoggingService.dto;

import com.habbitLoggingService.habbitLoggingService.model.HabitStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class HabitLogResponse {
    private String id;
    private String userId;
    private String title;
    private String description;
    private String frequency;
    private List<String> days;
    private int streak;
    private int longestStreak;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<LocalDateTime> completionLog;
    private LocalDateTime lastCompletionDate;
    private int targetCount;
    private HabitStatus status;
    private double successRate; // Percentage of successful completions
    private int totalCompletions;
    private LocalDateTime nextExpectedCompletion;
}