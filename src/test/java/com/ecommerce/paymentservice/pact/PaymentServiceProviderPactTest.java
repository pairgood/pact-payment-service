package com.ecommerce.paymentservice.pact;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.IgnoreNoPactsToVerify;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.PactBrokerAuth;
import com.ecommerce.paymentservice.model.Payment;
import com.ecommerce.paymentservice.repository.PaymentRepository;
import com.ecommerce.paymentservice.service.NotificationServiceClient;
import com.ecommerce.paymentservice.service.PaymentGatewayService;
import com.ecommerce.paymentservice.telemetry.TelemetryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@Provider("payment-service")   // MUST match spring.application.name exactly
@PactBroker(
    url = "http://localhost:9292",
    authentication = @PactBrokerAuth(username = "admin", password = "admin")
)
@IgnoreNoPactsToVerify  // Allow test to pass when no consumer pacts exist yet
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaymentServiceProviderPactTest {

    @LocalServerPort
    private int port;

    @MockBean
    private PaymentRepository paymentRepository;

    @MockBean
    private PaymentGatewayService paymentGatewayService;

    @MockBean
    private NotificationServiceClient notificationServiceClient;

    @MockBean
    private TelemetryClient telemetryClient;

    @BeforeEach
    void setUp(PactVerificationContext context) {
        // Context will be null when @IgnoreNoPactsToVerify creates a placeholder test
        if (context != null) {
            context.setTarget(new HttpTestTarget("localhost", port));
        }

        // Set up default mock behavior for telemetry client
        when(telemetryClient.startTrace(anyString(), anyString(), anyString(), anyString())).thenReturn("trace_123");
        doNothing().when(telemetryClient).finishTrace(anyString(), anyInt(), anyString());
        doNothing().when(telemetryClient).logEvent(anyString(), anyString());

        // Set up default mock behavior for external service clients
        doNothing().when(notificationServiceClient).sendPaymentConfirmation(anyLong(), anyLong(), anyLong());
        doNothing().when(notificationServiceClient).sendPaymentFailure(anyLong(), anyLong(), anyLong());
        doNothing().when(notificationServiceClient).sendRefundConfirmation(anyLong(), anyLong(), anyLong());
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void verifyPact(PactVerificationContext context) {
        // Context will be null when @IgnoreNoPactsToVerify creates a placeholder test
        if (context != null) {
            context.verifyInteraction();
        }
    }

    // State string must be IDENTICAL to consumer's given() — character for character
    @State("a payment with id 1 exists")
    void paymentWithId1Exists() {
        Payment payment = new Payment(100L, 42L, new BigDecimal("99.99"), Payment.PaymentMethod.CREDIT_CARD);
        payment.setId(1L);
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        payment.setTransactionId("txn_12345");

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @State("a payment with id 999 does not exist")
    void paymentWithId999DoesNotExist() {
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());
    }

    @State("payments exist for order 100")
    void paymentsExistForOrder100() {
        Payment payment1 = new Payment(100L, 42L, new BigDecimal("99.99"), Payment.PaymentMethod.CREDIT_CARD);
        payment1.setId(1L);
        payment1.setStatus(Payment.PaymentStatus.COMPLETED);

        Payment payment2 = new Payment(100L, 42L, new BigDecimal("49.99"), Payment.PaymentMethod.PAYPAL);
        payment2.setId(2L);
        payment2.setStatus(Payment.PaymentStatus.COMPLETED);

        List<Payment> payments = new ArrayList<>();
        payments.add(payment1);
        payments.add(payment2);

        when(paymentRepository.findByOrderId(100L)).thenReturn(payments);
    }

    @State("payments exist for user 42")
    void paymentsExistForUser42() {
        Payment payment1 = new Payment(100L, 42L, new BigDecimal("99.99"), Payment.PaymentMethod.CREDIT_CARD);
        payment1.setId(1L);
        payment1.setStatus(Payment.PaymentStatus.COMPLETED);

        Payment payment2 = new Payment(101L, 42L, new BigDecimal("149.99"), Payment.PaymentMethod.DEBIT_CARD);
        payment2.setId(2L);
        payment2.setStatus(Payment.PaymentStatus.COMPLETED);

        List<Payment> payments = new ArrayList<>();
        payments.add(payment1);
        payments.add(payment2);

        when(paymentRepository.findByUserId(42L)).thenReturn(payments);
    }

    @State("multiple payments exist in the system")
    void multiplePaymentsExist() {
        Payment payment1 = new Payment(100L, 42L, new BigDecimal("99.99"), Payment.PaymentMethod.CREDIT_CARD);
        payment1.setId(1L);
        payment1.setStatus(Payment.PaymentStatus.COMPLETED);

        Payment payment2 = new Payment(101L, 43L, new BigDecimal("149.99"), Payment.PaymentMethod.PAYPAL);
        payment2.setId(2L);
        payment2.setStatus(Payment.PaymentStatus.PENDING);

        List<Payment> payments = new ArrayList<>();
        payments.add(payment1);
        payments.add(payment2);

        when(paymentRepository.findAll()).thenReturn(payments);
    }

    @State("payment gateway is available")
    void paymentGatewayAvailable() throws Exception {
        // Mock successful payment processing
        when(paymentGatewayService.processPayment(any())).thenReturn("txn_" + System.currentTimeMillis());

        // Mock repository save to return the payment with an ID
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            if (payment.getId() == null) {
                payment.setId(1L);
            }
            return payment;
        });
    }

    @State("a completed payment with id 1 exists that can be refunded")
    void completedPaymentExists() throws Exception {
        Payment payment = new Payment(100L, 42L, new BigDecimal("99.99"), Payment.PaymentMethod.CREDIT_CARD);
        payment.setId(1L);
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        payment.setTransactionId("txn_12345");

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(paymentGatewayService).refundPayment(anyString());
    }
}
