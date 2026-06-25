package com.example.observability.order.service;

import com.example.observability.order.model.CheckoutRequest;
import com.example.observability.order.model.PaymentCommand;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.cloud.sleuth.annotation.SpanTag;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CheckoutPreparationService {
    private static final Logger log = LoggerFactory.getLogger(CheckoutPreparationService.class);

    private final OrderPricingService orderPricingService;

    public CheckoutPreparationService(OrderPricingService orderPricingService) {
        this.orderPricingService = orderPricingService;
    }

    @NewSpan("order.prepare-checkout")
    public PaymentCommand prepare(@SpanTag("checkout.customer") CheckoutRequest request) {
        validate(request);
        String orderId = "ord-" + UUID.randomUUID().toString().substring(0, 8);
        int amount = orderPricingService.amountFor(request.getSku(), request.getQuantity());
        log.info("checkout prepared orderId={} customerId={} sku={} quantity={} amountCents={}",
            orderId, request.getCustomerId(), request.getSku(), request.getQuantity(), amount);
        return new PaymentCommand(
            orderId,
            request.getCustomerId(),
            request.getSku(),
            request.getQuantity(),
            amount
        );
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
}
