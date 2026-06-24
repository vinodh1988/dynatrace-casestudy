package com.example.observability.order.model;

public class CheckoutResponse {
    private final String orderId;
    private final String status;
    private final String message;

    public CheckoutResponse(String orderId, String status, String message) {
        this.orderId = orderId;
        this.status = status;
        this.message = message;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
