package com.mbugajski.logistics.assignment.entity;

import com.mbugajski.logistics.courier.entity.Courier;
import com.mbugajski.logistics.shipment.entity.Shipment;
import com.mbugajski.logistics.vehicle.entity.Vehicle;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "assignments")
@Getter
public class ShipmentAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @ManyToOne
    @JoinColumn(name = "courier_id", nullable = false)
    private Courier courier;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    protected ShipmentAssignment() {
    }

    public ShipmentAssignment(Shipment shipment, Courier courier, Vehicle vehicle) {
        if (shipment == null) {
            throw new IllegalArgumentException("Shipment cannot be null.");
        }
        this.shipment = shipment;

        if (courier == null) {
            throw new IllegalArgumentException("Courier cannot be null.");
        }
        this.courier = courier;

        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle cannot be null.");
        }
        this.vehicle = vehicle;
    }
}
