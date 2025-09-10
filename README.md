# Payment Service

> **🟠 This service is highlighted in the architecture diagram below**

Payment processing and transaction management service for the e-commerce microservices ecosystem.

## Service Role: Both Consumer & Producer
This service processes payments for orders and produces payment events for the notification service.

## Architecture Overview

```
┌─────────────────┐    ┌─────────────────┐
│   User Service  │    │ Product Service │
│   (Port 8081)   │    │   (Port 8082)   │
│                 │    │                 │
│ • Authentication│    │ • Product Catalog│
│ • User Profiles │    │ • Inventory Mgmt│
│ • JWT Tokens    │    │ • Pricing       │
└─────────┬───────┘    └─────────┬───────┘
          │                      │
          │ validates users      │ fetches products
          │                      │
          ▼                      ▼
    ┌─────────────────────────────────┐
    │        Order Service            │
    │        (Port 8083)              │
    │                                 │
    │ • Order Management              │
    │ • Order Processing              │
    │ • Consumes User & Product APIs  │
    └─────────────┬───────────────────┘
                  │
                  │ triggers payment
                  │
                  ▼
    ┌─────────────────────────────────┐
    │   🟠  Payment Service           │
    │       (Port 8084)               │
    │                                 │
    │ • Payment Processing            │
    │ • Gateway Integration           │
    │ • Refund Management             │
    └─────────────┬───────────────────┘
                  │
                  │ sends notifications
                  │
                  ▼
    ┌─────────────────────────────────┐
    │    Notification Service         │
    │       (Port 8085)               │
    │                                 │
    │ • Email Notifications           │
    │ • SMS Notifications             │
    │ • Order & Payment Updates       │
    └─────────────────────────────────┘
                  │ All services send telemetry data
                  │
                  ▼
    ┌─────────────────────────────────┐
    │📊  Telemetry Service            │
    │       (Port 8086)               │
    │                                 │
    │ • Distributed Tracing           │
    │ • Service Metrics               │
    │ • Request Tracking              │
    │ • Performance Monitoring        │
    └─────────────────────────────────┘
```

## Pact Contract Testing

This service uses [Pact](https://pact.io/) for consumer contract testing to ensure reliable communication with external services.

### Consumer Role

This service acts as a consumer for the following external services:
- **Notification Service**: Payment confirmation, failure, and refund notifications
- **Telemetry Service**: Observability and monitoring data

### Running Pact Tests

#### Consumer Tests
```bash
# Run consumer tests and generate contracts
./gradlew pactTest

# Generated contracts will be in build/pacts/
```

#### Publishing Contracts
```bash
# Publish contracts to Pactflow
./gradlew pactPublish
```

### Contract Testing Approach

This implementation follows Pact's **"Be conservative in what you send"** principle:

- Consumer tests define minimal request structures with only required fields
- Request bodies cannot contain fields not defined in the contract
- Tests validate that actual API calls match contract expectations exactly
- Mock servers reject requests with unexpected extra fields

### Contract Files

Consumer contracts are generated in:
- `build/pacts/` - Local contract files  
- Pactflow - Centralized contract storage and management

### External Service Contracts

#### Notification Service Contracts
- **Payment Confirmation**: `POST /api/notifications/payment-confirmation`
  - Request: `{"paymentId": number, "userId": number, "orderId": number}`
  - Response: `200 OK`
- **Payment Failure**: `POST /api/notifications/payment-failure`
  - Request: `{"paymentId": number, "userId": number, "orderId": number}`
  - Response: `200 OK`
- **Refund Confirmation**: `POST /api/notifications/refund-confirmation`
  - Request: `{"paymentId": number, "userId": number, "orderId": number}`
  - Response: `200 OK`

#### Telemetry Service Contracts
- **Telemetry Events**: `POST /api/telemetry/events`
  - Request: Complex telemetry event data with traces, spans, timestamps, and metadata
  - Response: `200 OK`

### Troubleshooting

#### Common Issues

1. **Consumer Test Failures**
   - **Extra fields in request**: Remove any fields from request body that aren't actually needed
   - **Mock server expectation mismatch**: Verify HTTP method, path, headers, and body structure
   - **Content-Type headers**: Ensure request headers match exactly what the service sends
   - **URL path parameters**: Check that path parameters are correctly formatted in the contract

2. **Contract Generation Issues**
   - **Missing @Pact annotation**: Ensure each contract method has proper annotations
   - **Invalid JSON structure**: Verify body definitions match actual data structures
   - **Provider state setup**: Ensure provider state descriptions are descriptive and specific

3. **Pactflow Integration Issues**
   - **Authentication**: Verify `PACT_BROKER_TOKEN` environment variable is set
   - **Base URL**: Confirm `PACT_BROKER_BASE_URL` points to `https://pairgood.pactflow.io`
   - **Network connectivity**: Check firewall/proxy settings if publishing fails

#### Debug Commands

```bash
# Run with debug output
./gradlew pactTest --info --debug

# Run specific test class
./gradlew pactTest --tests="*NotificationServicePactTest*"

# Generate contracts without publishing
./gradlew pactTest -x pactPublish

# Clean and regenerate contracts
./gradlew clean pactTest
```

#### Debug Logging

Add to `application-test.properties` for detailed Pact logging:
```properties
logging.level.au.com.dius.pact=DEBUG
logging.level.org.apache.http=DEBUG
```

### Contract Evolution

When external services change their APIs:

1. **New Fields in Responses**: No action needed - consumers ignore extra fields
2. **Removed Response Fields**: Update consumer tests if those fields were being used
3. **New Required Request Fields**: Update consumer tests and service code
4. **Changed Endpoints**: Update consumer contract paths and service client code

### Integration with CI/CD

Consumer contract tests run automatically on:
- **Pull Requests**: Generate and validate contracts
- **Main Branch**: Publish contracts to Pactflow for provider verification
- **Feature Branches**: Generate contracts for validation (not published)

### Manual Testing

For local development against real services:
```bash
# Test against local services (disable Pact)
./gradlew test -Dpact.verifier.disabled=true

# Test against staging services
export EXTERNAL_SERVICE_URL=https://staging.example.com
./gradlew test -Dpact.verifier.disabled=true
```

### Contract Documentation

Generated contracts document:
- **API interactions**: What endpoints this service calls
- **Request formats**: Exact structure of requests sent
- **Response expectations**: What fields this service relies on
- **Error handling**: How this service handles different response scenarios

## Features

- **Payment Processing**: Secure payment transaction handling
- **Multiple Payment Methods**: Support for credit cards, debit cards, PayPal, etc.
- **Payment Gateway Integration**: Simulated payment gateway processing
- **Refund Management**: Complete refund processing capabilities
- **Transaction Tracking**: Detailed payment history and status tracking
- **Notification Integration**: Automatic payment confirmations and failure notifications

## Technology Stack

- **Framework**: Spring Boot 3.2.0
- **HTTP Client**: Spring WebFlux WebClient
- **Database**: H2 (in-memory)
- **ORM**: Spring Data JPA
- **Java Version**: 17
- **Contract Testing**: Pact 4.4.7

## API Endpoints

### Payment Processing
- `POST /api/payments/process` - Process payment for an order
- `GET /api/payments/{id}` - Get payment by ID
- `GET /api/payments/order/{orderId}` - Get payments for specific order
- `GET /api/payments/user/{userId}` - Get payments for specific user
- `GET /api/payments` - Get all payments

### Refund Management
- `POST /api/payments/{id}/refund` - Process refund for a payment

## Telemetry Integration

The Payment Service sends comprehensive telemetry data to the Telemetry Service for monitoring payment processing and transaction tracking:

### Telemetry Features
- **Request Tracing**: Complete tracing of payment processing workflows
- **Service Metrics**: Payment performance metrics including processing times and success rates
- **Error Tracking**: Automatic detection and reporting of payment failures and gateway errors
- **Transaction Monitoring**: Detailed tracking of payment transactions and refund operations
- **Security Metrics**: Monitoring of payment security events and anomalies

### Traced Operations
- Payment processing workflows and gateway interactions
- Refund processing and validation
- Payment status updates and lifecycle management
- Notification service integration calls
- Database operations (payment and transaction persistence)
- Payment gateway simulation and response handling

### Telemetry Configuration
The service is configured to send telemetry data to the Telemetry Service:
```yaml
telemetry:
  service:
    url: http://localhost:8086
    enabled: true
  tracing:
    sample-rate: 1.0
  metrics:
    enabled: true
    export-interval: 30s
  payment:
    track-gateway-calls: true
    track-refund-operations: true
```

## Running the Service

### Prerequisites
- Java 17+
- Gradle (or use included Gradle wrapper)
- **Notification Service** should be running on port 8085

### Start the Service
```bash
./gradlew bootRun
```

The service will start on **port 8084**.

### Database Access
- **H2 Console**: http://localhost:8084/h2-console
- **JDBC URL**: `jdbc:h2:mem:paymentdb`
- **Username**: `sa`
- **Password**: (empty)

## Service Dependencies

### Services This Service Consumes
- **Notification Service (port 8085)**: Sends payment confirmation and failure notifications

### Services That Use This Service
- **Order Service**: Triggers payment processing after order creation
- External payment gateways (simulated)

## Data Models

### Payment Entity
```json
{
  "id": 1,
  "orderId": 1,
  "userId": 1,
  "amount": 1299.99,
  "status": "COMPLETED",
  "paymentMethod": "CREDIT_CARD",
  "paymentDate": "2024-01-15T10:35:00",
  "transactionId": "TXN_ABC123DEF456",
  "paymentGatewayResponse": "Payment processed successfully"
}
```

### Payment Status Values
- `PENDING` - Payment initiated
- `PROCESSING` - Payment being processed
- `COMPLETED` - Payment successful
- `FAILED` - Payment failed
- `REFUNDED` - Payment refunded

### Payment Method Values
- `CREDIT_CARD` - Credit card payment
- `DEBIT_CARD` - Debit card payment
- `PAYPAL` - PayPal payment
- `BANK_TRANSFER` - Bank transfer
- `DIGITAL_WALLET` - Digital wallet payment

## Payment Gateway Simulation

The service includes a simulated payment gateway that:
- Introduces realistic processing delays
- Has a 10% random failure rate for testing
- Generates mock transaction IDs
- Simulates refund processing with 5% failure rate

## Example Usage

### Process a Payment
```bash
curl -X POST http://localhost:8084/api/payments/process \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 1,
    "userId": 1,
    "amount": 1299.99,
    "paymentMethod": "CREDIT_CARD",
    "cardNumber": "4111111111111111",
    "cardHolderName": "John Doe",
    "cvv": "123",
    "expiryDate": "12/25"
  }'
```

### Get Payment Details
```bash
curl -X GET http://localhost:8084/api/payments/1
```

### Get Payments for an Order
```bash
curl -X GET http://localhost:8084/api/payments/order/1
```

### Process a Refund
```bash
curl -X POST http://localhost:8084/api/payments/1/refund
```

### Get User's Payment History
```bash
curl -X GET http://localhost:8084/api/payments/user/1
```

## Service Configuration

The service can be configured via `application.yml`:

```yaml
services:
  notification-service:
    url: http://localhost:8085
```

## Error Handling

- **Payment Failures**: Automatically sends failure notifications
- **Gateway Errors**: Graceful handling of payment gateway issues
- **Refund Failures**: Proper error responses for refund issues
- **Network Issues**: Resilient handling of notification service unavailability

## Security Considerations

- **Card Data**: In production, card details should never be stored
- **PCI Compliance**: This is a demo - real implementations need PCI compliance
- **Tokenization**: Production systems should use payment tokens
- **Encryption**: All payment data should be encrypted in transit and at rest

## Related Services

- **[Order Service](../order-service/README.md)**: Initiates payment processing
- **[Notification Service](../notification-service/README.md)**: Receives payment events
- **[User Service](../user-service/README.md)**: Independent service
- **[Product Service](../product-service/README.md)**: Independent service
- **[Telemetry Service](../telemetry-service/README.md)**: Collects telemetry data from this service