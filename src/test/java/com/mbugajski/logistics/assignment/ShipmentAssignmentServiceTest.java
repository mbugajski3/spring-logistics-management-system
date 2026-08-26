package com.mbugajski.logistics.assignment;

import com.mbugajski.logistics.address.entity.Address;
import com.mbugajski.logistics.assignment.entity.AssignmentStatus;
import com.mbugajski.logistics.assignment.entity.ShipmentAssignment;
import com.mbugajski.logistics.assignment.exception.ActiveAssignmentNotFoundException;
import com.mbugajski.logistics.assignment.exception.AssignmentParameterInvalidStatus;
import com.mbugajski.logistics.assignment.exception.AssignmentVehicleOutOfSpaceException;
import com.mbugajski.logistics.assignment.repository.ShipmentAssignmentRepository;
import com.mbugajski.logistics.assignment.service.ShipmentAssignmentService;
import com.mbugajski.logistics.courier.entity.Courier;
import com.mbugajski.logistics.courier.exception.CourierInvalidStateException;
import com.mbugajski.logistics.courier.exception.CourierNotFoundException;
import com.mbugajski.logistics.courier.repository.CourierRepository;
import com.mbugajski.logistics.customer.entity.Customer;
import com.mbugajski.logistics.shipment.entity.Shipment;
import com.mbugajski.logistics.shipment.entity.ShipmentStatus;
import com.mbugajski.logistics.shipment.exception.ShipmentNotFoundException;
import com.mbugajski.logistics.shipment.repository.ShipmentRepository;
import com.mbugajski.logistics.vehicle.entity.Vehicle;
import com.mbugajski.logistics.vehicle.entity.VehicleType;
import com.mbugajski.logistics.vehicle.exception.VehicleInvalidStateException;
import com.mbugajski.logistics.vehicle.exception.VehicleNotFoundException;
import com.mbugajski.logistics.vehicle.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShipmentAssignmentServiceTest {

    @Mock
    private ShipmentAssignmentRepository assignmentRepository;

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private CourierRepository courierRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private ShipmentAssignmentService shipmentAssignmentService;

    @Test
    void shouldAssignShipment() {
        Shipment shipment = createShipment();
        Courier courier = createCourier();
        Vehicle vehicle = createVehicle();

        assertEquals(ShipmentStatus.CREATED, shipment.getStatus());
        assertTrue(courier.isActive());
        assertTrue(courier.isAvailable());
        assertTrue(vehicle.isActive());
        assertTrue(vehicle.isAvailable());

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        ShipmentAssignment createdAssignment = shipmentAssignmentService.assign(1L, 1L, 1L);

        assertSame(shipment, createdAssignment.getShipment());
        assertSame(courier, createdAssignment.getCourier());
        assertSame(vehicle, createdAssignment.getVehicle());

        assertEquals(ShipmentStatus.READY_FOR_PICKUP, shipment.getStatus());
        assertFalse(courier.isAvailable());
        assertFalse(vehicle.isAvailable());

        verify(assignmentRepository).save(any(ShipmentAssignment.class));
    }

    @Test
    void shouldThrowWhenInvalidShipmentId() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,() -> shipmentAssignmentService.assign(-5L, 1L, 1L));

        assertEquals("Shipment ID cannot be null, zero or below.", exception.getMessage());

        verify(assignmentRepository, never()).save(any(ShipmentAssignment.class));
    }

    @Test
    void shouldThrowWhenInvalidCourierId() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,() -> shipmentAssignmentService.assign(1L, -5L, 1L));

        assertEquals("Courier ID cannot be null, zero or below.", exception.getMessage());

        verify(assignmentRepository, never()).save(any(ShipmentAssignment.class));
    }

    @Test
    void shouldThrowWhenInvalidVehicleId() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,() -> shipmentAssignmentService.assign(1L, 1L, -5L));

        assertEquals("Vehicle ID cannot be null, zero or below.", exception.getMessage());

        verify(assignmentRepository, never()).save(any(ShipmentAssignment.class));
    }

    @Test
    void shouldThrowWhenShipmentNotFound() {
        when(shipmentRepository.findById(1L)).thenReturn(Optional.empty());

        ShipmentNotFoundException exception = assertThrows(ShipmentNotFoundException.class, () -> shipmentAssignmentService.assign(1L, 1L, 1L));

        assertEquals("Shipment with id 1 not found.", exception.getMessage());

        verify(assignmentRepository, never()).save(any(ShipmentAssignment.class));
        verify(courierRepository, never()).findById(1L);
        verify(vehicleRepository, never()).findById(1L);
    }

    @Test
    void shouldThrowWhenCourierNotFound() {
        Shipment shipment = createShipment();

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
        when(courierRepository.findById(1L)).thenReturn(Optional.empty());

        CourierNotFoundException exception = assertThrows(CourierNotFoundException.class, () -> shipmentAssignmentService.assign(1L, 1L, 1L));

        assertEquals("Courier with id 1 not found.", exception.getMessage());

        verify(assignmentRepository, never()).save(any(ShipmentAssignment.class));
        verify(vehicleRepository, never()).findById(1L);
    }

    @Test
    void shouldThrowWhenVehicleNotFound() {
        Shipment shipment = createShipment();
        Courier courier = createCourier();

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.empty());

        VehicleNotFoundException exception = assertThrows(VehicleNotFoundException.class, () -> shipmentAssignmentService.assign(1L, 1L, 1L));

        assertEquals("Vehicle with id 1 not found.", exception.getMessage());

        verify(assignmentRepository, never()).save(any(ShipmentAssignment.class));
    }

    @Test
    void shouldThrowWhenShipmentStatusIsNotCreated() {
        Shipment shipment = createShipment();
        shipment.markAsReadyForPickup();

        Courier courier = createCourier();
        Vehicle vehicle = createVehicle();

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        AssignmentParameterInvalidStatus exception = assertThrows(AssignmentParameterInvalidStatus.class, () -> shipmentAssignmentService.assign(1L, 1L, 1L));

        assertEquals("Shipment status must be 'CREATED' to assign.", exception.getMessage());
        assertTrue(courier.isAvailable());
        assertTrue(vehicle.isAvailable());
        assertEquals(ShipmentStatus.READY_FOR_PICKUP, shipment.getStatus());

        verify(assignmentRepository, never()).save(any(ShipmentAssignment.class));
    }

    @Test
    void shouldThrowWhenShipmentWeightIsOverMaximumLoad() {
        Shipment shipment = createShipment();
        Courier courier = createCourier();
        Vehicle vehicle = createVehicleWithLowMaximumLoad();

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        AssignmentVehicleOutOfSpaceException exception = assertThrows(AssignmentVehicleOutOfSpaceException.class,() -> shipmentAssignmentService.assign(1L, 1L, 1L));

        assertEquals("Shipment weight is too big.", exception.getMessage());
        assertTrue(courier.isAvailable());
        assertTrue(vehicle.isAvailable());
        assertEquals(ShipmentStatus.CREATED, shipment.getStatus());

        verify(assignmentRepository, never()).save(any(ShipmentAssignment.class));
    }

    @Test
    void shouldThrowWhenCourierIsBusy() {
        Shipment shipment = createShipment();
        Courier courier = createCourier();
        Vehicle vehicle = createVehicle();

        courier.markAsBusy();

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        CourierInvalidStateException exception = assertThrows(CourierInvalidStateException.class,() -> shipmentAssignmentService.assign(1L, 1L, 1L));

        assertEquals("Courier must be active and available to mark as busy.", exception.getMessage());
        assertEquals(ShipmentStatus.CREATED, shipment.getStatus());
        assertTrue(vehicle.isAvailable());
        assertFalse(courier.isAvailable());

        verify(assignmentRepository, never()).save(any(ShipmentAssignment.class));
    }

    @Test
    void shouldThrowWhenCourierIsInactive() {
        Shipment shipment = createShipment();
        Courier courier = createCourier();
        Vehicle vehicle = createVehicle();

        courier.deactivate();

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        CourierInvalidStateException exception = assertThrows(CourierInvalidStateException.class,() -> shipmentAssignmentService.assign(1L, 1L, 1L));

        assertEquals("Courier must be active and available to mark as busy.", exception.getMessage());
        assertEquals(ShipmentStatus.CREATED, shipment.getStatus());
        assertTrue(vehicle.isAvailable());
        assertFalse(courier.isActive());
        assertFalse(courier.isAvailable());

        verify(assignmentRepository, never()).save(any(ShipmentAssignment.class));
    }

    @Test
    void shouldThrowWhenVehicleIsNotAvailable() {
        Shipment shipment = createShipment();
        Courier courier = createCourier();
        Vehicle vehicle = createVehicle();

        vehicle.markAsBusy();

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        VehicleInvalidStateException exception = assertThrows(VehicleInvalidStateException.class,() -> shipmentAssignmentService.assign(1L, 1L, 1L));

        assertEquals("Vehicle must be active and available to mark as busy.", exception.getMessage());
        assertEquals(ShipmentStatus.CREATED, shipment.getStatus());
        assertFalse(vehicle.isAvailable());
        assertFalse(courier.isAvailable());

        verify(assignmentRepository, never()).save(any(ShipmentAssignment.class));
    }

    @Test
    void shouldThrowWhenVehicleIsInactive() {
        Shipment shipment = createShipment();
        Courier courier = createCourier();
        Vehicle vehicle = createVehicle();

        vehicle.deactivate();

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));


        VehicleInvalidStateException exception = assertThrows(VehicleInvalidStateException.class,() -> shipmentAssignmentService.assign(1L, 1L, 1L));

        assertEquals("Vehicle must be active and available to mark as busy.", exception.getMessage());
        assertEquals(ShipmentStatus.CREATED, shipment.getStatus());
        assertFalse(vehicle.isActive());

        verify(assignmentRepository, never()).save(any(ShipmentAssignment.class));
    }

    @Test
    void shouldReleaseResources() {
        Shipment shipment = createShipment();
        Courier courier = createCourier();
        Vehicle vehicle = createVehicle();

        courier.markAsBusy();
        vehicle.markAsBusy();

        ShipmentAssignment assignment = new ShipmentAssignment(shipment, courier, vehicle);

        when(assignmentRepository.findByShipmentId(1L)).thenReturn(Optional.of(assignment));

        shipmentAssignmentService.releaseResourcesForShipment(1L);

        assertTrue(courier.isAvailable());
        assertTrue(vehicle.isAvailable());
    }

    @Test
    void shouldDoNothingWhenShipmentAssignmentNotFound() {
        when(assignmentRepository.findByShipmentId(1L)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> shipmentAssignmentService.releaseResourcesForShipment(1L));

        verify(assignmentRepository).findByShipmentId(1L);
    }

    @Test
    void shouldThrowWhenShipmentIdIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,() -> shipmentAssignmentService.releaseResourcesForShipment(null));

        assertEquals("Shipment ID cannot be null, zero or below.", exception.getMessage());

        verifyNoInteractions(assignmentRepository);
    }

    @Test
    void shouldThrowWhenShipmentIdIsZeroOrBelow() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> shipmentAssignmentService.releaseResourcesForShipment(0L));

        assertEquals("Shipment ID cannot be null, zero or below.", exception.getMessage());

        verifyNoInteractions(assignmentRepository);
    }

    @Test
    void shouldReturnActiveAssignment() {
        ShipmentAssignment shipmentAssignment = new ShipmentAssignment(createShipment(), createCourier(), createVehicle());

        when(assignmentRepository.findByShipmentIdAndStatus(1L, AssignmentStatus.ACTIVE)).thenReturn(Optional.of(shipmentAssignment));

        ShipmentAssignment foundAssignment = shipmentAssignmentService.findActiveAssignment(1L);

        assertSame(shipmentAssignment, foundAssignment);
        assertEquals(AssignmentStatus.ACTIVE, foundAssignment.getStatus());

        verify(assignmentRepository).findByShipmentIdAndStatus(1L, AssignmentStatus.ACTIVE);
    }

    @Test
    void shouldThrowWhenActiveAssignmentNotFound() {
        when(assignmentRepository.findByShipmentIdAndStatus(1L, AssignmentStatus.ACTIVE)).thenReturn(Optional.empty());

        ActiveAssignmentNotFoundException exception = assertThrows(ActiveAssignmentNotFoundException.class,() -> shipmentAssignmentService.findActiveAssignment(1L));

        assertEquals("Active assignment for shipment with id 1 not found.", exception.getMessage());

        verify(assignmentRepository).findByShipmentIdAndStatus(1L, AssignmentStatus.ACTIVE);
    }

    @Test
    void shouldThrowWhenGivenNullParam() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,() -> shipmentAssignmentService.findActiveAssignment(null));

        assertEquals("Shipment ID cannot be null, zero or below.", exception.getMessage());

        verifyNoInteractions(assignmentRepository);
    }

    @Test
    void shouldThrowWhenGivenNegativeParam() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,() -> shipmentAssignmentService.findActiveAssignment(-1L));

        assertEquals("Shipment ID cannot be null, zero or below.", exception.getMessage());

        verifyNoInteractions(assignmentRepository);
    }

    @Test
    void shouldThrowWhenGivenZeroParam() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,() -> shipmentAssignmentService.findActiveAssignment(0L));

        assertEquals("Shipment ID cannot be null, zero or below.", exception.getMessage());

        verifyNoInteractions(assignmentRepository);
    }

    @Test
    void shouldCompleteAssignmentForShipment() {
        Shipment shipment = createShipment();
        Courier courier = createCourier();
        Vehicle vehicle = createVehicle();

        courier.markAsBusy();
        vehicle.markAsBusy();

        ShipmentAssignment assignment = new ShipmentAssignment(shipment, courier, vehicle);

        when(assignmentRepository.findByShipmentIdAndStatus(1L, AssignmentStatus.ACTIVE)).thenReturn(Optional.of(assignment));

        assertEquals(AssignmentStatus.ACTIVE, assignment.getStatus());
        assertNull(assignment.getFinishedAt());

        shipmentAssignmentService.completeAssignmentForShipment(1L);

        assertTrue(assignment.getCourier().isAvailable());
        assertTrue(assignment.getVehicle().isAvailable());

        assertEquals(AssignmentStatus.COMPLETED, assignment.getStatus());
        assertNotNull(assignment.getFinishedAt());

        verify(assignmentRepository).findByShipmentIdAndStatus(1L, AssignmentStatus.ACTIVE);
    }

    @Test
    void shouldCancelAssignmentForShipmentIfPresent() {
        Shipment shipment = createShipment();
        Courier courier = createCourier();
        Vehicle vehicle = createVehicle();

        courier.markAsBusy();
        vehicle.markAsBusy();

        ShipmentAssignment assignment = new ShipmentAssignment(shipment, courier, vehicle);

        when(assignmentRepository.findByShipmentIdAndStatus(1L, AssignmentStatus.ACTIVE)).thenReturn(Optional.of(assignment));

        assertEquals(AssignmentStatus.ACTIVE, assignment.getStatus());
        assertNull(assignment.getFinishedAt());

        shipmentAssignmentService.cancelAssignmentForShipmentIfPresent(1L);

        assertTrue(assignment.getCourier().isAvailable());
        assertTrue(assignment.getVehicle().isAvailable());

        assertEquals(AssignmentStatus.CANCELLED, assignment.getStatus());
        assertNotNull(assignment.getFinishedAt());

        verify(assignmentRepository).findByShipmentIdAndStatus(1L, AssignmentStatus.ACTIVE);
    }

    @Test
    void shouldDoNothingWhenActiveAssignmentNotFound() {
        when(assignmentRepository.findByShipmentIdAndStatus(1L, AssignmentStatus.ACTIVE)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> shipmentAssignmentService.cancelAssignmentForShipmentIfPresent(1L));

        verify(assignmentRepository).findByShipmentIdAndStatus(1L, AssignmentStatus.ACTIVE);
    }

    private Shipment createShipment() {
        Address customerAddress = new Address("Adrianowa", "20", "14", "Kielce", "50-231", "Poland");
        Address deliveryAddress = new Address("Wysyłkowa", "13", "3", "Krakow", "60-123", "Poland");

        Customer customer = new Customer("Adrian", "Nowak", "adrian@nowak.com", "+48 553 214 532", customerAddress);

        return new Shipment(customer, customerAddress, deliveryAddress, new BigDecimal("5.00"));
    }

    private Shipment createOverweightShipment() {
        Address customerAddress = new Address("Adrianowa", "20", "14", "Kielce", "50-231", "Poland");
        Address deliveryAddress = new Address("Wysyłkowa", "13", "3", "Krakow", "60-123", "Poland");

        Customer customer = new Customer("Adrian", "Nowak", "adrian@nowak.com", "+48 553 214 532", customerAddress);

        return new Shipment(customer, customerAddress, deliveryAddress, new BigDecimal("800.00"));
    }

    private Courier createCourier() {
        return new Courier("Postman", "Pat", "+48 999 234 523");
    }

    private Vehicle createVehicle() {
        return new Vehicle("Ford", "Transport", "GD 9231L", VehicleType.VAN, new BigDecimal("700.00"));
    }

    private Vehicle createVehicleWithLowMaximumLoad() {
        return new Vehicle("Ford", "Transport", "GD 9231L", VehicleType.VAN, new BigDecimal("2.00"));
    }
}
