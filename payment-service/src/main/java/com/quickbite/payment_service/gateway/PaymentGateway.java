package com.quickbite.payment_service.gateway;

import com.quickbite.payment_service.dto.PaymentRequest;
import com.quickbite.payment_service.dto.PaymentResponse;

public interface PaymentGateway {
    PaymentResponse processPayment(PaymentRequest request);
    PaymentResponse refundPayment(String transactionId);
}
