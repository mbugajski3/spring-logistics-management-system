package com.mbugajski.logistics.shipment.mapper;

import com.mbugajski.logistics.shipment.dto.response.AddressResponse;
import com.mbugajski.logistics.shipment.dto.response.ShipmentPaginationResponse;
import com.mbugajski.logistics.shipment.dto.response.ShipmentResponse;
import com.mbugajski.logistics.shipment.entity.Shipment;
import com.mbugajski.logistics.shipment.entity.ShipmentStatus;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;


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

    public static ShipmentPaginationResponse paginationResponse(Page<Shipment> shipmentPage) {
        List<Shipment> list = shipmentPage.getContent();
        List<ShipmentResponse> responseList = new ArrayList<>();

        for (Shipment shipment : list) {
            responseList.add(ShipmentMapper.toResponse(shipment));

        }


        return new ShipmentPaginationResponse(
                responseList,
                shipmentPage.getPageable().getPageNumber(),
                shipmentPage.getPageable().getPageSize(),
                shipmentPage.getTotalElements(),
                shipmentPage.getTotalPages(),
                shipmentPage.hasNext(),
                shipmentPage.hasPrevious()
                );
    }
}
