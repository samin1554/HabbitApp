package com.habbiteditingservice.habbiteditingservice.repository;

import com.habbiteditingservice.habbiteditingservice.model.Habits;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HabitEditingRepository extends MongoRepository<Habits, String> {

    // Get all habits for a user
    List<Habits> findByUserId(String userId);

    // Get a single habit by habitId and userId (ownership enforced)
    Optional<Habits> findByIdAndUserId(String id, String userId);

    // Get all habits with a specific title for a user
    List<Habits> findByUserIdAndTitle(String userId, String title);
}