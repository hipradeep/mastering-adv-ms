package com.hipradeep.userservice.service;

import com.hipradeep.userservice.model.User;
import com.hipradeep.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public User createUser(User user) {
        log.info("Saving user: {}", user.getFirstName());
        return userRepository.save(user);
    }

    public User getUserById(Long id) {
        log.info("Fetching user with id: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<User> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll();
    }
}
