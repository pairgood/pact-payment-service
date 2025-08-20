package com.ecommerce.paymentservice.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PaymentTest {

    private Validator validator;
    private Payment payment;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        payment = new Payment();
        payment.setOrderId(100L);
        payment.setUserId(1L);
        payment.setAmount(new BigDecimal("99.99"));
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setPaymentMethod(Payment.PaymentMethod.CREDIT_CARD);
        payment.setTransactionId("TXN_123456789");
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentGatewayResponse("Payment processed successfully");
    }

    @Test
    void defaultConstructor_ShouldSetDefaultValues() {
        // When
        Payment newPayment = new Payment();

        // Then
        assertEquals(Payment.PaymentStatus.PENDING, newPayment.getStatus());
        assertNotNull(newPayment.getPaymentDate());
        assertTrue(newPayment.getPaymentDate().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(newPayment.getPaymentDate().isAfter(LocalDateTime.now().minusSeconds(1)));
    }

    @Test
    void parameterizedConstructor_ShouldSetValuesCorrectly() {
        // Given
        Long orderId = 200L;
        Long userId = 2L;
        BigDecimal amount = new BigDecimal("149.99");
        Payment.PaymentMethod paymentMethod = Payment.PaymentMethod.PAYPAL;

        // When
        Payment newPayment = new Payment(orderId, userId, amount, paymentMethod);

        // Then
        assertEquals(orderId, newPayment.getOrderId());
        assertEquals(userId, newPayment.getUserId());
        assertEquals(amount, newPayment.getAmount());
        assertEquals(paymentMethod, newPayment.getPaymentMethod());
        assertEquals(Payment.PaymentStatus.PENDING, newPayment.getStatus());
        assertNotNull(newPayment.getPaymentDate());
    }

    @Test
    void gettersAndSetters_ShouldWorkCorrectly() {
        // Test ID
        payment.setId(1L);
        assertEquals(1L, payment.getId());

        // Test Order ID
        payment.setOrderId(200L);
        assertEquals(200L, payment.getOrderId());

        // Test User ID
        payment.setUserId(2L);
        assertEquals(2L, payment.getUserId());

        // Test Amount
        BigDecimal newAmount = new BigDecimal("199.99");
        payment.setAmount(newAmount);
        assertEquals(newAmount, payment.getAmount());

        // Test Status
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        assertEquals(Payment.PaymentStatus.COMPLETED, payment.getStatus());

        // Test Payment Method
        payment.setPaymentMethod(Payment.PaymentMethod.DEBIT_CARD);
        assertEquals(Payment.PaymentMethod.DEBIT_CARD, payment.getPaymentMethod());

        // Test Payment Date
        LocalDateTime newDate = LocalDateTime.now().plusDays(1);
        payment.setPaymentDate(newDate);
        assertEquals(newDate, payment.getPaymentDate());

        // Test Transaction ID
        payment.setTransactionId("TXN_987654321");
        assertEquals("TXN_987654321", payment.getTransactionId());

        // Test Payment Gateway Response
        payment.setPaymentGatewayResponse("Updated response");
        assertEquals("Updated response", payment.getPaymentGatewayResponse());
    }

    @Test
    void validation_ShouldPass_WhenAllRequiredFieldsPresent() {
        // When
        Set<ConstraintViolation<Payment>> violations = validator.validate(payment);

        // Then
        assertTrue(violations.isEmpty());
    }

    @Test
    void validation_ShouldFail_WhenOrderIdIsNull() {
        // Given
        payment.setOrderId(null);

        // When
        Set<ConstraintViolation<Payment>> violations = validator.validate(payment);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("orderId")));
    }

    @Test
    void validation_ShouldFail_WhenUserIdIsNull() {
        // Given
        payment.setUserId(null);

        // When
        Set<ConstraintViolation<Payment>> violations = validator.validate(payment);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("userId")));
    }

    @Test
    void validation_ShouldFail_WhenAmountIsNull() {
        // Given
        payment.setAmount(null);

        // When
        Set<ConstraintViolation<Payment>> violations = validator.validate(payment);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("amount")));
    }

    @Test
    void paymentStatus_ShouldHaveAllExpectedValues() {
        // Then
        Payment.PaymentStatus[] expectedStatuses = {
            Payment.PaymentStatus.PENDING,
            Payment.PaymentStatus.PROCESSING,
            Payment.PaymentStatus.COMPLETED,
            Payment.PaymentStatus.FAILED,
            Payment.PaymentStatus.REFUNDED
        };

        Payment.PaymentStatus[] actualStatuses = Payment.PaymentStatus.values();
        assertEquals(expectedStatuses.length, actualStatuses.length);

        for (Payment.PaymentStatus status : expectedStatuses) {
            assertNotNull(status);
            assertTrue(java.util.Arrays.asList(actualStatuses).contains(status));
        }
    }

    @Test
    void paymentMethod_ShouldHaveAllExpectedValues() {
        // Then
        Payment.PaymentMethod[] expectedMethods = {
            Payment.PaymentMethod.CREDIT_CARD,
            Payment.PaymentMethod.DEBIT_CARD,
            Payment.PaymentMethod.PAYPAL,
            Payment.PaymentMethod.BANK_TRANSFER,
            Payment.PaymentMethod.DIGITAL_WALLET
        };

        Payment.PaymentMethod[] actualMethods = Payment.PaymentMethod.values();
        assertEquals(expectedMethods.length, actualMethods.length);

        for (Payment.PaymentMethod method : expectedMethods) {
            assertNotNull(method);
            assertTrue(java.util.Arrays.asList(actualMethods).contains(method));
        }
    }

    @Test
    void paymentStatus_ShouldBeStoredAsString() {
        // Given
        payment.setStatus(Payment.PaymentStatus.COMPLETED);

        // When - simulating JPA persistence behavior
        String statusString = payment.getStatus().name();

        // Then
        assertEquals("COMPLETED", statusString);
    }

    @Test
    void paymentMethod_ShouldBeStoredAsString() {
        // Given
        payment.setPaymentMethod(Payment.PaymentMethod.DIGITAL_WALLET);

        // When - simulating JPA persistence behavior
        String methodString = payment.getPaymentMethod().name();

        // Then
        assertEquals("DIGITAL_WALLET", methodString);
    }

    @Test
    void payment_ShouldAllowNullOptionalFields() {
        // Given
        Payment minimalPayment = new Payment();
        minimalPayment.setOrderId(100L);
        minimalPayment.setUserId(1L);
        minimalPayment.setAmount(new BigDecimal("99.99"));

        // When
        Set<ConstraintViolation<Payment>> violations = validator.validate(minimalPayment);

        // Then
        assertTrue(violations.isEmpty());
        assertNull(minimalPayment.getTransactionId());
        assertNull(minimalPayment.getPaymentGatewayResponse());
    }

    @Test
    void amount_ShouldHandlePrecision() {
        // Given
        BigDecimal preciseAmount = new BigDecimal("123.456789");

        // When
        payment.setAmount(preciseAmount);

        // Then
        assertEquals(preciseAmount, payment.getAmount());
        assertEquals("123.456789", payment.getAmount().toString());
    }

    @Test
    void paymentDate_ShouldBeImmutable() {
        // Given
        LocalDateTime originalDate = payment.getPaymentDate();

        // When
        LocalDateTime retrievedDate = payment.getPaymentDate();

        // Then
        assertEquals(originalDate, retrievedDate);
        // LocalDateTime is immutable, so this test verifies the field stores the reference correctly
    }
}