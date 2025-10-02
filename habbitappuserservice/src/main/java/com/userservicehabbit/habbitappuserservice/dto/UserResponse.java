package com.userservicehabbit.habbitappuserservice.dto;

import com.userservicehabbit.habbitappuserservice.model.UserRole;
import lombok.Data;

import java.time.LocalDateTime;

@Data

public class UserResponse {
    private String id;
    private String email;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private LocalDateTime createdAt;
}
