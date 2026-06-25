package com.example.observability.payment.service;

import com.example.observability.payment.model.AuthorizationResult;
import com.example.observability.payment.model.PaymentCommand;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.cloud.sleuth.annotation.SpanTag;
import org.springframework.stereotype.Service;

@Service
public class PaymentAuthorizationService {
    private static final Logger log = LoggerFactory.getLogger(PaymentAuthorizationService.class);

    private final PaymentFraudScreeningService fraudScreeningService;
    private final PaymentGatewayService paymentGatewayService;

    public PaymentAuthorizationService(
        PaymentFraudScreeningService fraudScreeningService,
        PaymentGatewayService paymentGatewayService
    ) {
        this.fraudScreeningService = fraudScreeningService;
        this.paymentGatewayService = paymentGatewayService;
    }

    @NewSpan("payment.authorize")
    public AuthorizationResult authorize(@SpanTag("order.id") PaymentCommand command) throws InterruptedException {
        long started = System.nanoTime();
        if (!fraudScreeningService.approve(command)) {
            long elapsedMillis = Duration.ofNanos(System.nanoTime() - started).toMillis();
            log.warn("payment authorization blocked orderId={} customerId={} elapsedMs={}",
                command.getOrderId(), command.getCustomerId(), elapsedMillis);
            return new AuthorizationResult(false, "fraud-screening", elapsedMillis);
        }

        boolean captured = paymentGatewayService.capture(command);
        long elapsedMillis = Duration.ofNanos(System.nanoTime() - started).toMillis();
        log.info("payment authorization completed orderId={} approved={} elapsedMs={}",
            command.getOrderId(), captured, elapsedMillis);
        return new AuthorizationResult(captured, captured ? "approved" : "gateway-decline", elapsedMillis);
    }
}
