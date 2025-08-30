package com.hipradeep.authserver.controller;

import com.hipradeep.authserver.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

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
            String token = JwtUtil.generateToken(username);
            return Map.of("token", token);
        }
        throw new RuntimeException("Invalid credentials");
    }
}
