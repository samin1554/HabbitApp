package com.userservicehabbit.habbitappuserservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "email cannot be blank")
    @Email(message = "email is not valid")
    private String email;

    @NotBlank(message = "password cannot be blank")
    private String password;

    @NotBlank(message = "username cannot be blank")
    private String username;

    private String firstName;
    private String lastName;

}
