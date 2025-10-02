package com.userservicehabbit.habbitappuserservice.controller;


import com.userservicehabbit.habbitappuserservice.dto.RegisterRequest;
import com.userservicehabbit.habbitappuserservice.dto.UserResponse;
import com.userservicehabbit.habbitappuserservice.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserProfile(@PathVariable  String id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register( @RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(userService.register(registerRequest));
    }


    @GetMapping("/validate/{email}")
    public ResponseEntity<Boolean> checkEmailExists(@PathVariable String email) {
        return ResponseEntity.ok(userService.checkEmailExists(email));
    }

    @GetMapping("/{id}/validate")
    public ResponseEntity<Boolean> validateUser(@PathVariable String id) {
        try {
            UserResponse user = userService.getUserById(id);
            return ResponseEntity.ok(user != null);
        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
    }
}