package com.hipradeep.userservice.controller;

import com.hipradeep.userservice.service.KafkaProducerService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    private final KafkaProducerService kafkaProducerService;

    public UserController(KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
    }

    @PostMapping("/create")
    public String createUser(@RequestBody String userData) {
        String message = "USER_CREATED: " + userData;
        kafkaProducerService.sendMessage(message);
        return "User created event sent: " + message;
    }

    @PutMapping("/update")
    public String updateUser(@RequestBody String userData) {
        String message = "USER_UPDATED: " + userData;
        kafkaProducerService.sendMessage(message);
        return "User updated event sent: " + message;
    }

    @DeleteMapping("/delete")
    public String deleteUser(@RequestBody String userId) {
        String message = "USER_DELETED: " + userId;
        kafkaProducerService.sendMessage(message);
        return "User deleted event sent: " + message;
    }
}