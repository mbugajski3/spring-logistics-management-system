package com.mbugajski.logistics.vehicle;

import com.mbugajski.logistics.vehicle.dto.request.CreateVehicleRequest;
import com.mbugajski.logistics.vehicle.dto.request.UpdateVehicleStatusRequest;
import com.mbugajski.logistics.vehicle.entity.Vehicle;
import com.mbugajski.logistics.vehicle.entity.VehicleStatus;
import com.mbugajski.logistics.vehicle.entity.VehicleType;
import com.mbugajski.logistics.vehicle.exception.VehicleIllegalArgumentException;
import com.mbugajski.logistics.vehicle.exception.VehicleInvalidStateException;
import com.mbugajski.logistics.vehicle.exception.VehicleNotFoundException;
import com.mbugajski.logistics.vehicle.exception.VehicleRegistrationNumberAlreadyExistsException;
import com.mbugajski.logistics.vehicle.repository.VehicleRepository;
import com.mbugajski.logistics.vehicle.service.VehicleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleService vehicleService;

    @Test
    void shouldCreateVehicle() {
        CreateVehicleRequest vehicleRequest = new CreateVehicleRequest();
        vehicleRequest.setBrand("Ford");
        vehicleRequest.setModel("Ducato");
        vehicleRequest.setRegistrationNumber("GD 8032D");
        vehicleRequest.setVehicleType(VehicleType.VAN);
        vehicleRequest.setMaximumLoad(new BigDecimal("120.00"));

        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(vehicleRepository.existsByRegistrationNumber("GD 8032D")).thenReturn(false);

        Vehicle savedVehicle = vehicleService.create(vehicleRequest);

        assertEquals("Ford", savedVehicle.getBrand());
        assertEquals("Ducato", savedVehicle.getModel());
        assertEquals("GD 8032D", savedVehicle.getRegistrationNumber());
        assertEquals(VehicleType.VAN, savedVehicle.getVehicleType());
        assertEquals(new BigDecimal("120.00"), savedVehicle.getMaximumLoad());
        assertTrue(savedVehicle.isActive());
        assertTrue(savedVehicle.isAvailable());

        verify(vehicleRepository).existsByRegistrationNumber("GD 8032D");
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    void shouldThrowWhenRegistrationNumberAlreadyExists() {
        CreateVehicleRequest vehicleRequest = new CreateVehicleRequest();
        vehicleRequest.setBrand("Ford");
        vehicleRequest.setModel("Ducato");
        vehicleRequest.setRegistrationNumber("GD 8032D");
        vehicleRequest.setVehicleType(VehicleType.VAN);
        vehicleRequest.setMaximumLoad(new BigDecimal("120.00"));

        when(vehicleRepository.existsByRegistrationNumber("GD 8032D")).thenReturn(true);

        VehicleRegistrationNumberAlreadyExistsException exception = assertThrows(VehicleRegistrationNumberAlreadyExistsException.class,() -> vehicleService.create(vehicleRequest));

        assertEquals("GD 8032D is already assigned to another vehicle.", exception.getMessage());

        verify(vehicleRepository, never()).save(any(Vehicle.class));
        verify(vehicleRepository).existsByRegistrationNumber("GD 8032D");
    }

    @Test
    void shouldThrowWhenVehicleRequestIsNull() {
        VehicleIllegalArgumentException exception = assertThrows(VehicleIllegalArgumentException.class,() -> vehicleService.create(null));

        assertEquals("Vehicle request cannot be null.", exception.getMessage());

        verifyNoInteractions(vehicleRepository);
    }

    @Test
    void shouldReturnVehicleById() {
        Vehicle vehicle = new Vehicle("Ford", "Ducato", "GD 8032D", VehicleType.VAN, new BigDecimal("120.00"));

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        Vehicle foundVehicle = vehicleService.findById(1L);

        assertEquals("Ford", foundVehicle.getBrand());
        assertEquals("Ducato", foundVehicle.getModel());
        assertEquals("GD 8032D", foundVehicle.getRegistrationNumber());
        assertEquals(VehicleType.VAN, foundVehicle.getVehicleType());
        assertEquals(new BigDecimal("120.00"), foundVehicle.getMaximumLoad());
        assertTrue(foundVehicle.isActive());
        assertTrue(foundVehicle.isAvailable());

        verify(vehicleRepository).findById(1L);
    }

    @Test
    void shouldReturnAllVehicles() {
        Vehicle vehicle1 = new Vehicle("Ford", "Ducato", "GD 8032D", VehicleType.VAN, new BigDecimal("120.00"));
        Vehicle vehicle2 = new Vehicle("Fiat", "Transport", "GD 0324C", VehicleType.VAN, new BigDecimal("120.00"));

        when(vehicleRepository.findAll()).thenReturn(List.of(vehicle1,vehicle2));

        List<Vehicle> foundVehicles = vehicleService.findAll();

        assertEquals(2, foundVehicles.size());

        verify(vehicleRepository).findAll();
    }

    @Test
    void shouldThrowWhenVehicleIsNotFound() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.empty());

        VehicleNotFoundException exception = assertThrows(VehicleNotFoundException.class,() -> vehicleService.findById(1L));

        assertEquals("Vehicle with id 1 not found.", exception.getMessage());

        verify(vehicleRepository).findById(1L);
    }

    @Test
    void shouldThrowWhenVehicleIdIsZero() {
        VehicleIllegalArgumentException exception = assertThrows(VehicleIllegalArgumentException.class,() -> vehicleService.findById(0L));

        assertEquals("Vehicle id cannot be null, 0 or below.", exception.getMessage());

        verifyNoInteractions(vehicleRepository);
    }

    @Test
    void shouldThrowWhenVehicleIdIsNull() {
        VehicleIllegalArgumentException exception = assertThrows(VehicleIllegalArgumentException.class,() -> vehicleService.findById(null));

        assertEquals("Vehicle id cannot be null, 0 or below.", exception.getMessage());

        verifyNoInteractions(vehicleRepository);
    }

    @Test
    void shouldMarkVehicleAsBusy() {
        Vehicle vehicle = new Vehicle("Ford", "Ducato", "GD 8032D", VehicleType.VAN, new BigDecimal("120.00"));

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        Vehicle busyVehicle = vehicleService.markAsBusy(1L);

        assertTrue(busyVehicle.isActive());
        assertFalse(busyVehicle.isAvailable());

        verify(vehicleRepository).findById(1L);
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void shouldMarkVehicleAsAvailable() {
        Vehicle vehicle = new Vehicle("Ford", "Ducato", "GD 8032D", VehicleType.VAN, new BigDecimal("120.00"));
        vehicle.markAsBusy();

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        Vehicle availableVehicle = vehicleService.markAsAvailable(1L);

        assertTrue(availableVehicle.isActive());
        assertTrue(availableVehicle.isAvailable());

        verify(vehicleRepository).findById(1L);
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void shouldDeactivateVehicle() {
        Vehicle vehicle = new Vehicle("Ford", "Ducato", "GD 8032D", VehicleType.VAN, new BigDecimal("120.00"));

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        Vehicle inactiveVehicle = vehicleService.deactivate(1L);

        assertFalse(inactiveVehicle.isActive());
        assertFalse(inactiveVehicle.isAvailable());

        verify(vehicleRepository).findById(1L);
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void shouldActivateVehicle() {
        Vehicle vehicle = new Vehicle("Ford", "Ducato", "GD 8032D", VehicleType.VAN, new BigDecimal("120.00"));
        vehicle.deactivate();

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        Vehicle activeVehicle = vehicleService.activate(1L);

        assertTrue(activeVehicle.isActive());
        assertTrue(activeVehicle.isAvailable());

        verify(vehicleRepository).findById(1L);
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void shouldThrowWhenMarkingAsBusyAlreadyBusyVehicle() {
        Vehicle vehicle = new Vehicle("Ford", "Ducato", "GD 8032D", VehicleType.VAN, new BigDecimal("120.00"));
        vehicle.markAsBusy();

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        VehicleInvalidStateException exception = assertThrows(VehicleInvalidStateException.class,() -> vehicleService.markAsBusy(1L));

        assertEquals("Vehicle must be active and available to mark as busy.", exception.getMessage());
        assertTrue(vehicle.isActive());
        assertFalse(vehicle.isAvailable());

        verify(vehicleRepository).findById(1L);
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void shouldThrowWhenMarkingAsAvailableAlreadyAvailableVehicle() {
        Vehicle vehicle = new Vehicle("Ford", "Ducato", "GD 8032D", VehicleType.VAN, new BigDecimal("120.00"));

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        VehicleInvalidStateException exception = assertThrows(VehicleInvalidStateException.class,() -> vehicleService.markAsAvailable(1L));

        assertEquals("Vehicle must be active and busy to mark as available.", exception.getMessage());
        assertTrue(vehicle.isActive());
        assertTrue(vehicle.isAvailable());

        verify(vehicleRepository).findById(1L);
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void shouldThrowWhenActivatingAlreadyActiveVehicle() {
        Vehicle vehicle = new Vehicle("Ford", "Ducato", "GD 8032D", VehicleType.VAN, new BigDecimal("120.00"));

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        VehicleInvalidStateException exception = assertThrows(VehicleInvalidStateException.class,() -> vehicleService.activate(1L));

        assertEquals("Only inactive vehicle can be activated.", exception.getMessage());
        assertTrue(vehicle.isActive());
        assertTrue(vehicle.isAvailable());

        verify(vehicleRepository).findById(1L);
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void shouldThrowWhenDeactivatingAlreadyInactiveVehicle() {
        Vehicle vehicle = new Vehicle("Ford", "Ducato", "GD 8032D", VehicleType.VAN, new BigDecimal("120.00"));
        vehicle.deactivate();

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        VehicleInvalidStateException exception = assertThrows(VehicleInvalidStateException.class,() -> vehicleService.deactivate(1L));

        assertEquals("Only an active and available vehicle can be deactivated.", exception.getMessage());
        assertFalse(vehicle.isActive());
        assertFalse(vehicle.isAvailable());

        verify(vehicleRepository).findById(1L);
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void shouldThrowWhenDeactivatingBusyVehicle() {
        Vehicle vehicle = new Vehicle("Ford", "Ducato", "GD 8032D", VehicleType.VAN, new BigDecimal("120.00"));
        vehicle.markAsBusy();

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        VehicleInvalidStateException exception = assertThrows(VehicleInvalidStateException.class,() -> vehicleService.deactivate(1L));

        assertEquals("Only an active and available vehicle can be deactivated.", exception.getMessage());
        assertTrue(vehicle.isActive());
        assertFalse(vehicle.isAvailable());

        verify(vehicleRepository).findById(1L);
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void shouldUpdateVehicleStatusFromAvailableToBusy() {
        Vehicle vehicle = new Vehicle("Ford", "Ducato", "GD 8032D", VehicleType.VAN, new BigDecimal("120.00"));

        UpdateVehicleStatusRequest request = new UpdateVehicleStatusRequest();
        request.setStatus(VehicleStatus.BUSY);

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        Vehicle updatedVehicle = vehicleService.updateStatus(1L, request);

        assertTrue(updatedVehicle.isActive());
        assertFalse(updatedVehicle.isAvailable());

        verify(vehicleRepository).findById(1L);
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void shouldUpdateVehicleStatusFromBusyToAvailable() {
        Vehicle vehicle = new Vehicle("Ford", "Ducato", "GD 8032D", VehicleType.VAN, new BigDecimal("120.00"));
        vehicle.markAsBusy();

        UpdateVehicleStatusRequest request = new UpdateVehicleStatusRequest();
        request.setStatus(VehicleStatus.AVAILABLE);

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        Vehicle updatedVehicle = vehicleService.updateStatus(1L, request);

        assertTrue(updatedVehicle.isActive());
        assertTrue(updatedVehicle.isAvailable());

        verify(vehicleRepository).findById(1L);
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void shouldUpdateVehicleStatusFromInactiveToAvailable() {
        Vehicle vehicle = new Vehicle("Ford", "Ducato", "GD 8032D", VehicleType.VAN, new BigDecimal("120.00"));
        vehicle.deactivate();

        assertFalse(vehicle.isActive());
        assertFalse(vehicle.isAvailable());

        UpdateVehicleStatusRequest request = new UpdateVehicleStatusRequest();
        request.setStatus(VehicleStatus.AVAILABLE);

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        Vehicle updatedVehicle = vehicleService.updateStatus(1L, request);

        assertTrue(updatedVehicle.isActive());
        assertTrue(updatedVehicle.isAvailable());

        verify(vehicleRepository).findById(1L);
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void shouldUpdateVehicleStatusFromAvailableToInactive() {
        Vehicle vehicle = new Vehicle("Ford", "Ducato", "GD 8032D", VehicleType.VAN, new BigDecimal("120.00"));

        UpdateVehicleStatusRequest request = new UpdateVehicleStatusRequest();
        request.setStatus(VehicleStatus.INACTIVE);

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        Vehicle updatedVehicle = vehicleService.updateStatus(1L, request);

        assertFalse(updatedVehicle.isActive());
        assertFalse(updatedVehicle.isAvailable());

        verify(vehicleRepository).findById(1L);
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void shouldThrowWhenUpdatingAvailableVehicleToAvailable() {
        Vehicle vehicle = new Vehicle("Ford", "Ducato", "GD 8032D", VehicleType.VAN, new BigDecimal("120.00"));

        UpdateVehicleStatusRequest request = new UpdateVehicleStatusRequest();
        request.setStatus(VehicleStatus.AVAILABLE);

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        VehicleInvalidStateException exception = assertThrows(VehicleInvalidStateException.class, () -> vehicleService.updateStatus(1L, request));

        assertEquals("Vehicle is already available.", exception.getMessage());
        assertTrue(vehicle.isActive());
        assertTrue(vehicle.isAvailable());

        verify(vehicleRepository).findById(1L);
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void shouldThrowWhenUpdatingBusyVehicleToBusy() {
        Vehicle vehicle = new Vehicle("Ford", "Ducato", "GD 8032D", VehicleType.VAN, new BigDecimal("120.00"));
        vehicle.markAsBusy();

        UpdateVehicleStatusRequest request = new UpdateVehicleStatusRequest();
        request.setStatus(VehicleStatus.BUSY);

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        VehicleInvalidStateException exception = assertThrows(VehicleInvalidStateException.class, () -> vehicleService.updateStatus(1L, request));

        assertEquals("Vehicle is already busy.", exception.getMessage());
        assertTrue(vehicle.isActive());
        assertFalse(vehicle.isAvailable());

        verify(vehicleRepository).findById(1L);
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void shouldThrowWhenUpdatingInactiveVehicleToInactive() {
        Vehicle vehicle = new Vehicle("Ford", "Ducato", "GD 8032D", VehicleType.VAN, new BigDecimal("120.00"));
        vehicle.deactivate();

        UpdateVehicleStatusRequest request = new UpdateVehicleStatusRequest();
        request.setStatus(VehicleStatus.INACTIVE);

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        VehicleInvalidStateException exception = assertThrows(VehicleInvalidStateException.class, () -> vehicleService.updateStatus(1L, request));

        assertEquals("Vehicle is already inactive.", exception.getMessage());
        assertFalse(vehicle.isActive());
        assertFalse(vehicle.isAvailable());

        verify(vehicleRepository).findById(1L);
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void shouldThrowWhenUpdatingInactiveVehicleToBusy() {
        Vehicle vehicle = new Vehicle("Ford", "Ducato", "GD 8032D", VehicleType.VAN, new BigDecimal("120.00"));
        vehicle.deactivate();

        UpdateVehicleStatusRequest request = new UpdateVehicleStatusRequest();
        request.setStatus(VehicleStatus.BUSY);

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        VehicleInvalidStateException exception = assertThrows(VehicleInvalidStateException.class, () -> vehicleService.updateStatus(1L, request));
        assertEquals("Inactive vehicle cannot be marked as busy.", exception.getMessage());
        assertFalse(vehicle.isActive());
        assertFalse(vehicle.isAvailable());

        verify(vehicleRepository).findById(1L);
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void shouldThrowWhenUpdatingBusyVehicleToInactive() {
        Vehicle vehicle = new Vehicle("Ford", "Ducato", "GD 8032D", VehicleType.VAN, new BigDecimal("120.00"));
        vehicle.markAsBusy();

        UpdateVehicleStatusRequest request = new UpdateVehicleStatusRequest();
        request.setStatus(VehicleStatus.INACTIVE);

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        VehicleInvalidStateException exception = assertThrows(VehicleInvalidStateException.class, () -> vehicleService.updateStatus(1L, request));

        assertEquals("Busy vehicle cannot be deactivated.", exception.getMessage());
        assertTrue(vehicle.isActive());
        assertFalse(vehicle.isAvailable());

        verify(vehicleRepository).findById(1L);
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void shouldThrowWhenUpdateStatusRequestIsNull() {
        VehicleIllegalArgumentException exception = assertThrows(VehicleIllegalArgumentException.class, () -> vehicleService.updateStatus(1L, null));

        assertEquals("Update status request cannot be null.", exception.getMessage());

        verifyNoInteractions(vehicleRepository);
    }

    @Test
    void shouldThrowWhenVehicleStatusIsNull() {
        UpdateVehicleStatusRequest request = new UpdateVehicleStatusRequest();
        request.setStatus(null);

        VehicleIllegalArgumentException exception = assertThrows(VehicleIllegalArgumentException.class, () -> vehicleService.updateStatus(1L, request));

        assertEquals("Vehicle status cannot be null.", exception.getMessage());

        verifyNoInteractions(vehicleRepository);
    }
}
