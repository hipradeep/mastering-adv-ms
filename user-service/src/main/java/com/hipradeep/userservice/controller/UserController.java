package com.hipradeep.userservice.controller;

import com.hipradeep.userservice.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    @PostMapping
    public ApiResponse<String> createUser(@RequestBody Object userRequest) {
        log.info("Received request to create user: {}", userRequest);
        return ApiResponse.success("User created successfully (Simulated)");
    }

}