package com.ecommerce.paymentservice.pact;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.Map;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Pact consumer contract tests for Telemetry Service integration.
 * 
 * These tests define the contract between payment-service (consumer)
 * and telemetry-service (provider) for observability and monitoring data.
 */
@ExtendWith(PactConsumerTestExt.class)
class TelemetryServicePactTest {

    @Pact(consumer = "payment-service", provider = "telemetry-service")
    public RequestResponsePact telemetryEventContract(PactDslWithProvider builder) {
        return builder
            .given("telemetry service is available")
            .uponReceiving("a telemetry event")
            .path("/api/telemetry/events")
            .method("POST")
            .headers("Content-Type", "application/json")
            .body(newJsonBody(body -> {
                body.stringType("traceId");
                body.stringType("spanId");
                body.stringType("serviceName");
                body.stringType("operation");
                body.stringValue("eventType", "SPAN");
                body.stringType("timestamp");
                body.stringType("status");
            }).build())
            .willRespondWith()
            .status(200)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "telemetryEventContract", pactVersion = PactSpecVersion.V3)
    void testSendTelemetryEvent(MockServer mockServer) {
        // Create a simple WebClient to send telemetry data
        WebClient webClient = WebClient.builder().build();
        
        // Create telemetry event data matching what the service would send
        Map<String, Object> eventData = Map.of(
            "traceId", "trace_12345",
            "spanId", "span_67890", 
            "serviceName", "payment-service",
            "operation", "process_payment",
            "eventType", "SPAN",
            "timestamp", LocalDateTime.now().toString(),
            "status", "SUCCESS"
        );
        
        // Execute the actual HTTP call that represents what our telemetry client would do
        assertDoesNotThrow(() -> {
            webClient.post()
                .uri(mockServer.getUrl() + "/api/telemetry/events")
                .bodyValue(eventData)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
        });
    }
}