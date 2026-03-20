package com.digitalid.domain;

import java.time.LocalDate;

import com.digitalid.exception.InvalidStatusTransitionException;

public class DigitalID {
    private final String id;
    private final String firstName;
    private final String surname;
    private final String gender;
    private final LocalDate dateOfBirth;
    private String nationality;
    private String address;
    private String postcode;
    private IdentityStatus status;
    private boolean restricted;
    private final LocalDate createdDate;

    public DigitalID(String id, String firstName, String surname, String gender, LocalDate dateOfBirth,
                     String nationality, String address, String postcode) {
        this.id = id;
        this.firstName = firstName;
        this.surname = surname;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.nationality = nationality;
        this.address = address;
        this.postcode = postcode;
        this.status = IdentityStatus.ACTIVE;
        this.restricted = false;
        this.createdDate = LocalDate.now();
    }

    // lifecycle methods

    public void activate() {
        if (this.status == IdentityStatus.REVOKED) {
            throw new InvalidStatusTransitionException(this.id, this.status, IdentityStatus.ACTIVE);
        }

        this.status = IdentityStatus.ACTIVE;
    }

    public void suspend() {
        if (this.status == IdentityStatus.REVOKED) {
            throw new InvalidStatusTransitionException(this.id, this.status, IdentityStatus.SUSPENDED);
        }
        this.status = IdentityStatus.SUSPENDED;
    }

    public void revoke() {
        this.status = IdentityStatus.REVOKED;
    }

    // update methods
    public void updateNationality(String nationality) {
        if (this.status == IdentityStatus.REVOKED) {
            throw new InvalidStatusTransitionException(this.id, this.status, this.status);
        }
        this.nationality = nationality;
    }

    public void updateAddress(String address) {
        if (this.status == IdentityStatus.REVOKED) {
            throw new InvalidStatusTransitionException(this.id, this.status, this.status);
        }
        this.address = address;
    }

    public void updatePostcode(String postcode) {
        if (this.status == IdentityStatus.REVOKED) {
            throw new InvalidStatusTransitionException(this.id, this.status, this.status);
        }
        this.postcode = postcode;
    }

    public void setRestriction(boolean restricted) {
        if (this.status == IdentityStatus.REVOKED) {
            throw new InvalidStatusTransitionException(this.id, this.status, this.status);
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
    public String getFirstName() { return firstName; }
    public String getSurname() { return surname; }
    public String getFullName() { return firstName + " " + surname; }
    public String getGender() { return gender; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getNationality() { return nationality; }
    public String getAddress() { return address; }
    public String getPostcode() { return postcode; }
    public IdentityStatus getStatus() { return status; }
    public LocalDate getCreatedDate() { return createdDate; }
}
