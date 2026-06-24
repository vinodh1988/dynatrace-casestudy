package com.example.observability.payment.service;

import com.example.observability.payment.jmx.PaymentStats;
import com.example.observability.payment.model.PaymentCommand;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

@Service
public class PaymentProcessor {
    private static final Logger log = LoggerFactory.getLogger(PaymentProcessor.class);

    private final PaymentStats paymentStats;
    private final Timer paymentTimer;
    private final Counter declinedCounter;
    private final Random random = new Random();

    public PaymentProcessor(PaymentStats paymentStats, MeterRegistry registry) {
        this.paymentStats = paymentStats;
        this.paymentTimer = registry.timer("checkout.payment.duration");
        this.declinedCounter = registry.counter("checkout.payment.declines");
    }

    @JmsListener(destination = "checkout.payments")
    public void process(PaymentCommand command) throws InterruptedException {
        long started = System.nanoTime();
        int latency = 120 + random.nextInt(900);
        if ("espresso-machine".equals(command.getSku())) {
            latency += 700;
        }
        Thread.sleep(latency);

        if (random.nextInt(10) == 0) {
            paymentStats.recordDeclined();
            declinedCounter.increment();
            log.warn("payment declined orderId={} customerId={} amountCents={}",
                command.getOrderId(), command.getCustomerId(), command.getAmountCents());
            return;
        }

        long elapsedMillis = Duration.ofNanos(System.nanoTime() - started).toMillis();
        paymentTimer.record(Duration.ofMillis(elapsedMillis));
        paymentStats.recordProcessed(elapsedMillis);
        log.info("payment captured orderId={} customerId={} sku={} amountCents={} elapsedMs={}",
            command.getOrderId(), command.getCustomerId(), command.getSku(), command.getAmountCents(), elapsedMillis);
    }
}
