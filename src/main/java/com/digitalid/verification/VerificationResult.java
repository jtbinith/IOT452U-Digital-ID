package com.digitalid.verification;

// The object returned by every verification. Carries both the result and a human-readable reason.

public class VerificationResult {

    private final boolean valid;
    private final String reason;

    public VerificationResult(boolean valid, String reason) {
        this.valid = valid;
        this.reason = reason;
    }

    public boolean isValid() { return valid; }
    public String getReason() { return reason; }
}