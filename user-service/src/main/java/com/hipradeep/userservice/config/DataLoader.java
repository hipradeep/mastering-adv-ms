package com.hipradeep.userservice.config;

import com.hipradeep.userservice.model.User;
import com.hipradeep.userservice.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void init() {
        // Initialize username and bcrypted password in database
        userRepository.save(new User(null, "admin", passwordEncoder.encode("admin123"), "admin@example.com", "System", "Admin"));
        userRepository.save(new User(null, "user", passwordEncoder.encode("password123"), "user@example.com", "Regular", "User"));
        
        System.out.println("=== Database User Initialization (BCrypted) Completed successfully ===");
    }
}