package com.example.observability.payment.jmx;

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

@Component
public class PaymentStats implements PaymentStatsMBean {
    private final AtomicLong processedPayments = new AtomicLong();
    private final AtomicLong declinedPayments = new AtomicLong();
    private final AtomicLong slowPayments = new AtomicLong();
    private final AtomicLong totalPaymentMillis = new AtomicLong();

    public void recordProcessed(long millis) {
        processedPayments.incrementAndGet();
        totalPaymentMillis.addAndGet(millis);
        if (millis > 1200) {
            slowPayments.incrementAndGet();
        }
    }

    public void recordDeclined() {
        declinedPayments.incrementAndGet();
    }

    @Override
    public long getProcessedPayments() {
        return processedPayments.get();
    }

    @Override
    public long getDeclinedPayments() {
        return declinedPayments.get();
    }

    @Override
    public long getSlowPayments() {
        return slowPayments.get();
    }

    @Override
    public double getAveragePaymentMillis() {
        long count = processedPayments.get();
        return count == 0 ? 0 : (double) totalPaymentMillis.get() / count;
    }
}
