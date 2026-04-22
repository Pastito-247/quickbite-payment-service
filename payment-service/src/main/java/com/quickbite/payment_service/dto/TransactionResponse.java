package com.quickbite.payment_service.dto;

import com.quickbite.payment_service.enums.TransactionStatus;
import com.quickbite.payment_service.enums.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionResponse {
    private Long id;
    private Long paymentId;
    private TransactionType type;
    private BigDecimal amount;
    private String description;
    private TransactionStatus status;
    private LocalDateTime createdAt;
}
