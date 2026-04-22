package com.quickbite.payment_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class PaymentRequest {
    @NotBlank(message = "El ID del pedido es requerido")
    private String orderId;

    @NotNull(message = "El monto es requerido")
    @Positive(message = "El monto debe ser mayor a 0")
    private BigDecimal amount;

    private String currency = "CLP";

    @NotBlank(message = "El método de pago es requerido")
    private String paymentMethod;

    private Map<String, Object> paymentDetails;
}
