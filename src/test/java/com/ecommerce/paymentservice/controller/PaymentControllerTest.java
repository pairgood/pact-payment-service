package com.ecommerce.paymentservice.controller;

import com.ecommerce.paymentservice.dto.PaymentRequest;
import com.ecommerce.paymentservice.model.Payment;
import com.ecommerce.paymentservice.service.PaymentService;
import com.ecommerce.paymentservice.telemetry.TelemetryClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@ActiveProfiles("test")
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private TelemetryClient telemetryClient;

    @Autowired
    private ObjectMapper objectMapper;

    private Payment testPayment;
    private PaymentRequest testPaymentRequest;

    @BeforeEach
    void setUp() {
        testPayment = new Payment();
        testPayment.setId(1L);
        testPayment.setOrderId(100L);
        testPayment.setUserId(1L);
        testPayment.setAmount(new BigDecimal("99.99"));
        testPayment.setStatus(Payment.PaymentStatus.COMPLETED);
        testPayment.setPaymentMethod(Payment.PaymentMethod.CREDIT_CARD);
        testPayment.setTransactionId("TXN_123456789");
        testPayment.setPaymentDate(LocalDateTime.now());
        testPayment.setPaymentGatewayResponse("Payment processed successfully");

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
    void processPayment_ShouldReturnPayment_WhenValidRequest() throws Exception {
        // Given
        when(paymentService.processPayment(any(PaymentRequest.class))).thenReturn(testPayment);

        // When & Then
        mockMvc.perform(post("/api/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPaymentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.orderId").value(100L))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.amount").value(99.99))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.paymentMethod").value("CREDIT_CARD"))
                .andExpect(jsonPath("$.transactionId").value("TXN_123456789"));

        verify(paymentService).processPayment(any(PaymentRequest.class));
    }

    @Test
    void processPayment_ShouldReturnBadRequest_WhenPaymentFails() throws Exception {
        // Given
        when(paymentService.processPayment(any(PaymentRequest.class)))
                .thenThrow(new RuntimeException("Payment processing failed"));

        // When & Then
        mockMvc.perform(post("/api/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPaymentRequest)))
                .andExpect(status().isBadRequest());

        verify(paymentService).processPayment(any(PaymentRequest.class));
    }

    @Test
    void getPaymentById_ShouldReturnPayment_WhenPaymentExists() throws Exception {
        // Given
        when(paymentService.getPaymentById(1L)).thenReturn(testPayment);

        // When & Then
        mockMvc.perform(get("/api/payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.orderId").value(100L))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.amount").value(99.99))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        verify(paymentService).getPaymentById(1L);
    }

    @Test
    void getPaymentById_ShouldReturnNotFound_WhenPaymentDoesNotExist() throws Exception {
        // Given
        when(paymentService.getPaymentById(999L))
                .thenThrow(new RuntimeException("Payment not found"));

        // When & Then
        mockMvc.perform(get("/api/payments/999"))
                .andExpect(status().isNotFound());

        verify(paymentService).getPaymentById(999L);
    }

    @Test
    void getPaymentsByOrderId_ShouldReturnPayments_WhenPaymentsExist() throws Exception {
        // Given
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentService.getPaymentsByOrderId(100L)).thenReturn(payments);

        // When & Then
        mockMvc.perform(get("/api/payments/order/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].orderId").value(100L));

        verify(paymentService).getPaymentsByOrderId(100L);
    }

    @Test
    void getPaymentsByUserId_ShouldReturnPayments_WhenPaymentsExist() throws Exception {
        // Given
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentService.getPaymentsByUserId(1L)).thenReturn(payments);

        // When & Then
        mockMvc.perform(get("/api/payments/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].userId").value(1L));

        verify(paymentService).getPaymentsByUserId(1L);
    }

    @Test
    void refundPayment_ShouldReturnRefundedPayment_WhenRefundSuccessful() throws Exception {
        // Given
        Payment refundedPayment = new Payment();
        refundedPayment.setId(1L);
        refundedPayment.setStatus(Payment.PaymentStatus.REFUNDED);
        when(paymentService.refundPayment(1L)).thenReturn(refundedPayment);

        // When & Then
        mockMvc.perform(post("/api/payments/1/refund"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("REFUNDED"));

        verify(paymentService).refundPayment(1L);
    }

    @Test
    void refundPayment_ShouldReturnBadRequest_WhenRefundFails() throws Exception {
        // Given
        when(paymentService.refundPayment(1L))
                .thenThrow(new RuntimeException("Cannot refund payment that is not completed"));

        // When & Then
        mockMvc.perform(post("/api/payments/1/refund"))
                .andExpect(status().isBadRequest());

        verify(paymentService).refundPayment(1L);
    }

    @Test
    void getAllPayments_ShouldReturnAllPayments() throws Exception {
        // Given
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentService.getAllPayments()).thenReturn(payments);

        // When & Then
        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(paymentService).getAllPayments();
    }
}