package com.quickbite.payment_service.gateway;

import com.quickbite.payment_service.dto.PaymentRequest;
import com.quickbite.payment_service.dto.PaymentResponse;
import com.quickbite.payment_service.enums.PaymentStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class MercadoPagoGateway implements PaymentGateway {

    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        // Simulación de integración con MercadoPago
        // En producción, aquí se llamaría a la API real de MercadoPago
        
        String transactionId = "MP-" + UUID.randomUUID().toString();
        
        // Simulamos un procesamiento exitoso
        return PaymentResponse.builder()
                .paymentId(null)
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(PaymentStatus.COMPLETED)
                .paymentMethod("MERCADO_PAGO")
                .transactionId(transactionId)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Override
    public PaymentResponse refundPayment(String transactionId) {
        // Simulación de reembolso con MercadoPago
        return PaymentResponse.builder()
                .paymentId(null)
                .orderId(null)
                .amount(BigDecimal.ZERO)
                .currency("CLP")
                .status(PaymentStatus.REFUNDED)
                .paymentMethod("MERCADO_PAGO")
                .transactionId(transactionId)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
