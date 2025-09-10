package com.ecommerce.paymentservice.pact;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.LambdaDsl;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.ecommerce.paymentservice.telemetry.TelemetryClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;

@ExtendWith(PactConsumerTestExt.class)
class TelemetryServicePactTest {

    @Pact(consumer = "payment-service", provider = "telemetry-service")
    public RequestResponsePact telemetryEventPact(PactDslWithProvider builder) {
        return builder
            .given("telemetry service is available")
            .uponReceiving("a telemetry event")
            .path("/api/telemetry/events")
            .method("POST")
            .headers(Map.of(
                "Content-Type", "application/json"
            ))
            .body(LambdaDsl.newJsonBody((body) -> body
                .stringType("traceId")
                .stringType("spanId")
                .stringType("serviceName", "payment-service")
                .stringType("operation")
                .stringType("eventType")
                .stringType("timestamp")
                .stringType("status")
                .stringType("httpMethod")
                .stringType("httpUrl")
                .stringType("userId")
            ).build())
            .willRespondWith()
            .status(200)
            .headers(Map.of("Content-Type", "application/json"))
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "telemetryEventPact")
    void testStartTrace(MockServer mockServer) {
        // Arrange: Set up client with mock server URL
        TelemetryClient client = new TelemetryClient(mockServer.getUrl(), "payment-service");
        
        // Act: Make the API call - should not throw exception
        assertThatCode(() -> client.startTrace("test_operation", "POST", "/api/test", "123"))
            .doesNotThrowAnyException();
    }

    @Pact(consumer = "payment-service", provider = "telemetry-service")
    public RequestResponsePact telemetryCompleteEventPact(PactDslWithProvider builder) {
        return builder
            .given("telemetry service is available")
            .uponReceiving("a telemetry completion event")
            .path("/api/telemetry/events")
            .method("POST")
            .headers(Map.of(
                "Content-Type", "application/json"
            ))
            .body(LambdaDsl.newJsonBody((body) -> body
                .stringType("traceId")
                .stringType("spanId")
                .stringType("serviceName", "payment-service")
                .stringType("operation")
                .stringType("eventType")
                .stringType("timestamp")
                .numberType("durationMs")
                .stringType("status")
                .numberType("httpStatusCode")
                .stringType("errorMessage")
            ).build())
            .willRespondWith()
            .status(200)
            .headers(Map.of("Content-Type", "application/json"))
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "telemetryCompleteEventPact")
    void testFinishTrace(MockServer mockServer) {
        // Arrange: Set up client with mock server URL and context
        TelemetryClient client = new TelemetryClient(mockServer.getUrl(), "payment-service");
        
        // Set up trace context
        TelemetryClient.TraceContext.setTraceId("test_trace_123");
        TelemetryClient.TraceContext.setSpanId("test_span_456");
        TelemetryClient.TraceContext.setStartTime(System.currentTimeMillis() - 100);
        
        try {
            // Act: Make the API call - should not throw exception
            assertThatCode(() -> client.finishTrace("test_operation", 200, null))
                .doesNotThrowAnyException();
        } finally {
            // Clean up trace context
            TelemetryClient.TraceContext.clear();
        }
    }

    @Pact(consumer = "payment-service", provider = "telemetry-service")
    public RequestResponsePact telemetryServiceCallEventPact(PactDslWithProvider builder) {
        return builder
            .given("telemetry service is available")
            .uponReceiving("a telemetry service call event")
            .path("/api/telemetry/events")
            .method("POST")
            .headers(Map.of(
                "Content-Type", "application/json"
            ))
            .body(LambdaDsl.newJsonBody((body) -> body
                .stringType("traceId")
                .stringType("spanId")
                .stringType("parentSpanId")
                .stringType("serviceName", "payment-service")
                .stringType("operation")
                .stringType("eventType")
                .stringType("timestamp")
                .numberType("durationMs")
                .stringType("status")
                .stringType("httpMethod")
                .stringType("httpUrl")
                .numberType("httpStatusCode")
                .stringType("metadata")
            ).build())
            .willRespondWith()
            .status(200)
            .headers(Map.of("Content-Type", "application/json"))
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "telemetryServiceCallEventPact")
    void testRecordServiceCall(MockServer mockServer) {
        // Arrange: Set up client with mock server URL and context
        TelemetryClient client = new TelemetryClient(mockServer.getUrl(), "payment-service");
        
        // Set up trace context
        TelemetryClient.TraceContext.setTraceId("test_trace_123");
        TelemetryClient.TraceContext.setSpanId("test_span_456");
        
        try {
            // Act: Make the API call - should not throw exception
            assertThatCode(() -> client.recordServiceCall(
                "notification-service", 
                "send_notification", 
                "POST", 
                "/api/notifications/payment-confirmation", 
                150L, 
                200
            )).doesNotThrowAnyException();
        } finally {
            // Clean up trace context
            TelemetryClient.TraceContext.clear();
        }
    }
}