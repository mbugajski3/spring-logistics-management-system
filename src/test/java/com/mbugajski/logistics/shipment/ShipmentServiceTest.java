package com.mbugajski.logistics.shipment;

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
import com.mbugajski.logistics.shipment.service.ShipmentService;
import com.mbugajski.logistics.shipment.entity.ShipmentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

    @Test
    void shouldMarkShipmentAsReadyForPickup() {
        Shipment shipment = createShipment();

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));

        Shipment updatedShipment = shipmentService.markAsReadyForPickup(1L);

        assertEquals(ShipmentStatus.READY_FOR_PICKUP, updatedShipment.getStatus());

        verify(shipmentRepository).findById(1L);
        verify(shipmentRepository, never()).save(any(Shipment.class));
    }

    @Test
    void shouldThrowWhenShipmentIsAlreadyReadyForPickup() {
        Shipment shipment = createShipment();

        shipment.markAsReadyForPickup();

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));

        ShipmentInvalidStatusException exception = assertThrows(ShipmentInvalidStatusException.class, () -> shipmentService.markAsReadyForPickup(1L));

        assertEquals("Only a shipment with status 'CREATED' can be marked as 'READY_FOR_PICKUP'.", exception.getMessage());
        assertEquals(ShipmentStatus.READY_FOR_PICKUP, shipment.getStatus());

        verify(shipmentRepository).findById(1L);
        verify(shipmentRepository, never()).save(any(Shipment.class));
    }

    @Test
    void shouldMarkShipmentAsInTransit() {
        Shipment shipment = createShipment();
        shipment.markAsReadyForPickup();

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));

        Shipment updatedShipment = shipmentService.markAsInTransit(1L);

        assertEquals(ShipmentStatus.IN_TRANSIT, updatedShipment.getStatus());

        verify(shipmentRepository).findById(1L);
        verify(shipmentRepository, never()).save(any(Shipment.class));
    }

    @Test
    void shouldThrowWhenShipmentIsNotReadyForPickup() {
        Shipment shipment = createShipment();

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));

        ShipmentInvalidStatusException exception = assertThrows(ShipmentInvalidStatusException.class, () -> shipmentService.markAsInTransit(1L));

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

        Shipment updatedShipment = shipmentService.markAsDelivered(1L);

        assertEquals(ShipmentStatus.DELIVERED, updatedShipment.getStatus());

        verify(shipmentRepository).findById(1L);
        verify(shipmentRepository, never()).save(any(Shipment.class));
    }

    @Test
    void shouldThrowWhenShipmentIsNotInTransit() {
        Shipment shipment = createShipment();

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));

        ShipmentInvalidStatusException exception = assertThrows(ShipmentInvalidStatusException.class, () -> shipmentService.markAsDelivered(1L));

        assertEquals("Only a shipment with status 'IN_TRANSIT' can be marked as 'DELIVERED'.", exception.getMessage());
        assertEquals(ShipmentStatus.CREATED, shipment.getStatus());

        verify(shipmentRepository).findById(1L);
        verify(shipmentRepository, never()).save(any(Shipment.class));
    }

    @Test
    void shouldCancelShipmentWhenCreated() {
        Shipment shipment = createShipment();

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));

        Shipment updatedShipment = shipmentService.markAsCancelled(1L);

        assertEquals(ShipmentStatus.CANCELLED, updatedShipment.getStatus());

        verify(shipmentRepository).findById(1L);
        verify(shipmentRepository, never()).save(any(Shipment.class));
    }

    @Test
    void shouldCancelShipmentWhenReadyForPickup() {
        Shipment shipment = createShipment();
        shipment.markAsReadyForPickup();

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));

        Shipment updatedShipment = shipmentService.markAsCancelled(1L);

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

        ShipmentInvalidStatusException exception = assertThrows(ShipmentInvalidStatusException.class, () -> shipmentService.markAsCancelled(1L));

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

        Shipment deliveredShipment = shipmentService.markAsDelivered(1L);

        assertEquals(ShipmentStatus.DELIVERED, deliveredShipment.getStatus());

        verify(shipmentAssignmentService).releaseResourcesForShipment(1L);
    }

    @Test
    void shouldReleaseResourcesAfterCancelling() {
        Shipment shipment = createShipment();
        shipment.markAsReadyForPickup();

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));

        Shipment cancelledShipment = shipmentService.markAsCancelled(1L);

        assertEquals(ShipmentStatus.CANCELLED, cancelledShipment.getStatus());

        verify(shipmentAssignmentService).releaseResourcesForShipment(1L);
    }

    @Test
    void shouldThrowWhenPageIsNegative() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,() -> shipmentService.findAllByPageNumber(-5, 4));

        assertEquals("Page number cannot be negative.", exception.getMessage());

        verifyNoInteractions(shipmentRepository);
    }

    @Test
    void shouldThrowWhenPageSizeIsEqualOrLessThanZero() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,() -> shipmentService.findAllByPageNumber(2, 0));

        assertEquals("Page size must be greater than 0.", exception.getMessage());

        verifyNoInteractions(shipmentRepository);
    }

    @Test
    void shouldThrowWhenPageSizeIsOverLimit() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,() -> shipmentService.findAllByPageNumber(2, 101));

        assertEquals("Page size cannot be more than 100.", exception.getMessage());

        verifyNoInteractions(shipmentRepository);
    }

    @Test
    void shouldReturnPage() {
        Page<Shipment> expectedPage = mock(Page.class);

        when(shipmentRepository.findAll(any(Pageable.class))).thenReturn(expectedPage);

        Page<Shipment> result = shipmentService.findAllByPageNumber(2, 10);

        assertSame(expectedPage, result);
        verify(shipmentRepository).findAll(PageRequest.of(2, 10));
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
