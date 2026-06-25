package com.example.observability.payment.service;

import com.example.observability.payment.model.PaymentCommand;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.cloud.sleuth.annotation.SpanTag;
import org.springframework.stereotype.Service;

@Service
public class PaymentFraudScreeningService {
    private static final Logger log = LoggerFactory.getLogger(PaymentFraudScreeningService.class);

    private final Random random = new Random();

    @NewSpan("payment.fraud-screen")
    public boolean approve(@SpanTag("order.id") PaymentCommand command) throws InterruptedException {
        int latency = 35 + random.nextInt(120);
        Thread.sleep(latency);
        boolean approved = random.nextInt(20) != 0;
        log.info("fraud screen completed orderId={} customerId={} approved={} latencyMs={}",
            command.getOrderId(), command.getCustomerId(), approved, latency);
        return approved;
    }
}
