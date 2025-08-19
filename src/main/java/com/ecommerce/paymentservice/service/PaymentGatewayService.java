package com.ecommerce.paymentservice.service;

import com.ecommerce.paymentservice.dto.PaymentRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentGatewayService {
    
    public String processPayment(PaymentRequest paymentRequest) {
        // Simulate payment gateway processing
        try {
            Thread.sleep(1000); // Simulate processing delay
            
            // Simulate random payment failures (10% chance)
            if (Math.random() < 0.1) {
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
            Thread.sleep(500); // Simulate processing delay
            
            // Simulate random refund failures (5% chance)
            if (Math.random() < 0.05) {
                throw new RuntimeException("Refund failed - bank processing error");
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Refund processing interrupted");
        }
    }
}