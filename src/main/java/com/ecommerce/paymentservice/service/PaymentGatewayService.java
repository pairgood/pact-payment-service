package com.ecommerce.paymentservice.service;

import com.ecommerce.paymentservice.dto.PaymentRequest;
import com.ecommerce.paymentservice.telemetry.TelemetryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentGatewayService {
    
    @Autowired(required = false)
    private TelemetryClient telemetryClient;
    
    @Value("${payment.gateway.simulate-failures:true}")
    private boolean simulateFailures;
    
    @Value("${payment.gateway.processing-delay:1000}")
    private long processingDelay;
    
    @Value("${payment.gateway.refund-delay:500}")
    private long refundDelay;
    
    public String processPayment(PaymentRequest paymentRequest) {
        long startTime = System.currentTimeMillis();
        int statusCode = 200;
        String url = "https://payment-gateway.example.com/api/process";
        
        // Simulate payment gateway processing
        try {
            Thread.sleep(processingDelay); // Simulate processing delay
            
            // Simulate random payment failures (10% chance) only if enabled
            if (simulateFailures && Math.random() < 0.1) {
                statusCode = 400;
                throw new RuntimeException("Payment declined by bank");
            }
            
            // Generate mock transaction ID
            String transactionId = "TXN_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
            
            long duration = System.currentTimeMillis() - startTime;
            if (telemetryClient != null) {
                telemetryClient.recordServiceCall("payment-gateway", "process_payment", "POST", url, duration, statusCode);
            }
            
            return transactionId;
            
        } catch (InterruptedException e) {
            statusCode = 500;
            long duration = System.currentTimeMillis() - startTime;
            if (telemetryClient != null) {
                telemetryClient.recordServiceCall("payment-gateway", "process_payment", "POST", url, duration, statusCode);
            }
            Thread.currentThread().interrupt();
            throw new RuntimeException("Payment processing interrupted");
        } catch (RuntimeException e) {
            long duration = System.currentTimeMillis() - startTime;
            if (telemetryClient != null) {
                telemetryClient.recordServiceCall("payment-gateway", "process_payment", "POST", url, duration, statusCode);
            }
            throw e;
        }
    }
    
    public void refundPayment(String transactionId) {
        long startTime = System.currentTimeMillis();
        int statusCode = 200;
        String url = "https://payment-gateway.example.com/api/refund";
        
        // Simulate refund processing
        try {
            Thread.sleep(refundDelay); // Simulate processing delay
            
            // Simulate random refund failures (5% chance) only if enabled
            if (simulateFailures && Math.random() < 0.05) {
                statusCode = 400;
                throw new RuntimeException("Refund failed - bank processing error");
            }
            
            long duration = System.currentTimeMillis() - startTime;
            if (telemetryClient != null) {
                telemetryClient.recordServiceCall("payment-gateway", "refund_payment", "POST", url, duration, statusCode);
            }
            
        } catch (InterruptedException e) {
            statusCode = 500;
            long duration = System.currentTimeMillis() - startTime;
            if (telemetryClient != null) {
                telemetryClient.recordServiceCall("payment-gateway", "refund_payment", "POST", url, duration, statusCode);
            }
            Thread.currentThread().interrupt();
            throw new RuntimeException("Refund processing interrupted");
        } catch (RuntimeException e) {
            long duration = System.currentTimeMillis() - startTime;
            if (telemetryClient != null) {
                telemetryClient.recordServiceCall("payment-gateway", "refund_payment", "POST", url, duration, statusCode);
            }
            throw e;
        }
    }
}