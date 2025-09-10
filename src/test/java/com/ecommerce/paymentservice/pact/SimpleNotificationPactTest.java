package com.ecommerce.paymentservice.pact;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.LambdaDsl;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;

@ExtendWith(PactConsumerTestExt.class)
class SimpleNotificationPactTest {

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
        // Create a simple WebClient to test the mock server
        WebClient webClient = WebClient.builder().build();
        
        // Act: Make the API call - should not throw exception
        assertThatCode(() -> {
            webClient.post()
                .uri(mockServer.getUrl() + "/api/notifications/payment-confirmation")
                .bodyValue(Map.of("paymentId", 1L, "userId", 100L, "orderId", 200L))
                .retrieve()
                .bodyToMono(Void.class)
                .block();
        }).doesNotThrowAnyException();
    }
}