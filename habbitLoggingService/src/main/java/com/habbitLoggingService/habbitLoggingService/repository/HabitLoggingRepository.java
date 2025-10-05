package com.habbitLoggingService.habbitLoggingService.repository;

import com.habbitLoggingService.habbitLoggingService.model.HabitLogs;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HabitLoggingRepository extends MongoRepository<HabitLogs, String> {
    
    // Find all habits for a user
    List<HabitLogs> findByUserId(String userId);
    
    // Find a specific habit log by habit ID
    Optional<HabitLogs> findById(String id);
    
    // Find a specific habit log by user ID and habit ID
    Optional<HabitLogs> findByIdAndUserId(String id, String userId);
    
    // Find habits by status
    List<HabitLogs> findByUserIdAndStatus(String userId, com.habbitLoggingService.habbitLoggingService.model.HabitStatus status);
    
    // Find habits with completions in date range
    @Query("{'userId': ?0, 'completionLog': {$elemMatch: {$gte: ?1, $lte: ?2}}}")
    List<HabitLogs> findByUserIdAndCompletionLogBetween(String userId, LocalDateTime start, LocalDateTime end);
    
    // Find habits with current streak greater than specified value
    List<HabitLogs> findByUserIdAndStreakGreaterThan(String userId, int minStreak);
}