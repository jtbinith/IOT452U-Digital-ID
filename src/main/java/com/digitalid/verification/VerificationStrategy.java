package com.digitalid.verification;

import com.digitalid.domain.DigitalID;

// The interface. Every strategy must implement one method: verify()

public interface VerificationStrategy {

    VerificationResult verify(DigitalID identity);
}