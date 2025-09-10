package com.ecommerce.paymentservice.pact;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.LambdaDsl;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.ecommerce.paymentservice.service.NotificationServiceClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;

@ExtendWith(PactConsumerTestExt.class)
class NotificationServicePactTest {

    @Pact(consumer = "payment-service", provider = "notification-service")
    public RequestResponsePact paymentConfirmationPact(PactDslWithProvider builder) {
        return builder
            .given("notification service is available")
            .uponReceiving("a payment confirmation notification request")
            .path("/api/notifications/payment-confirmation")
            .method("POST")
            .headers(Map.of(
                "Content-Type", "application/json"
            ))
            .body(LambdaDsl.newJsonBody((body) -> body
                .numberType("paymentId", 1L)
                .numberType("userId", 100L)
                .numberType("orderId", 200L)
            ).build())
            .willRespondWith()
            .status(200)
            .headers(Map.of("Content-Type", "application/json"))
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "paymentConfirmationPact")
    void testSendPaymentConfirmation(MockServer mockServer) {
        // Arrange: Set up client with mock server URL
        NotificationServiceClient client = new NotificationServiceClient(mockServer.getUrl());
        
        // Act: Make the API call - should not throw exception
        assertThatCode(() -> client.sendPaymentConfirmation(1L, 100L, 200L))
            .doesNotThrowAnyException();
    }

    @Pact(consumer = "payment-service", provider = "notification-service")
    public RequestResponsePact paymentFailurePact(PactDslWithProvider builder) {
        return builder
            .given("notification service is available")
            .uponReceiving("a payment failure notification request")
            .path("/api/notifications/payment-failure")
            .method("POST")
            .headers(Map.of(
                "Content-Type", "application/json"
            ))
            .body(LambdaDsl.newJsonBody((body) -> body
                .numberType("paymentId", 2L)
                .numberType("userId", 101L)
                .numberType("orderId", 201L)
            ).build())
            .willRespondWith()
            .status(200)
            .headers(Map.of("Content-Type", "application/json"))
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "paymentFailurePact")
    void testSendPaymentFailure(MockServer mockServer) {
        // Arrange: Set up client with mock server URL
        NotificationServiceClient client = new NotificationServiceClient(mockServer.getUrl());
        
        // Act: Make the API call - should not throw exception
        assertThatCode(() -> client.sendPaymentFailure(2L, 101L, 201L))
            .doesNotThrowAnyException();
    }

    @Pact(consumer = "payment-service", provider = "notification-service")
    public RequestResponsePact refundConfirmationPact(PactDslWithProvider builder) {
        return builder
            .given("notification service is available")
            .uponReceiving("a refund confirmation notification request")
            .path("/api/notifications/refund-confirmation")
            .method("POST")
            .headers(Map.of(
                "Content-Type", "application/json"
            ))
            .body(LambdaDsl.newJsonBody((body) -> body
                .numberType("paymentId", 3L)
                .numberType("userId", 102L)
                .numberType("orderId", 202L)
            ).build())
            .willRespondWith()
            .status(200)
            .headers(Map.of("Content-Type", "application/json"))
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "refundConfirmationPact")
    void testSendRefundConfirmation(MockServer mockServer) {
        // Arrange: Set up client with mock server URL
        NotificationServiceClient client = new NotificationServiceClient(mockServer.getUrl());
        
        // Act: Make the API call - should not throw exception
        assertThatCode(() -> client.sendRefundConfirmation(3L, 102L, 202L))
            .doesNotThrowAnyException();
    }
}