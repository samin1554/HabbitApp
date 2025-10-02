package com.habbitactivityservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HabitRequest {
    private String userId;
    private String title;
    private String description;
    private String frequency;      // e.g., DAILY, WEEKLY
    private List<String> days;     // e.g., ["MONDAY", "WEDNESDAY"]
}