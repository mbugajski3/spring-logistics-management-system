package com.mbugajski.logistics.courier.service;

import com.mbugajski.logistics.courier.dto.request.CreateCourierRequest;
import com.mbugajski.logistics.courier.entity.Courier;
import com.mbugajski.logistics.courier.exception.CourierNotFoundException;
import com.mbugajski.logistics.courier.exception.CourierPhoneNumberAlreadyExistsException;
import com.mbugajski.logistics.courier.repository.CourierRepository;
import org.springframework.stereotype.Service;

@Service
public class CourierService {

    private final CourierRepository courierRepository;

    public CourierService(CourierRepository courierRepository) {
        this.courierRepository = courierRepository;
    }

    public Courier create(CreateCourierRequest courierRequest) {
        if (courierRequest == null) {
            throw new IllegalArgumentException("Courier request cannot be null.");
        }

        String requestFirstName = courierRequest.getFirstName().trim();
        String requestLastName = courierRequest.getLastName().trim();
        String requestPhoneNumber = courierRequest.getPhoneNumber().trim();

        if (courierRepository.existsByPhoneNumber(requestPhoneNumber)) {
            throw new CourierPhoneNumberAlreadyExistsException();
        }

        Courier mappedCourier = new Courier(requestFirstName, requestLastName, requestPhoneNumber);

        return courierRepository.save(mappedCourier);
    }

    public Courier findById(Long courierId) {
        if (courierId == null || courierId <= 0) {
            throw new IllegalArgumentException("ID cannot be null, 0 or below.");
        }

        return courierRepository
                .findById(courierId)
                .orElseThrow(() -> new CourierNotFoundException(courierId));
    }
}
