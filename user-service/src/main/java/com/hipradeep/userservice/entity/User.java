package com.hipradeep.userservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "User", timeToLive = 600) // 10 minutes TTL
public class User implements Serializable {
    @Id
    private Long id;

    @Indexed
    private String username;

    @Indexed
    private String email;

    private String firstName;
    private String lastName;
}
