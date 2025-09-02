package com.hipradeep.userservice.service;

import com.hipradeep.userservice.client.OrderServiceFeignClient;
import com.hipradeep.userservice.dto.Address;
import com.hipradeep.userservice.dto.Order;
import com.hipradeep.userservice.dto.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserService {

    private final Map<Long, User> users = new HashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);
    private final OrderServiceFeignClient orderServiceClient;

    public UserService(OrderServiceFeignClient orderServiceClient) {
        this.orderServiceClient = orderServiceClient;
        initializeDummyData();
    }

    private void initializeDummyData() {
        // Create some dummy users
        User user1 = User.builder()
                .id(idCounter.getAndIncrement())
                .username("johndoe")
                .email("john.doe@email.com")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("+1-555-0101")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now().minusDays(30))
                .lastLogin(LocalDateTime.now().minusHours(2))
                .addresses(Arrays.asList(
                        Address.builder()
                                .street("123 Main St")
                                .city("New York")
                                .state("NY")
                                .zipCode("10001")
                                .country("USA")
                                .isPrimary(true)
                                .build()
                ))
                .build();

        User user2 = User.builder()
                .id(idCounter.getAndIncrement())
                .username("janesmith")
                .email("jane.smith@email.com")
                .firstName("Jane")
                .lastName("Smith")
                .phoneNumber("+1-555-0102")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now().minusDays(25))
                .lastLogin(LocalDateTime.now().minusDays(1))
                .addresses(Arrays.asList(
                        Address.builder()
                                .street("456 Oak Ave")
                                .city("Los Angeles")
                                .state("CA")
                                .zipCode("90001")
                                .country("USA")
                                .isPrimary(true)
                                .build(),
                        Address.builder()
                                .street("789 Pine Rd")
                                .city("San Francisco")
                                .state("CA")
                                .zipCode("94102")
                                .country("USA")
                                .isPrimary(false)
                                .build()
                ))
                .build();

        User user3 = User.builder()
                .id(idCounter.getAndIncrement())
                .username("bobwilson")
                .email("bob.wilson@email.com")
                .firstName("Bob")
                .lastName("Wilson")
                .phoneNumber("+1-555-0103")
                .status("INACTIVE")
                .createdAt(LocalDateTime.now().minusDays(15))
                .lastLogin(LocalDateTime.now().minusDays(10))
                .addresses(Collections.emptyList())
                .build();

        users.put(user1.getId(), user1);
        users.put(user2.getId(), user2);
        users.put(user3.getId(), user3);
    }

    // Create
    public User createUser(User user) {
        Long newId = idCounter.getAndIncrement();
        user.setId(newId);
        user.setCreatedAt(LocalDateTime.now());
        user.setLastLogin(LocalDateTime.now());
        if (user.getStatus() == null) {
            user.setStatus("PENDING");
        }
        users.put(newId, user);
        return user;
    }

    // Read - Get all users
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    // Read - Get user by ID
    public Optional<User> getUserById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    // Read - Get user by username
    public Optional<User> getUserByUsername(String username) {
        return users.values().stream()
                .filter(user -> user.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    // Read - Get user by email
    public Optional<User> getUserByEmail(String email) {
        return users.values().stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    // Read - Get users by status
    public List<User> getUsersByStatus(String status) {
        return users.values().stream()
                .filter(user -> user.getStatus().equalsIgnoreCase(status))
                .toList();
    }

    // Update
    public Optional<User> updateUser(Long id, User userDetails) {
        return Optional.ofNullable(users.computeIfPresent(id, (key, existingUser) -> {
            existingUser.setUsername(userDetails.getUsername());
            existingUser.setEmail(userDetails.getEmail());
            existingUser.setFirstName(userDetails.getFirstName());
            existingUser.setLastName(userDetails.getLastName());
            existingUser.setPhoneNumber(userDetails.getPhoneNumber());
            existingUser.setStatus(userDetails.getStatus());
            existingUser.setAddresses(userDetails.getAddresses());
            return existingUser;
        }));
    }

    // Update user status only
    public Optional<User> updateUserStatus(Long id, String status) {
        return Optional.ofNullable(users.computeIfPresent(id, (key, user) -> {
            user.setStatus(status);
            return user;
        }));
    }

    // Update last login timestamp
    public Optional<User> updateLastLogin(Long id) {
        return Optional.ofNullable(users.computeIfPresent(id, (key, user) -> {
            user.setLastLogin(LocalDateTime.now());
            return user;
        }));
    }

    // Delete
    public boolean deleteUser(Long id) {
        return users.remove(id) != null;
    }

    // Check if username exists
    public boolean usernameExists(String username) {
        return users.values().stream()
                .anyMatch(user -> user.getUsername().equalsIgnoreCase(username));
    }

    // Check if email exists
    public boolean emailExists(String email) {
        return users.values().stream()
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(email));
    }

    // New method to get user with orders
    public Optional<User> getUserWithOrders(Long id) {
        return getUserById(id).map(user -> {
            List<Order> orders = orderServiceClient.getOrdersByCustomerId(id);
            user.setOrders(orders);
            return user;
        });
    }

    // New method to get all users with their orders
    public List<User> getAllUsersWithOrders() {
        List<User> allUsers = getAllUsers();
        allUsers.forEach(user -> {
            List<Order> orders = orderServiceClient.getOrdersByCustomerId(user.getId());
            user.setOrders(orders);
        });
        return allUsers;
    }

    // New method to get users by status with orders
    public List<User> getUsersByStatusWithOrders(String status) {
        List<User> usersByStatus = getUsersByStatus(status);
        usersByStatus.forEach(user -> {
            List<Order> orders = orderServiceClient.getOrdersByCustomerId(user.getId());
            user.setOrders(orders);
        });
        return usersByStatus;
    }
}
