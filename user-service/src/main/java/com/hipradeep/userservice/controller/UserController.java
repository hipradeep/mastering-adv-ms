package com.hipradeep.userservice.controller;

import com.hipradeep.userservice.dto.ApiResponse;
import com.hipradeep.userservice.entity.User;
import com.hipradeep.userservice.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ApiResponse<User> createUser(@RequestBody User user) {
        return ApiResponse.success(userService.createUser(user));
    }

    @GetMapping
    public ApiResponse<List<User>> getAllUsers() {
        return ApiResponse.success(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ApiResponse<User> getUserById(@PathVariable Long id) {
        return ApiResponse.success(userService.getUserById(id));
    }

    @GetMapping("/username/{username}")
    public ApiResponse<User> getUserByUsername(@PathVariable String username) {
        return ApiResponse.success(userService.getUserByUsername(username));
    }

    @GetMapping("/email/{email}")
    public ApiResponse<User> getUserByEmail(@PathVariable String email) {
        return ApiResponse.success(userService.getUserByEmail(email));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteUser(@PathVariable Long id) {
        return ApiResponse.success(userService.deleteUser(id));
    }

}