package com.mbugajski.logistics.assignment.repository;

import com.mbugajski.logistics.assignment.entity.ShipmentAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShipmentAssignmentRepository extends JpaRepository<ShipmentAssignment, Long> {
    Optional<ShipmentAssignment> findByShipmentId(Long shipmentId);
}
