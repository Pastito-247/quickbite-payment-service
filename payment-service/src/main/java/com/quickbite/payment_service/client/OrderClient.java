package com.quickbite.payment_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "order-service", url = "http://localhost:8081")
public interface OrderClient {

    @GetMapping("/api/orders/{orderId}")
    Map<String, Object> getOrder(@PathVariable String orderId);

    @PutMapping("/api/orders/{orderId}/status")
    void updateOrderStatus(@PathVariable String orderId, @RequestBody Map<String, String> status);

    @GetMapping("/api/orders/{orderId}/customer")
    Map<String, Object> getOrderCustomer(@PathVariable String orderId);
}
