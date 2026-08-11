package com.mbugajski.logistics.courier;

import com.mbugajski.logistics.courier.dto.request.CreateCourierRequest;
import com.mbugajski.logistics.courier.entity.Courier;
import com.mbugajski.logistics.courier.exception.CourierPhoneNumberAlreadyExistsException;
import com.mbugajski.logistics.courier.repository.CourierRepository;
import com.mbugajski.logistics.courier.service.CourierService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
}
