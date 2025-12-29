package com.hipradeep.userservice.controller;

import com.hipradeep.userservice.model.User;
import com.hipradeep.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@RequestBody User user) {
        log.info("Received request to create user: {}", user);
        return userService.createUser(user);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public User getUser(@PathVariable Long id) {
        log.info("Received request to get user by ID: {}", id);
        return userService.getUserById(id);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<User> getAllUsers() {
        log.info("Received request to get all users");
        return userService.getAllUsers();
    }

    @GetMapping("/trace")
    public String simulateTrace() {
        log.info("Simulating trace in User Service");
        return "Trace simulated! Check logs for Trace ID.";
    }

}