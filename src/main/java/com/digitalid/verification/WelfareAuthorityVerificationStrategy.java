package com.digitalid.verification;

import com.digitalid.domain.DigitalID;

public class WelfareAuthorityVerificationStrategy implements VerificationStrategy {

    private static final int MAX_SUSPENSIONS = 2;

    @Override
    public VerificationResult verify(DigitalID identity) {
        if (!identity.isActive()) {
            return new VerificationResult(false, "Identity is not active");
        }
        if (identity.getSuspensionCount() > MAX_SUSPENSIONS) {
            return new VerificationResult(false, "Identity has exceeded the maximum number of suspensions");
        }
        return new VerificationResult(true, "Identity is active with an acceptable suspension history");
    }
}
