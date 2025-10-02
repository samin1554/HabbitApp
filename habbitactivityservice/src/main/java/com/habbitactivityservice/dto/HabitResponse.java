package com.habbitactivityservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HabitResponse {
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
}