package com.mbugajski.logistics.address.repository;

import com.mbugajski.logistics.address.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
}
