package com.digitalid.verification;

import com.digitalid.domain.DigitalID;

public class BankVerificationStrategy implements VerificationStrategy {

    @Override
    public VerificationResult verify(DigitalID identity) {
        if (identity.isActive()) {
            return new VerificationResult(true, "Identity is active");
        }
        return new VerificationResult(false, "Identity is not active");
    }
}

