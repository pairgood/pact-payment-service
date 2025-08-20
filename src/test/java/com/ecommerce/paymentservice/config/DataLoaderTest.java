package com.ecommerce.paymentservice.config;

import com.ecommerce.paymentservice.model.Payment;
import com.ecommerce.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataLoaderTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private DataLoader dataLoader;

    @BeforeEach
    void setUp() {
        // Reset mocks before each test
        reset(paymentRepository);
    }

    @Test
    void run_ShouldLoadSeedData_WhenDatabaseIsEmpty() throws Exception {
        // Given
        when(paymentRepository.count()).thenReturn(0L);

        // When
        dataLoader.run();

        // Then
        verify(paymentRepository).count();
        verify(paymentRepository, times(8)).save(any(Payment.class));
    }

    @Test
    void run_ShouldNotLoadSeedData_WhenDatabaseHasData() throws Exception {
        // Given
        when(paymentRepository.count()).thenReturn(5L);

        // When
        dataLoader.run();

        // Then
        verify(paymentRepository).count();
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void run_ShouldCreatePaymentsWithCorrectData() throws Exception {
        // Given
        when(paymentRepository.count()).thenReturn(0L);
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);

        // When
        dataLoader.run();

        // Then
        verify(paymentRepository, times(8)).save(paymentCaptor.capture());
        List<Payment> savedPayments = paymentCaptor.getAllValues();

        assertEquals(8, savedPayments.size());

        // Verify first payment (John Doe's gaming setup)
        Payment firstPayment = savedPayments.get(0);
        assertEquals(1L, firstPayment.getId());
        assertEquals(1L, firstPayment.getUserId());
        assertEquals(1L, firstPayment.getOrderId());
        assertEquals(new BigDecimal("1479.97"), firstPayment.getAmount());
        assertEquals(Payment.PaymentStatus.COMPLETED, firstPayment.getStatus());
        assertEquals(Payment.PaymentMethod.CREDIT_CARD, firstPayment.getPaymentMethod());
        assertEquals("txn_john_gaming_001", firstPayment.getTransactionId());
        assertEquals("Visa ending in 1234", firstPayment.getPaymentGatewayResponse());

        // Verify second payment (Jane Smith's book collection)
        Payment secondPayment = savedPayments.get(1);
        assertEquals(2L, secondPayment.getId());
        assertEquals(2L, secondPayment.getUserId());
        assertEquals(2L, secondPayment.getOrderId());
        assertEquals(new BigDecimal("159.97"), secondPayment.getAmount());
        assertEquals(Payment.PaymentStatus.COMPLETED, secondPayment.getStatus());
        assertEquals(Payment.PaymentMethod.PAYPAL, secondPayment.getPaymentMethod());
        assertEquals("txn_jane_books_002", secondPayment.getTransactionId());
        assertEquals("PayPal account jane.smith@example.com", secondPayment.getPaymentGatewayResponse());

        // Verify a pending payment (Charlie Brown's wardrobe)
        Payment pendingPayment = savedPayments.get(4);
        assertEquals(5L, pendingPayment.getId());
        assertEquals(5L, pendingPayment.getUserId());
        assertEquals(5L, pendingPayment.getOrderId());
        assertEquals(new BigDecimal("219.94"), pendingPayment.getAmount());
        assertEquals(Payment.PaymentStatus.PENDING, pendingPayment.getStatus());
        assertEquals(Payment.PaymentMethod.CREDIT_CARD, pendingPayment.getPaymentMethod());

        // Verify a refunded payment (Diana Clark's tech upgrade)
        Payment refundedPayment = savedPayments.get(5);
        assertEquals(6L, refundedPayment.getId());
        assertEquals(6L, refundedPayment.getUserId());
        assertEquals(6L, refundedPayment.getOrderId());
        assertEquals(new BigDecimal("289.98"), refundedPayment.getAmount());
        assertEquals(Payment.PaymentStatus.REFUNDED, refundedPayment.getStatus());
        assertEquals(Payment.PaymentMethod.DEBIT_CARD, refundedPayment.getPaymentMethod());

        // Verify a failed payment
        Payment failedPayment = savedPayments.get(7);
        assertEquals(8L, failedPayment.getId());
        assertEquals(2L, failedPayment.getUserId());
        assertEquals(8L, failedPayment.getOrderId()); // Historical payment
        assertEquals(new BigDecimal("25.50"), failedPayment.getAmount());
        assertEquals(Payment.PaymentStatus.FAILED, failedPayment.getStatus());
        assertEquals(Payment.PaymentMethod.CREDIT_CARD, failedPayment.getPaymentMethod());
    }

    @Test
    void run_ShouldCreatePaymentsWithAllPaymentMethods() throws Exception {
        // Given
        when(paymentRepository.count()).thenReturn(0L);
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);

        // When
        dataLoader.run();

        // Then
        verify(paymentRepository, times(8)).save(paymentCaptor.capture());
        List<Payment> savedPayments = paymentCaptor.getAllValues();

        // Verify all payment methods are represented
        assertTrue(savedPayments.stream().anyMatch(p -> p.getPaymentMethod() == Payment.PaymentMethod.CREDIT_CARD));
        assertTrue(savedPayments.stream().anyMatch(p -> p.getPaymentMethod() == Payment.PaymentMethod.PAYPAL));
        assertTrue(savedPayments.stream().anyMatch(p -> p.getPaymentMethod() == Payment.PaymentMethod.BANK_TRANSFER));
        assertTrue(savedPayments.stream().anyMatch(p -> p.getPaymentMethod() == Payment.PaymentMethod.DEBIT_CARD));
    }

    @Test
    void run_ShouldCreatePaymentsWithAllStatuses() throws Exception {
        // Given
        when(paymentRepository.count()).thenReturn(0L);
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);

        // When
        dataLoader.run();

        // Then
        verify(paymentRepository, times(8)).save(paymentCaptor.capture());
        List<Payment> savedPayments = paymentCaptor.getAllValues();

        // Verify all payment statuses are represented
        assertTrue(savedPayments.stream().anyMatch(p -> p.getStatus() == Payment.PaymentStatus.COMPLETED));
        assertTrue(savedPayments.stream().anyMatch(p -> p.getStatus() == Payment.PaymentStatus.PROCESSING));
        assertTrue(savedPayments.stream().anyMatch(p -> p.getStatus() == Payment.PaymentStatus.PENDING));
        assertTrue(savedPayments.stream().anyMatch(p -> p.getStatus() == Payment.PaymentStatus.REFUNDED));
        assertTrue(savedPayments.stream().anyMatch(p -> p.getStatus() == Payment.PaymentStatus.FAILED));
    }

    @Test
    void run_ShouldCreatePaymentsWithValidAmounts() throws Exception {
        // Given
        when(paymentRepository.count()).thenReturn(0L);
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);

        // When
        dataLoader.run();

        // Then
        verify(paymentRepository, times(8)).save(paymentCaptor.capture());
        List<Payment> savedPayments = paymentCaptor.getAllValues();

        // Verify all payments have positive amounts
        for (Payment payment : savedPayments) {
            assertNotNull(payment.getAmount());
            assertTrue(payment.getAmount().compareTo(BigDecimal.ZERO) > 0);
        }
    }

    @Test
    void run_ShouldCreatePaymentsWithValidDates() throws Exception {
        // Given
        when(paymentRepository.count()).thenReturn(0L);
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);

        // When
        dataLoader.run();

        // Then
        verify(paymentRepository, times(8)).save(paymentCaptor.capture());
        List<Payment> savedPayments = paymentCaptor.getAllValues();

        // Verify all payments have payment dates
        for (Payment payment : savedPayments) {
            assertNotNull(payment.getPaymentDate());
        }
    }

    @Test
    void run_ShouldHandleRepositoryException() throws Exception {
        // Given
        when(paymentRepository.count()).thenReturn(0L);
        when(paymentRepository.save(any(Payment.class)))
            .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> dataLoader.run());
    }

    @Test
    void run_ShouldCreatePaymentsWithTransactionIds() throws Exception {
        // Given
        when(paymentRepository.count()).thenReturn(0L);
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);

        // When
        dataLoader.run();

        // Then
        verify(paymentRepository, times(8)).save(paymentCaptor.capture());
        List<Payment> savedPayments = paymentCaptor.getAllValues();

        // Verify completed payments have transaction IDs
        for (Payment payment : savedPayments) {
            if (payment.getStatus() == Payment.PaymentStatus.COMPLETED || 
                payment.getStatus() == Payment.PaymentStatus.REFUNDED ||
                payment.getStatus() == Payment.PaymentStatus.PROCESSING ||
                payment.getStatus() == Payment.PaymentStatus.FAILED) {
                assertNotNull(payment.getTransactionId());
                assertTrue(payment.getTransactionId().startsWith("txn_"));
            }
        }
    }
}