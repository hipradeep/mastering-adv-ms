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
    public String createUser(@RequestBody String userData, @RequestParam(required = false) Integer partition) {
        String message = "USER_CREATED: " + userData;
        kafkaProducerService.sendMessage(message, partition);
        return "User created event sent to partition " + (partition != null ? partition : "default") + ": " + message;
    }

    @PutMapping("/update")
    public String updateUser(@RequestBody String userData, @RequestParam(required = false) Integer partition) {
        String message = "USER_UPDATED: " + userData;
        kafkaProducerService.sendMessage(message, partition);
        return "User updated event sent to partition " + (partition != null ? partition : "default") + ": " + message;
    }

    @DeleteMapping("/delete")
    public String deleteUser(@RequestBody String userId, @RequestParam(required = false) Integer partition) {
        String message = "USER_DELETED: " + userId;
        kafkaProducerService.sendMessage(message, partition);
        return "User deleted event sent to partition " + (partition != null ? partition : "default") + ": " + message;
    }
}