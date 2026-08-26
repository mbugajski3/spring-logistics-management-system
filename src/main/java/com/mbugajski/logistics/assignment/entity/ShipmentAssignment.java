package com.mbugajski.logistics.assignment.entity;

import com.mbugajski.logistics.assignment.exception.AssignmentInvalidStateException;
import com.mbugajski.logistics.courier.entity.Courier;
import com.mbugajski.logistics.shipment.entity.Shipment;
import com.mbugajski.logistics.vehicle.entity.Vehicle;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentStatus status;

    @Column(nullable = false)
    private LocalDateTime assignedAt;

    private LocalDateTime finishedAt;
    
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

        this.status = AssignmentStatus.ACTIVE;

        this.assignedAt = LocalDateTime.now();

        this.finishedAt = null;
    }

    public void complete() {
        if (status != AssignmentStatus.ACTIVE) {
            throw new AssignmentInvalidStateException("Status must be active to complete.");
        }

        status = AssignmentStatus.COMPLETED;
        finishedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (status != AssignmentStatus.ACTIVE) {
            throw new AssignmentInvalidStateException("Status must be active to cancel.");
        }

        status = AssignmentStatus.CANCELLED;
        finishedAt = LocalDateTime.now();
    }
}
