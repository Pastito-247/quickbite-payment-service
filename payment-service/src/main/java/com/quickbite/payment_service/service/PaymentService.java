package com.quickbite.payment_service.service;

import com.quickbite.payment_service.dto.PaymentRequest;
import com.quickbite.payment_service.dto.PaymentResponse;
import com.quickbite.payment_service.entity.Payment;
import com.quickbite.payment_service.enums.PaymentStatus;
import com.quickbite.payment_service.enums.TransactionStatus;
import com.quickbite.payment_service.enums.TransactionType;
import com.quickbite.payment_service.exception.PaymentProcessingException;
import com.quickbite.payment_service.exception.ResourceNotFoundException;
import com.quickbite.payment_service.client.OrderClient;
import com.quickbite.payment_service.factory.PaymentGatewayFactory;
import com.quickbite.payment_service.gateway.PaymentGateway;
import com.quickbite.payment_service.repository.PaymentRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentGatewayFactory paymentGatewayFactory;
    private final WalletService walletService;
    private final TransactionService transactionService;
    private final TrackingIdGenerator trackingIdGenerator;
    private final OrderClient orderClient;

    @CircuitBreaker(name = "paymentGateway", fallbackMethod = "processPaymentFallback")
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        // Verificar si ya existe un pago para esta orden
        paymentRepository.findByOrderId(request.getOrderId())
                .ifPresent(payment -> {
                    if (payment.getStatus() == PaymentStatus.COMPLETED) {
                        throw new IllegalArgumentException("Ya existe un pago completado para esta orden");
                    }
                });

        // Obtener la pasarela de pago correspondiente
        PaymentGateway gateway = paymentGatewayFactory.getGateway(request.getPaymentMethod());

        // Si es pago con billetera, verificar y deducir saldo
        if (request.getPaymentMethod().equalsIgnoreCase("WALLET")) {
            String userId = extractUserIdFromRequest(request);
            walletService.deductBalance(userId, request.getAmount());
        }

        // Procesar el pago a través de la pasarela
        PaymentResponse gatewayResponse = gateway.processPayment(request);

        // Guardar el pago en la base de datos
        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(gatewayResponse.getStatus())
                .paymentMethod(request.getPaymentMethod())
                .transactionId(gatewayResponse.getTransactionId())
                .trackingId(trackingIdGenerator.generateTrackingId())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        // Crear registro de transacción
        transactionService.createTransaction(
                savedPayment.getId(),
                TransactionType.PAYMENT,
                request.getAmount(),
                "Pago procesado para orden " + request.getOrderId(),
                TransactionStatus.SUCCESS
        );

        // Notificar al servicio de pedidos sobre el pago
        try {
            Map<String, Object> paymentInfo = Map.of(
                "paymentId", savedPayment.getId(),
                "transactionId", savedPayment.getTransactionId(),
                "trackingId", savedPayment.getTrackingId(),
                "status", savedPayment.getStatus().toString(),
                "amount", savedPayment.getAmount(),
                "paymentMethod", savedPayment.getPaymentMethod()
            );

            if (savedPayment.getStatus() == PaymentStatus.COMPLETED) {
                orderClient.confirmPayment(request.getOrderId(), paymentInfo);
            } else if (savedPayment.getStatus() == PaymentStatus.PENDING) {
                orderClient.updateOrderStatus(request.getOrderId(), Map.of(
                    "status", "PAYMENT_PENDING",
                    "paymentId", savedPayment.getId()
                ));
            }
        } catch (Exception e) {
            log.error("Error notifying order service about payment: {}", e.getMessage());
            // No fallar el pago si la notificación falla
        }

        return mapToResponse(savedPayment);
    }

    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con ID: " + id));
        return mapToResponse(payment);
    }

    public PaymentResponse getPaymentByOrderId(String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado para la orden: " + orderId));
        return mapToResponse(payment);
    }

    public List<PaymentResponse> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @CircuitBreaker(name = "paymentGateway", fallbackMethod = "refundPaymentFallback")
    @Transactional
    public PaymentResponse refundPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con ID: " + paymentId));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new IllegalArgumentException("Solo se pueden reembolsar pagos completados");
        }

        // Obtener la pasarela de pago
        PaymentGateway gateway = paymentGatewayFactory.getGateway(payment.getPaymentMethod());

        // Procesar el reembolso
        PaymentResponse gatewayResponse = gateway.refundPayment(payment.getTransactionId());

        // Si es pago con billetera, devolver el saldo
        if (payment.getPaymentMethod().equalsIgnoreCase("WALLET")) {
            String userId = extractUserIdFromPayment(payment);
            walletService.addBalance(userId, payment.getAmount());
        }

        // Actualizar el estado del pago
        payment.setStatus(PaymentStatus.REFUNDED);
        Payment savedPayment = paymentRepository.save(payment);

        // Crear registro de transacción de reembolso
        transactionService.createTransaction(
                savedPayment.getId(),
                TransactionType.REFUND,
                payment.getAmount(),
                "Reembolso procesado para orden " + payment.getOrderId(),
                TransactionStatus.SUCCESS
        );

        return mapToResponse(savedPayment);
    }

    // Fallback method para Circuit Breaker
    public PaymentResponse processPaymentFallback(PaymentRequest request, Exception exception) {
        throw new PaymentProcessingException("Servicio de pasarela de pago no disponible. Intente nuevamente más tarde.");
    }

    // Fallback method para Circuit Breaker
    public PaymentResponse refundPaymentFallback(Long paymentId, Exception exception) {
        throw new PaymentProcessingException("Servicio de pasarela de pago no disponible. Intente nuevamente más tarde.");
    }

    private String extractUserIdFromRequest(PaymentRequest request) {
        // Primero intentar obtener del request
        if (request.getPaymentDetails() != null && request.getPaymentDetails().containsKey("userId")) {
            return request.getPaymentDetails().get("userId").toString();
        }
        
        // Si no está en el request, lanzar excepción para que el controller lo inyecte desde el contexto
        throw new IllegalArgumentException("ID de usuario no proporcionado para pago con billetera. " +
                "Debe incluirse en el request o estar disponible en el contexto de seguridad.");
    }

    private String extractUserIdFromPayment(Payment payment) {
        // Para reembolsos, necesitamos obtener el userId del pedido original
        try {
            // Intentar obtener del servicio de pedidos usando el orderId
            if (payment.getOrderId() != null) {
                // Esto debería ser implementado con comunicación real al Order Service
                // Por ahora, devolvemos un placeholder
                log.warn("Using placeholder userId for payment refund. Implement real Order Service integration.");
                return "PLACEHOLDER_USER_ID";
            }
            throw new IllegalArgumentException("No se puede determinar el usuario para el reembolso");
        } catch (Exception e) {
            log.error("Error extracting userId from payment: {}", e.getMessage());
            throw new RuntimeException("No se puede procesar el reembolso sin información del usuario");
        }
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .trackingId(payment.getTrackingId())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
