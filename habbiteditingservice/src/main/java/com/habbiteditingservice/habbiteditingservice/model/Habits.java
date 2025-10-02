package com.habbiteditingservice.habbiteditingservice.model;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@Table
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
