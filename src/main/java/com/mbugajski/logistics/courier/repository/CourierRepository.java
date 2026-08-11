package com.mbugajski.logistics.courier.repository;

import com.mbugajski.logistics.courier.entity.Courier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourierRepository extends JpaRepository<Courier, Long> {
}
