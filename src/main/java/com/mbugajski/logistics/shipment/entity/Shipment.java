package com.mbugajski.logistics.shipment.entity;

import com.mbugajski.logistics.address.entity.Address;
import com.mbugajski.logistics.customer.entity.Customer;
import com.mbugajski.logistics.shipment.entity.ShipmentStatus;
import com.mbugajski.logistics.shipment.exception.*;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "shipments")
@Getter
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pickup_address_id", nullable = false)
    private Address pickupAddress;

    @ManyToOne(optional = false)
    @JoinColumn(name = "delivery_address_id", nullable = false)
    private Address deliveryAddress;

    @Column(nullable = false,precision = 10, scale = 2)
    private BigDecimal weight;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
     ShipmentStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false,precision = 10, scale = 2)
    private BigDecimal price;

    protected Shipment() {
    }

    public Shipment(Customer customer, Address pickupAddress, Address deliveryAddress, BigDecimal weight) {
        if (customer == null) {
            throw new ShipmentNullCustomerException();
        }

        if (pickupAddress == null) {
            throw new ShipmentNullAddressException("Pickup address cannot be null.");
        }

        if (deliveryAddress == null) {
            throw new ShipmentNullAddressException("Delivery address cannot be null.");
        }

        if (weight == null) {
            throw new ShipmentNullWeightException();
        }

        if (weight.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ShipmentInvalidWeightException();
        }

        if (weight.compareTo(new BigDecimal("20")) > 0) {
            throw new ShipmentOverweightException(weight);
        }

        this.id = id;
        this.customer = customer;
        this.pickupAddress = pickupAddress;
        this.deliveryAddress = deliveryAddress;
        this.weight = weight;
        this.status = ShipmentStatus.CREATED;
        this.createdAt = LocalDateTime.now();
        this.price = calculatePrice();
    }

    private BigDecimal calculatePrice() {
        if (weight.compareTo(new BigDecimal("1")) <= 0) {
            price = new BigDecimal("12.00");
        } else if (weight.compareTo(new BigDecimal("5")) <= 0) {
            price = new BigDecimal("17.00");
        } else if (weight.compareTo(new BigDecimal("10")) <= 0) {
            price = new BigDecimal("25.00");
        } else {
            price = new BigDecimal("35.00");
        }

        return price;
    }

    public void markAsReadyForPickup() {
        if (ShipmentStatus.CREATED != this.status) {
            throw new ShipmentInvalidStatusException("Only a shipment with status 'CREATED' can be marked as 'READY_FOR_PICKUP'.");
        }
        this.status = ShipmentStatus.READY_FOR_PICKUP;
    }

    public void markAsInTransit() {
        if (ShipmentStatus.READY_FOR_PICKUP != this.status) {
            throw new ShipmentInvalidStatusException("Only a shipment with status 'READY_FOR_PICKUP' can be marked as 'IN_TRANSIT'.");
        }
//        if (assignedCourier == null || assignedVehicle == null) {
//            throw new IllegalStateException("Shipment must have assigned courier and vehicle to change status to 'IN_TRANSIT'");
//        }
        this.status = ShipmentStatus.IN_TRANSIT;
    }

    public void markAsDelivered() {
        if (ShipmentStatus.IN_TRANSIT != this.status) {
            throw new ShipmentInvalidStatusException("Only a shipment with status 'IN_TRANSIT' can be marked as 'DELIVERED'.");
        }
        this.status = ShipmentStatus.DELIVERED;
    }

    public void markAsCancelled() {
        if (ShipmentStatus.CREATED != this.status && ShipmentStatus.READY_FOR_PICKUP != this.status) {
            throw new ShipmentInvalidStatusException("Only a shipment with status 'CREATED' or 'READY_FOR_PICKUP' can be cancelled.");
        }
        this.status = ShipmentStatus.CANCELLED;
    }
}
