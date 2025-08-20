package com.ecommerce.paymentservice.telemetry;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class TelemetryClientTest {

    private TelemetryClient telemetryClient;
    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        telemetryClient = new TelemetryClient();
        
        // Set the telemetry service URL to point to our mock server
        ReflectionTestUtils.setField(telemetryClient, "telemetryServiceUrl", 
            mockWebServer.url("/").toString().replaceAll("/$", ""));
        ReflectionTestUtils.setField(telemetryClient, "serviceName", "payment-service");
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
        // Clear trace context after each test
        TelemetryClient.TraceContext.clear();
    }

    @Test
    void startTrace_ShouldGenerateTraceIdAndSendEvent() throws InterruptedException {
        // Given
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        // When
        String traceId = telemetryClient.startTrace("processPayment", "POST", "/api/payments/process", "user123");

        // Then
        assertNotNull(traceId);
        assertTrue(traceId.startsWith("trace_"));
        assertEquals(traceId, TelemetryClient.TraceContext.getTraceId());
        assertNotNull(TelemetryClient.TraceContext.getSpanId());
        assertNotNull(TelemetryClient.TraceContext.getStartTime());

        // Verify the request was sent
        RecordedRequest request = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(request);
        assertEquals("POST", request.getMethod());
        assertEquals("/api/telemetry/events", request.getPath());
    }

    @Test
    void finishTrace_ShouldSendCompletionEvent() throws InterruptedException {
        // Given
        mockWebServer.enqueue(new MockResponse().setResponseCode(200)); // for startTrace
        mockWebServer.enqueue(new MockResponse().setResponseCode(200)); // for finishTrace

        // Start a trace first
        telemetryClient.startTrace("processPayment", "POST", "/api/payments/process", "user123");
        
        // Consume the first request
        mockWebServer.takeRequest(1, TimeUnit.SECONDS);

        // When
        telemetryClient.finishTrace("processPayment", 200, null);

        // Then
        assertNull(TelemetryClient.TraceContext.getTraceId()); // Should be cleared
        assertNull(TelemetryClient.TraceContext.getSpanId());
        assertNull(TelemetryClient.TraceContext.getStartTime());

        // Verify the completion request was sent
        RecordedRequest request = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(request);
        assertEquals("POST", request.getMethod());
        assertEquals("/api/telemetry/events", request.getPath());
    }

    @Test
    void finishTrace_ShouldHandleErrorStatus() throws InterruptedException {
        // Given
        mockWebServer.enqueue(new MockResponse().setResponseCode(200)); // for startTrace
        mockWebServer.enqueue(new MockResponse().setResponseCode(200)); // for finishTrace

        telemetryClient.startTrace("processPayment", "POST", "/api/payments/process", "user123");
        mockWebServer.takeRequest(1, TimeUnit.SECONDS);

        // When
        telemetryClient.finishTrace("processPayment", 500, "Internal server error");

        // Then
        RecordedRequest request = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(request);
        String body = request.getBody().readUtf8();
        assertTrue(body.contains("ERROR") || body.contains("500"));
    }

    @Test
    void recordServiceCall_ShouldSendServiceCallEvent() throws InterruptedException {
        // Given
        mockWebServer.enqueue(new MockResponse().setResponseCode(200)); // for startTrace
        mockWebServer.enqueue(new MockResponse().setResponseCode(200)); // for recordServiceCall

        telemetryClient.startTrace("processPayment", "POST", "/api/payments/process", "user123");
        mockWebServer.takeRequest(1, TimeUnit.SECONDS);

        // When
        telemetryClient.recordServiceCall("notification-service", "sendConfirmation", "POST", 
            "/api/notifications/payment-confirmation", 150, 200);

        // Then
        RecordedRequest request = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(request);
        assertEquals("POST", request.getMethod());
        assertEquals("/api/telemetry/events", request.getPath());
    }

    @Test
    void recordServiceCall_ShouldNotSendEvent_WhenNoActiveTrace() throws InterruptedException {
        // Given - no active trace
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        // When
        telemetryClient.recordServiceCall("notification-service", "sendConfirmation", "POST", 
            "/api/notifications/payment-confirmation", 150, 200);

        // Then
        RecordedRequest request = mockWebServer.takeRequest(100, TimeUnit.MILLISECONDS);
        assertNull(request); // No request should be sent
    }

    @Test
    void logEvent_ShouldSendLogEvent() throws InterruptedException {
        // Given
        mockWebServer.enqueue(new MockResponse().setResponseCode(200)); // for startTrace
        mockWebServer.enqueue(new MockResponse().setResponseCode(200)); // for logEvent

        telemetryClient.startTrace("processPayment", "POST", "/api/payments/process", "user123");
        mockWebServer.takeRequest(1, TimeUnit.SECONDS);

        // When
        telemetryClient.logEvent("Payment processing started", "INFO");

        // Then
        RecordedRequest request = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(request);
        assertEquals("POST", request.getMethod());
        assertEquals("/api/telemetry/events", request.getPath());
    }

    @Test
    void logEvent_ShouldNotSendEvent_WhenNoActiveTrace() throws InterruptedException {
        // Given - no active trace
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        // When
        telemetryClient.logEvent("Payment processing started", "INFO");

        // Then
        RecordedRequest request = mockWebServer.takeRequest(100, TimeUnit.MILLISECONDS);
        assertNull(request); // No request should be sent
    }

    @Test
    void sendTelemetryEvent_ShouldHandleNetworkFailure() throws InterruptedException {
        // Given
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        // When & Then - should not throw exception
        assertDoesNotThrow(() -> 
            telemetryClient.startTrace("processPayment", "POST", "/api/payments/process", "user123"));

        // Verify request was attempted
        RecordedRequest request = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(request);
    }

    @Test
    void generateTraceId_ShouldReturnUniqueIds() {
        // When
        String traceId1 = telemetryClient.startTrace("operation1", "GET", "/test1", "user1");
        TelemetryClient.TraceContext.clear();
        String traceId2 = telemetryClient.startTrace("operation2", "GET", "/test2", "user2");

        // Then
        assertNotEquals(traceId1, traceId2);
        assertTrue(traceId1.startsWith("trace_"));
        assertTrue(traceId2.startsWith("trace_"));
    }

    @Test
    void traceContext_ShouldStoreAndRetrieveValues() {
        // When
        TelemetryClient.TraceContext.setTraceId("test-trace-123");
        TelemetryClient.TraceContext.setSpanId("test-span-456");
        TelemetryClient.TraceContext.setStartTime(12345L);

        // Then
        assertEquals("test-trace-123", TelemetryClient.TraceContext.getTraceId());
        assertEquals("test-span-456", TelemetryClient.TraceContext.getSpanId());
        assertEquals(12345L, TelemetryClient.TraceContext.getStartTime());
    }

    @Test
    void traceContext_ShouldClearAllValues() {
        // Given
        TelemetryClient.TraceContext.setTraceId("test-trace-123");
        TelemetryClient.TraceContext.setSpanId("test-span-456");
        TelemetryClient.TraceContext.setStartTime(12345L);

        // When
        TelemetryClient.TraceContext.clear();

        // Then
        assertNull(TelemetryClient.TraceContext.getTraceId());
        assertNull(TelemetryClient.TraceContext.getSpanId());
        assertNull(TelemetryClient.TraceContext.getStartTime());
    }

    @Test
    void traceContext_ShouldPropagateTraceInfo() {
        // When
        TelemetryClient.TraceContext.propagate("propagated-trace", "propagated-span");

        // Then
        assertEquals("propagated-trace", TelemetryClient.TraceContext.getTraceId());
        assertEquals("propagated-span", TelemetryClient.TraceContext.getSpanId());
    }

    @Test
    void constructor_ShouldInitializeWebClient() {
        // When & Then
        assertDoesNotThrow(() -> new TelemetryClient());
    }

    @Test
    void startTrace_ShouldHandleNullUserId() throws InterruptedException {
        // Given
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        // When & Then
        assertDoesNotThrow(() -> 
            telemetryClient.startTrace("processPayment", "POST", "/api/payments/process", null));

        RecordedRequest request = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(request);
    }

    @Test
    void finishTrace_ShouldCalculateDuration() throws InterruptedException {
        // Given
        mockWebServer.enqueue(new MockResponse().setResponseCode(200)); // for startTrace
        mockWebServer.enqueue(new MockResponse().setResponseCode(200)); // for finishTrace

        telemetryClient.startTrace("processPayment", "POST", "/api/payments/process", "user123");
        mockWebServer.takeRequest(1, TimeUnit.SECONDS);

        // Simulate some processing time
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When
        telemetryClient.finishTrace("processPayment", 200, null);

        // Then
        RecordedRequest request = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(request);
        String body = request.getBody().readUtf8();
        assertTrue(body.contains("durationMs"));
    }

    @Test
    void recordServiceCall_ShouldHandleErrorStatusCode() throws InterruptedException {
        // Given
        mockWebServer.enqueue(new MockResponse().setResponseCode(200)); // for startTrace
        mockWebServer.enqueue(new MockResponse().setResponseCode(200)); // for recordServiceCall

        telemetryClient.startTrace("processPayment", "POST", "/api/payments/process", "user123");
        mockWebServer.takeRequest(1, TimeUnit.SECONDS);

        // When
        telemetryClient.recordServiceCall("notification-service", "sendConfirmation", "POST", 
            "/api/notifications/payment-confirmation", 150, 404);

        // Then
        RecordedRequest request = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(request);
        String body = request.getBody().readUtf8();
        assertTrue(body.contains("ERROR") || body.contains("404"));
    }
}