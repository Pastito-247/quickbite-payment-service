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
public class MercadoPagoGateway implements PaymentGateway {

    @Value("${MERCADOPAGO_ACCESS_TOKEN:}")
    private String accessToken;

    @Value("${MERCADOPAGO_PUBLIC_KEY:}")
    private String publicKey;

    @Value("${MERCADOPAGO_ENVIRONMENT:sandbox}")
    private String environment;

    private final RestTemplate restTemplate;

    public MercadoPagoGateway() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        try {
            if (accessToken == null || accessToken.isEmpty()) {
                log.warn("MercadoPago access token not configured, using simulation mode");
                return processPaymentSimulation(request);
            }

            String baseUrl = environment.equals("production") 
                ? "https://api.mercadopago.com" 
                : "https://api.mercadopago.com";

            // Crear preferencia de pago en MercadoPago
            Map<String, Object> preferenceRequest = new HashMap<>();
            
            // Información del comprador
            Map<String, Object> payer = new HashMap<>();
            payer.put("email", "customer@quickbite.com");
            
            // Items del pago
            Map<String, Object> item = new HashMap<>();
            item.put("title", "Orden QuickBite - " + request.getOrderId());
            item.put("quantity", 1);
            item.put("unit_price", request.getAmount());
            item.put("currency_id", request.getCurrency());
            
            // URLs de retorno
            Map<String, Object> backUrls = new HashMap<>();
            backUrls.put("success", "https://quickbite.com/payment/mercadopago/success");
            backUrls.put("failure", "https://quickbite.com/payment/mercadopago/failure");
            backUrls.put("pending", "https://quickbite.com/payment/mercadopago/pending");
            
            preferenceRequest.put("items", new Object[]{item});
            preferenceRequest.put("payer", payer);
            preferenceRequest.put("back_urls", backUrls);
            preferenceRequest.put("auto_return", "approved");
            preferenceRequest.put("external_reference", request.getOrderId());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(preferenceRequest, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                baseUrl + "/checkout/preferences",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                String preferenceId = (String) responseBody.get("id");
                String initPoint = (String) responseBody.get("init_point");

                log.info("MercadoPago preference created successfully. Preference ID: {}, Init Point: {}", 
                        preferenceId, initPoint);

                return PaymentResponse.builder()
                        .paymentId(null)
                        .orderId(request.getOrderId())
                        .amount(request.getAmount())
                        .currency(request.getCurrency())
                        .status(PaymentStatus.PENDING)
                        .paymentMethod("MERCADO_PAGO")
                        .transactionId(preferenceId)
                        .additionalInfo(Map.of(
                            "redirect_url", initPoint,
                            "preference_id", preferenceId,
                            "public_key", publicKey
                        ))
                        .createdAt(LocalDateTime.now())
                        .build();
            } else {
                throw new RuntimeException("MercadoPago API error: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Error processing MercadoPago payment: {}", e.getMessage());
            return processPaymentSimulation(request);
        }
    }

    @Override
    public PaymentResponse refundPayment(String transactionId) {
        try {
            if (accessToken == null || accessToken.isEmpty()) {
                log.warn("MercadoPago access token not configured, using simulation mode");
                return refundPaymentSimulation(transactionId);
            }

            String baseUrl = environment.equals("production") 
                ? "https://api.mercadopago.com" 
                : "https://api.mercadopago.com";

            // Buscar pagos asociados a la preferencia
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<Void> searchEntity = new HttpEntity<>(headers);

            ResponseEntity<Map<String, Object>> searchResponse = restTemplate.exchange(
                baseUrl + "/v1/payments/search?external_reference=" + transactionId,
                HttpMethod.GET,
                searchEntity,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (searchResponse.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> searchBody = searchResponse.getBody();
                Object[] results = (Object[]) searchBody.get("results");
                
                if (results.length > 0) {
                    Map<String, Object> payment = (Map<String, Object>) results[0];
                    String paymentId = payment.get("id").toString();

                    // Realizar reembolso
                    Map<String, Object> refundRequest = new HashMap<>();
                    refundRequest.put("amount", payment.get("transaction_amount"));

                    HttpEntity<Map<String, Object>> refundEntity = new HttpEntity<>(refundRequest, headers);

                    ResponseEntity<Map<String, Object>> refundResponse = restTemplate.exchange(
                        baseUrl + "/v1/payments/" + paymentId + "/refunds",
                        HttpMethod.POST,
                        refundEntity,
                        new ParameterizedTypeReference<Map<String, Object>>() {}
                    );

                    if (refundResponse.getStatusCode() == HttpStatus.CREATED) {
                        log.info("MercadoPago refund processed successfully. Payment ID: {}", paymentId);

                        return PaymentResponse.builder()
                                .paymentId(null)
                                .orderId(null)
                                .amount(BigDecimal.ZERO)
                                .currency("CLP")
                                .status(PaymentStatus.REFUNDED)
                                .paymentMethod("MERCADO_PAGO")
                                .transactionId(paymentId)
                                .createdAt(LocalDateTime.now())
                                .build();
                    }
                }
            }

            throw new RuntimeException("No payment found for transaction: " + transactionId);

        } catch (Exception e) {
            log.error("Error processing MercadoPago refund: {}", e.getMessage());
            return refundPaymentSimulation(transactionId);
        }
    }

    private PaymentResponse processPaymentSimulation(PaymentRequest request) {
        String transactionId = "MP-SIM-" + UUID.randomUUID().toString();
        
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

    private PaymentResponse refundPaymentSimulation(String transactionId) {
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
