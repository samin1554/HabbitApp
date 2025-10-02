package com.habbitactivityservice.controller;

import com.habbitactivityservice.dto.HabitRequest;
import com.habbitactivityservice.dto.HabitResponse;
import com.habbitactivityservice.service.HabitService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/habits")
@AllArgsConstructor
@Slf4j
public class HabitController {

    private final HabitService habitService;

    @PostMapping("/create")
    public ResponseEntity<HabitResponse> createHabit(@RequestBody HabitRequest habitRequest) {
        log.info("Received createHabit request: {}", habitRequest);
        HabitResponse response = habitService.createHabit(habitRequest);
        log.info("Habit created successfully with ID: {}", response.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HabitResponse> getHabitById(@PathVariable String id) {
        log.info("Received getHabitById request for ID: {}", id);
        HabitResponse response = habitService.getHabitById(id);
        log.info("Found habit with ID: {}", id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<HabitResponse>> getUserHabits(@PathVariable String userId) {
        log.info("Received getUserHabits request for userId: {}", userId);
        List<HabitResponse> response = habitService.getHabitsByUserId(userId);
        log.info("Found {} habits for userId: {}", response.size(), userId);
        return ResponseEntity.ok(response);
    }
}