package com.habbitLoggingService.habbitLoggingService.repository;

import com.habbitLoggingService.habbitLoggingService.model.HabitLogs;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HabitLoggingRepository  extends MongoRepository<HabitLogs, String> {
}