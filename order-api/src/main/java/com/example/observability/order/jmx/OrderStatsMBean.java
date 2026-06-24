package com.example.observability.order.jmx;

public interface OrderStatsMBean {
    long getTotalOrders();

    long getFailedOrders();

    long getSlowOrders();

    double getAverageCheckoutMillis();
}
