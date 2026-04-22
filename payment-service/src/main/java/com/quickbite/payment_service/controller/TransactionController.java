package com.quickbite.payment_service.controller;

import com.quickbite.payment_service.dto.TransactionResponse;
import com.quickbite.payment_service.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/payment/{paymentId}")
    public List<TransactionResponse> getTransactionsByPaymentId(@PathVariable Long paymentId) {
        return transactionService.getTransactionsByPaymentId(paymentId);
    }

    @GetMapping("/{id}")
    public TransactionResponse getTransactionById(@PathVariable Long id) {
        return transactionService.getTransactionById(id);
    }
}
