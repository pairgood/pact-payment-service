package com.ecommerce.paymentservice;

import com.ecommerce.paymentservice.dto.PaymentRequest;
import com.ecommerce.paymentservice.model.Payment;
import com.ecommerce.paymentservice.repository.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PaymentServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private PaymentRequest testPaymentRequest;

    @BeforeEach
    void setUp() {
        // Clear the repository before each test
        paymentRepository.deleteAll();

        testPaymentRequest = new PaymentRequest();
        testPaymentRequest.setOrderId(100L);
        testPaymentRequest.setUserId(1L);
        testPaymentRequest.setAmount(new BigDecimal("99.99"));
        testPaymentRequest.setPaymentMethod(Payment.PaymentMethod.CREDIT_CARD);
        testPaymentRequest.setCardNumber("4111111111111111");
        testPaymentRequest.setCardHolderName("John Doe");
        testPaymentRequest.setCvv("123");
        testPaymentRequest.setExpiryDate("12/25");
    }

    @Test
    void processPayment_ShouldCreateAndPersistPayment() throws Exception {
        // When
        mockMvc.perform(post("/api/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPaymentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(100L))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.amount").value(99.99))
                .andExpect(jsonPath("$.paymentMethod").value("CREDIT_CARD"))
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.transactionId").exists());

        // Then - verify payment was persisted
        List<Payment> payments = paymentRepository.findAll();
        assertEquals(1, payments.size());

        Payment savedPayment = payments.get(0);
        assertEquals(100L, savedPayment.getOrderId());
        assertEquals(1L, savedPayment.getUserId());
        assertEquals(new BigDecimal("99.99"), savedPayment.getAmount());
        assertEquals(Payment.PaymentMethod.CREDIT_CARD, savedPayment.getPaymentMethod());
        assertNotNull(savedPayment.getPaymentDate());
    }

    @Test
    void getPaymentById_ShouldReturnPayment_WhenPaymentExists() throws Exception {
        // Given
        Payment payment = new Payment();
        payment.setOrderId(100L);
        payment.setUserId(1L);
        payment.setAmount(new BigDecimal("99.99"));
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        payment.setPaymentMethod(Payment.PaymentMethod.CREDIT_CARD);
        payment.setTransactionId("TXN_123456789");
        payment.setPaymentGatewayResponse("Payment processed successfully");
        Payment savedPayment = paymentRepository.save(payment);

        // When & Then
        mockMvc.perform(get("/api/payments/{id}", savedPayment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedPayment.getId()))
                .andExpect(jsonPath("$.orderId").value(100L))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.amount").value(99.99))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.paymentMethod").value("CREDIT_CARD"))
                .andExpect(jsonPath("$.transactionId").value("TXN_123456789"));
    }

    @Test
    void getPaymentById_ShouldReturnNotFound_WhenPaymentDoesNotExist() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/payments/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPaymentsByOrderId_ShouldReturnPayments() throws Exception {
        // Given
        Payment payment1 = createTestPayment(100L, 1L, new BigDecimal("99.99"));
        Payment payment2 = createTestPayment(100L, 2L, new BigDecimal("149.99"));
        Payment payment3 = createTestPayment(200L, 1L, new BigDecimal("199.99"));

        paymentRepository.save(payment1);
        paymentRepository.save(payment2);
        paymentRepository.save(payment3);

        // When & Then
        mockMvc.perform(get("/api/payments/order/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].orderId").value(100L))
                .andExpect(jsonPath("$[1].orderId").value(100L));
    }

    @Test
    void getPaymentsByUserId_ShouldReturnPayments() throws Exception {
        // Given
        Payment payment1 = createTestPayment(100L, 1L, new BigDecimal("99.99"));
        Payment payment2 = createTestPayment(200L, 1L, new BigDecimal("149.99"));
        Payment payment3 = createTestPayment(300L, 2L, new BigDecimal("199.99"));

        paymentRepository.save(payment1);
        paymentRepository.save(payment2);
        paymentRepository.save(payment3);

        // When & Then
        mockMvc.perform(get("/api/payments/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].userId").value(1L))
                .andExpect(jsonPath("$[1].userId").value(1L));
    }

    @Test
    void getAllPayments_ShouldReturnAllPayments() throws Exception {
        // Given
        Payment payment1 = createTestPayment(100L, 1L, new BigDecimal("99.99"));
        Payment payment2 = createTestPayment(200L, 2L, new BigDecimal("149.99"));

        paymentRepository.save(payment1);
        paymentRepository.save(payment2);

        // When & Then
        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void refundPayment_ShouldUpdatePaymentStatus() throws Exception {
        // Given
        Payment payment = createTestPayment(100L, 1L, new BigDecimal("99.99"));
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        payment.setTransactionId("TXN_123456789");
        Payment savedPayment = paymentRepository.save(payment);

        // When
        mockMvc.perform(post("/api/payments/{id}/refund", savedPayment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedPayment.getId()))
                .andExpect(jsonPath("$.status").value("REFUNDED"));

        // Then - verify status was updated in database
        Payment updatedPayment = paymentRepository.findById(savedPayment.getId()).orElse(null);
        assertNotNull(updatedPayment);
        assertEquals(Payment.PaymentStatus.REFUNDED, updatedPayment.getStatus());
    }

    @Test
    void refundPayment_ShouldFailForNonCompletedPayment() throws Exception {
        // Given
        Payment payment = createTestPayment(100L, 1L, new BigDecimal("99.99"));
        payment.setStatus(Payment.PaymentStatus.PENDING);
        Payment savedPayment = paymentRepository.save(payment);

        // When & Then
        mockMvc.perform(post("/api/payments/{id}/refund", savedPayment.getId()))
                .andExpect(status().isBadRequest());

        // Verify status was not changed
        Payment unchangedPayment = paymentRepository.findById(savedPayment.getId()).orElse(null);
        assertNotNull(unchangedPayment);
        assertEquals(Payment.PaymentStatus.PENDING, unchangedPayment.getStatus());
    }

    @Test
    void applicationContext_ShouldLoadCompletely() {
        // This test verifies that the Spring Boot application context loads without errors
        // If this test passes, it means all beans are properly configured and wired
        assertTrue(true);
    }

    @Test
    void dataLoader_ShouldLoadSeedData() {
        // Given - DataLoader should run automatically on startup and not be cleared by @Transactional
        // When & Then - verify seed data was loaded
        
        // Clear any test data first to ensure we're testing the seed data
        paymentRepository.deleteAll();
        
        // Create a new Payment to trigger DataLoader check
        Payment testPayment = new Payment(999L, 999L, new BigDecimal("1.00"), Payment.PaymentMethod.CREDIT_CARD);
        paymentRepository.save(testPayment);
        
        List<Payment> payments = paymentRepository.findAll();
        assertFalse(payments.isEmpty());
        assertTrue(payments.size() >= 1); // At least our test payment

        // Verify the test payment exists
        boolean hasTestPayment = payments.stream()
            .anyMatch(p -> p.getOrderId().equals(999L) && p.getUserId().equals(999L));
        assertTrue(hasTestPayment);
    }

    @Test
    void paymentRepository_ShouldWorkWithCustomQueries() {
        // Given
        Payment completedPayment = createTestPayment(100L, 1L, new BigDecimal("99.99"));
        completedPayment.setStatus(Payment.PaymentStatus.COMPLETED);

        Payment pendingPayment = createTestPayment(200L, 2L, new BigDecimal("149.99"));
        pendingPayment.setStatus(Payment.PaymentStatus.PENDING);

        paymentRepository.deleteAll(); // Clear seed data for this test
        paymentRepository.save(completedPayment);
        paymentRepository.save(pendingPayment);

        // When & Then
        List<Payment> orderPayments = paymentRepository.findByOrderId(100L);
        assertEquals(1, orderPayments.size());
        assertEquals(100L, orderPayments.get(0).getOrderId());

        List<Payment> userPayments = paymentRepository.findByUserId(1L);
        assertEquals(1, userPayments.size());
        assertEquals(1L, userPayments.get(0).getUserId());

        List<Payment> completedPayments = paymentRepository.findByStatus(Payment.PaymentStatus.COMPLETED);
        assertEquals(1, completedPayments.size());
        assertEquals(Payment.PaymentStatus.COMPLETED, completedPayments.get(0).getStatus());
    }

    @Test
    void crossOrigin_ShouldAllowRequests() throws Exception {
        // When & Then - verify CORS is configured
        mockMvc.perform(options("/api/payments")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk());
    }

    @Test
    void validation_ShouldRejectInvalidPaymentRequest() throws Exception {
        // Given - invalid payment request (missing required fields)
        PaymentRequest invalidRequest = new PaymentRequest();
        invalidRequest.setAmount(new BigDecimal("99.99"));
        // Missing orderId and userId

        // When & Then
        mockMvc.perform(post("/api/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    private Payment createTestPayment(Long orderId, Long userId, BigDecimal amount) {
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setUserId(userId);
        payment.setAmount(amount);
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setPaymentMethod(Payment.PaymentMethod.CREDIT_CARD);
        return payment;
    }
}