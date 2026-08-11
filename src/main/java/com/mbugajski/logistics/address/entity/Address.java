package com.mbugajski.logistics.address.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "addresses")
@Getter
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String street;

    @Column(nullable = false)
    private String buildingNumber;
    private String apartmentNumber;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String postalCode;

    @Column(nullable = false)
    private String country;

    protected Address() {
    }

    public Address(String street, String buildingNumber, String apartmentNumber, String city, String postalCode, String country) {
        if (street == null || street.isBlank()) {
            throw new IllegalArgumentException("Street cannot be empty.");
        }
        this.street = street.trim();

        if (buildingNumber == null
                || buildingNumber.isBlank()
                || !buildingNumber.matches("\\d+[A-Za-z0-9/-]*")) {
            throw new IllegalArgumentException("Invalid building number.");
        }
        this.buildingNumber = buildingNumber.trim();

        if (apartmentNumber != null
                && (apartmentNumber.isBlank()
                || !apartmentNumber.matches("\\d+[A-Za-z0-9/-]*"))) {
            throw new IllegalArgumentException("Invalid apartment number.");
        }
        this.apartmentNumber = apartmentNumber == null
                ? null
                : apartmentNumber.trim();

        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("City cannot be empty.");
        }
        this.city = city.trim();

        if (postalCode == null || postalCode.isBlank()) {
            throw new IllegalArgumentException("Postal code cannot be empty.");
        }
        this.postalCode = postalCode.trim();

        if (country == null || country.isBlank()) {
            throw new IllegalArgumentException("Country cannot be empty.");
        }
        this.country = country.trim();

    }

    @Override
    public String toString() {
        String apartmentPart = apartmentNumber == null
                ? ""
                : " m." + apartmentNumber;

        return "ul. " +
                street +
                " " +
                buildingNumber +
                apartmentPart +
                "\n" +
                postalCode +
                " " +
                city +
                "\n" +
                country;
    }
}
