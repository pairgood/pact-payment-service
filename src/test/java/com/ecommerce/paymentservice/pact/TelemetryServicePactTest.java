package com.ecommerce.paymentservice.pact;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.ecommerce.paymentservice.telemetry.TelemetryClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

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
    public RequestResponsePact telemetrySpanEventContract(PactDslWithProvider builder) {
        return builder
            .given("telemetry service is available")
            .uponReceiving("a telemetry span event")
            .path("/api/telemetry/events")
            .method("POST")
            .headers("Content-Type", "application/json")
            .body(newJsonBody(body -> {
                body.stringMatcher("traceId", "trace_\\w+");
                body.stringMatcher("spanId", "span_\\w+");
                body.stringType("serviceName");
                body.stringType("operation");
                body.stringValue("eventType", "SPAN");
                body.stringType("timestamp");
                body.stringValue("status", "SUCCESS");
                body.stringType("httpMethod");
                body.stringType("httpUrl");
                body.stringType("userId");
            }).build())
            .willRespondWith()
            .status(200)
            .toPact();
    }

    @Pact(consumer = "payment-service", provider = "telemetry-service")
    public RequestResponsePact telemetrySpanFinishContract(PactDslWithProvider builder) {
        return builder
            .given("telemetry service is available")
            .uponReceiving("a telemetry span finish event")
            .path("/api/telemetry/events")
            .method("POST")
            .headers("Content-Type", "application/json")
            .body(newJsonBody(body -> {
                body.stringType("traceId");
                body.stringType("spanId");
                body.stringType("serviceName");
                body.stringMatcher("operation", ".*_complete");
                body.stringValue("eventType", "SPAN");
                body.stringType("timestamp");
                body.numberType("durationMs");
                body.stringType("status");
                body.numberType("httpStatusCode");
                body.stringType("errorMessage");
            }).build())
            .willRespondWith()
            .status(200)
            .toPact();
    }

    @Pact(consumer = "payment-service", provider = "telemetry-service")
    public RequestResponsePact telemetryServiceCallContract(PactDslWithProvider builder) {
        return builder
            .given("telemetry service is available")
            .uponReceiving("a telemetry service call event")
            .path("/api/telemetry/events")
            .method("POST")
            .headers("Content-Type", "application/json")
            .body(newJsonBody(body -> {
                body.stringType("traceId");
                body.stringType("spanId");
                body.stringType("parentSpanId");
                body.stringType("serviceName");
                body.stringMatcher("operation", ".*_.*");
                body.stringValue("eventType", "SPAN");
                body.stringType("timestamp");
                body.numberType("durationMs");
                body.stringType("status");
                body.stringType("httpMethod");
                body.stringType("httpUrl");
                body.numberType("httpStatusCode");
                body.stringMatcher("metadata", "Outbound call to .*");
            }).build())
            .willRespondWith()
            .status(200)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "telemetrySpanEventContract", pactVersion = PactSpecVersion.V3)
    void testStartTrace(MockServer mockServer) {
        // Create telemetry client with mock server URL
        TelemetryClient client = new TelemetryClient(mockServer.getUrl(), "payment-service");
        
        // Execute the actual service call
        assertDoesNotThrow(() -> {
            client.startTrace("process_payment", "POST", "/api/payments", "user123");
        });
    }

    @Test
    @PactTestFor(pactMethod = "telemetrySpanFinishContract", pactVersion = PactSpecVersion.V3)
    void testFinishTrace(MockServer mockServer) {
        // Create telemetry client with mock server URL
        TelemetryClient client = new TelemetryClient(mockServer.getUrl(), "payment-service");
        
        // Set up trace context first
        TelemetryClient.TraceContext.setTraceId("trace_12345");
        TelemetryClient.TraceContext.setSpanId("span_67890");
        TelemetryClient.TraceContext.setStartTime(System.currentTimeMillis() - 1000);
        
        // Execute the actual service call
        assertDoesNotThrow(() -> {
            client.finishTrace("process_payment", 200, "");
        });
        
        // Clean up
        TelemetryClient.TraceContext.clear();
    }

    @Test
    @PactTestFor(pactMethod = "telemetryServiceCallContract", pactVersion = PactSpecVersion.V3)
    void testRecordServiceCall(MockServer mockServer) {
        // Create telemetry client with mock server URL
        TelemetryClient client = new TelemetryClient(mockServer.getUrl(), "payment-service");
        
        // Set up trace context first
        TelemetryClient.TraceContext.setTraceId("trace_12345");
        TelemetryClient.TraceContext.setSpanId("span_67890");
        
        // Execute the actual service call
        assertDoesNotThrow(() -> {
            client.recordServiceCall(
                "notification-service", 
                "send_notification", 
                "POST", 
                "/api/notifications/payment-confirmation", 
                150L, 
                200
            );
        });
        
        // Clean up
        TelemetryClient.TraceContext.clear();
    }
}