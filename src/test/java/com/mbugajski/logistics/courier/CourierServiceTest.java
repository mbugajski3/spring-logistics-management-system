package com.mbugajski.logistics.courier;

import com.mbugajski.logistics.courier.dto.request.CreateCourierRequest;
import com.mbugajski.logistics.courier.entity.Courier;
import com.mbugajski.logistics.courier.exception.CourierInvalidStateException;
import com.mbugajski.logistics.courier.exception.CourierNotFoundException;
import com.mbugajski.logistics.courier.exception.CourierPhoneNumberAlreadyExistsException;
import com.mbugajski.logistics.courier.repository.CourierRepository;
import com.mbugajski.logistics.courier.service.CourierService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CourierServiceTest {

    @Mock
    private CourierRepository courierRepository;

    @InjectMocks
    private CourierService courierService;

    @Test
    void shouldCreateCourier() {
        CreateCourierRequest courierRequest = new CreateCourierRequest();
        courierRequest.setFirstName("Adam");
        courierRequest.setLastName("Nowak");
        courierRequest.setPhoneNumber("+48 699 340 123");

        when(courierRepository.existsByPhoneNumber(courierRequest.getPhoneNumber())).thenReturn(false);
        when(courierRepository.save(any(Courier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Courier courierSaved = courierService.create(courierRequest);

        assertEquals("Adam", courierSaved.getFirstName());
        assertEquals("Nowak", courierSaved.getLastName());
        assertEquals("+48 699 340 123", courierSaved.getPhoneNumber());
        assertTrue(courierSaved.isActive());
        assertTrue(courierSaved.isAvailable());

        verify(courierRepository).existsByPhoneNumber("+48 699 340 123");
        verify(courierRepository).save(any(Courier.class));
    }

    @Test
    void shouldThrowWhenCourierPhoneNumberAlreadyExists() {
        CreateCourierRequest courierRequest = new CreateCourierRequest();
        courierRequest.setFirstName("Adam");
        courierRequest.setLastName("Nowak");
        courierRequest.setPhoneNumber("+48 699 340 123");

        when(courierRepository.existsByPhoneNumber(courierRequest.getPhoneNumber())).thenReturn(true);

        CourierPhoneNumberAlreadyExistsException exception = assertThrows(CourierPhoneNumberAlreadyExistsException.class, () -> courierService.create(courierRequest));

        assertEquals("Courier with this phone number already exists.", exception.getMessage());

        verify(courierRepository, never()).save(any(Courier.class));
        verify(courierRepository).existsByPhoneNumber("+48 699 340 123");
    }

    @Test
    void shouldThrowWhenCourierRequestIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> courierService.create(null));

        assertEquals("Courier request cannot be null.", exception.getMessage());

        verifyNoInteractions(courierRepository);
    }

    @Test
    void shouldReturnCourierById() {
        Courier courier = new Courier("Adrian", "Nowak", "+48 677 354 242");

        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

        Courier foundCourier = courierService.findById(1L);

        assertEquals("Adrian", foundCourier.getFirstName());
        assertEquals("Nowak", foundCourier.getLastName());
        assertEquals("+48 677 354 242", foundCourier.getPhoneNumber());

        verify(courierRepository).findById(1L);
    }

    @Test
    void shouldThrowWhenCourierNotFound() {
        when(courierRepository.findById(1L)).thenReturn(Optional.empty());

        CourierNotFoundException exception = assertThrows(CourierNotFoundException.class, () -> courierService.findById(1L));

        assertEquals("Courier with id 1 not found.", exception.getMessage());

        verify(courierRepository).findById(1L);
    }

    @Test
    void shouldThrowWhenCourierIdIsZero() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> courierService.findById(0L));

        assertEquals("ID cannot be null, 0 or below.", exception.getMessage());

        verifyNoInteractions(courierRepository);
    }

    @Test
    void shouldThrowWhenCourierIdIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> courierService.findById(null));

        assertEquals("ID cannot be null, 0 or below.", exception.getMessage());

        verifyNoInteractions(courierRepository);
    }

    @Test
    void shouldMarkCourierAsBusy() {
        Courier courier = new Courier("Adrian", "Nowak", "+48 677 354 242");

        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

        Courier courierFound = courierService.markAsBusy(1L);

        assertFalse(courierFound.isAvailable());
        assertTrue(courierFound.isActive());

        verify(courierRepository).findById(1L);
        verify(courierRepository, never()).save(any(Courier.class));
    }

    @Test
    void shouldMarkCourierAsAvailable() {
        Courier courier = new Courier("Adrian", "Nowak", "+48 677 354 242");
        courier.markAsBusy();

        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

        Courier courierFound = courierService.markAsAvailable(1L);

        assertTrue(courierFound.isAvailable());
        assertTrue(courierFound.isActive());

        verify(courierRepository).findById(1L);
        verify(courierRepository, never()).save(any(Courier.class));
    }

    @Test
    void shouldDeactivateCourier() {
        Courier courier = new Courier("Adrian", "Nowak", "+48 677 354 242");

        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

        Courier courierFound = courierService.deactivate(1L);

        assertFalse(courierFound.isAvailable());
        assertFalse(courierFound.isActive());

        verify(courierRepository).findById(1L);
        verify(courierRepository, never()).save(any(Courier.class));
    }

    @Test
    void shouldActivateCourier() {
        Courier courier = new Courier("Adrian", "Nowak", "+48 677 354 242");
        courier.deactivate();

        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

        Courier courierFound = courierService.activate(1L);

        assertTrue(courierFound.isAvailable());
        assertTrue(courierFound.isActive());

        verify(courierRepository).findById(1L);
        verify(courierRepository, never()).save(any(Courier.class));
    }

    @Test
    void shouldThrowWhenMarkingAlreadyBusyCourierAsBusy() {
        Courier courier = new Courier("Adrian", "Nowak", "+48 677 354 242");
        courier.markAsBusy();

        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

        CourierInvalidStateException exception = assertThrows(CourierInvalidStateException.class, () -> courierService.markAsBusy(1L));

        assertEquals("Courier must be active and available to mark as busy.", exception.getMessage());
        assertFalse(courier.isAvailable());
        assertTrue(courier.isActive());

        verify(courierRepository).findById(1L);
        verify(courierRepository, never()).save(any(Courier.class));
    }

    @Test
    void shouldThrowWhenMarkingAvailableCourierAsAvailable() {
        Courier courier = new Courier("Adrian", "Nowak", "+48 677 354 242");

        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

        CourierInvalidStateException exception = assertThrows(CourierInvalidStateException.class, () -> courierService.markAsAvailable(1L));

        assertEquals("Courier must be active and busy to mark as available.", exception.getMessage());
        assertTrue(courier.isAvailable());
        assertTrue(courier.isActive());

        verify(courierRepository).findById(1L);
        verify(courierRepository, never()).save(any(Courier.class));
    }

    @Test
    void shouldThrowWhenDeactivatingBusyCourier() {
        Courier courier = new Courier("Adrian", "Nowak", "+48 677 354 242");
        courier.markAsBusy();

        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

        CourierInvalidStateException exception = assertThrows(CourierInvalidStateException.class, () -> courierService.deactivate(1L));

        assertEquals("Courier must be active and not busy to deactivate.", exception.getMessage());
        assertFalse(courier.isAvailable());
        assertTrue(courier.isActive());

        verify(courierRepository).findById(1L);
        verify(courierRepository, never()).save(any(Courier.class));
    }

    @Test
    void shouldThrowWhenActivatingAlreadyActiveCourier() {
        Courier courier = new Courier("Adrian", "Nowak", "+48 677 354 242");

        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

        CourierInvalidStateException exception = assertThrows(CourierInvalidStateException.class, () -> courierService.activate(1L));

        assertEquals("Only inactive courier can be activated.", exception.getMessage());
        assertTrue(courier.isAvailable());
        assertTrue(courier.isActive());

        verify(courierRepository).findById(1L);
        verify(courierRepository, never()).save(any(Courier.class));
    }
}
