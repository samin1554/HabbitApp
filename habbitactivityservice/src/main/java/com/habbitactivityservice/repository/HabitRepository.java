package com.habbitactivityservice.repository;

import com.habbitactivityservice.model.Habit;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HabitRepository extends MongoRepository<Habit, String> {
    List<Habit> findByUserId(String userId);
}