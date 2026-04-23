package com.quickbite.payment_service.entity;

import com.quickbite.payment_service.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String orderId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Builder.Default
    @Column(nullable = false, length = 3)
    private String currency = "CLP";

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(nullable = false)
    private String paymentMethod;

    private String transactionId;

    @Column(name = "tracking_id", unique = true, nullable = false)
    private String trackingId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
