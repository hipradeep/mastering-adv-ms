package com.hipradeep.userservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
public class CacheMonitorController {

    private final RedisConnectionFactory redisConnectionFactory;

    @GetMapping("/info")
    public Map<String, Object> getCacheInfo() {
        Map<String, Object> info = new HashMap<>();

        try (var connection = redisConnectionFactory.getConnection()) {
            var serverInfo = connection.info();
            if (serverInfo != null) {
                info.put("redis_version", serverInfo.getProperty("redis_version"));
                info.put("used_memory", serverInfo.getProperty("used_memory_human"));
                info.put("connected_clients", serverInfo.getProperty("connected_clients"));
                info.put("total_commands_processed", serverInfo.getProperty("total_commands_processed"));
            }

            // Get database size
            info.put("db_size", connection.dbSize());

            // Get keys patterns
            // Note: In production, avoid 'keys' command if DB is huge. scan() is preferred.
            try {
                var keyCommands = connection.keyCommands();
                info.put("user_keys_count", keyCommands.keys("users::*".getBytes()).size());
                info.put("all_keys_count", keyCommands.keys("*".getBytes()).size());
            } catch (Exception e) {
                info.put("keys_error", "Could not fetch keys: " + e.getMessage());
            }
        }

        return info;
    }

    @DeleteMapping("/flush")
    public String flushCache() {
        try (var connection = redisConnectionFactory.getConnection()) {
            connection.serverCommands().flushAll();
            return "Redis cache flushed successfully!";
        } catch (Exception e) {
            return "Failed to flush cache: " + e.getMessage();
        }
    }
}
