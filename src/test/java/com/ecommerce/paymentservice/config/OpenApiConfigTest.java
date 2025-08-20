package com.ecommerce.paymentservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OpenApiConfigTest {

    private OpenApiConfig openApiConfig;

    @BeforeEach
    void setUp() {
        openApiConfig = new OpenApiConfig();
    }

    @Test
    void paymentServiceOpenAPI_ShouldReturnConfiguredOpenAPI() {
        // When
        OpenAPI openAPI = openApiConfig.paymentServiceOpenAPI();

        // Then
        assertNotNull(openAPI);
        assertNotNull(openAPI.getInfo());
        assertNotNull(openAPI.getServers());
    }

    @Test
    void paymentServiceOpenAPI_ShouldHaveCorrectInfoConfiguration() {
        // When
        OpenAPI openAPI = openApiConfig.paymentServiceOpenAPI();
        Info info = openAPI.getInfo();

        // Then
        assertEquals("Payment Service API", info.getTitle());
        assertEquals("1.0", info.getVersion());
        assertNotNull(info.getDescription());
        assertTrue(info.getDescription().contains("payment processing"));
        assertTrue(info.getDescription().contains("e-commerce microservices"));
    }

    @Test
    void paymentServiceOpenAPI_ShouldHaveCorrectContactInfo() {
        // When
        OpenAPI openAPI = openApiConfig.paymentServiceOpenAPI();
        Contact contact = openAPI.getInfo().getContact();

        // Then
        assertNotNull(contact);
        assertEquals("support@ecommerce.com", contact.getEmail());
        assertEquals("E-Commerce Support", contact.getName());
        assertEquals("https://www.ecommerce.com", contact.getUrl());
    }

    @Test
    void paymentServiceOpenAPI_ShouldHaveCorrectLicenseInfo() {
        // When
        OpenAPI openAPI = openApiConfig.paymentServiceOpenAPI();
        License license = openAPI.getInfo().getLicense();

        // Then
        assertNotNull(license);
        assertEquals("MIT License", license.getName());
        assertEquals("https://choosealicense.com/licenses/mit/", license.getUrl());
    }

    @Test
    void paymentServiceOpenAPI_ShouldHaveCorrectServerConfiguration() {
        // When
        OpenAPI openAPI = openApiConfig.paymentServiceOpenAPI();
        List<Server> servers = openAPI.getServers();

        // Then
        assertNotNull(servers);
        assertEquals(1, servers.size());

        Server devServer = servers.get(0);
        assertEquals("http://localhost:8084", devServer.getUrl());
        assertEquals("Server URL in Development environment", devServer.getDescription());
    }

    @Test
    void paymentServiceOpenAPI_ShouldHaveDetailedDescription() {
        // When
        OpenAPI openAPI = openApiConfig.paymentServiceOpenAPI();
        String description = openAPI.getInfo().getDescription();

        // Then
        assertNotNull(description);
        assertTrue(description.contains("payment processing"));
        assertTrue(description.contains("refunds"));
        assertTrue(description.contains("payment method management"));
        assertTrue(description.contains("transaction history"));
        assertTrue(description.contains("payment validation"));
        assertTrue(description.contains("e-commerce microservices ecosystem"));
    }

    @Test
    void paymentServiceOpenAPI_ShouldBeConsistentAcrossMultipleCalls() {
        // When
        OpenAPI openAPI1 = openApiConfig.paymentServiceOpenAPI();
        OpenAPI openAPI2 = openApiConfig.paymentServiceOpenAPI();

        // Then
        assertNotSame(openAPI1, openAPI2); // Different instances
        assertEquals(openAPI1.getInfo().getTitle(), openAPI2.getInfo().getTitle());
        assertEquals(openAPI1.getInfo().getVersion(), openAPI2.getInfo().getVersion());
        assertEquals(openAPI1.getServers().size(), openAPI2.getServers().size());
    }

    @Test
    void paymentServiceOpenAPI_ShouldHaveValidConfiguration() {
        // When
        OpenAPI openAPI = openApiConfig.paymentServiceOpenAPI();

        // Then
        // Verify all required components are present
        assertNotNull(openAPI.getInfo());
        assertNotNull(openAPI.getInfo().getTitle());
        assertNotNull(openAPI.getInfo().getVersion());
        assertNotNull(openAPI.getInfo().getContact());
        assertNotNull(openAPI.getInfo().getLicense());
        assertNotNull(openAPI.getServers());
        assertFalse(openAPI.getServers().isEmpty());

        // Verify no null or empty strings
        assertFalse(openAPI.getInfo().getTitle().trim().isEmpty());
        assertFalse(openAPI.getInfo().getVersion().trim().isEmpty());
        assertFalse(openAPI.getInfo().getDescription().trim().isEmpty());
    }

    @Test
    void openApiConfig_ShouldBeInstantiable() {
        // When & Then
        assertDoesNotThrow(() -> new OpenApiConfig());
    }

    @Test
    void paymentServiceOpenAPI_ShouldUseCorrectPort() {
        // When
        OpenAPI openAPI = openApiConfig.paymentServiceOpenAPI();
        String serverUrl = openAPI.getServers().get(0).getUrl();

        // Then
        assertTrue(serverUrl.contains("8084")); // Payment service port
    }
}