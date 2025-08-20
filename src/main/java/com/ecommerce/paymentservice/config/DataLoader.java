package com.ecommerce.paymentservice.config;

import com.ecommerce.paymentservice.model.Payment;
import com.ecommerce.paymentservice.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Override
    public void run(String... args) throws Exception {
        // Only load data if the database is empty
        if (paymentRepository.count() == 0) {
            loadSeedData();
        }
    }
    
    private void loadSeedData() {
        System.out.println("🌱 Loading Payment Service seed data...");
        
        // Create payments corresponding to orders
        List<Payment> payments = new ArrayList<>();
        
        // Payment 1: John Doe's gaming setup (Completed Order)
        payments.add(createPayment(1L, 1L, 1L, new BigDecimal("1479.97"), 
                                  Payment.PaymentStatus.COMPLETED, Payment.PaymentMethod.CREDIT_CARD,
                                  "txn_john_gaming_001", LocalDateTime.now().minusDays(5),
                                  "Visa ending in 1234"));
        
        // Payment 2: Jane Smith's book collection (Completed Order)
        payments.add(createPayment(2L, 2L, 2L, new BigDecimal("159.97"), 
                                  Payment.PaymentStatus.COMPLETED, Payment.PaymentMethod.PAYPAL,
                                  "txn_jane_books_002", LocalDateTime.now().minusDays(3),
                                  "PayPal account jane.smith@example.com"));
        
        // Payment 3: Bob Wilson's home office setup (Shipped Order)
        payments.add(createPayment(3L, 3L, 3L, new BigDecimal("409.97"), 
                                  Payment.PaymentStatus.COMPLETED, Payment.PaymentMethod.CREDIT_CARD,
                                  "txn_bob_office_003", LocalDateTime.now().minusDays(2),
                                  "MasterCard ending in 5678"));
        
        // Payment 4: Alice Johnson's fitness gear (Processing Order)
        payments.add(createPayment(4L, 4L, 4L, new BigDecimal("109.95"), 
                                  Payment.PaymentStatus.PROCESSING, Payment.PaymentMethod.BANK_TRANSFER,
                                  "txn_alice_fitness_004", LocalDateTime.now().minusDays(1),
                                  "Bank transfer from Wells Fargo"));
        
        // Payment 5: Charlie Brown's wardrobe update (Pending Order)
        payments.add(createPayment(5L, 5L, 5L, new BigDecimal("219.94"), 
                                  Payment.PaymentStatus.PENDING, Payment.PaymentMethod.CREDIT_CARD,
                                  "txn_charlie_clothes_005", LocalDateTime.now().minusHours(6),
                                  "Amex ending in 9012"));
        
        // Payment 6: Diana Clark's tech upgrade (Cancelled Order)
        payments.add(createPayment(6L, 6L, 6L, new BigDecimal("289.98"), 
                                  Payment.PaymentStatus.REFUNDED, Payment.PaymentMethod.DEBIT_CARD,
                                  "txn_diana_tech_006", LocalDateTime.now().minusHours(3),
                                  "Debit card ending in 3456 - REFUNDED"));
        
        // Additional historical payments for demonstration
        payments.add(createPayment(7L, 1L, 7L, new BigDecimal("99.99"), 
                                  Payment.PaymentStatus.COMPLETED, Payment.PaymentMethod.CREDIT_CARD,
                                  "txn_john_prev_007", LocalDateTime.now().minusWeeks(2),
                                  "Previous purchase - Visa 1234"));
        
        payments.add(createPayment(8L, 2L, 8L, new BigDecimal("25.50"), 
                                  Payment.PaymentStatus.FAILED, Payment.PaymentMethod.CREDIT_CARD,
                                  "txn_jane_failed_008", LocalDateTime.now().minusWeeks(1),
                                  "Payment failed - insufficient funds"));
        
        for (Payment payment : payments) {
            paymentRepository.save(payment);
        }
        
        System.out.println("✅ Created " + payments.size() + " payments with various statuses");
        System.out.println("💳 Payment methods: CREDIT_CARD, DEBIT_CARD, PAYPAL, BANK_TRANSFER");
        System.out.println("📊 Statuses: COMPLETED, PROCESSING, PENDING, REFUNDED, FAILED");
    }
    
    private Payment createPayment(Long id, Long userId, Long orderId, BigDecimal amount, 
                                 Payment.PaymentStatus status, Payment.PaymentMethod method,
                                 String transactionId, LocalDateTime paymentDate, String paymentDetails) {
        Payment payment = new Payment();
        payment.setId(id);
        payment.setUserId(userId);
        payment.setOrderId(orderId);
        payment.setAmount(amount);
        payment.setStatus(status);
        payment.setPaymentMethod(method);
        payment.setTransactionId(transactionId);
        payment.setPaymentDate(paymentDate);
        payment.setPaymentGatewayResponse(paymentDetails);
        return payment;
    }
}