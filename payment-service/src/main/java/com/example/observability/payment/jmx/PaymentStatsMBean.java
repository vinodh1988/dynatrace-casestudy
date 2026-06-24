package com.example.observability.payment.jmx;

public interface PaymentStatsMBean {
    long getProcessedPayments();

    long getDeclinedPayments();

    long getSlowPayments();

    double getAveragePaymentMillis();
}
