package com.ecommerce.paymentservice.service;

import com.ecommerce.paymentservice.dto.PaymentRequest;
import com.ecommerce.paymentservice.model.Payment;
import com.ecommerce.paymentservice.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private PaymentGatewayService paymentGatewayService;
    
    @Autowired
    private NotificationServiceClient notificationServiceClient;
    
    public Payment processPayment(PaymentRequest paymentRequest) {
        Payment payment = new Payment(
            paymentRequest.getOrderId(),
            paymentRequest.getUserId(),
            paymentRequest.getAmount(),
            paymentRequest.getPaymentMethod()
        );
        
        // Save payment with PENDING status
        payment = paymentRepository.save(payment);
        
        try {
            // Process payment through gateway
            payment.setStatus(Payment.PaymentStatus.PROCESSING);
            paymentRepository.save(payment);
            
            String transactionId = paymentGatewayService.processPayment(paymentRequest);
            
            // Payment successful
            payment.setTransactionId(transactionId);
            payment.setStatus(Payment.PaymentStatus.COMPLETED);
            payment.setPaymentGatewayResponse("Payment processed successfully");
            
            Payment completedPayment = paymentRepository.save(payment);
            
            // Send payment confirmation notification
            notificationServiceClient.sendPaymentConfirmation(
                completedPayment.getId(), 
                completedPayment.getUserId(), 
                completedPayment.getOrderId()
            );
            
            return completedPayment;
            
        } catch (Exception e) {
            // Payment failed
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setPaymentGatewayResponse("Payment failed: " + e.getMessage());
            
            Payment failedPayment = paymentRepository.save(payment);
            
            // Send payment failure notification
            notificationServiceClient.sendPaymentFailure(
                failedPayment.getId(), 
                failedPayment.getUserId(), 
                failedPayment.getOrderId()
            );
            
            throw new RuntimeException("Payment processing failed: " + e.getMessage());
        }
    }
    
    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Payment not found"));
    }
    
    public List<Payment> getPaymentsByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }
    
    public List<Payment> getPaymentsByUserId(Long userId) {
        return paymentRepository.findByUserId(userId);
    }
    
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
    
    public Payment refundPayment(Long id) {
        Payment payment = getPaymentById(id);
        
        if (payment.getStatus() != Payment.PaymentStatus.COMPLETED) {
            throw new RuntimeException("Cannot refund payment that is not completed");
        }
        
        try {
            paymentGatewayService.refundPayment(payment.getTransactionId());
            
            payment.setStatus(Payment.PaymentStatus.REFUNDED);
            Payment refundedPayment = paymentRepository.save(payment);
            
            // Send refund notification
            notificationServiceClient.sendRefundConfirmation(
                refundedPayment.getId(), 
                refundedPayment.getUserId(), 
                refundedPayment.getOrderId()
            );
            
            return refundedPayment;
            
        } catch (Exception e) {
            throw new RuntimeException("Refund processing failed: " + e.getMessage());
        }
    }
}