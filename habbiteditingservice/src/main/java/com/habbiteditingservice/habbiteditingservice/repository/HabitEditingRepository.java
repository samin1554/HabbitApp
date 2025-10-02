package com.habbiteditingservice.habbiteditingservice.repository;

import com.habbiteditingservice.habbiteditingservice.model.Habits;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HabitEditingRepository extends MongoRepository<Habits, String> {

    List<Habits> findByUserId(String userId);
    Optional<Habits> findByHabitId(String Id);

    List<Habits> findByUserIdAndTitle(String userId, String title);
    List<Habits> findByUserIdAndStreak(String userId, int streak);
    List<Habits> findByUserIdAndLongestStreak(String userId, int longestStreak);






}
