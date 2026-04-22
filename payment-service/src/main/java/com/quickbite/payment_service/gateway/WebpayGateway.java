package com.quickbite.payment_service.gateway;

import com.quickbite.payment_service.dto.PaymentRequest;
import com.quickbite.payment_service.dto.PaymentResponse;
import com.quickbite.payment_service.enums.PaymentStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class WebpayGateway implements PaymentGateway {

    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        // Simulación de integración con Webpay
        // En producción, aquí se llamaría a la API real de Webpay
        
        String transactionId = "WEBPAY-" + UUID.randomUUID().toString();
        
        // Simulamos un procesamiento exitoso
        return PaymentResponse.builder()
                .paymentId(null) // Se asignará al guardar en BD
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(PaymentStatus.COMPLETED)
                .paymentMethod("WEBPAY")
                .transactionId(transactionId)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Override
    public PaymentResponse refundPayment(String transactionId) {
        // Simulación de reembolso con Webpay
        return PaymentResponse.builder()
                .paymentId(null)
                .orderId(null)
                .amount(BigDecimal.ZERO)
                .currency("CLP")
                .status(PaymentStatus.REFUNDED)
                .paymentMethod("WEBPAY")
                .transactionId(transactionId)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
