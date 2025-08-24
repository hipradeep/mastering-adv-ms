package com.hipradeep.userservice.config;

import com.hipradeep.userservice.dto.UserResponse;
import com.hipradeep.userservice.util.JsonUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class DataLoader {

    @PostConstruct
    public void init() {
        // Demonstrate using common lib JsonUtils
        UserResponse sampleUser = new UserResponse(100L, "admin", "admin@example.com", "System", "Admin");
        String json = JsonUtils.toJson(sampleUser);
        System.out.println("Sample User JSON: " + json);
    }
}