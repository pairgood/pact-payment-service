package com.ecommerce.paymentservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class NotificationServiceClient {
    
    private final WebClient webClient;
    
    @Value("${services.notification-service.url:http://localhost:8085}")
    private String notificationServiceUrl;
    
    public NotificationServiceClient() {
        this.webClient = WebClient.builder().build();
    }
    
    public void sendPaymentConfirmation(Long paymentId, Long userId, Long orderId) {
        try {
            webClient.post()
                .uri(notificationServiceUrl + "/api/notifications/payment-confirmation")
                .bodyValue(Map.of("paymentId", paymentId, "userId", userId, "orderId", orderId))
                .retrieve()
                .bodyToMono(Void.class)
                .block();
        } catch (Exception e) {
            System.err.println("Failed to send payment confirmation notification: " + e.getMessage());
        }
    }
    
    public void sendPaymentFailure(Long paymentId, Long userId, Long orderId) {
        try {
            webClient.post()
                .uri(notificationServiceUrl + "/api/notifications/payment-failure")
                .bodyValue(Map.of("paymentId", paymentId, "userId", userId, "orderId", orderId))
                .retrieve()
                .bodyToMono(Void.class)
                .block();
        } catch (Exception e) {
            System.err.println("Failed to send payment failure notification: " + e.getMessage());
        }
    }
    
    public void sendRefundConfirmation(Long paymentId, Long userId, Long orderId) {
        try {
            webClient.post()
                .uri(notificationServiceUrl + "/api/notifications/refund-confirmation")
                .bodyValue(Map.of("paymentId", paymentId, "userId", userId, "orderId", orderId))
                .retrieve()
                .bodyToMono(Void.class)
                .block();
        } catch (Exception e) {
            System.err.println("Failed to send refund confirmation notification: " + e.getMessage());
        }
    }
}