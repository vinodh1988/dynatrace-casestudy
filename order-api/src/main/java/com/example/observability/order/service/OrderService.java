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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final CheckoutPreparationService checkoutPreparationService;
    private final PaymentDispatchService paymentDispatchService;
    private final OrderStats orderStats;
    private final Timer checkoutTimer;
    private final Counter checkoutFailures;
    private final List<String> orders = Collections.synchronizedList(new ArrayList<>());

    public OrderService(
        CheckoutPreparationService checkoutPreparationService,
        PaymentDispatchService paymentDispatchService,
        OrderStats orderStats,
        MeterRegistry registry
    ) {
        this.checkoutPreparationService = checkoutPreparationService;
        this.paymentDispatchService = paymentDispatchService;
        this.orderStats = orderStats;
        this.checkoutTimer = registry.timer("checkout.order.duration");
        this.checkoutFailures = registry.counter("checkout.order.failures");
    }

    public CheckoutResponse checkout(CheckoutRequest request) {
        long started = System.nanoTime();
        try {
            PaymentCommand command = checkoutPreparationService.prepare(request);
            paymentDispatchService.dispatch(command);
            orders.add(command.getOrderId() + ":" + command.getSku() + ":" + command.getQuantity());
            long elapsedMillis = Duration.ofNanos(System.nanoTime() - started).toMillis();
            checkoutTimer.record(Duration.ofMillis(elapsedMillis));
            orderStats.recordOrder(elapsedMillis);
            log.info("checkout accepted orderId={} customerId={} sku={} amountCents={}",
                command.getOrderId(), command.getCustomerId(), command.getSku(), command.getAmountCents());
            return new CheckoutResponse(command.getOrderId(), "ACCEPTED", "Order accepted and payment queued");
        } catch (RuntimeException ex) {
            checkoutFailures.increment();
            orderStats.recordFailure();
            log.warn("checkout failed customerId={} sku={} reason={}",
                request == null ? null : request.getCustomerId(),
                request == null ? null : request.getSku(),
                ex.getMessage());
            throw ex;
        }
    }

    public List<String> orders() {
        synchronized (orders) {
            return new ArrayList<>(orders);
        }
    }
}
