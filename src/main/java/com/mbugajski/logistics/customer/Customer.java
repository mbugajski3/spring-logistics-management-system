package com.mbugajski.logistics.customer;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class Customer {

    private final long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private Address address;
    private BigDecimal debt;
    private boolean active;

    public Customer(long id, String firstName, String lastName, String email, String phoneNumber, Address address) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID must be greater than 0.");
        }
        this.id = id;

        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name cannot be empty.");
        }
        this.firstName = firstName.trim();

        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name cannot be empty.");
        }
        this.lastName = lastName.trim();

        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new IllegalArgumentException("Email cannot be empty, and must contain '@'.");
        }
        this.email = email.trim();

        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be empty.");
        }
        this.phoneNumber = phoneNumber.trim();

        if (address == null) {
            throw new IllegalArgumentException("Address cannot be null.");
        }
        this.address = address;

        this.debt = BigDecimal.ZERO;
        this.active = true;
    }

    public void addDebt(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null.");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0.");
        }
        debt = debt.add(amount);
    }

    public void payDebt(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null.");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0.");
        }

        if (debt.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalStateException("Customer has no debt.");
        }

        if (amount.compareTo(debt) > 0) {
            throw new IllegalArgumentException("Amount cannot be greater than debt.");
        }

        debt = debt.subtract(amount);
    }

    public void activate() {
        active = true;
    }

    public void deactivate() {
        active = false;
    }

    public void changeFirstName(String firstName) {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name cannot be blank.");
        }

        this.firstName = firstName.trim();
    }

    public void changeLastName(String lastName) {
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name cannot be blank.");
        }

        this.lastName = lastName.trim();
    }

    public void changeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be blank.");
        }

        this.email = email.trim().toLowerCase();
    }

    public void changePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be blank.");
        }

        this.phoneNumber = phoneNumber.trim();
    }

    public void changeAddress(Address address) {
        if (address == null) {
            throw new IllegalArgumentException("Address cannot be null.");
        }

        this.address = address;
    }

    @Override
    public String toString() {
        return "Customer: " +
                firstName +
                " " +
                lastName +
                ", email: " +
                email +
                ", phone number: " +
                phoneNumber +
                ", address: " +
                address +
                ", debt: " +
                debt +
                ", active: " +
                active;
    }
}
