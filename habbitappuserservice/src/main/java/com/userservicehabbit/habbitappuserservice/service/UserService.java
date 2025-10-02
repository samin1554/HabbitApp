package com.userservicehabbit.habbitappuserservice.service;

import com.userservicehabbit.habbitappuserservice.dto.RegisterRequest;
import com.userservicehabbit.habbitappuserservice.dto.UserResponse;
import com.userservicehabbit.habbitappuserservice.model.Users;
import com.userservicehabbit.habbitappuserservice.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j

public class UserService {
    @Autowired
    private UserRepository userRepository;

    //Check for email -> if exists -> returns details
    public UserResponse register(@Valid RegisterRequest registerRequest){
        if(userRepository.existsByEmail(registerRequest.getEmail())) {
            Users existingUser = userRepository.findByEmail(registerRequest.getEmail());
            UserResponse userResponse = new UserResponse();
            userResponse.setId(existingUser.getId());
            userResponse.setEmail(existingUser.getEmail());
            userResponse.setUsername(existingUser.getUsername());
            userResponse.setPassword(existingUser.getPassword());
            userResponse.setFirstName(existingUser.getFirstName());
            userResponse.setLastName(existingUser.getLastName());
            userResponse.setCreatedAt(existingUser.getCreatedAt());

            return userResponse;
        }

        // email doesnt exist so create new user

        Users user = new Users();
        user.setEmail(registerRequest.getEmail());
        user.setUsername(registerRequest.getUsername());
        user.setPassword(registerRequest.getPassword());
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        userRepository.save(user);


        // create and return new user
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setEmail(user.getEmail());
        userResponse.setUsername(user.getUsername());
        userResponse.setPassword(user.getPassword());
        userResponse.setFirstName(user.getFirstName());
        userResponse.setLastName(user.getLastName());
        userResponse.setCreatedAt(user.getCreatedAt());
        return userResponse;


    }

    public UserResponse getUserById(String id) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setEmail(user.getEmail());
        userResponse.setUsername(user.getUsername());
        userResponse.setPassword(user.getPassword());
        userResponse.setFirstName(user.getFirstName());
        userResponse.setLastName(user.getLastName());
        userResponse.setCreatedAt(user.getCreatedAt());
        return userResponse;
    }


    public boolean checkEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

}
