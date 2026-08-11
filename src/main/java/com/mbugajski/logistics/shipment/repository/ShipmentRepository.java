package com.mbugajski.logistics.shipment.repository;

import com.mbugajski.logistics.shipment.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
}
