package com.example.observability.payment.service;

import com.example.observability.payment.model.PaymentCommand;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.cloud.sleuth.annotation.SpanTag;
import org.springframework.stereotype.Service;

@Service
public class PaymentGatewayService {
    private static final Logger log = LoggerFactory.getLogger(PaymentGatewayService.class);

    private final Random random = new Random();

    @NewSpan("payment.gateway-capture")
    public boolean capture(@SpanTag("order.id") PaymentCommand command) throws InterruptedException {
        int latency = 120 + random.nextInt(900);
        if ("espresso-machine".equals(command.getSku())) {
            latency += 700;
        }
        Thread.sleep(latency);
        boolean captured = random.nextInt(10) != 0;
        log.info("gateway capture completed orderId={} sku={} amountCents={} captured={} latencyMs={}",
            command.getOrderId(), command.getSku(), command.getAmountCents(), captured, latency);
        return captured;
    }
}
