package com.mbugajski.logistics.customer.controller;

import com.mbugajski.logistics.customer.service.CustomerService;
import com.mbugajski.logistics.customer.dto.request.CreateCustomerRequest;
import com.mbugajski.logistics.customer.dto.request.UpdateCustomerRequest;
import com.mbugajski.logistics.customer.entity.Customer;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public List<Customer> findAll() {
        return customerService.findAll();
    }

    @GetMapping("/{customerId}")
    public Customer findById(@PathVariable long customerId) {
        return customerService.findById(customerId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Customer create(@Valid @RequestBody CreateCustomerRequest customerRequest) {
        return customerService.create(customerRequest);
    }

    @DeleteMapping("/{customerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long customerId) {
        customerService.deleteById(customerId);
    }

    @PatchMapping("/{customerId}")
    public Customer update(@PathVariable long customerId, @Valid @RequestBody UpdateCustomerRequest updateCustomerRequest) {
        return customerService.update(customerId, updateCustomerRequest);
    }

    @PatchMapping("/{customerId}/activate")
    public Customer activate(@PathVariable long customerId) {
        return customerService.activate(customerId);
    }

    @PatchMapping("/{customerId}/deactivate")
    public Customer deactivate(@PathVariable long customerId) {
        return customerService.deactivate(customerId);
    }

}
