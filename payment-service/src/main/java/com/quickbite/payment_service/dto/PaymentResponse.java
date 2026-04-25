package com.quickbite.payment_service.dto;

import com.quickbite.payment_service.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class PaymentResponse {
    private Long paymentId;
    private String orderId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String paymentMethod;
    private String transactionId;
    private String trackingId;
    private LocalDateTime createdAt;
    private Map<String, Object> additionalInfo;
}
