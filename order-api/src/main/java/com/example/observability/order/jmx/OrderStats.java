package com.example.observability.order.jmx;

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

@Component
public class OrderStats implements OrderStatsMBean {
    private final AtomicLong totalOrders = new AtomicLong();
    private final AtomicLong failedOrders = new AtomicLong();
    private final AtomicLong slowOrders = new AtomicLong();
    private final AtomicLong totalCheckoutMillis = new AtomicLong();

    public void recordOrder(long millis) {
        totalOrders.incrementAndGet();
        totalCheckoutMillis.addAndGet(millis);
        if (millis > 1000) {
            slowOrders.incrementAndGet();
        }
    }

    public void recordFailure() {
        failedOrders.incrementAndGet();
    }

    @Override
    public long getTotalOrders() {
        return totalOrders.get();
    }

    @Override
    public long getFailedOrders() {
        return failedOrders.get();
    }

    @Override
    public long getSlowOrders() {
        return slowOrders.get();
    }

    @Override
    public double getAverageCheckoutMillis() {
        long count = totalOrders.get();
        return count == 0 ? 0 : (double) totalCheckoutMillis.get() / count;
    }
}
