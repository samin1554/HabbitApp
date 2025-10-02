package com.habbiteditingservice.habbiteditingservice.controller;

import com.habbiteditingservice.habbiteditingservice.dto.HabitResponse;
import com.habbiteditingservice.habbiteditingservice.dto.RequestHabit;
import com.habbiteditingservice.habbiteditingservice.editingservice.EditHabitService;
import com.habbiteditingservice.habbiteditingservice.editingservice.UserValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/habits")
@RequiredArgsConstructor
@Slf4j
public class HabitEditingController {

    private final EditHabitService editHabitService;
    private final UserValidationService userValidationService;

    @PutMapping("/{id}")
    public ResponseEntity<HabitResponse> updateHabit(
            @PathVariable String id,
            @RequestHeader("X-User-ID") String userId,
            @RequestBody RequestHabit request) {

        // Validate user exists
        if (!userValidationService.validateUser(userId)) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        try {
            HabitResponse response = editHabitService.updateHabit(id, userId, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error updating habit: {}", e.getMessage());
            if (e.getMessage().contains("not allowed")) {
                return ResponseEntity.status(403).build(); // Forbidden for wrong user
            }
            return ResponseEntity.notFound().build(); // Habit not found
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHabit(
            @PathVariable String id,
            @RequestHeader("X-User-ID") String userId) {

        // Validate user exists
        if (!userValidationService.validateUser(userId)) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        try {
            editHabitService.deleteHabit(id, userId);
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (RuntimeException e) {
            log.error("Error deleting habit: {}", e.getMessage());
            if (e.getMessage().contains("not allowed")) {
                return ResponseEntity.status(403).build(); // Forbidden
            }
            return ResponseEntity.notFound().build(); // Habit not found
        }
    }
}