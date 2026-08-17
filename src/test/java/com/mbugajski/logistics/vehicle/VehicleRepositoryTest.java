package com.mbugajski.logistics.vehicle;

import com.mbugajski.logistics.vehicle.entity.Vehicle;
import com.mbugajski.logistics.vehicle.entity.VehicleType;
import com.mbugajski.logistics.vehicle.repository.VehicleRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class VehicleRepositoryTest {

    @Autowired
    public VehicleRepository vehicleRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldSaveVehicle() {
        Vehicle vehicle = new Vehicle("Ford", "Transit", "GD 0406H", VehicleType.VAN, new BigDecimal("30"));

        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        vehicleRepository.flush();
        entityManager.clear();

        Vehicle foundVehicle = vehicleRepository.findById(savedVehicle.getId()).orElseThrow();

        assertNotNull(foundVehicle.getId());
        assertEquals("Ford", foundVehicle.getBrand());
        assertEquals("Transit", foundVehicle.getModel());
        assertEquals("GD 0406H", foundVehicle.getRegistrationNumber());
        assertEquals(VehicleType.VAN, foundVehicle.getVehicleType());
        assertEquals(new BigDecimal("30.00"), foundVehicle.getMaximumLoad());
        assertTrue(foundVehicle.isActive());
        assertTrue(foundVehicle.isAvailable());
    }

    @Test
    void shouldPersistVehicleAvailabilityChange() {
        Vehicle vehicle = new Vehicle("Ford", "Transit", "GD 0406H", VehicleType.VAN, new BigDecimal("30"));

        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        vehicleRepository.flush();
        entityManager.clear();

        Vehicle foundVehicle = vehicleRepository.findById(savedVehicle.getId()).orElseThrow();
        foundVehicle.markAsBusy();

        vehicleRepository.flush();
        entityManager.clear();

        Vehicle markedVehicle = vehicleRepository.findById(foundVehicle.getId()).orElseThrow();

        assertNotNull(markedVehicle.getId());
        assertTrue(markedVehicle.isActive());
        assertFalse(markedVehicle.isAvailable());
    }

    @Test
    void shouldThrowWhenSavingVehicleWithDuplicateRegistrationNumber() {
        Vehicle vehicle1 = new Vehicle("Ford", "Transit", "GD 0406H", VehicleType.VAN, new BigDecimal("30"));
        Vehicle vehicle2 = new Vehicle("Fiat", "Ducato", "GD 0406H", VehicleType.VAN, new BigDecimal("50"));

        vehicleRepository.saveAndFlush(vehicle1);

        assertThrows(DataIntegrityViolationException.class, () -> vehicleRepository.saveAndFlush(vehicle2));
    }
}
