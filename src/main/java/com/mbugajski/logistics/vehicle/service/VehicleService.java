package com.mbugajski.logistics.vehicle.service;

import com.mbugajski.logistics.vehicle.dto.request.CreateVehicleRequest;
import com.mbugajski.logistics.vehicle.entity.Vehicle;
import com.mbugajski.logistics.vehicle.entity.VehicleType;
import com.mbugajski.logistics.vehicle.exception.VehicleIllegalArgumentException;
import com.mbugajski.logistics.vehicle.exception.VehicleNotFoundException;
import com.mbugajski.logistics.vehicle.exception.VehicleRegistrationNumberAlreadyExistsException;
import com.mbugajski.logistics.vehicle.repository.VehicleRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        if (vehicleRepository == null) {
            throw new IllegalArgumentException("Vehicle repository cannot be null.");
        }
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional
    public Vehicle create(CreateVehicleRequest vehicleRequest) {
        if (vehicleRequest == null) {
            throw new VehicleIllegalArgumentException("Vehicle request cannot be null.");
        }

        String registrationNumber = vehicleRequest.getRegistrationNumber().trim();

        if (vehicleRepository.existsByRegistrationNumber(registrationNumber)) {
            throw new VehicleRegistrationNumberAlreadyExistsException(registrationNumber);
        }

        String brand = vehicleRequest.getBrand();
        String model = vehicleRequest.getModel();
        VehicleType vehicleType = vehicleRequest.getVehicleType();
        BigDecimal maximumLoad = vehicleRequest.getMaximumLoad();

        Vehicle requestedVehicle = new Vehicle(brand, model, registrationNumber, vehicleType, maximumLoad);

        return vehicleRepository.save(requestedVehicle);
    }

    public Vehicle findById(Long vehicleId) {
        if (vehicleId == null || vehicleId <= 0) {
            throw new VehicleIllegalArgumentException("Vehicle id cannot be null, 0 or below.");
        }

        return vehicleRepository.findById(vehicleId).orElseThrow(() -> new VehicleNotFoundException(vehicleId));
    }

    public List<Vehicle> findAll() {
        return vehicleRepository.findAll();
    }

    @Transactional
    public Vehicle markAsBusy(Long vehicleId) {
        Vehicle foundVehicle = findById(vehicleId);
        foundVehicle.markAsBusy();

        return foundVehicle;
    }

    @Transactional
    public Vehicle markAsAvailable(Long vehicleId) {
        Vehicle foundVehicle = findById(vehicleId);
        foundVehicle.markAsAvailable();

        return foundVehicle;
    }

    @Transactional
    public Vehicle deactivate(Long vehicleId) {
        Vehicle foundVehicle = findById(vehicleId);
        foundVehicle.deactivate();

        return foundVehicle;
    }

    @Transactional
    public Vehicle activate(Long vehicleId) {
        Vehicle foundVehicle = findById(vehicleId);
        foundVehicle.activate();

        return foundVehicle;
    }
}
