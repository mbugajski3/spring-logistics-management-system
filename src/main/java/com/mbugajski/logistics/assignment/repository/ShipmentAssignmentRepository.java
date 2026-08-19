package com.mbugajski.logistics.assignment.repository;

import com.mbugajski.logistics.assignment.entity.ShipmentAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentAssignmentRepository extends JpaRepository<ShipmentAssignment, Long> {
}
