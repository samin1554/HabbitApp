package com.habbitactivityservice.service;

import com.habbitactivityservice.dto.HabitRequest;
import com.habbitactivityservice.dto.HabitResponse;
import com.habbitactivityservice.model.Habit;
import com.habbitactivityservice.repository.HabitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HabitService {

    private final HabitRepository habitRepository;
    private final UserValidationService userValidationService;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name}")
    private String exchange;

    @Value("${rabbitmq.routing.key}")
    private String routing;

    /**
     * Create a new habit for a registered user
     */
    public HabitResponse createHabit(HabitRequest request) {
        log.info("Creating habit for user: {}", request.getUserId());

        // 1. Validate user
        boolean isValidUser = userValidationService.validateUser(request.getUserId());
        if (!isValidUser) {
            throw new RuntimeException("Invalid User: " + request.getUserId());
        }

        // 2. Build Habit
        Habit habit = new Habit();
        habit.setUserId(request.getUserId());
        habit.setTitle(request.getTitle());
        habit.setDescription(request.getDescription());
        habit.setFrequency(request.getFrequency());
        habit.setDays(request.getDays());
        habit.setStreak(0); // default
        habit.setLongestStreak(0); // default
        habit.setCreatedAt(LocalDateTime.now());
        habit.setUpdatedAt(LocalDateTime.now());

        // 3. Save Habit
        Habit savedHabit = habitRepository.save(habit);
        log.info("Habit saved with ID: {}", savedHabit.getId());

        // 4. Send message to RabbitMQ
        try {
            rabbitTemplate.convertAndSend(exchange, routing, savedHabit);
            log.info("Habit sent to RabbitMQ");
        } catch (Exception e) {
            log.error("Error sending habit to RabbitMQ: {}", e.getMessage(), e);
        }

        // 5. Return mapped response
        return mapResponse(savedHabit);
    }

    /**
     * Get all habits for a given user
     */
    public List<HabitResponse> getHabitsByUserId(String userId) {
        boolean isValidUser = userValidationService.validateUser(userId);
        if (!isValidUser) {
            throw new RuntimeException("Invalid User: " + userId);
        }

        List<Habit> habits = habitRepository.findByUserId(userId);
        return habits.stream().map(this::mapResponse).collect(Collectors.toList());
    }

    /**
     * Get a specific habit by ID
     */
    public HabitResponse getHabitById(String habitId) {
        return habitRepository.findById(habitId)
                .map(this::mapResponse)
                .orElseThrow(() -> new RuntimeException("Habit not found with Id: " + habitId));
    }

    /**
     * Helper: Map Habit -> HabitResponse
     */
    private HabitResponse mapResponse(Habit habit) {
        HabitResponse response = new HabitResponse();
        response.setId(habit.getId());
        response.setUserId(habit.getUserId());
        response.setTitle(habit.getTitle());
        response.setDescription(habit.getDescription());
        response.setFrequency(habit.getFrequency());
        response.setDays(habit.getDays());
        response.setStreak(habit.getStreak());
        response.setLongestStreak(habit.getLongestStreak());
        response.setCreatedAt(habit.getCreatedAt());
        response.setUpdatedAt(habit.getUpdatedAt());
        return response;
    }
}