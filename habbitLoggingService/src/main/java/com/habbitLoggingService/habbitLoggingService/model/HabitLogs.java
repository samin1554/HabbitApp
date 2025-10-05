package com.habbitLoggingService.habbitLoggingService.model;

import org.springframework.data.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "habit_logs")
@NoArgsConstructor
@AllArgsConstructor
public class HabitLogs {

    @Id
    private String id; // This is the habit ID (same as in other services)
    private String userId; // This is the user ID (same as in other services)
    private String title;
    private String description;
    private String frequency; // e.g., "daily", "3 times per week"
    private List<String> days; // Which days of the week
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int streak;
    private int longestStreak;
    private List<LocalDateTime> completionLog; // When habit was completed
    private LocalDateTime lastCompletionDate; // Last time habit was completed
    private int targetCount; // How many times per period (e.g., 3 for "3 times per week")
    private HabitStatus status;
}