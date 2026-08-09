package com.mbugajski.logistics.customer.entity;

import com.mbugajski.logistics.customer.exception.CustomerAlreadyActiveException;
import com.mbugajski.logistics.customer.exception.CustomerAlreadyInactiveException;
import com.mbugajski.logistics.customer.exception.CustomerHasOutstandingDebtException;
import jakarta.persistence.*;
import lombok.Getter;
import java.math.BigDecimal;

@Entity
@Table(name = "customers")
@Getter
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String phoneNumber;

    @ManyToOne
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @Column(nullable = false,precision = 10, scale = 2)
    private BigDecimal debt = new BigDecimal("0.00");

    @Column(nullable = false)
    private boolean active = true;

    protected Customer() {
    }

    public Customer(String firstName, String lastName, String email, String phoneNumber, Address address) {
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

        this.debt = new BigDecimal("0.00");
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
        if (!active) {
            this.active = true;
        } else {
            throw new CustomerAlreadyActiveException();
        }
    }

    public void deactivate() {
        if (!active) {
            throw new CustomerAlreadyInactiveException();
        }

        if (debt.compareTo(BigDecimal.ZERO) > 0) {
            throw new CustomerHasOutstandingDebtException();
        }

        this.active = false;
    }

    public void changeFirstName(final String firstName) {
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
