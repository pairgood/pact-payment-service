package com.ecommerce.paymentservice.service;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceClientTest {

    private NotificationServiceClient notificationServiceClient;
    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        notificationServiceClient = new NotificationServiceClient();
        // Set the URL to point to our mock server
        ReflectionTestUtils.setField(notificationServiceClient, "notificationServiceUrl", 
            mockWebServer.url("/").toString().replaceAll("/$", ""));
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void sendPaymentConfirmation_ShouldSendRequest_WhenServiceRespondsSuccessfully() throws InterruptedException {
        // Given
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        // When
        assertDoesNotThrow(() -> 
            notificationServiceClient.sendPaymentConfirmation(1L, 100L, 50L));

        // Then
        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/api/notifications/payment-confirmation", request.getPath());
        assertNotNull(request.getBody());
    }

    @Test
    void sendPaymentConfirmation_ShouldHandleError_WhenServiceFails() throws InterruptedException {
        // Given
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        // When & Then - should not throw exception, just log error
        assertDoesNotThrow(() -> 
            notificationServiceClient.sendPaymentConfirmation(1L, 100L, 50L));

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/api/notifications/payment-confirmation", request.getPath());
    }

    @Test
    void sendPaymentFailure_ShouldSendRequest_WhenServiceRespondsSuccessfully() throws InterruptedException {
        // Given
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        // When
        assertDoesNotThrow(() -> 
            notificationServiceClient.sendPaymentFailure(1L, 100L, 50L));

        // Then
        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/api/notifications/payment-failure", request.getPath());
        assertNotNull(request.getBody());
    }

    @Test
    void sendPaymentFailure_ShouldHandleError_WhenServiceFails() throws InterruptedException {
        // Given
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        // When & Then - should not throw exception, just log error
        assertDoesNotThrow(() -> 
            notificationServiceClient.sendPaymentFailure(1L, 100L, 50L));

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/api/notifications/payment-failure", request.getPath());
    }

    @Test
    void sendRefundConfirmation_ShouldSendRequest_WhenServiceRespondsSuccessfully() throws InterruptedException {
        // Given
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        // When
        assertDoesNotThrow(() -> 
            notificationServiceClient.sendRefundConfirmation(1L, 100L, 50L));

        // Then
        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/api/notifications/refund-confirmation", request.getPath());
        assertNotNull(request.getBody());
    }

    @Test
    void sendRefundConfirmation_ShouldHandleError_WhenServiceFails() throws InterruptedException {
        // Given
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        // When & Then - should not throw exception, just log error
        assertDoesNotThrow(() -> 
            notificationServiceClient.sendRefundConfirmation(1L, 100L, 50L));

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/api/notifications/refund-confirmation", request.getPath());
    }

    @Test
    void constructor_ShouldInitializeWebClient() {
        // When
        NotificationServiceClient client = new NotificationServiceClient();

        // Then
        assertNotNull(client);
        // WebClient is package-private, so we can't directly test it
        // But we can verify the constructor doesn't throw an exception
    }
}