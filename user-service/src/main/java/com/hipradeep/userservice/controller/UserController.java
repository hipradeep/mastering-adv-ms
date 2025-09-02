package com.hipradeep.userservice.controller;

import com.hipradeep.userservice.dto.Order;
import com.hipradeep.userservice.dto.User;
import com.hipradeep.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Create a new user
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        // Check if username or email already exists
        if (userService.usernameExists(user.getUsername())) {
            return ResponseEntity.badRequest().body(null);
        }
        if (userService.emailExists(user.getEmail())) {
            return ResponseEntity.badRequest().body(null);
        }

        User createdUser = userService.createUser(user);
        return ResponseEntity.ok(createdUser);
    }

    // Get all users
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // Get user by ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get user by username
    @GetMapping("/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        Optional<User> user = userService.getUserByUsername(username);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get user by email
    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        Optional<User> user = userService.getUserByEmail(email);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get users by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<User>> getUsersByStatus(@PathVariable String status) {
        List<User> users = userService.getUsersByStatus(status);
        return ResponseEntity.ok(users);
    }

    // Update entire user
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        Optional<User> updatedUser = userService.updateUser(id, userDetails);
        return updatedUser.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update user status only
    @PatchMapping("/{id}/status")
    public ResponseEntity<User> updateUserStatus(@PathVariable Long id, @RequestBody String status) {
        Optional<User> updatedUser = userService.updateUserStatus(id, status);
        return updatedUser.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update last login
    @PatchMapping("/{id}/last-login")
    public ResponseEntity<User> updateLastLogin(@PathVariable Long id) {
        Optional<User> updatedUser = userService.updateLastLogin(id);
        return updatedUser.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Delete user
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        boolean deleted = userService.deleteUser(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    // Check if username exists
    @GetMapping("/check-username/{username}")
    public ResponseEntity<Boolean> checkUsernameExists(@PathVariable String username) {
        boolean exists = userService.usernameExists(username);
        return ResponseEntity.ok(exists);
    }

    // Check if email exists
    @GetMapping("/check-email/{email}")
    public ResponseEntity<Boolean> checkEmailExists(@PathVariable String email) {
        boolean exists = userService.emailExists(email);
        return ResponseEntity.ok(exists);
    }

    // Get user with orders
    @GetMapping("/{id}/with-orders")
    public ResponseEntity<User> getUserWithOrders(@PathVariable Long id) {
        Optional<User> user = userService.getUserWithOrders(id);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get all users with orders
    @GetMapping("/with-orders")
    public ResponseEntity<List<User>> getAllUsersWithOrders() {
        List<User> users = userService.getAllUsersWithOrders();
        return ResponseEntity.ok(users);
    }

    // http://localhost:8080/api/users/status/ACTIVE/with-orders
    // Get users by status with orders
    @GetMapping("/status/{status}/with-orders")
    public ResponseEntity<List<User>> getUsersByStatusWithOrders(@PathVariable String status) {
        List<User> users = userService.getUsersByStatusWithOrders(status);
        return ResponseEntity.ok(users);
    }

    // Get orders for a specific user
    @GetMapping("/{id}/orders")
    public ResponseEntity<List<Order>> getUserOrders(@PathVariable Long id) {
        Optional<User> user = userService.getUserWithOrders(id);
        return user.map(u -> ResponseEntity.ok(u.getOrders()))
                .orElse(ResponseEntity.notFound().build());
    }

    // Create order for user
    @PostMapping("/{id}/orders")
    public ResponseEntity<Order> createOrderForUser(@PathVariable Long id, @RequestBody Order order) {
        Optional<User> userOptional = userService.getUserById(id);
        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOptional.get();
        order.setCustomerId(user.getId());
        order.setCustomerName(user.getFirstName() + " " + user.getLastName());

        // You might want to use the OrderServiceClient here
        // For simplicity, we'll just return the order object
        return ResponseEntity.ok(order);
    }
}