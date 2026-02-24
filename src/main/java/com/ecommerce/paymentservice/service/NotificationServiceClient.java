package com.ecommerce.paymentservice.service;

import com.ecommerce.paymentservice.telemetry.TelemetryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class NotificationServiceClient {
    
    private final WebClient webClient;
    
    @Value("${services.notification-service.url:http://localhost:8085}")
    private String notificationServiceUrl;
    
    @Autowired(required = false)
    private TelemetryClient telemetryClient;
    
    @Autowired
    public NotificationServiceClient(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    // Constructor for testing with custom URL
    public NotificationServiceClient(String baseUrl) {
        this.webClient = WebClient.builder().build();
        this.notificationServiceUrl = baseUrl;
    }
    
    public void sendPaymentConfirmation(Long paymentId, Long userId, Long orderId) {
        long startTime = System.currentTimeMillis();
        String url = notificationServiceUrl + "/api/notifications/payment-confirmation";
        int statusCode = 200;
        
        try {
            webClient.post()
                .uri(url)
                .bodyValue(Map.of("paymentId", paymentId, "userId", userId, "orderId", orderId))
                .retrieve()
                .bodyToMono(Void.class)
                .block();
            
            long duration = System.currentTimeMillis() - startTime;
            if (telemetryClient != null) {
                telemetryClient.recordServiceCall("notification-service", "send_payment_confirmation", "POST", url, duration, statusCode);
            }
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            statusCode = 500;
            if (telemetryClient != null) {
                telemetryClient.recordServiceCall("notification-service", "send_payment_confirmation", "POST", url, duration, statusCode);
            }
            System.err.println("Failed to send payment confirmation notification: " + e.getMessage());
        }
    }
    
    public void sendPaymentFailure(Long paymentId, Long userId, Long orderId) {
        long startTime = System.currentTimeMillis();
        String url = notificationServiceUrl + "/api/notifications/payment-failure";
        int statusCode = 200;
        
        try {
            webClient.post()
                .uri(url)
                .bodyValue(Map.of("paymentId", paymentId, "userId", userId, "orderId", orderId))
                .retrieve()
                .bodyToMono(Void.class)
                .block();
            
            long duration = System.currentTimeMillis() - startTime;
            if (telemetryClient != null) {
                telemetryClient.recordServiceCall("notification-service", "send_payment_failure", "POST", url, duration, statusCode);
            }
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            statusCode = 500;
            if (telemetryClient != null) {
                telemetryClient.recordServiceCall("notification-service", "send_payment_failure", "POST", url, duration, statusCode);
            }
            System.err.println("Failed to send payment failure notification: " + e.getMessage());
        }
    }
    
    public void sendRefundConfirmation(Long paymentId, Long userId, Long orderId) {
        long startTime = System.currentTimeMillis();
        String url = notificationServiceUrl + "/api/notifications/refund-confirmation";
        int statusCode = 200;
        
        try {
            webClient.post()
                .uri(url)
                .bodyValue(Map.of("paymentId", paymentId, "userId", userId, "orderId", orderId))
                .retrieve()
                .bodyToMono(Void.class)
                .block();
            
            long duration = System.currentTimeMillis() - startTime;
            if (telemetryClient != null) {
                telemetryClient.recordServiceCall("notification-service", "send_refund_confirmation", "POST", url, duration, statusCode);
            }
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            statusCode = 500;
            if (telemetryClient != null) {
                telemetryClient.recordServiceCall("notification-service", "send_refund_confirmation", "POST", url, duration, statusCode);
            }
            System.err.println("Failed to send refund confirmation notification: " + e.getMessage());
        }
    }
}