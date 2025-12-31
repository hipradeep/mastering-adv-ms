package com.hipradeep.payment.controller;

import com.hipradeep.payment.entity.UserBalance;
import com.hipradeep.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public List<UserBalance> getAllUserBalances() {
        return paymentService.getAllUserBalances();
    }

    @GetMapping("/{userId}")
    public UserBalance getUserBalance(@PathVariable int userId) {
        return paymentService.getUserBalance(userId);
    }
}
