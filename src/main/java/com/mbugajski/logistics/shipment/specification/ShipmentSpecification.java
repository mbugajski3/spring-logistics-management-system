package com.mbugajski.logistics.shipment.specification;

import com.mbugajski.logistics.shipment.entity.Shipment;
import com.mbugajski.logistics.shipment.entity.ShipmentStatus;
import org.springframework.data.jpa.domain.PredicateSpecification;

public class ShipmentSpecification {

    public static PredicateSpecification<Shipment> findByStatus(ShipmentStatus status) {
        return (from, builder) -> {
            return builder.equal(from.get("status"), status);
        };
    }

    public static PredicateSpecification<Shipment> findByCustomerId(Long customerId) {
        return (from, builder) -> {
            return builder.equal(from.get("customer").get("id"), customerId);
        };
    }
}
