package com.hipradeep.userservice.service;

import com.hipradeep.userservice.model.User;
import com.hipradeep.userservice.repository.UserRepository;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final Tracer tracer;

    public User createUser(User user) {
        Span newSpan = tracer.nextSpan().name("user-creation");
        try (Tracer.SpanInScope ws = tracer.withSpan(newSpan.start())) {
            log.info("Saving user: {}", user.getFirstName());
            return userRepository.save(user);
        } finally {
            newSpan.end();
        }
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
