package com.habbiteditingservice.habbiteditingservice.editingservice;

import com.habbiteditingservice.habbiteditingservice.dto.HabitResponse;
import com.habbiteditingservice.habbiteditingservice.dto.RequestHabit;
import com.habbiteditingservice.habbiteditingservice.model.Habits;
import com.habbiteditingservice.habbiteditingservice.repository.HabitEditingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class EditHabitService {

    private final HabitEditingRepository habitEditingRepository;
    private final RabbitTemplate rabbitTemplate;
    private final UserValidationService userValidationService;

    @Value("${rabbitmq.exchange.name}")
    private String exchange;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    // Update habit
    public HabitResponse updateHabit(String id, String userId, RequestHabit request) {
        // Validate user
        if (!userValidationService.validateUser(userId)) {
            throw new RuntimeException("Invalid user: " + userId);
        }

        // Find habit by ID and userId (ownership enforced in repository)
        Habits habit = habitEditingRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException(
                        "Habit not found with ID: " + id + " for user: " + userId));

        // Update fields
        habit.setTitle(request.getTitle());
        habit.setDescription(request.getDescription());
        habit.setFrequency(request.getFrequency());
        habit.setDays(request.getDays());
        habit.setUpdatedAt(LocalDateTime.now());

        // Save
        Habits savedHabit = habitEditingRepository.save(habit);

        // Send message via RabbitMQ
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, savedHabit);
            log.info("Habit update sent to RabbitMQ with ID: {}", savedHabit.getId());
        } catch (Exception e) {
            log.error("Error sending habit update to RabbitMQ: {}", e.getMessage(), e);
        }

        return mapToResponse(savedHabit);
    }

    // Delete habit
    public void deleteHabit(String id, String userId) {
        // Validate user
        if (!userValidationService.validateUser(userId)) {
            throw new RuntimeException("Invalid user: " + userId);
        }

        // Find habit by ID and userId (ownership enforced)
        Habits habit = habitEditingRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException(
                        "Habit not found with ID: " + id + " for user: " + userId));

        // Delete
        habitEditingRepository.deleteById(id);

        // Send message via RabbitMQ
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, habit);
            log.info("Habit deletion sent to RabbitMQ with ID: {}", habit.getId());
        } catch (Exception e) {
            log.error("Error sending habit deletion to RabbitMQ: {}", e.getMessage(), e);
        }
    }

    private HabitResponse mapToResponse(Habits habit) {
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