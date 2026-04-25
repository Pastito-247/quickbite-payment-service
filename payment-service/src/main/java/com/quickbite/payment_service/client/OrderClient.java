package com.quickbite.payment_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "order-service", url = "${ORDER_SERVICE_URL:http://localhost:8081}")
public interface OrderClient {

    @GetMapping("/api/orders/{orderId}")
    Map<String, Object> getOrderById(@PathVariable String orderId);

    @PutMapping("/api/orders/{orderId}/status")
    Map<String, Object> updateOrderStatus(@PathVariable String orderId, @RequestBody Map<String, Object> statusUpdate);

    @GetMapping("/api/orders/{orderId}/customer")
    Map<String, Object> getOrderCustomer(@PathVariable String orderId);

    @PostMapping("/api/orders/{orderId}/payment-confirmed")
    Map<String, Object> confirmPayment(@PathVariable String orderId, @RequestBody Map<String, Object> paymentInfo);

    @PostMapping("/api/orders/{orderId}/payment-failed")
    Map<String, Object> paymentFailed(@PathVariable String orderId, @RequestBody Map<String, Object> errorInfo);

    @GetMapping("/api/orders/customer/{userId}")
    Map<String, Object>[] getOrdersByCustomer(@PathVariable String userId);
}
