package com.digitalid.verification;

import java.time.LocalDate;

import com.digitalid.domain.DigitalID;

public class TaxAuthorityVerificationStrategy implements VerificationStrategy {

    @Override
    public VerificationResult verify(DigitalID identity) {
        if (identity.isSuspended()) {
            return new VerificationResult(false, "Identity is currently suspended");
        }
        if (!identity.isActive()) {
            return new VerificationResult(false, "Identity is not active");
        }
        return new VerificationResult(true, "Identity is active and has no suspension history");
    }

    public VerificationResult verify(DigitalID identity, LocalDate from, LocalDate to) {
        VerificationResult basicResult = verify(identity);
        if (!basicResult.isValid()) {
            return basicResult;
        }
        if (identity.wasSuspendedBetween(from, to)) {
            return new VerificationResult(false, "Identity was suspended during the reporting period");
        }
        return new VerificationResult(true, "Identity is active with no suspensions in the reporting period");
    }
}