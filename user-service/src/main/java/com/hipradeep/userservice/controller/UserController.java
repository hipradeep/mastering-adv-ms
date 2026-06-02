package com.hipradeep.userservice.controller;

import com.hipradeep.userservice.dto.ApiResponse;
import com.hipradeep.userservice.dto.UserRequest;
import com.hipradeep.userservice.dto.UserResponse;
import com.hipradeep.userservice.model.User;
import com.hipradeep.userservice.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> ApiResponse.success(mapToResponse(user)))
                .orElseGet(() -> ApiResponse.error("User not found with ID: " + id, "NOT_FOUND"));
    }

    @GetMapping
    public ApiResponse<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ApiResponse.success(users);
    }

    @PostMapping
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody UserRequest userRequest) {
        User user = new User();
        user.setUsername(userRequest.getUsername());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword())); // Encrypt password using BCrypt
        user.setEmail(userRequest.getEmail());
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());

        User savedUser = userRepository.save(user);
        return ApiResponse.success(mapToResponse(savedUser));
    }

    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        );
    }
}