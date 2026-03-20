package com.digitalid.verification;

import com.digitalid.domain.DigitalID;

public class TaxAuthorityVerificationStrategy implements VerificationStrategy {

    @Override
    public VerificationResult verify(DigitalID identity) {
        if (!identity.isActive()) {
            return new VerificationResult(false, "Identity is not active");
        }
        if (identity.isSuspended()) {
            return new VerificationResult(false, "Identity is currently suspended");
        }
        return new VerificationResult(true, "Identity is active and has no suspension history");
    }
}