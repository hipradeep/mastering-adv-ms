package com.hipradeep.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String status; // ACTIVE, INACTIVE, PENDING
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private List<Address> addresses;

    // Transient field for orders (not stored in user service)
    private transient List<Order> orders;
}

