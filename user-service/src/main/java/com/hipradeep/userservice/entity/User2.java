package com.hipradeep.userservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User2 implements Serializable {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
}
