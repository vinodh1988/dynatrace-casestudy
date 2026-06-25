package com.example.observability.order.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.cloud.sleuth.annotation.SpanTag;
import org.springframework.stereotype.Service;

@Service
public class OrderPricingService {
    private static final Logger log = LoggerFactory.getLogger(OrderPricingService.class);

    @NewSpan("order.price-checkout")
    public int amountFor(@SpanTag("checkout.sku") String sku, @SpanTag("checkout.quantity") int quantity) {
        int unitPrice = priceFor(sku);
        int amount = unitPrice * quantity;
        log.info("checkout priced sku={} quantity={} unitPriceCents={} amountCents={}",
            sku, quantity, unitPrice, amount);
        return amount;
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
