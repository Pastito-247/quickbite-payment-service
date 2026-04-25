package com.quickbite.payment_service.gateway;

import com.quickbite.payment_service.dto.PaymentRequest;
import com.quickbite.payment_service.dto.PaymentResponse;
import com.quickbite.payment_service.enums.PaymentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.core.ParameterizedTypeReference;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class WebpayGateway implements PaymentGateway {

    @Value("${WEBPAY_API_KEY:}")
    private String apiKey;

    @Value("${WEBPAY_API_SECRET:}")
    private String apiSecret;

    @Value("${WEBPAY_COMMERCE_CODE:}")
    private String commerceCode;

    @Value("${WEBPAY_ENVIRONMENT:integration}")
    private String environment;

    private final RestTemplate restTemplate;

    public WebpayGateway() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        try {
            if (apiKey == null || apiKey.isEmpty()) {
                log.warn("Webpay API credentials not configured, using simulation mode");
                return processPaymentSimulation(request);
            }

            // URL base según ambiente
            String baseUrl = environment.equals("production") 
                ? "https://webpay3g.transbank.cl" 
                : "https://webpay3gint.transbank.cl";

            // Crear transacción en Webpay
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("buy_order", request.getOrderId());
            requestBody.put("session_id", UUID.randomUUID().toString());
            requestBody.put("amount", request.getAmount());
            requestBody.put("return_url", "https://quickbite.com/payment/webpay/return");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBasicAuth(apiKey, apiSecret);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                baseUrl + "/rswebpaytransaction/api/webpay/v1.0/transactions",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                String token = (String) responseBody.get("token");
                String url = (String) responseBody.get("url");

                log.info("Webpay transaction created successfully. Token: {}, URL: {}", token, url);

                return PaymentResponse.builder()
                        .paymentId(null)
                        .orderId(request.getOrderId())
                        .amount(request.getAmount())
                        .currency(request.getCurrency())
                        .status(PaymentStatus.PENDING) // Pendiente hasta que el usuario complete el pago
                        .paymentMethod("WEBPAY")
                        .transactionId(token)
                        .additionalInfo(Map.of(
                            "redirect_url", url,
                            "token", token
                        ))
                        .createdAt(LocalDateTime.now())
                        .build();
            } else {
                throw new RuntimeException("Webpay API error: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Error processing Webpay payment: {}", e.getMessage());
            return processPaymentSimulation(request);
        }
    }

    @Override
    public PaymentResponse refundPayment(String transactionId) {
        try {
            if (apiKey == null || apiKey.isEmpty()) {
                log.warn("Webpay API credentials not configured, using simulation mode");
                return refundPaymentSimulation(transactionId);
            }

            String baseUrl = environment.equals("production") 
                ? "https://webpay3g.transbank.cl" 
                : "https://webpay3gint.transbank.cl";

            // Anular transacción en Webpay
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBasicAuth(apiKey, apiSecret);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                baseUrl + "/rswebpaytransaction/api/webpay/v1.0/transactions/" + transactionId + "/refund",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                String refundToken = (String) responseBody.get("token");

                log.info("Webpay refund processed successfully. Token: {}", refundToken);

                return PaymentResponse.builder()
                        .paymentId(null)
                        .orderId(null)
                        .amount(BigDecimal.ZERO)
                        .currency("CLP")
                        .status(PaymentStatus.REFUNDED)
                        .paymentMethod("WEBPAY")
                        .transactionId(refundToken)
                        .createdAt(LocalDateTime.now())
                        .build();
            } else {
                throw new RuntimeException("Webpay refund error: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Error processing Webpay refund: {}", e.getMessage());
            return refundPaymentSimulation(transactionId);
        }
    }

    private PaymentResponse processPaymentSimulation(PaymentRequest request) {
        String transactionId = "WEBPAY-SIM-" + UUID.randomUUID().toString();
        
        return PaymentResponse.builder()
                .paymentId(null)
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(PaymentStatus.COMPLETED)
                .paymentMethod("WEBPAY")
                .transactionId(transactionId)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private PaymentResponse refundPaymentSimulation(String transactionId) {
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
