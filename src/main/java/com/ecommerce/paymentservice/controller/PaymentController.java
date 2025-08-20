package com.ecommerce.paymentservice.controller;

import com.ecommerce.paymentservice.dto.PaymentRequest;
import com.ecommerce.paymentservice.model.Payment;
import com.ecommerce.paymentservice.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
@Tag(name = "Payment Management", description = "API for processing payments, handling refunds, and managing payment records")
public class PaymentController {
    
    @Autowired
    private PaymentService paymentService;
    
    @PostMapping("/process")
    @Operation(summary = "Process payment", description = "Processes a payment for an order using the provided payment information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment processed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid payment data or payment failed"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Payment> processPayment(@Valid @RequestBody PaymentRequest paymentRequest) {
        try {
            Payment payment = paymentService.processPayment(paymentRequest);
            return ResponseEntity.ok(payment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID", description = "Retrieves a specific payment record using its unique identifier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment found and returned successfully"),
        @ApiResponse(responseCode = "404", description = "Payment not found with the provided ID"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Payment> getPaymentById(
        @Parameter(description = "Unique identifier of the payment", required = true, example = "1")
        @PathVariable Long id) {
        try {
            Payment payment = paymentService.getPaymentById(id);
            return ResponseEntity.ok(payment);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payments by order ID", description = "Retrieves all payment records associated with a specific order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payments retrieved successfully for the order"),
        @ApiResponse(responseCode = "404", description = "No payments found for the specified order"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Payment>> getPaymentsByOrderId(
        @Parameter(description = "Unique identifier of the order", required = true, example = "123")
        @PathVariable Long orderId) {
        List<Payment> payments = paymentService.getPaymentsByOrderId(orderId);
        return ResponseEntity.ok(payments);
    }
    
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get payments by user ID", description = "Retrieves all payment records associated with a specific user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payments retrieved successfully for the user"),
        @ApiResponse(responseCode = "404", description = "No payments found for the specified user"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Payment>> getPaymentsByUserId(
        @Parameter(description = "Unique identifier of the user", required = true, example = "456")
        @PathVariable Long userId) {
        List<Payment> payments = paymentService.getPaymentsByUserId(userId);
        return ResponseEntity.ok(payments);
    }
    
    @PostMapping("/{id}/refund")
    @Operation(summary = "Refund payment", description = "Processes a refund for a previously completed payment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment refunded successfully"),
        @ApiResponse(responseCode = "400", description = "Payment cannot be refunded or refund failed"),
        @ApiResponse(responseCode = "404", description = "Payment not found with the provided ID"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Payment> refundPayment(
        @Parameter(description = "Unique identifier of the payment to refund", required = true, example = "1")
        @PathVariable Long id) {
        try {
            Payment payment = paymentService.refundPayment(id);
            return ResponseEntity.ok(payment);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping
    @Operation(summary = "Retrieve all payments", description = "Returns a list of all payment records in the system (admin access typically required)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payments retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Payment>> getAllPayments() {
        List<Payment> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(payments);
    }
}