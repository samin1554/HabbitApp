package com.habbiteditingservice.habbiteditingservice.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HabitResponse {
    private String id;
    private String userId;
    private String title;
    private String description;
    private String frequency;
    private String days;
    private int streak;
    private int longestStreak;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
