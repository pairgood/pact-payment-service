package com.ecommerce.paymentservice.pact;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.ecommerce.paymentservice.service.NotificationServiceClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Pact consumer contract tests for Notification Service integration.
 * 
 * These tests define the contract between payment-service (consumer)
 * and notification-service (provider) following Pact's "Be conservative
 * in what you send" principle.
 */
@ExtendWith(PactConsumerTestExt.class)
class NotificationServicePactTest {

    @Pact(consumer = "payment-service", provider = "notification-service")
    public RequestResponsePact paymentConfirmationContract(PactDslWithProvider builder) {
        return builder
            .given("notification service is available")
            .uponReceiving("a payment confirmation notification request")
            .path("/api/notifications/payment-confirmation")
            .method("POST")
            .headers("Content-Type", "application/json")
            .body(newJsonBody(body -> {
                body.numberType("paymentId");
                body.numberType("userId");
                body.numberType("orderId");
            }).build())
            .willRespondWith()
            .status(200)
            .toPact();
    }

    @Pact(consumer = "payment-service", provider = "notification-service")
    public RequestResponsePact paymentFailureContract(PactDslWithProvider builder) {
        return builder
            .given("notification service is available")
            .uponReceiving("a payment failure notification request")
            .path("/api/notifications/payment-failure")
            .method("POST")
            .headers("Content-Type", "application/json")
            .body(newJsonBody(body -> {
                body.numberType("paymentId");
                body.numberType("userId");
                body.numberType("orderId");
            }).build())
            .willRespondWith()
            .status(200)
            .toPact();
    }

    @Pact(consumer = "payment-service", provider = "notification-service")
    public RequestResponsePact refundConfirmationContract(PactDslWithProvider builder) {
        return builder
            .given("notification service is available")
            .uponReceiving("a refund confirmation notification request")
            .path("/api/notifications/refund-confirmation")
            .method("POST")
            .headers("Content-Type", "application/json")
            .body(newJsonBody(body -> {
                body.numberType("paymentId");
                body.numberType("userId");
                body.numberType("orderId");
            }).build())
            .willRespondWith()
            .status(200)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "paymentConfirmationContract", pactVersion = PactSpecVersion.V3)
    void testSendPaymentConfirmation(MockServer mockServer) {
        // Create service client with mock server URL
        NotificationServiceClient client = new NotificationServiceClient(mockServer.getUrl());
        
        // Execute the actual service call
        assertDoesNotThrow(() -> {
            client.sendPaymentConfirmation(1L, 100L, 200L);
        });
    }

    @Test
    @PactTestFor(pactMethod = "paymentFailureContract", pactVersion = PactSpecVersion.V3)
    void testSendPaymentFailure(MockServer mockServer) {
        // Create service client with mock server URL
        NotificationServiceClient client = new NotificationServiceClient(mockServer.getUrl());
        
        // Execute the actual service call
        assertDoesNotThrow(() -> {
            client.sendPaymentFailure(2L, 101L, 201L);
        });
    }

    @Test
    @PactTestFor(pactMethod = "refundConfirmationContract", pactVersion = PactSpecVersion.V3)
    void testSendRefundConfirmation(MockServer mockServer) {
        // Create service client with mock server URL
        NotificationServiceClient client = new NotificationServiceClient(mockServer.getUrl());
        
        // Execute the actual service call
        assertDoesNotThrow(() -> {
            client.sendRefundConfirmation(3L, 102L, 202L);
        });
    }
}