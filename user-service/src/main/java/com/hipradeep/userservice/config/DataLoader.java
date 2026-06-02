package com.hipradeep.userservice.config;

import com.hipradeep.userservice.model.User;
import com.hipradeep.userservice.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class DataLoader {

    private final UserRepository userRepository;

    public DataLoader(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void init() {
        // Only initialize username and password in database
        userRepository.save(new User(null, "admin", "admin123"));
        userRepository.save(new User(null, "user", "password123"));
        
        System.out.println("=== Database User Initialization Completed successfully ===");
    }
}