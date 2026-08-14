package com.mbugajski.logistics.courier;

import com.mbugajski.logistics.courier.entity.Courier;
import com.mbugajski.logistics.courier.exception.CourierNotFoundException;
import com.mbugajski.logistics.courier.repository.CourierRepository;
import com.mbugajski.logistics.shipment.repository.ShipmentRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DataJpaTest
public class CourierRepositoryTest {

    @Autowired
    private CourierRepository courierRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldSaveCourier() {
        Courier courier = new Courier("Listonosz", "Pat", "+48 593 239 402");

        Courier savedCourier = courierRepository.save(courier);
        courierRepository.flush();
        entityManager.clear();

        Long courierId = savedCourier.getId();

        Courier foundCourier = courierRepository.findById(courierId).orElseThrow();

        assertNotNull(foundCourier.getId());
        assertEquals("Listonosz", foundCourier.getFirstName());
        assertEquals("Pat", foundCourier.getLastName());
        assertEquals("+48 593 239 402", foundCourier.getPhoneNumber());
        assertTrue(foundCourier.isActive());
        assertTrue(foundCourier.isAvailable());
    }

    @Test
    void shouldPersistCourierAvailabilityChange() {
        Courier courier = new Courier("Listonosz", "Pat", "+48 593 239 402");

        Courier savedCourier = courierRepository.save(courier);
        Long courierId = savedCourier.getId();

        savedCourier.markAsBusy();

        courierRepository.flush();
        entityManager.clear();

        Courier foundCourier = courierRepository.findById(courierId).orElseThrow();

        assertNotNull(foundCourier.getId());
        assertEquals("Listonosz", foundCourier.getFirstName());
        assertEquals("Pat", foundCourier.getLastName());
        assertEquals("+48 593 239 402", foundCourier.getPhoneNumber());
        assertTrue(foundCourier.isActive());
        assertFalse(foundCourier.isAvailable());
    }

    @Test
    void shouldThrowWhenSavingCourierWithDuplicatePhoneNumber() {
        Courier courier1 = new Courier("Listonosz", "Pat", "+48 593 239 402");
        Courier courier2 = new Courier("Example", "Test", "+48 593 239 402");

        courierRepository.save(courier1);

        assertThrows(DataIntegrityViolationException.class, () -> courierRepository.saveAndFlush(courier2));
    }
}
