package com.example.observability.order.service;

import com.example.observability.order.jmx.OrderStats;
import com.example.observability.order.model.CheckoutRequest;
import com.example.observability.order.model.CheckoutResponse;
import com.example.observability.order.model.PaymentCommand;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final JmsTemplate jmsTemplate;
    private final OrderStats orderStats;
    private final Timer checkoutTimer;
    private final Counter checkoutFailures;
    private final List<String> orders = Collections.synchronizedList(new ArrayList<>());

    public OrderService(JmsTemplate jmsTemplate, OrderStats orderStats, MeterRegistry registry) {
        this.jmsTemplate = jmsTemplate;
        this.orderStats = orderStats;
        this.checkoutTimer = registry.timer("checkout.order.duration");
        this.checkoutFailures = registry.counter("checkout.order.failures");
    }

    public CheckoutResponse checkout(CheckoutRequest request) {
        long started = System.nanoTime();
        try {
            validate(request);
            String orderId = "ord-" + UUID.randomUUID().toString().substring(0, 8);
            int amount = priceFor(request.getSku()) * request.getQuantity();
            PaymentCommand command = new PaymentCommand(
                orderId,
                request.getCustomerId(),
                request.getSku(),
                request.getQuantity(),
                amount
            );
            jmsTemplate.convertAndSend("checkout.payments", command);
            orders.add(orderId + ":" + request.getSku() + ":" + request.getQuantity());
            long elapsedMillis = Duration.ofNanos(System.nanoTime() - started).toMillis();
            checkoutTimer.record(Duration.ofMillis(elapsedMillis));
            orderStats.recordOrder(elapsedMillis);
            log.info("checkout accepted orderId={} customerId={} sku={} amountCents={}",
                orderId, request.getCustomerId(), request.getSku(), amount);
            return new CheckoutResponse(orderId, "ACCEPTED", "Order accepted and payment queued");
        } catch (RuntimeException ex) {
            checkoutFailures.increment();
            orderStats.recordFailure();
            log.warn("checkout failed customerId={} sku={} reason={}",
                request.getCustomerId(), request.getSku(), ex.getMessage());
            throw ex;
        }
    }

    public List<String> orders() {
        synchronized (orders) {
            return new ArrayList<>(orders);
        }
    }

    private void validate(CheckoutRequest request) {
        if (request == null || !StringUtils.hasText(request.getCustomerId())) {
            throw new IllegalArgumentException("customerId is required");
        }
        if (!StringUtils.hasText(request.getSku())) {
            throw new IllegalArgumentException("sku is required");
        }
        if (request.getQuantity() <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
    }

    private int priceFor(String sku) {
        if ("espresso-machine".equals(sku)) {
            return 19999;
        }
        if ("travel-mug".equals(sku)) {
            return 1899;
        }
        return 1299;
    }
}
