package com.example.observability.order.web;

import com.example.observability.order.model.CheckoutRequest;
import com.example.observability.order.model.CheckoutResponse;
import com.example.observability.order.model.UxEvent;
import com.example.observability.order.service.OrderService;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class OrderController {
    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/catalog")
    public List<Map<String, Object>> catalog() {
        return Arrays.asList(
            Map.of("sku", "coffee-beans", "name", "Coffee Beans", "priceCents", 1299),
            Map.of("sku", "travel-mug", "name", "Travel Mug", "priceCents", 1899),
            Map.of("sku", "espresso-machine", "name", "Espresso Machine", "priceCents", 19999)
        );
    }

    @PostMapping("/checkout")
    public CheckoutResponse checkout(@RequestBody CheckoutRequest request) {
        return orderService.checkout(request);
    }

    @GetMapping("/orders")
    public List<String> orders() {
        return orderService.orders();
    }

    @PostMapping("/ux-events")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void uxEvent(@RequestBody UxEvent event) {
        log.info("ux event name={} at={} data={}", event.getName(), event.getAt(), event.getData());
    }

    @GetMapping("/simulate/slow")
    public Map<String, Object> slow(@RequestParam(defaultValue = "2500") long delayMs) throws InterruptedException {
        Thread.sleep(delayMs);
        log.info("simulated slow request delayMs={}", delayMs);
        return Map.of("status", "slow", "delayMs", delayMs);
    }

    @GetMapping("/simulate/error")
    public Map<String, Object> error() {
        log.error("simulated checkout API failure");
        throw new IllegalStateException("simulated checkout API failure");
    }
}
