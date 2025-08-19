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