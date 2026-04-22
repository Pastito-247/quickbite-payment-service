package com.quickbite.payment_service.repository;

import com.quickbite.payment_service.entity.Payment;
import com.quickbite.payment_service.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(String orderId);

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByPaymentMethod(String paymentMethod);
}
