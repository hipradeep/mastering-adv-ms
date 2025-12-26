package com.hipradeep.userservice.controller;

import com.hipradeep.userservice.dto.ApiResponse;
import com.hipradeep.userservice.entity.User;
import com.hipradeep.userservice.entity.User2;
import com.hipradeep.userservice.service.UserService2;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
    @RequestMapping("/api2/users")
public class UserController2 {

    private final UserService2 userService;

    public UserController2(UserService2 userService) {
        this.userService = userService;
    }

    @PostMapping
    public ApiResponse<User2> createUser(@RequestBody User2 user) {
        return ApiResponse.success(userService.createUser(user));
    }

    @GetMapping
    public ApiResponse<List<User2>> getAllUsers() {
        return ApiResponse.success(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ApiResponse<User2> getUserById(@PathVariable Long id) {
        return ApiResponse.success(userService.getUserById(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteUser(@PathVariable Long id) {
        return ApiResponse.success(userService.deleteUser(id));
    }

}