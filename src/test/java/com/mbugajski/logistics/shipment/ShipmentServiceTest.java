package com.mbugajski.logistics.shipment;

import com.mbugajski.logistics.address.dto.request.CreateAddressRequest;
import com.mbugajski.logistics.address.entity.Address;
import com.mbugajski.logistics.address.repository.AddressRepository;
import com.mbugajski.logistics.assignment.entity.AssignmentStatus;
import com.mbugajski.logistics.assignment.entity.ShipmentAssignment;
import com.mbugajski.logistics.assignment.exception.ActiveAssignmentNotFoundException;
import com.mbugajski.logistics.assignment.service.ShipmentAssignmentService;
import com.mbugajski.logistics.courier.entity.Courier;
import com.mbugajski.logistics.customer.entity.Customer;
import com.mbugajski.logistics.customer.exception.CustomerNotFoundException;
import com.mbugajski.logistics.customer.repository.CustomerRepository;
import com.mbugajski.logistics.shipment.dto.request.CreateShipmentRequest;
import com.mbugajski.logistics.shipment.entity.Shipment;
import com.mbugajski.logistics.shipment.exception.ShipmentInvalidStatusException;
import com.mbugajski.logistics.shipment.exception.ShipmentNotFoundException;
import com.mbugajski.logistics.shipment.repository.ShipmentRepository;
import com.mbugajski.logistics.shipment.repository.ShipmentSortBy;
import com.mbugajski.logistics.shipment.service.ShipmentService;
import com.mbugajski.logistics.shipment.entity.ShipmentStatus;
import com.mbugajski.logistics.vehicle.entity.Vehicle;
import com.mbugajski.logistics.vehicle.entity.VehicleType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.PredicateSpecification;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShipmentServiceTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private ShipmentAssignmentService shipmentAssignmentService;

    @InjectMocks
    private ShipmentService shipmentService;


    @Test
    void shouldCreateShipment() {
        CreateShipmentRequest createShipmentRequest = new CreateShipmentRequest();
        CreateAddressRequest requestPickupAddress = createPickupAddressRequest();
        CreateAddressRequest requestDeliveryAddress = createDeliveryAddressRequest();

        Customer customerFromRequest = new Customer(
                "Adrian",
                "Nowak",
                "adrian@nowak.com",
                "+48 599 433 231",
                new Address(
                        "Zachodnia",
                        "15",
                        "5",
                        "Kielce",
                        "60-231",
                        "Poland"
                )
        );

        createShipmentRequest.setCustomerId(1L);
        createShipmentRequest.setPickupAddress(requestPickupAddress);
        createShipmentRequest.setDeliveryAddress(requestDeliveryAddress);
        createShipmentRequest.setWeight(new BigDecimal("12.00"));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customerFromRequest));
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Shipment shipmentSaved = shipmentService.create(createShipmentRequest);

        assertEquals("Adrian", shipmentSaved.getCustomer().getFirstName());
        assertEquals("Odbiorowa", shipmentSaved.getPickupAddress().getStreet());
        assertEquals("Wysyłkowa", shipmentSaved.getDeliveryAddress().getStreet());
        assertEquals(new BigDecimal("12.00"), shipmentSaved.getWeight());
        assertEquals(ShipmentStatus.CREATED, shipmentSaved.getStatus());
        assertEquals(new BigDecimal("35.00"), shipmentSaved.getPrice());

        verify(customerRepository).findById(1L);
        verify(addressRepository, times(2)).save(any(Address.class));
        verify(shipmentRepository).save(any(Shipment.class));
    }

    @Test
    void shouldThrowWhenCustomerNotFound() {
        CreateShipmentRequest shipmentRequest = new CreateShipmentRequest();
        shipmentRequest.setCustomerId(1L);

        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        CustomerNotFoundException exception = assertThrows(CustomerNotFoundException.class, () -> shipmentService.create(shipmentRequest));

        assertEquals("Customer with ID 1 was not found.", exception.getMessage());

        verify(customerRepository).findById(1L);
        verify(addressRepository, never()).save(any(Address.class));
        verify(shipmentRepository, never()).save(any(Shipment.class));
    }

    @Test
    void shouldThrowWhenRequestIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> shipmentService.create(null));

        assertEquals("CreateShipmentRequest cannot be null.", exception.getMessage());

        verifyNoInteractions(customerRepository, addressRepository, shipmentRepository);
    }

    @Test
    void shouldReturnFoundShipment() {
        Shipment shipment = createShipment();

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));

        Shipment foundShipment = shipmentService.findById(1L);

        assertEquals("Adrian", foundShipment.getCustomer().getFirstName());
        assertEquals("Odbiorowa", foundShipment.getPickupAddress().getStreet());
        assertEquals("Wysyłkowa", foundShipment.getDeliveryAddress().getStreet());
        assertEquals(new BigDecimal("12.00"), foundShipment.getWeight());

        verify(shipmentRepository).findById(1L);
    }

    @Test
    void shouldThrowWhenShipmentNotFound() {
        when(shipmentRepository.findById(1L)).thenReturn(Optional.empty());

        ShipmentNotFoundException exception = assertThrows(ShipmentNotFoundException.class, () -> shipmentService.findById(1L));

        assertEquals("Shipment with id 1 not found.", exception.getMessage());

        verify(shipmentRepository).findById(1L);
    }

    @Test
    void shouldThrowWhenShipmentIsNegative() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> shipmentService.findById(-5L));

        assertEquals("Shipment id must be greater than 0.", exception.getMessage());

        verify(shipmentRepository, never()).findById(-5L);
    }

    @Test
    void shouldThrowWhenShipmentIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> shipmentService.findById(null));

        assertEquals("Shipment id must be greater than 0.", exception.getMessage());

        verifyNoInteractions(shipmentRepository);
    }

    @Test
    void shouldThrowWhenShipmentIdEqualsZero() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> shipmentService.findById(0L));

        assertEquals("Shipment id must be greater than 0.", exception.getMessage());

        verifyNoInteractions(shipmentRepository);
    }

//    @Test
//    void shouldMarkShipmentAsReadyForPickup() {
//        Shipment shipment = createShipment();
//
//        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
//
//        Shipment updatedShipment = shipmentService.markAsReadyForPickup(1L);
//
//        assertEquals(ShipmentStatus.READY_FOR_PICKUP, updatedShipment.getStatus());
//
//        verify(shipmentRepository).findById(1L);
//        verify(shipmentRepository, never()).save(any(Shipment.class));
//    }

//    @Test
//    void shouldThrowWhenShipmentIsAlreadyReadyForPickup() {
//        Shipment shipment = createShipment();
//
//        shipment.markAsReadyForPickup();
//
//        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
//
//        ShipmentInvalidStatusException exception = assertThrows(ShipmentInvalidStatusException.class, () -> shipmentService.markAsReadyForPickup(1L));
//
//        assertEquals("Only a shipment with status 'CREATED' can be marked as 'READY_FOR_PICKUP'.", exception.getMessage());
//        assertEquals(ShipmentStatus.READY_FOR_PICKUP, shipment.getStatus());
//
//        verify(shipmentRepository).findById(1L);
//        verify(shipmentRepository, never()).save(any(Shipment.class));
//    }

    @Test
    void shouldMarkShipmentAsInTransit() {
        Shipment shipment = createShipment();
        shipment.markAsReadyForPickup();

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));

        Shipment updatedShipment = shipmentService.confirmPickup(1L);

        assertEquals(ShipmentStatus.IN_TRANSIT, updatedShipment.getStatus());

        verify(shipmentRepository).findById(1L);
        verify(shipmentRepository, never()).save(any(Shipment.class));
    }

    @Test
    void shouldThrowWhenShipmentIsNotReadyForPickup() {
        Shipment shipment = createShipment();

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));

        ShipmentInvalidStatusException exception = assertThrows(ShipmentInvalidStatusException.class, () -> shipmentService.confirmPickup(1L));

        assertEquals("Only a shipment with status 'READY_FOR_PICKUP' can be marked as 'IN_TRANSIT'.", exception.getMessage());
        assertEquals(ShipmentStatus.CREATED, shipment.getStatus());

        verify(shipmentRepository).findById(1L);
        verify(shipmentRepository, never()).save(any(Shipment.class));
    }

    @Test
    void shouldMarkShipmentAsDelivered() {
        Shipment shipment = createShipment();
        shipment.markAsReadyForPickup();
        shipment.markAsInTransit();

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));

        Shipment updatedShipment = shipmentService.confirmDelivery(1L);

        assertEquals(ShipmentStatus.DELIVERED, updatedShipment.getStatus());

        verify(shipmentRepository).findById(1L);
        verify(shipmentRepository, never()).save(any(Shipment.class));
    }

    @Test
    void shouldThrowWhenShipmentIsNotInTransit() {
        Shipment shipment = createShipment();

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));

        ShipmentInvalidStatusException exception = assertThrows(ShipmentInvalidStatusException.class, () -> shipmentService.confirmDelivery(1L));

        assertEquals("Only a shipment with status 'IN_TRANSIT' can be marked as 'DELIVERED'.", exception.getMessage());
        assertEquals(ShipmentStatus.CREATED, shipment.getStatus());

        verify(shipmentRepository).findById(1L);
        verify(shipmentRepository, never()).save(any(Shipment.class));
    }

    @Test
    void shouldCancelShipmentWhenCreated() {
        Shipment shipment = createShipment();

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));

        Shipment updatedShipment = shipmentService.cancel(1L);

        assertEquals(ShipmentStatus.CANCELLED, updatedShipment.getStatus());

        verify(shipmentRepository).findById(1L);
        verify(shipmentRepository, never()).save(any(Shipment.class));
    }

    @Test
    void shouldCancelShipmentWhenReadyForPickup() {
        Shipment shipment = createShipment();
        shipment.markAsReadyForPickup();

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));

        Shipment updatedShipment = shipmentService.cancel(1L);

        assertEquals(ShipmentStatus.CANCELLED, updatedShipment.getStatus());

        verify(shipmentRepository).findById(1L);
        verify(shipmentRepository, never()).save(any(Shipment.class));
    }

    @Test
    void shouldThrowWhenCancellingInTransitShipment() {
        Shipment shipment = createShipment();
        shipment.markAsReadyForPickup();
        shipment.markAsInTransit();

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));

        ShipmentInvalidStatusException exception = assertThrows(ShipmentInvalidStatusException.class, () -> shipmentService.cancel(1L));

        assertEquals("Only a shipment with status 'CREATED' or 'READY_FOR_PICKUP' can be cancelled.", exception.getMessage());
        assertEquals(ShipmentStatus.IN_TRANSIT, shipment.getStatus());

        verify(shipmentRepository).findById(1L);
        verify(shipmentRepository, never()).save(any(Shipment.class));
    }

    @Test
    void shouldReleaseResourcesAfterDelivery() {
        Shipment shipment = createShipment();
        shipment.markAsReadyForPickup();
        shipment.markAsInTransit();

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));

        Shipment deliveredShipment = shipmentService.confirmDelivery(1L);

        assertEquals(ShipmentStatus.DELIVERED, deliveredShipment.getStatus());

        verify(shipmentAssignmentService).completeAssignmentForShipment(1L);
    }

    @Test
    void shouldReleaseResourcesAfterCancelling() {
        Shipment shipment = createShipment();
        shipment.markAsReadyForPickup();

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));

        Shipment cancelledShipment = shipmentService.cancel(1L);

        assertEquals(ShipmentStatus.CANCELLED, cancelledShipment.getStatus());

        verify(shipmentAssignmentService).cancelAssignmentForShipmentIfPresent(1L);
    }

    @Test
    void shouldThrowWhenPageIsNegative() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,() -> shipmentService.findAllByPageNumber(-5, 4, null, null));

        assertEquals("Page number cannot be negative.", exception.getMessage());

        verifyNoInteractions(shipmentRepository);
    }

    @Test
    void shouldThrowWhenPageSizeIsEqualOrLessThanZero() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,() -> shipmentService.findAllByPageNumber(2, 0, null, null));

        assertEquals("Page size must be greater than 0.", exception.getMessage());

        verifyNoInteractions(shipmentRepository);
    }

    @Test
    void shouldThrowWhenPageSizeIsOverLimit() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,() -> shipmentService.findAllByPageNumber(2, 101, null, null));

        assertEquals("Page size cannot be more than 100.", exception.getMessage());

        verifyNoInteractions(shipmentRepository);
    }

    @Test
    void shouldReturnPage() {
        Page<Shipment> expectedPage = mock(Page.class);

        when(shipmentRepository.findAll(any(Pageable.class))).thenReturn(expectedPage);

        Page<Shipment> result = shipmentService.findAllByPageNumber(2, 10, null, null);

        assertSame(expectedPage, result);
        verify(shipmentRepository).findAll(PageRequest.of(2, 10));
    }

    @Test
    void shouldReturnDefaultPagination() {
        Page<Shipment> expectedPage = mock(Page.class);

        when(shipmentRepository.findAll(any(Pageable.class))).thenReturn(expectedPage);

        Page<Shipment> result = shipmentService.findAllByPageNumber(2,10, null, null);

        assertSame(expectedPage, result);
        verify(shipmentRepository).findAll(PageRequest.of(2, 10));
    }

    @Test
    void shouldReturnPageFilteredByStatus() {
        Page<Shipment> expectedPage = mock(Page.class);

        doReturn(expectedPage).when(shipmentRepository).findBy(any(PredicateSpecification.class), any());

        Page<Shipment> result = shipmentService.findAllByPageNumber(2, 10, ShipmentStatus.CREATED, null);

        assertSame(expectedPage, result);

        verify(shipmentRepository).findBy(any(PredicateSpecification.class), any());

        verify(shipmentRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void shouldReturnPageFilteredByCustomerId() {
        Page<Shipment> expectedPage = mock(Page.class);

        doReturn(expectedPage).when(shipmentRepository).findBy(any(PredicateSpecification.class), any());

        Page<Shipment> result = shipmentService.findAllByPageNumber(2, 10, null, 1L);

        assertSame(expectedPage, result);

        verify(shipmentRepository).findBy(any(PredicateSpecification.class), any());

        verify(shipmentRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void shouldReturnPageFilteredByStatusAndCustomerId() {
        Page<Shipment> expectedPage = mock(Page.class);

        doReturn(expectedPage).when(shipmentRepository).findBy(any(PredicateSpecification.class), any());

        Page<Shipment> result = shipmentService.findAllByPageNumber(2, 10, ShipmentStatus.CREATED, 1L);

        assertSame(expectedPage, result);

        verify(shipmentRepository).findBy(any(PredicateSpecification.class), any());

        verify(shipmentRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void shouldReturnPageSortedByParamAscending() {
        Page<Shipment> expectedPage = mock(Page.class);

        when(shipmentRepository.findAll(any(Pageable.class))).thenReturn(expectedPage);

        Page<Shipment> result = shipmentService.findAllByPageNumber(2, 10, null, null, ShipmentSortBy.price, "asc");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        assertSame(expectedPage, result);

        verify(shipmentRepository).findAll(pageableCaptor.capture());

        Pageable capturedPageable = pageableCaptor.getValue();

        assertEquals(2, capturedPageable.getPageNumber());
        assertEquals(10, capturedPageable.getPageSize());

        Sort.Order priceOrder = capturedPageable.getSort().getOrderFor("price");

        assertNotNull(priceOrder);
        assertEquals("price", priceOrder.getProperty());
        assertEquals(Sort.Direction.ASC, priceOrder.getDirection());
    }

    @Test
    void shouldReturnPageSortedByParamDescending() {
        Page<Shipment> expectedPage = mock(Page.class);

        when(shipmentRepository.findAll(any(Pageable.class))).thenReturn(expectedPage);

        Page<Shipment> result = shipmentService.findAllByPageNumber(2, 10, null, null, ShipmentSortBy.price, "desc");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        assertSame(expectedPage, result);

        verify(shipmentRepository).findAll(pageableCaptor.capture());

        Pageable capturedPageable = pageableCaptor.getValue();

        assertEquals(2, capturedPageable.getPageNumber());
        assertEquals(10, capturedPageable.getPageSize());

        Sort.Order priceOrder = capturedPageable.getSort().getOrderFor("price");

        assertNotNull(priceOrder);
        assertEquals("price", priceOrder.getProperty());
        assertEquals(Sort.Direction.DESC, priceOrder.getDirection());
    }

    @Test
    void shouldReturnPageSortedByParamWeight() {
        Page<Shipment> expectedPage = mock(Page.class);

        when(shipmentRepository.findAll(any(Pageable.class))).thenReturn(expectedPage);

        Page<Shipment> result = shipmentService.findAllByPageNumber(2, 10, null, null, ShipmentSortBy.weight, "desc");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        assertSame(expectedPage, result);

        verify(shipmentRepository).findAll(pageableCaptor.capture());

        Pageable capturedPageable = pageableCaptor.getValue();

        assertEquals(2, capturedPageable.getPageNumber());
        assertEquals(10, capturedPageable.getPageSize());

        Sort.Order weightOrder = capturedPageable.getSort().getOrderFor("weight");

        assertNotNull(weightOrder);
        assertEquals("weight", weightOrder.getProperty());
        assertEquals(Sort.Direction.DESC, weightOrder.getDirection());
    }

    @Test
    void shouldReturnPageSortedByParamCreatedAt() {
        Page<Shipment> expectedPage = mock(Page.class);

        when(shipmentRepository.findAll(any(Pageable.class))).thenReturn(expectedPage);

        Page<Shipment> result = shipmentService.findAllByPageNumber(2, 10, null, null, ShipmentSortBy.createdAt, "desc");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        assertSame(expectedPage, result);

        verify(shipmentRepository).findAll(pageableCaptor.capture());

        Pageable capturedPageable = pageableCaptor.getValue();

        assertEquals(2, capturedPageable.getPageNumber());
        assertEquals(10, capturedPageable.getPageSize());

        Sort.Order createdAtOrder = capturedPageable.getSort().getOrderFor("createdAt");

        assertNotNull(createdAtOrder);
        assertEquals("createdAt", createdAtOrder.getProperty());
        assertEquals(Sort.Direction.DESC, createdAtOrder.getDirection());
    }

    @Test
    void shouldReturnPageWithoutSorting() {
        Page<Shipment> expectedPage = mock(Page.class);

        when(shipmentRepository.findAll(any(Pageable.class))).thenReturn(expectedPage);

        Page<Shipment> result = shipmentService.findAllByPageNumber(2, 10, null, null);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        assertSame(expectedPage, result);

        verify(shipmentRepository).findAll(pageableCaptor.capture());

        Pageable capturedPageable = pageableCaptor.getValue();

        assertEquals(2, capturedPageable.getPageNumber());
        assertEquals(10, capturedPageable.getPageSize());
        assertTrue(capturedPageable.getSort().isUnsorted());
    }

    @Test
    void shouldThrowWhenDirectionParamIsInvalid() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> shipmentService.findAllByPageNumber(2, 10, null, null, ShipmentSortBy.price, null));

        assertEquals("If sortBy exists, then direction cannot be null.", exception.getMessage());
        verifyNoInteractions(shipmentRepository);
    }

    @Test
    void shouldThrowWhenSortParamIsInvalid() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> shipmentService.findAllByPageNumber(2, 10, null, null, null, "desc"));

        assertEquals("If sortBy dont exist, then direction is useless.", exception.getMessage());
        verifyNoInteractions(shipmentRepository);
    }

    @Test
    void shouldConfirmPickup() {
        Shipment shipment = createShipment();
        shipment.markAsReadyForPickup();
        Courier courier = new Courier("Adam", "Nowak", "+48 565 423 123");
        Vehicle vehicle = new Vehicle("Ford", "Ducato", "GD 2313D", VehicleType.VAN, new BigDecimal("124.00"));

        ShipmentAssignment shipmentAssignment = new ShipmentAssignment(shipment, courier, vehicle);

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
        when(shipmentAssignmentService.findActiveAssignment(1L)).thenReturn(shipmentAssignment);

        Shipment pickedUpShipment = shipmentService.confirmPickup(1L);

        assertEquals(AssignmentStatus.ACTIVE, shipmentAssignment.getStatus());
        assertEquals(ShipmentStatus.IN_TRANSIT, pickedUpShipment.getStatus());
        assertSame(shipment, pickedUpShipment);

        verify(shipmentRepository).findById(1L);
        verify(shipmentAssignmentService).findActiveAssignment(1L);
    }

    @Test
    void shouldThrowWhenActiveAssignmentNotFound() {
        Shipment shipment = createShipment();
        shipment.markAsReadyForPickup();

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
        when(shipmentAssignmentService.findActiveAssignment(1L)).thenThrow(new ActiveAssignmentNotFoundException(1L));

        ActiveAssignmentNotFoundException exception = assertThrows(ActiveAssignmentNotFoundException.class,() -> shipmentService.confirmPickup(1L));

        assertEquals("Active assignment for shipment with id 1 not found.", exception.getMessage());
        assertEquals(ShipmentStatus.READY_FOR_PICKUP, shipment.getStatus());

        verify(shipmentRepository).findById(1L);
        verify(shipmentAssignmentService).findActiveAssignment(1L);
    }

    @Test
    void shouldConfirmDelivery() {
        Shipment shipment = createShipment();
        shipment.markAsReadyForPickup();
        shipment.markAsInTransit();

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));

        Shipment deliveredShipment = shipmentService.confirmDelivery(1L);

        assertEquals(ShipmentStatus.DELIVERED, deliveredShipment.getStatus());
        assertSame(shipment, deliveredShipment);

        verify(shipmentRepository).findById(1L);
        verify(shipmentAssignmentService).completeAssignmentForShipment(1L);
    }

    @Test
    void shouldCancelShipment() {
        Shipment shipment = createShipment();

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));

        Shipment canceledShipment = shipmentService.cancel(1L);

        assertEquals(ShipmentStatus.CANCELLED, canceledShipment.getStatus());
        assertSame(shipment, canceledShipment);

        verify(shipmentRepository).findById(1L);
        verify(shipmentAssignmentService).cancelAssignmentForShipmentIfPresent(1L);
    }


    public CreateAddressRequest createPickupAddressRequest() {
        CreateAddressRequest request = new CreateAddressRequest();

        request.setStreet("Odbiorowa");
        request.setBuildingNumber("10");
        request.setApartmentNumber("2");
        request.setCity("Odbiór");
        request.setPostalCode("10-120");
        request.setCountry("Poland");

        return request;
    }

    public CreateAddressRequest createDeliveryAddressRequest() {
        CreateAddressRequest request = new CreateAddressRequest();

        request.setStreet("Wysyłkowa");
        request.setBuildingNumber("5");
        request.setApartmentNumber("3");
        request.setCity("Wysyłka");
        request.setPostalCode("90-192");
        request.setCountry("Poland");

        return request;
    }

    public Address mapToAddress(CreateAddressRequest request) {
        return new Address(
                request.getStreet(),
                request.getBuildingNumber(),
                request.getApartmentNumber(),
                request.getCity(),
                request.getPostalCode(),
                request.getCountry()
        );
    }

    public Shipment createShipment() {
        CreateAddressRequest requestPickupAddress = createPickupAddressRequest();
        CreateAddressRequest requestDeliveryAddress = createDeliveryAddressRequest();

        Customer customer = new Customer(
                "Adrian",
                "Nowak",
                "adrian@nowak.com",
                "+48 599 433 231",
                new Address(
                        "Zachodnia",
                        "15",
                        "5",
                        "Kielce",
                        "60-231",
                        "Poland"
                )
        );

        return new Shipment(customer, mapToAddress(requestPickupAddress), mapToAddress(requestDeliveryAddress), new BigDecimal("12.00"));

    }


}
