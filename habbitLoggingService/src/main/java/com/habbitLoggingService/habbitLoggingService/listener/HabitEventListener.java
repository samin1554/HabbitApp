package com.habbitLoggingService.habbitLoggingService.listener;

import com.habbitLoggingService.habbitLoggingService.dto.HabitLogRequest;
import com.habbitLoggingService.habbitLoggingService.service.HabitLoggingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class HabitEventListener {

    private final HabitLoggingService habitLoggingService;

    /**
     * Listen for habit completion events from other services
     */
    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void handleHabitEvent(HabitEventMessage message) {
        log.info("Received habit event: {} for user: {}", message.getEventType(), message.getUserId());
        
        try {
            switch (message.getEventType()) {
                case "HABIT_COMPLETED":
                    handleHabitCompletion(message);
                    break;
                case "HABIT_CREATED":
                    log.info("Habit created: {} for user: {}", message.getHabitId(), message.getUserId());
                    // You can initialize analytics here if needed
                    break;
                case "HABIT_UPDATED":
                    log.info("Habit updated: {} for user: {}", message.getHabitId(), message.getUserId());
                    // Update analytics if needed
                    break;
                case "HABIT_DELETED":
                    log.info("Habit deleted: {} for user: {}", message.getHabitId(), message.getUserId());
                    // Archive or delete analytics if needed
                    break;
                default:
                    log.warn("Unknown event type: {}", message.getEventType());
            }
        } catch (Exception e) {
            log.error("Error processing habit event: {}", e.getMessage(), e);
        }
    }

    private void handleHabitCompletion(HabitEventMessage message) {
        HabitLogRequest request = new HabitLogRequest();
        request.setHabitId(message.getHabitId());
        request.setUserId(message.getUserId());
        request.setCompletionTime(message.getTimestamp() != null ? message.getTimestamp() : LocalDateTime.now());
        request.setNotes(message.getNotes());
        
        habitLoggingService.logHabitCompletion(request);
        log.info("Successfully logged habit completion for habit: {} user: {}", 
                message.getHabitId(), message.getUserId());
    }

    /**
     * Message structure for habit events
     */
    public static class HabitEventMessage {
        private String eventType;
        private String habitId;
        private String userId;
        private LocalDateTime timestamp;
        private String notes;

        // Getters and setters
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        
        public String getHabitId() { return habitId; }
        public void setHabitId(String habitId) { this.habitId = habitId; }
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }
}