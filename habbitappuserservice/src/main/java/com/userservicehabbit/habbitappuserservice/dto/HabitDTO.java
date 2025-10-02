package com.userservicehabbit.habbitappuserservice.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class HabitDTO {
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