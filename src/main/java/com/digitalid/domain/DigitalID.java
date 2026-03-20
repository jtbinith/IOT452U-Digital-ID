package com.digitalid.domain;

import java.time.LocalDate;

public class DigitalID {
    private final String id;
    private final String name;
    private final String gender;
    private final LocalDate dateOfBirth;
    private String nationality;
    private String address;
    private IdentityStatus status;
    private boolean restricted;
    private final LocalDate createdDate;

    public DigitalID(String id, String name, String gender, LocalDate dateOfBirth, String nationality) {
        this.id = id;
        this.name = name;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.nationality = nationality;
        this.status = IdentityStatus.ACTIVE;
        this.restricted = false;
        this.createdDate = LocalDate.now();
    }

    // lifecycle methods

    public void activate() {
        if (this.status == IdentityStatus.REVOKED) {
            throw new IllegalStateException("Cannot activate a revoked identity");
        }

        this.status = IdentityStatus.ACTIVE;
    }

    public void suspend() {
        if (this.status == IdentityStatus.REVOKED) {
            throw new IllegalStateException("Cannot suspend a revoked identity");
        }
        this.status = IdentityStatus.SUSPENDED;
    }

    public void revoke() {
        this.status = IdentityStatus.REVOKED;
    }

    // update methods
    public void updateNationality(String nationality) {
        if (this.status == IdentityStatus.REVOKED) {
            throw new IllegalStateException("Cannot update a revoked identity");
        }
        this.nationality = nationality;
    }

    public void updateAddress(String address) {
        if (this.status == IdentityStatus.REVOKED) {
            throw new IllegalStateException("Cannot update a revoked identity");
        }
        this.address = address;
    }

    public void setRestriction(boolean restricted) {
        if (this.status == IdentityStatus.REVOKED) {
            throw new IllegalStateException("Cannot modify a revoked identity");
        }
        this.restricted = restricted;
    }

    // query methods
    public boolean isActive() { return this.status == IdentityStatus.ACTIVE; }
    public boolean isSuspended() { return this.status == IdentityStatus.SUSPENDED; }
    public boolean isRevoked() { return this.status == IdentityStatus.REVOKED; }
    public boolean isRestricted() { return this.restricted; }

    // getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getGender() { return gender; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getNationality() { return nationality; }
    public String getAddress() { return address; }
    public IdentityStatus getStatus() { return status; }
    public LocalDate getCreatedDate() { return createdDate; }
}
