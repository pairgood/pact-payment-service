package com.ecommerce.paymentservice.service;

import com.ecommerce.paymentservice.dto.PaymentRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentGatewayService {
    
    @Value("${payment.gateway.simulate-failures:true}")
    private boolean simulateFailures;
    
    @Value("${payment.gateway.processing-delay:1000}")
    private long processingDelay;
    
    @Value("${payment.gateway.refund-delay:500}")
    private long refundDelay;
    
    public String processPayment(PaymentRequest paymentRequest) {
        // Simulate payment gateway processing
        try {
            Thread.sleep(processingDelay); // Simulate processing delay
            
            // Simulate random payment failures (10% chance) only if enabled
            if (simulateFailures && Math.random() < 0.1) {
                throw new RuntimeException("Payment declined by bank");
            }
            
            // Generate mock transaction ID
            return "TXN_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Payment processing interrupted");
        }
    }
    
    public void refundPayment(String transactionId) {
        // Simulate refund processing
        try {
            Thread.sleep(refundDelay); // Simulate processing delay
            
            // Simulate random refund failures (5% chance) only if enabled
            if (simulateFailures && Math.random() < 0.05) {
                throw new RuntimeException("Refund failed - bank processing error");
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Refund processing interrupted");
        }
    }
}