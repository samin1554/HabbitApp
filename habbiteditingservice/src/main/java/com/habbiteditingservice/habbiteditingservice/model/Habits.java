package com.habbiteditingservice.habbiteditingservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@Document(collection = "habits")
@AllArgsConstructor
public class Habits {
    @Id
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