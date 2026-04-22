package com.quickbite.payment_service.gateway;

import com.quickbite.payment_service.dto.PaymentRequest;
import com.quickbite.payment_service.dto.PaymentResponse;
import com.quickbite.payment_service.enums.PaymentStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class WalletGateway implements PaymentGateway {

    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        // Simulación de pago con billetera virtual
        // La lógica de saldo se manejará en el servicio
        
        String transactionId = "WALLET-" + UUID.randomUUID().toString();
        
        return PaymentResponse.builder()
                .paymentId(null)
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(PaymentStatus.COMPLETED)
                .paymentMethod("WALLET")
                .transactionId(transactionId)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Override
    public PaymentResponse refundPayment(String transactionId) {
        return PaymentResponse.builder()
                .paymentId(null)
                .orderId(null)
                .amount(BigDecimal.ZERO)
                .currency("CLP")
                .status(PaymentStatus.REFUNDED)
                .paymentMethod("WALLET")
                .transactionId(transactionId)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
