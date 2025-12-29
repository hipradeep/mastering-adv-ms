package com.hipradeep.paymentservice.controller;

import com.hipradeep.paymentservice.entity.UserTransaction;
import com.hipradeep.paymentservice.repository.UserTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private UserTransactionRepository userTransactionRepository;

    @GetMapping
    public List<UserTransaction> getAllTransactions() {
        return userTransactionRepository.findAll();
    }
}
