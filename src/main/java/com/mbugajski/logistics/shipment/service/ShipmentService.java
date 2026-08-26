package com.mbugajski.logistics.shipment.service;
import com.mbugajski.logistics.address.dto.request.CreateAddressRequest;
import com.mbugajski.logistics.address.entity.Address;
import com.mbugajski.logistics.address.repository.AddressRepository;
import com.mbugajski.logistics.assignment.entity.ShipmentAssignment;
import com.mbugajski.logistics.assignment.service.ShipmentAssignmentService;
import com.mbugajski.logistics.customer.entity.Customer;
import com.mbugajski.logistics.customer.exception.CustomerNotFoundException;
import com.mbugajski.logistics.customer.repository.CustomerRepository;
import com.mbugajski.logistics.shipment.dto.request.CreateShipmentRequest;
import com.mbugajski.logistics.shipment.entity.Shipment;
import com.mbugajski.logistics.shipment.entity.ShipmentStatus;
import com.mbugajski.logistics.shipment.exception.ShipmentNotFoundException;
import com.mbugajski.logistics.shipment.repository.ShipmentRepository;
import com.mbugajski.logistics.shipment.repository.ShipmentSortBy;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.PredicateSpecification;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.Optional;

import static com.mbugajski.logistics.shipment.specification.ShipmentSpecification.findByCustomerId;
import static com.mbugajski.logistics.shipment.specification.ShipmentSpecification.findByStatus;

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

    public Page<Shipment> findAllByPageNumber(int pageNumber, int pageSize, ShipmentStatus status, Long customerId) {
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

        if (status == null && customerId == null) {
            return shipmentRepository.findAll(pageable);
        }

        if (customerId == null) {
            PredicateSpecification<Shipment> predicateSpecification = findByStatus(status);

            return shipmentRepository.findBy(predicateSpecification, query -> query.page(pageable));
        }

        if (status == null) {
            PredicateSpecification<Shipment> predicateSpecification = findByCustomerId(customerId);

            return shipmentRepository.findBy(predicateSpecification, query -> query.page(pageable));
        }

        PredicateSpecification<Shipment> predicateSpecification = findByCustomerId(customerId).and(findByStatus(status));

        return shipmentRepository.findBy(predicateSpecification, query -> query.page(pageable));
    }

    public Page<Shipment> findAllByPageNumber(int pageNumber, int pageSize, ShipmentStatus status, Long customerId, ShipmentSortBy sortParam, String direction) {
        if (pageNumber < 0) {
            throw new IllegalArgumentException("Page number cannot be negative.");
        }

        if (pageSize <= 0) {
            throw new IllegalArgumentException("Page size must be greater than 0.");
        }

        if (pageSize > 100) {
            throw new IllegalArgumentException("Page size cannot be more than 100.");
        }

        Pageable pageable;
        Sort sort = Sort.unsorted();
        PredicateSpecification<Shipment> predicateSpecification;

        if (sortParam != null && direction == null) {
            throw new IllegalArgumentException("If sortBy exists, then direction cannot be null.");
        }

        if (sortParam == null && direction != null) {
            throw new IllegalArgumentException("If sortBy dont exist, then direction is useless.");
        }

        if (direction != null && !"asc".equals(direction) && !"desc".equals(direction)) {
            throw new IllegalArgumentException("Direction can be only asc or desc.");
        }

        if (status == null && customerId == null) {
            predicateSpecification = null;

        } else if (customerId == null) {
            predicateSpecification = findByStatus(status);

        } else if (status == null) {
            predicateSpecification = findByCustomerId(customerId);

        } else {
            predicateSpecification = findByStatus(status).and(findByCustomerId(customerId));
        }

        if (sortParam == ShipmentSortBy.createdAt) {
            if (direction.equals("desc")) {
                sort = Sort.by("createdAt").descending();

            } else if (direction.equals("asc")) {
                sort = Sort.by("createdAt").ascending();
            }

        } else if (sortParam == ShipmentSortBy.price) {
            if (direction.equals("desc")) {
                sort = Sort.by("price").descending();

            } else if (direction.equals("asc")) {
                sort = Sort.by("price").ascending();
            }

        } else if (sortParam == ShipmentSortBy.weight) {
            if (direction.equals("desc")) {
                sort = Sort.by("weight").descending();

            } else if (direction.equals("asc")) {
                sort = Sort.by("weight").ascending();
            }
        }

        pageable = PageRequest.of(pageNumber, pageSize, sort);

        if (predicateSpecification == null) {
            return shipmentRepository.findAll(pageable);
        }

        return shipmentRepository.findBy(predicateSpecification, query -> query.page(pageable));
    }

    @Transactional
    public Shipment confirmPickup(Long id) {
        Shipment shipmentFound = findById(id);

        shipmentAssignmentService.findActiveAssignment(id);

        shipmentFound.markAsInTransit();

        return shipmentFound;
    }

    @Transactional
    public Shipment confirmDelivery(Long id) {
        Shipment shipmentFound = findById(id);
        shipmentFound.markAsDelivered();
        shipmentAssignmentService.completeAssignmentForShipment(id);

        return shipmentFound;
    }

    @Transactional
    public Shipment cancel(Long id) {
        Shipment shipmentFound = findById(id);

        shipmentFound.markAsCancelled();

        shipmentAssignmentService.cancelAssignmentForShipmentIfPresent(id);

        return shipmentFound;
    }
}
