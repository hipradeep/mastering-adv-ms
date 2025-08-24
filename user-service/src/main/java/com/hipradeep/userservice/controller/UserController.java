package com.hipradeep.userservice.controller;


import com.hipradeep.userservice.dto.ApiResponse;
import com.hipradeep.userservice.util.JsonUtils;
import com.hipradeep.userservice.dto.UserResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUserById(@PathVariable Long id) {
        // Simulate getting user from database
        UserResponse user = new UserResponse(id, "johndoe", "john@example.com", "John", "Doe");

        // Using common lib ApiResponse
        return ApiResponse.success(user);
    }

    @GetMapping
    public ApiResponse<List<UserResponse>> getAllUsers() {
        // Simulate getting all users
        List<UserResponse> users = List.of(
                new UserResponse(1L, "johndoe", "john@example.com", "John", "Doe"),
                new UserResponse(2L, "janedoe", "jane@example.com", "Jane", "Doe")
        );

        return ApiResponse.success(users);
    }

    @PostMapping
    public ApiResponse<UserResponse> createUser(@RequestBody UserResponse userRequest) {
        // Simulate user creation
        UserResponse createdUser = new UserResponse(
                3L,
                userRequest.getUsername(),
                userRequest.getEmail(),
                userRequest.getFirstName(),
                userRequest.getLastName()
        );

        return ApiResponse.success(createdUser);
    }

    @GetMapping("/json-test")
    public String testJsonUtils() {
        // Test common lib JsonUtils
        UserResponse user = new UserResponse(1L, "testuser", "test@example.com", "Test", "User");

        String json = JsonUtils.toJson(user);
        System.out.println("JSON Output: " + json);

        // Convert back to object
        UserResponse parsedUser = JsonUtils.fromJson(json, UserResponse.class);
        System.out.println("Parsed User: " + parsedUser.getUsername());

        return json;
    }
}