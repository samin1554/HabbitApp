package com.habbiteditingservice.habbiteditingservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class RequestHabit {
    private String userId;
    private String habitId;
    private String title;
    private String description;
    private String frequency;
    private List<String> days;

}

