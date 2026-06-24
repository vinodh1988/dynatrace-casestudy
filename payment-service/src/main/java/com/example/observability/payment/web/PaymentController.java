package com.example.observability.payment.web;

import com.example.observability.payment.jmx.PaymentStats;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentStats stats;

    public PaymentController(PaymentStats stats) {
        this.stats = stats;
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        return Map.of(
            "processedPayments", stats.getProcessedPayments(),
            "declinedPayments", stats.getDeclinedPayments(),
            "slowPayments", stats.getSlowPayments(),
            "averagePaymentMillis", stats.getAveragePaymentMillis()
        );
    }
}
