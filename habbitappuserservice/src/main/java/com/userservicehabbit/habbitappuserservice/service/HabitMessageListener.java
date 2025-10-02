package com.userservicehabbit.habbitappuserservice.service;

import com.userservicehabbit.habbitappuserservice.dto.HabitDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HabitMessageListener {

    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void handleHabitMessage(HabitDTO habit) {
        log.info("Received habit message: {}", habit);

    }
}