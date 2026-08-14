package com.mbugajski.logistics.shipment.mapper;

import com.mbugajski.logistics.shipment.dto.response.AddressResponse;
import com.mbugajski.logistics.shipment.dto.response.ShipmentResponse;
import com.mbugajski.logistics.shipment.entity.Shipment;
import com.mbugajski.logistics.shipment.entity.ShipmentStatus;


public class ShipmentMapper {

    public static ShipmentResponse toResponse(Shipment shipment) {
        return new ShipmentResponse(
                shipment.getId(),
                shipment.getCustomer().getId(),
                shipment.getCustomer().getFirstName(),
                shipment.getCustomer().getLastName(),

                new AddressResponse(
                        shipment.getPickupAddress().getStreet(),
                        shipment.getPickupAddress().getBuildingNumber(),
                        shipment.getPickupAddress().getApartmentNumber(),
                        shipment.getPickupAddress().getCity(),
                        shipment.getPickupAddress().getPostalCode(),
                        shipment.getPickupAddress().getCountry()
                ),

                new AddressResponse(
                        shipment.getDeliveryAddress().getStreet(),
                        shipment.getDeliveryAddress().getBuildingNumber(),
                        shipment.getDeliveryAddress().getApartmentNumber(),
                        shipment.getDeliveryAddress().getCity(),
                        shipment.getDeliveryAddress().getPostalCode(),
                        shipment.getDeliveryAddress().getCountry()
                ),

                shipment.getWeight(),
                shipment.getPrice(),
                shipment.getStatus(),
                shipment.getCreatedAt()
        );
    }
}
