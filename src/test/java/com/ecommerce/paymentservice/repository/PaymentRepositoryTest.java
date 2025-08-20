package com.ecommerce.paymentservice.repository;

import com.ecommerce.paymentservice.model.Payment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class PaymentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PaymentRepository paymentRepository;

    private Payment testPayment1;
    private Payment testPayment2;
    private Payment testPayment3;

    @BeforeEach
    void setUp() {
        testPayment1 = new Payment();
        testPayment1.setOrderId(100L);
        testPayment1.setUserId(1L);
        testPayment1.setAmount(new BigDecimal("99.99"));
        testPayment1.setStatus(Payment.PaymentStatus.COMPLETED);
        testPayment1.setPaymentMethod(Payment.PaymentMethod.CREDIT_CARD);
        testPayment1.setTransactionId("TXN_123456789");
        testPayment1.setPaymentDate(LocalDateTime.now());
        testPayment1.setPaymentGatewayResponse("Payment processed successfully");

        testPayment2 = new Payment();
        testPayment2.setOrderId(200L);
        testPayment2.setUserId(1L); // Same user, different order
        testPayment2.setAmount(new BigDecimal("149.99"));
        testPayment2.setStatus(Payment.PaymentStatus.PENDING);
        testPayment2.setPaymentMethod(Payment.PaymentMethod.PAYPAL);
        testPayment2.setPaymentDate(LocalDateTime.now());

        testPayment3 = new Payment();
        testPayment3.setOrderId(100L); // Same order as payment1
        testPayment3.setUserId(2L); // Different user
        testPayment3.setAmount(new BigDecimal("19.99"));
        testPayment3.setStatus(Payment.PaymentStatus.FAILED);
        testPayment3.setPaymentMethod(Payment.PaymentMethod.DEBIT_CARD);
        testPayment3.setPaymentDate(LocalDateTime.now());
        testPayment3.setPaymentGatewayResponse("Payment failed - insufficient funds");
    }

    @Test
    void save_ShouldPersistPayment() {
        // When
        Payment savedPayment = paymentRepository.save(testPayment1);

        // Then
        assertNotNull(savedPayment.getId());
        assertEquals(testPayment1.getOrderId(), savedPayment.getOrderId());
        assertEquals(testPayment1.getUserId(), savedPayment.getUserId());
        assertEquals(testPayment1.getAmount(), savedPayment.getAmount());
        assertEquals(testPayment1.getStatus(), savedPayment.getStatus());
        assertEquals(testPayment1.getPaymentMethod(), savedPayment.getPaymentMethod());
    }

    @Test
    void findById_ShouldReturnPayment_WhenPaymentExists() {
        // Given
        Payment savedPayment = entityManager.persistAndFlush(testPayment1);

        // When
        Optional<Payment> found = paymentRepository.findById(savedPayment.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals(savedPayment.getId(), found.get().getId());
        assertEquals(testPayment1.getOrderId(), found.get().getOrderId());
    }

    @Test
    void findById_ShouldReturnEmpty_WhenPaymentDoesNotExist() {
        // When
        Optional<Payment> found = paymentRepository.findById(999L);

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void findByOrderId_ShouldReturnPayments_WhenPaymentsExist() {
        // Given
        entityManager.persistAndFlush(testPayment1);
        entityManager.persistAndFlush(testPayment2);
        entityManager.persistAndFlush(testPayment3); // Same order as payment1

        // When
        List<Payment> payments = paymentRepository.findByOrderId(100L);

        // Then
        assertEquals(2, payments.size());
        assertTrue(payments.stream().allMatch(p -> p.getOrderId().equals(100L)));
    }

    @Test
    void findByOrderId_ShouldReturnEmptyList_WhenNoPaymentsExist() {
        // When
        List<Payment> payments = paymentRepository.findByOrderId(999L);

        // Then
        assertTrue(payments.isEmpty());
    }

    @Test
    void findByUserId_ShouldReturnPayments_WhenPaymentsExist() {
        // Given
        entityManager.persistAndFlush(testPayment1);
        entityManager.persistAndFlush(testPayment2); // Same user as payment1
        entityManager.persistAndFlush(testPayment3);

        // When
        List<Payment> payments = paymentRepository.findByUserId(1L);

        // Then
        assertEquals(2, payments.size());
        assertTrue(payments.stream().allMatch(p -> p.getUserId().equals(1L)));
    }

    @Test
    void findByUserId_ShouldReturnEmptyList_WhenNoPaymentsExist() {
        // When
        List<Payment> payments = paymentRepository.findByUserId(999L);

        // Then
        assertTrue(payments.isEmpty());
    }

    @Test
    void findByStatus_ShouldReturnPayments_WhenPaymentsWithStatusExist() {
        // Given
        entityManager.persistAndFlush(testPayment1); // COMPLETED
        entityManager.persistAndFlush(testPayment2); // PENDING
        entityManager.persistAndFlush(testPayment3); // FAILED

        // When
        List<Payment> completedPayments = paymentRepository.findByStatus(Payment.PaymentStatus.COMPLETED);
        List<Payment> pendingPayments = paymentRepository.findByStatus(Payment.PaymentStatus.PENDING);
        List<Payment> failedPayments = paymentRepository.findByStatus(Payment.PaymentStatus.FAILED);

        // Then
        assertEquals(1, completedPayments.size());
        assertEquals(Payment.PaymentStatus.COMPLETED, completedPayments.get(0).getStatus());

        assertEquals(1, pendingPayments.size());
        assertEquals(Payment.PaymentStatus.PENDING, pendingPayments.get(0).getStatus());

        assertEquals(1, failedPayments.size());
        assertEquals(Payment.PaymentStatus.FAILED, failedPayments.get(0).getStatus());
    }

    @Test
    void findByStatus_ShouldReturnEmptyList_WhenNoPaymentsWithStatusExist() {
        // Given
        entityManager.persistAndFlush(testPayment1); // COMPLETED

        // When
        List<Payment> refundedPayments = paymentRepository.findByStatus(Payment.PaymentStatus.REFUNDED);

        // Then
        assertTrue(refundedPayments.isEmpty());
    }

    @Test
    void findAll_ShouldReturnAllPayments() {
        // Given
        entityManager.persistAndFlush(testPayment1);
        entityManager.persistAndFlush(testPayment2);
        entityManager.persistAndFlush(testPayment3);

        // When
        List<Payment> allPayments = paymentRepository.findAll();

        // Then
        assertEquals(3, allPayments.size());
    }

    @Test
    void delete_ShouldRemovePayment() {
        // Given
        Payment savedPayment = entityManager.persistAndFlush(testPayment1);

        // When
        paymentRepository.delete(savedPayment);
        entityManager.flush();

        // Then
        Optional<Payment> found = paymentRepository.findById(savedPayment.getId());
        assertFalse(found.isPresent());
    }

    @Test
    void count_ShouldReturnCorrectCount() {
        // Given
        entityManager.persistAndFlush(testPayment1);
        entityManager.persistAndFlush(testPayment2);

        // When
        long count = paymentRepository.count();

        // Then
        assertEquals(2, count);
    }

    @Test
    void payment_ShouldHaveCorrectConstraints() {
        // Given - payment with null required fields
        Payment invalidPayment = new Payment();

        // When & Then - should fail validation when trying to persist
        assertThrows(Exception.class, () -> {
            entityManager.persistAndFlush(invalidPayment);
        });
    }

    @Test
    void payment_ShouldSetDefaultValues() {
        // Given
        Payment payment = new Payment();

        // When - creating payment without setting status or date
        // The default constructor should set these

        // Then
        assertEquals(Payment.PaymentStatus.PENDING, payment.getStatus());
        assertNotNull(payment.getPaymentDate());
    }
}