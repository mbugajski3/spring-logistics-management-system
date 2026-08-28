package com.mbugajski.logistics.courier.repository;

import com.mbugajski.logistics.courier.entity.Courier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourierRepository extends JpaRepository<Courier, Long> {
    boolean existsByPhoneNumber(String phoneNumber);
    Optional<Courier> findFirstByAvailableTrue();
}
