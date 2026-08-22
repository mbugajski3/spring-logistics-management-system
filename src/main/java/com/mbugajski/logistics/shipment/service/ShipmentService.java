package com.mbugajski.logistics.shipment.service;

import com.mbugajski.logistics.address.dto.request.CreateAddressRequest;
import com.mbugajski.logistics.address.entity.Address;
import com.mbugajski.logistics.address.repository.AddressRepository;
import com.mbugajski.logistics.assignment.service.ShipmentAssignmentService;
import com.mbugajski.logistics.customer.entity.Customer;
import com.mbugajski.logistics.customer.exception.CustomerNotFoundException;
import com.mbugajski.logistics.customer.repository.CustomerRepository;
import com.mbugajski.logistics.shipment.dto.request.CreateShipmentRequest;
import com.mbugajski.logistics.shipment.entity.Shipment;
import com.mbugajski.logistics.shipment.exception.ShipmentInvalidStatusException;
import com.mbugajski.logistics.shipment.exception.ShipmentNotFoundException;
import com.mbugajski.logistics.shipment.repository.ShipmentRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final ShipmentAssignmentService shipmentAssignmentService;

    public ShipmentService(ShipmentRepository shipmentRepository, CustomerRepository customerRepository, AddressRepository addressRepository, ShipmentAssignmentService shipmentAssignmentService) {
        this.shipmentRepository = shipmentRepository;
        this.customerRepository = customerRepository;
        this.addressRepository = addressRepository;
        this.shipmentAssignmentService = shipmentAssignmentService;
    }

    @Transactional
    public Shipment create(CreateShipmentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("CreateShipmentRequest cannot be null.");
        }

        Long customerIdFromRequest = request.getCustomerId();

        Customer customerFound = customerRepository
                .findById(customerIdFromRequest)
                .orElseThrow(() -> new CustomerNotFoundException(customerIdFromRequest));

        CreateAddressRequest requestPickupAddress = request.getPickupAddress();
        CreateAddressRequest requestDeliveryAddress = request.getDeliveryAddress();

        Address createdPickupAddress = mapToAddress(requestPickupAddress);

        Address createdDeliveryAddress = mapToAddress(requestDeliveryAddress);

        BigDecimal shipmentWeight = request.getWeight();

        Address pickupAddress = addressRepository.save(createdPickupAddress);
        Address deliveryAddress = addressRepository.save(createdDeliveryAddress);

        Shipment createdShipment = new Shipment(customerFound, pickupAddress, deliveryAddress, shipmentWeight);

        return shipmentRepository.save(createdShipment);
    }

    private Address mapToAddress(CreateAddressRequest request) {
        return new Address(
                request.getStreet(),
                request.getBuildingNumber(),
                request.getApartmentNumber(),
                request.getCity(),
                request.getPostalCode(),
                request.getCountry()
        );
    }

    public Shipment findById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Shipment id must be greater than 0.");
        }

        return shipmentRepository
                .findById(id)
                .orElseThrow(() -> new ShipmentNotFoundException(id));
    }

    public Page<Shipment> findAllByPageNumber(int pageNumber, int pageSize) {
        if (pageNumber < 0) {
            throw new IllegalArgumentException("Page number cannot be negative.");
        }

        if (pageSize <= 0) {
            throw new IllegalArgumentException("Page size must be greater than 0.");
        }

        if (pageSize > 100) {
            throw new IllegalArgumentException("Page size cannot be more than 100.");
        }

        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        return shipmentRepository.findAll(pageable);
    }

    @Transactional
    public Shipment markAsReadyForPickup(Long id) {
        Shipment shipmentFound = findById(id);

        shipmentFound.markAsReadyForPickup();

        return shipmentFound;
    }

    @Transactional
    public Shipment markAsInTransit(Long id) {
        Shipment shipmentFound = findById(id);

        shipmentFound.markAsInTransit();

        return shipmentFound;
    }

    @Transactional
    public Shipment markAsDelivered(Long id) {
        Shipment shipmentFound = findById(id);

        shipmentFound.markAsDelivered();

        shipmentAssignmentService.releaseResourcesForShipment(id);

        return shipmentFound;
    }

    @Transactional
    public Shipment markAsCancelled(Long id) {
        Shipment shipmentFound = findById(id);

        shipmentFound.markAsCancelled();

        shipmentAssignmentService.releaseResourcesForShipment(id);

        return shipmentFound;
    }
}
