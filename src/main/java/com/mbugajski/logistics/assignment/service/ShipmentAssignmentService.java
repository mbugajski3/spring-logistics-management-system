package com.mbugajski.logistics.assignment.service;

import com.mbugajski.logistics.assignment.entity.AssignmentStatus;
import com.mbugajski.logistics.assignment.entity.ShipmentAssignment;
import com.mbugajski.logistics.assignment.exception.ActiveAssignmentNotFoundException;
import com.mbugajski.logistics.assignment.exception.AssignmentParameterInvalidStatus;
import com.mbugajski.logistics.assignment.exception.AssignmentVehicleOutOfSpaceException;
import com.mbugajski.logistics.assignment.repository.ShipmentAssignmentRepository;
import com.mbugajski.logistics.courier.entity.Courier;
import com.mbugajski.logistics.courier.exception.CourierNotFoundException;
import com.mbugajski.logistics.courier.repository.CourierRepository;
import com.mbugajski.logistics.shipment.entity.Shipment;
import com.mbugajski.logistics.shipment.entity.ShipmentStatus;
import com.mbugajski.logistics.shipment.exception.ShipmentInvalidStatusException;
import com.mbugajski.logistics.shipment.exception.ShipmentNotFoundException;
import com.mbugajski.logistics.shipment.repository.ShipmentRepository;
import com.mbugajski.logistics.vehicle.entity.Vehicle;
import com.mbugajski.logistics.vehicle.exception.VehicleNotFoundException;
import com.mbugajski.logistics.vehicle.repository.VehicleRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class ShipmentAssignmentService {

    private final ShipmentAssignmentRepository assignmentRepository;
    private final ShipmentRepository shipmentRepository;
    private final CourierRepository courierRepository;
    private final VehicleRepository vehicleRepository;

    public ShipmentAssignmentService(ShipmentAssignmentRepository assignmentRepository, ShipmentRepository shipmentRepository, CourierRepository courierRepository, VehicleRepository vehicleRepository) {
        this.assignmentRepository = assignmentRepository;
        this.shipmentRepository = shipmentRepository;
        this.courierRepository = courierRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional
    public ShipmentAssignment assign(Long shipmentId, Long courierId, Long vehicleId) {
        if (shipmentId == null || shipmentId <= 0) {
            throw new IllegalArgumentException("Shipment ID cannot be null, zero or below.");
        }

        if (courierId == null || courierId <= 0) {
            throw new IllegalArgumentException("Courier ID cannot be null, zero or below.");
        }

        if (vehicleId == null || vehicleId <= 0) {
            throw new IllegalArgumentException("Vehicle ID cannot be null, zero or below.");
        }

        Shipment shipmentFound = shipmentRepository.findById(shipmentId).orElseThrow(() -> new ShipmentNotFoundException(shipmentId));
        Courier courierFound = courierRepository.findById(courierId).orElseThrow(() -> new CourierNotFoundException(courierId));
        Vehicle vehicleFound = vehicleRepository.findById(vehicleId).orElseThrow(() -> new VehicleNotFoundException(vehicleId));

        if (shipmentFound.getStatus() != ShipmentStatus.CREATED) {
            throw new AssignmentParameterInvalidStatus("Shipment status must be 'CREATED' to assign.");
        }

        BigDecimal shipmentWeight = shipmentFound.getWeight();
        BigDecimal vehicleMaximumLoad = vehicleFound.getMaximumLoad();

        if (vehicleMaximumLoad.compareTo(shipmentWeight) < 0) {
            throw new AssignmentVehicleOutOfSpaceException("Shipment weight is too big.");
        }

        courierFound.markAsBusy();
        vehicleFound.markAsBusy();
        shipmentFound.markAsReadyForPickup();

        ShipmentAssignment assignment = new ShipmentAssignment(shipmentFound, courierFound, vehicleFound);
        assignmentRepository.save(assignment);

        return assignment;
    }

    @Transactional
    public void releaseResourcesForShipment(Long shipmentId) {
        if (shipmentId == null || shipmentId <= 0) {
            throw new IllegalArgumentException("Shipment ID cannot be null, zero or below.");
        }

        Optional<ShipmentAssignment> foundAssignment = assignmentRepository.findByShipmentId(shipmentId);

        if (foundAssignment.isPresent()) {
            ShipmentAssignment shipmentAssignment = foundAssignment.get();

            shipmentAssignment.getCourier().markAsAvailable();
            shipmentAssignment.getVehicle().markAsAvailable();
        }
    }

    public ShipmentAssignment findActiveAssignment(Long shipmentId) {
        if (shipmentId == null || shipmentId <= 0) {
            throw new IllegalArgumentException("Shipment ID cannot be null, zero or below.");
        }

        return assignmentRepository
                .findByShipmentIdAndStatus(shipmentId, AssignmentStatus.ACTIVE)
                .orElseThrow(() -> new ActiveAssignmentNotFoundException(shipmentId));
    }

    @Transactional
    public void completeAssignmentForShipment(Long shipmentId) {
        ShipmentAssignment assignment = findActiveAssignment(shipmentId);
        assignment.complete();
        assignment.getCourier().markAsAvailable();
        assignment.getVehicle().markAsAvailable();
    }

    @Transactional
    public void cancelAssignmentForShipmentIfPresent(Long shipmentId) {
        Optional<ShipmentAssignment> foundAssignment = assignmentRepository.findByShipmentIdAndStatus(shipmentId, AssignmentStatus.ACTIVE);

        if (foundAssignment.isPresent()) {
            ShipmentAssignment assignment = foundAssignment.get();

            assignment.cancel();
            assignment.getCourier().markAsAvailable();
            assignment.getVehicle().markAsAvailable();
        }
    }

    @Transactional
    public ShipmentAssignment reassign(Long shipmentId) {
        Shipment shipmentFound = shipmentRepository.findById(shipmentId).orElseThrow(() -> new ShipmentNotFoundException(shipmentId));

        if (shipmentFound.getStatus() != ShipmentStatus.READY_FOR_PICKUP) {
            throw new ShipmentInvalidStatusException("Shipment status must be 'READY_FOR_PICKUP' to reassign.");
        }

        ShipmentAssignment activeAssignment = findActiveAssignment(shipmentId);

        Courier courierFound = courierRepository.findFirstByAvailableTrue().orElseThrow(CourierNotFoundException::new);
        Vehicle vehicleFound = vehicleRepository.findFirstByAvailableTrueAndMaximumLoadGreaterThanEqualOrderByMaximumLoadAsc(shipmentFound.getWeight()).orElseThrow(() -> new VehicleNotFoundException(shipmentFound.getWeight()));

        BigDecimal shipmentWeight = shipmentFound.getWeight();
        BigDecimal vehicleMaxLoad = vehicleFound.getMaximumLoad();

        if (vehicleMaxLoad.compareTo(shipmentWeight) < 0) {
            throw new AssignmentVehicleOutOfSpaceException("Shipment weight is too big.");
        }

        activeAssignment.getCourier().markAsAvailable();
        activeAssignment.getVehicle().markAsAvailable();

        courierFound.markAsBusy();
        vehicleFound.markAsBusy();

        ShipmentAssignment reassignment = new ShipmentAssignment(shipmentFound, courierFound, vehicleFound);
        activeAssignment.reassign();
        assignmentRepository.save(reassignment);

        return reassignment;
    }
}
