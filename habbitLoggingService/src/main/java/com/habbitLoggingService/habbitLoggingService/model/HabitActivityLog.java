package com.habbitLoggingService.habbitLoggingService.model;

import org.springframework.data.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Data
@Builder
@Document(collection = "habit_activity_logs")
@NoArgsConstructor
@AllArgsConstructor
public class HabitActivityLog {
    
    @Id
    private String id;
    
    private String habitId;        // Reference to habit in activity service
    private String userId;
    
    @CreatedDate
    private LocalDateTime loggedAt;
    
    private LocalDateTime completedAt;  // When the habit was actually completed
    private ActivityType activityType;  // COMPLETED, MISSED, RESET, SKIPPED
    private String notes;              // Optional user notes
    private String source;             // WEB, MOBILE, API
    
    // Metadata for analytics
    private boolean isOnTime;          // Was it completed on the expected day?
    private int daysSinceLastCompletion;
}