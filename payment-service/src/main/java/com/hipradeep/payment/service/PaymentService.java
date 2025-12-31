package com.hipradeep.payment.service;

import com.hipradeep.payment.entity.UserBalance;
import com.hipradeep.payment.repository.UserBalanceRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final UserBalanceRepository userBalanceRepository;

    @PostConstruct
    public void initUserBalanceInDb() {
        if(userBalanceRepository.count()==0) {
            userBalanceRepository.saveAll(Stream.of(
                    new UserBalance(101, 5000),
                    new UserBalance(102, 3000),
                    new UserBalance(103, 4200),
                    new UserBalance(104, 20000),
                    new UserBalance(105, 999)
            ).collect(Collectors.toList()));
        }
    }

    public java.util.List<UserBalance> getAllUserBalances() {
        return userBalanceRepository.findAll();
    }

    public UserBalance getUserBalance(int userId) {
        return userBalanceRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }
}
