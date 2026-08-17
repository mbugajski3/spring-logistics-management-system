package com.mbugajski.logistics.vehicle.entity;

import com.mbugajski.logistics.vehicle.exception.VehicleInvalidStateException;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;

@Entity
@Table(name = "vehicles")
@Getter
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false, unique = true)
    private String registrationNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleType vehicleType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal maximumLoad;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private boolean available;

    protected Vehicle() {
    }

    public Vehicle(String brand, String model, String registrationNumber, VehicleType vehicleType, BigDecimal maximumLoad) {
        if (brand == null || brand.isBlank()) {
            throw new IllegalArgumentException("Brand cannot be empty or null.");
        }
        this.brand = brand.trim();

        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("Model cannot be null or empty.");
        }
        this.model = model.trim();

        if (registrationNumber == null || registrationNumber.isBlank()) {
            throw new IllegalArgumentException("Registration number cannot be null or blank.");
        }
        this.registrationNumber = registrationNumber.trim();

        if (vehicleType == null) {
            throw new IllegalArgumentException("Vehicle type cannot be null.");
        }
        this.vehicleType = vehicleType;

        if (maximumLoad == null) {
            throw new IllegalArgumentException("Maximum load cannot be null.");
        }

        if (maximumLoad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Maximum load cannot be 0 or below.");
        }

        if (maximumLoad.compareTo(new BigDecimal("1600.00")) > 0) {
            throw new IllegalArgumentException("Maximum load cannot be over 1600 kg.");
        }
        this.maximumLoad = maximumLoad;

        this.active = true;
        this.available = true;
    }

    public void deactivate() {
        if (!this.active || !this.available) {
            throw new VehicleInvalidStateException("Only an active and available vehicle can be deactivated.");
        }
        this.active = false;
        this.available = false;
    }

    public void activate() {
        if (this.active) {
            throw new VehicleInvalidStateException("Only inactive vehicle can be activated.");
        }
        this.active = true;
        this.available = true;
    }

    public void markAsBusy() {
        if (!this.active || !this.available) {
            throw new VehicleInvalidStateException("Vehicle must be active and available to mark as busy.");
        }
        this.available = false;
    }

    public void markAsAvailable() {
        if (!this.active || this.available) {
            throw new VehicleInvalidStateException("Vehicle must be active and busy to mark as available.");
        }
        this.available = true;
    }

}
