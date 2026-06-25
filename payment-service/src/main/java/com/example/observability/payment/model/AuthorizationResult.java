package com.example.observability.payment.model;

public class AuthorizationResult {
    private final boolean approved;
    private final String reason;
    private final long elapsedMillis;

    public AuthorizationResult(boolean approved, String reason, long elapsedMillis) {
        this.approved = approved;
        this.reason = reason;
        this.elapsedMillis = elapsedMillis;
    }

    public boolean isApproved() {
        return approved;
    }

    public String getReason() {
        return reason;
    }

    public long getElapsedMillis() {
        return elapsedMillis;
    }
}
