package com.ecommerce.paymentservice.service;

import com.ecommerce.paymentservice.dto.PaymentRequest;
import com.ecommerce.paymentservice.model.Payment;
import com.ecommerce.paymentservice.repository.PaymentRepository;
import com.ecommerce.paymentservice.telemetry.TelemetryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentGatewayService paymentGatewayService;

    @Mock
    private NotificationServiceClient notificationServiceClient;

    @Mock
    private TelemetryClient telemetryClient;

    @InjectMocks
    private PaymentService paymentService;

    private PaymentRequest testPaymentRequest;
    private Payment testPayment;

    @BeforeEach
    void setUp() {
        testPaymentRequest = new PaymentRequest();
        testPaymentRequest.setOrderId(100L);
        testPaymentRequest.setUserId(1L);
        testPaymentRequest.setAmount(new BigDecimal("99.99"));
        testPaymentRequest.setPaymentMethod(Payment.PaymentMethod.CREDIT_CARD);
        testPaymentRequest.setCardNumber("4111111111111111");
        testPaymentRequest.setCardHolderName("John Doe");
        testPaymentRequest.setCvv("123");
        testPaymentRequest.setExpiryDate("12/25");

        testPayment = new Payment();
        testPayment.setId(1L);
        testPayment.setOrderId(100L);
        testPayment.setUserId(1L);
        testPayment.setAmount(new BigDecimal("99.99"));
        testPayment.setStatus(Payment.PaymentStatus.PENDING);
        testPayment.setPaymentMethod(Payment.PaymentMethod.CREDIT_CARD);
    }

    @Test
    void processPayment_ShouldReturnCompletedPayment_WhenPaymentSuccessful() {
        // Given
        String transactionId = "TXN_123456789";
        
        Payment pendingPayment = new Payment();
        pendingPayment.setId(1L);
        pendingPayment.setUserId(1L);
        pendingPayment.setOrderId(100L);
        pendingPayment.setStatus(Payment.PaymentStatus.PENDING);
        
        Payment processingPayment = new Payment();
        processingPayment.setId(1L);
        processingPayment.setUserId(1L);
        processingPayment.setOrderId(100L);
        processingPayment.setStatus(Payment.PaymentStatus.PROCESSING);

        Payment completedPayment = new Payment();
        completedPayment.setId(1L);
        completedPayment.setUserId(1L);
        completedPayment.setOrderId(100L);
        completedPayment.setStatus(Payment.PaymentStatus.COMPLETED);
        completedPayment.setTransactionId(transactionId);
        
        when(paymentRepository.save(any(Payment.class)))
            .thenReturn(pendingPayment)     // First save (initial)
            .thenReturn(processingPayment)  // Second save (processing)
            .thenReturn(completedPayment);  // Third save (completed)
            
        when(paymentGatewayService.processPayment(testPaymentRequest)).thenReturn(transactionId);

        // When
        Payment result = paymentService.processPayment(testPaymentRequest);

        // Then
        assertNotNull(result);
        verify(paymentRepository, times(3)).save(any(Payment.class)); // initial, processing, completed
        verify(paymentGatewayService).processPayment(testPaymentRequest);
        verify(notificationServiceClient).sendPaymentConfirmation(
            any(Long.class), any(Long.class), any(Long.class)
        );
    }

    @Test
    void processPayment_ShouldReturnFailedPayment_WhenGatewayFails() {
        // Given
        Payment pendingPayment = new Payment();
        pendingPayment.setId(1L);
        pendingPayment.setUserId(1L);
        pendingPayment.setOrderId(100L);
        pendingPayment.setStatus(Payment.PaymentStatus.PENDING);
        
        Payment processingPayment = new Payment();
        processingPayment.setId(1L);
        processingPayment.setUserId(1L);
        processingPayment.setOrderId(100L);
        processingPayment.setStatus(Payment.PaymentStatus.PROCESSING);

        Payment failedPayment = new Payment();
        failedPayment.setId(1L);
        failedPayment.setUserId(1L);
        failedPayment.setOrderId(100L);
        failedPayment.setStatus(Payment.PaymentStatus.FAILED);
        
        when(paymentRepository.save(any(Payment.class)))
            .thenReturn(pendingPayment)     // First save (initial)
            .thenReturn(processingPayment)  // Second save (processing)
            .thenReturn(failedPayment);     // Third save (failed)
            
        when(paymentGatewayService.processPayment(testPaymentRequest))
            .thenThrow(new RuntimeException("Payment declined"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> paymentService.processPayment(testPaymentRequest));

        assertTrue(exception.getMessage().contains("Payment processing failed"));
        verify(paymentRepository, times(3)).save(any(Payment.class)); // initial, processing, failed
        verify(paymentGatewayService).processPayment(testPaymentRequest);
        verify(notificationServiceClient).sendPaymentFailure(
            any(Long.class), any(Long.class), any(Long.class)
        );
    }

    @Test
    void getPaymentById_ShouldReturnPayment_WhenPaymentExists() {
        // Given
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));

        // When
        Payment result = paymentService.getPaymentById(1L);

        // Then
        assertNotNull(result);
        assertEquals(testPayment.getId(), result.getId());
        verify(paymentRepository).findById(1L);
    }

    @Test
    void getPaymentById_ShouldThrowException_WhenPaymentNotFound() {
        // Given
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> paymentService.getPaymentById(999L));

        assertEquals("Payment not found", exception.getMessage());
        verify(paymentRepository).findById(999L);
    }

    @Test
    void getPaymentsByOrderId_ShouldReturnPayments() {
        // Given
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentRepository.findByOrderId(100L)).thenReturn(payments);

        // When
        List<Payment> result = paymentService.getPaymentsByOrderId(100L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPayment.getId(), result.get(0).getId());
        verify(paymentRepository).findByOrderId(100L);
    }

    @Test
    void getPaymentsByUserId_ShouldReturnPayments() {
        // Given
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentRepository.findByUserId(1L)).thenReturn(payments);

        // When
        List<Payment> result = paymentService.getPaymentsByUserId(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPayment.getId(), result.get(0).getId());
        verify(paymentRepository).findByUserId(1L);
    }

    @Test
    void getAllPayments_ShouldReturnAllPayments() {
        // Given
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentRepository.findAll()).thenReturn(payments);

        // When
        List<Payment> result = paymentService.getAllPayments();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(paymentRepository).findAll();
    }

    @Test
    void refundPayment_ShouldReturnRefundedPayment_WhenPaymentCompleted() {
        // Given
        testPayment.setStatus(Payment.PaymentStatus.COMPLETED);
        testPayment.setTransactionId("TXN_123456789");
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));

        Payment refundedPayment = new Payment();
        refundedPayment.setId(1L);
        refundedPayment.setUserId(1L);
        refundedPayment.setOrderId(100L);
        refundedPayment.setStatus(Payment.PaymentStatus.REFUNDED);
        when(paymentRepository.save(any(Payment.class))).thenReturn(refundedPayment);

        // When
        Payment result = paymentService.refundPayment(1L);

        // Then
        assertNotNull(result);
        verify(paymentGatewayService).refundPayment("TXN_123456789");
        verify(paymentRepository).save(any(Payment.class));
        verify(notificationServiceClient).sendRefundConfirmation(
            any(Long.class), any(Long.class), any(Long.class)
        );
    }

    @Test
    void refundPayment_ShouldThrowException_WhenPaymentNotCompleted() {
        // Given
        testPayment.setStatus(Payment.PaymentStatus.PENDING);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> paymentService.refundPayment(1L));

        assertEquals("Cannot refund payment that is not completed", exception.getMessage());
        verify(paymentGatewayService, never()).refundPayment(anyString());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void refundPayment_ShouldThrowException_WhenGatewayRefundFails() {
        // Given
        testPayment.setStatus(Payment.PaymentStatus.COMPLETED);
        testPayment.setTransactionId("TXN_123456789");
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        doThrow(new RuntimeException("Refund failed")).when(paymentGatewayService)
            .refundPayment("TXN_123456789");

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> paymentService.refundPayment(1L));

        assertTrue(exception.getMessage().contains("Refund processing failed"));
        verify(paymentGatewayService).refundPayment("TXN_123456789");
        verify(paymentRepository, never()).save(any(Payment.class));
    }
}