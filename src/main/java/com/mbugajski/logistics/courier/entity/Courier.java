package com.mbugajski.logistics.courier.entity;

import com.mbugajski.logistics.courier.exception.CourierInvalidStateException;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "couriers")
@Getter
public class Courier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Column(nullable = false)
    private boolean available;

    @Column(nullable = false)
    private boolean active;

    protected Courier() {
    }

    public Courier(String firstName, String lastName, String phoneNumber) {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name cannot be empty.");
        }
        this.firstName = firstName.trim();

        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name cannot be empty.");
        }
        this.lastName = lastName.trim();

        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be empty.");
        }
        this.phoneNumber = phoneNumber.trim();

        this.available = true;
        this.active = true;
    }

    public void deactivate() {
        if (!this.active || !this.available) {
            throw new CourierInvalidStateException("Courier must be active and not busy to deactivate.");
        }
        this.active = false;
        this.available = false;

    }

    public void activate() {
        if (this.active) {
            throw new CourierInvalidStateException("Only inactive courier can be activated.");
        }
        this.active = true;
        this.available = true;
    }

    public void markAsBusy() {
        if (!this.active || !this.available) {
            throw new CourierInvalidStateException("Courier must be active and available to mark as busy.");
        }
        this.available = false;
    }

    public void markAsAvailable() {
        if (!this.active || this.available) {
            throw new CourierInvalidStateException("Courier must be active and busy to mark as available.");
        }
        this.available = true;
    }
}
