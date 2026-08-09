package com.mbugajski.logistics.customer.repository;

import com.mbugajski.logistics.customer.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
}
