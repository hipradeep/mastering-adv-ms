package com.hipradeep.authserver.controller;

import com.hipradeep.authserver.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> creds) {
        String username = creds.get("username");
        String password = creds.get("password");

        // Simple in-memory check for demo
        if("admin".equals(username) && "admin123".equals(password)) {
            List<String> roles = List.of("ADMIN"); // assign ADMIN role
            String token = JwtUtil.generateToken(username, roles);
            return Map.of("token", token);
        }

        // Optionally handle other users
        if("user".equals(username) && "user123".equals(password)) {
            List<String> roles = List.of("USER"); // assign USER role
            String token = JwtUtil.generateToken(username, roles);
            return Map.of("token", token);
        }

        throw new RuntimeException("Invalid credentials");
    }
}

