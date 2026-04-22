package com.quickbite.payment_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletRequest {
    @NotNull(message = "El monto es requerido")
    @Positive(message = "El monto debe ser mayor a 0")
    private BigDecimal amount;

    private String currency = "CLP";
}
