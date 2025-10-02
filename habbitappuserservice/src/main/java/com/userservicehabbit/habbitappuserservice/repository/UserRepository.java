package com.userservicehabbit.habbitappuserservice.repository;

import com.userservicehabbit.habbitappuserservice.model.Users;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, String> {
    Optional<Users> findById(String id);
    boolean existsByEmail(String email);


    Users findByEmail(@NotBlank(message = "email cannot be blank") @Email(message = "email is not valid") String email);
}
