package com.quickbite.payment_service.service;

import com.quickbite.payment_service.dto.TransactionResponse;
import com.quickbite.payment_service.entity.Transaction;
import com.quickbite.payment_service.enums.TransactionStatus;
import com.quickbite.payment_service.enums.TransactionType;
import com.quickbite.payment_service.exception.ResourceNotFoundException;
import com.quickbite.payment_service.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public List<TransactionResponse> getTransactionsByPaymentId(Long paymentId) {
        return transactionRepository.findByPaymentIdOrderByCreatedAtDesc(paymentId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TransactionResponse getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transacción no encontrada con ID: " + id));
        return mapToResponse(transaction);
    }

    @Transactional
    public TransactionResponse createTransaction(Long paymentId, TransactionType type, BigDecimal amount, String description, TransactionStatus status) {
        Transaction transaction = Transaction.builder()
                .paymentId(paymentId)
                .type(type)
                .amount(amount)
                .description(description)
                .status(status)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        return mapToResponse(savedTransaction);
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .paymentId(transaction.getPaymentId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .status(transaction.getStatus())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
