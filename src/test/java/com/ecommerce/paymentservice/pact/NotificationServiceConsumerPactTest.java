package com.ecommerce.paymentservice.pact;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.LambdaDsl;
import au.com.dius.pact.consumer.dsl.PactBuilder;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.ecommerce.paymentservice.service.NotificationServiceClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "notification-service")
class NotificationServiceConsumerPactTest {

    @Pact(consumer = "payment-service", provider = "notification-service")
    V4Pact sendPaymentConfirmation(PactBuilder builder) {
        return builder
            .given("a user with id 42 exists")
            .expectsToReceiveHttpInteraction("a request to send payment confirmation for payment 100", interaction -> interaction
                .withRequest(request -> request
                    .method("POST")
                    .path("/api/notifications/payment-confirmation")
                    .header("Content-Type", "application/json")
                    .body(LambdaDsl.newJsonBody(body -> {
                        body.integerType("paymentId", 100);
                        body.integerType("userId", 42);
                        body.integerType("orderId", 200);
                    }).build())
                )
                .willRespondWith(response -> response
                    .status(200)
                )
            )
            .toPact();
    }

    @Pact(consumer = "payment-service", provider = "notification-service")
    V4Pact sendPaymentFailure(PactBuilder builder) {
        return builder
            .given("a user with id 42 exists")
            .expectsToReceiveHttpInteraction("a request to send payment failure for payment 100", interaction -> interaction
                .withRequest(request -> request
                    .method("POST")
                    .path("/api/notifications/payment-failure")
                    .header("Content-Type", "application/json")
                    .body(LambdaDsl.newJsonBody(body -> {
                        body.integerType("paymentId", 100);
                        body.integerType("userId", 42);
                        body.integerType("orderId", 200);
                    }).build())
                )
                .willRespondWith(response -> response
                    .status(200)
                )
            )
            .toPact();
    }

    @Pact(consumer = "payment-service", provider = "notification-service")
    V4Pact sendRefundConfirmation(PactBuilder builder) {
        return builder
            .given("a user with id 42 exists")
            .expectsToReceiveHttpInteraction("a request to send refund confirmation for payment 100", interaction -> interaction
                .withRequest(request -> request
                    .method("POST")
                    .path("/api/notifications/refund-confirmation")
                    .header("Content-Type", "application/json")
                    .body(LambdaDsl.newJsonBody(body -> {
                        body.integerType("paymentId", 100);
                        body.integerType("userId", 42);
                        body.integerType("orderId", 200);
                    }).build())
                )
                .willRespondWith(response -> response
                    .status(200)
                )
            )
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "sendPaymentConfirmation")
    void shouldSendPaymentConfirmation(MockServer mockServer) {
        NotificationServiceClient client = new NotificationServiceClient();
        ReflectionTestUtils.setField(client, "notificationServiceUrl", mockServer.getUrl());
        ReflectionTestUtils.setField(client, "webClient", WebClient.builder().build());
        
        assertDoesNotThrow(() -> client.sendPaymentConfirmation(100L, 42L, 200L));
    }

    @Test
    @PactTestFor(pactMethod = "sendPaymentFailure")
    void shouldSendPaymentFailure(MockServer mockServer) {
        NotificationServiceClient client = new NotificationServiceClient();
        ReflectionTestUtils.setField(client, "notificationServiceUrl", mockServer.getUrl());
        ReflectionTestUtils.setField(client, "webClient", WebClient.builder().build());
        
        assertDoesNotThrow(() -> client.sendPaymentFailure(100L, 42L, 200L));
    }

    @Test
    @PactTestFor(pactMethod = "sendRefundConfirmation")
    void shouldSendRefundConfirmation(MockServer mockServer) {
        NotificationServiceClient client = new NotificationServiceClient();
        ReflectionTestUtils.setField(client, "notificationServiceUrl", mockServer.getUrl());
        ReflectionTestUtils.setField(client, "webClient", WebClient.builder().build());
        
        assertDoesNotThrow(() -> client.sendRefundConfirmation(100L, 42L, 200L));
    }
}
