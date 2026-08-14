package com.mbugajski.logistics.shipment;

import com.mbugajski.logistics.address.dto.request.CreateAddressRequest;
import com.mbugajski.logistics.address.entity.Address;
import com.mbugajski.logistics.customer.entity.Customer;
import com.mbugajski.logistics.shipment.controller.ShipmentController;
import com.mbugajski.logistics.shipment.dto.request.CreateShipmentRequest;
import com.mbugajski.logistics.shipment.entity.Shipment;
import com.mbugajski.logistics.shipment.exception.ShipmentInvalidStatusException;
import com.mbugajski.logistics.shipment.exception.ShipmentNotFoundException;
import com.mbugajski.logistics.shipment.service.ShipmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;
import java.math.BigDecimal;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShipmentController.class)
public class ShipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShipmentService shipmentService;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void shouldReturnAllShipments() throws Exception {
        Customer customer1 = new Customer("Adrian", "Nowak", "adrian@nowak.com", "+48 782 230 124", createAddress());
        Customer customer2 = new Customer("Janusz", "Lis", "janusz@lis.com", "+48 664 231 421", createAddress());
        Shipment shipment1 = createShipment(customer1);
        Shipment shipment2 = createShipment(customer2);

        ReflectionTestUtils.setField(customer1, "id", 1L);
        ReflectionTestUtils.setField(customer2, "id", 2L);

        ReflectionTestUtils.setField(shipment1, "id", 1L);
        ReflectionTestUtils.setField(shipment2, "id", 2L);

        List<Shipment> shipmentList = List.of(shipment1, shipment2);

        when(shipmentService.findAll()).thenReturn(shipmentList);

        mockMvc.perform(get("/api/shipments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].customerId").value(1))
                .andExpect(jsonPath("$[0].customerFirstName").value("Adrian"))
                .andExpect(jsonPath("$[0].customerLastName").value("Nowak"))
                .andExpect(jsonPath("$[0].pickupAddress.street").value("Odbiorowa"))
                .andExpect(jsonPath("$[0].deliveryAddress.street").value("Wysyłkowa"))
                .andExpect(jsonPath("$[0].weight").value(12.0))
                .andExpect(jsonPath("$[0].status").value("CREATED"))
                .andExpect(jsonPath("$[0].price").value(35.0))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].customerId").value(2))
                .andExpect(jsonPath("$[1].customerFirstName").value("Janusz"))
                .andExpect(jsonPath("$[1].customerLastName").value("Lis"))
                .andExpect(jsonPath("$[1].pickupAddress.street").value("Odbiorowa"))
                .andExpect(jsonPath("$[1].deliveryAddress.street").value("Wysyłkowa"))
                .andExpect(jsonPath("$[1].weight").value(12.0))
                .andExpect(jsonPath("$[1].status").value("CREATED"))
                .andExpect(jsonPath("$[1].price").value(35.0));

        verify(shipmentService).findAll();
    }

    @Test
    void shouldReturnShipmentById() throws Exception {
        Customer customer = new Customer("Adrian", "Nowak", "adrian@nowak.com", "+48 782 230 124", createAddress());
        Shipment shipment = createShipment(customer);

        ReflectionTestUtils.setField(customer, "id", 1L);
        ReflectionTestUtils.setField(shipment, "id", 1L);

        when(shipmentService.findById(1L)).thenReturn(shipment);

        mockMvc.perform(get("/api/shipments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.customerId").value(1))
                .andExpect(jsonPath("$.customerFirstName").value("Adrian"))
                .andExpect(jsonPath("$.customerLastName").value("Nowak"))
                .andExpect(jsonPath("$.pickupAddress.street").value("Odbiorowa"))
                .andExpect(jsonPath("$.deliveryAddress.street").value("Wysyłkowa"))
                .andExpect(jsonPath("$.weight").value(12.0))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.price").value(35.0));

        verify(shipmentService).findById(1L);
    }

    @Test
    void shouldCreateShipment() throws Exception {
        Customer customer = new Customer("Adrian", "Nowak", "adrian@nowak.com", "+48 782 230 124", createAddress());
        CreateAddressRequest pickupAddress = createPickupAddressRequest();
        CreateAddressRequest deliveryAddress = createDeliveryAddressRequest();
        CreateShipmentRequest shipmentRequest = new CreateShipmentRequest();

        Shipment shipment = new Shipment(customer, mapToAddress(pickupAddress), mapToAddress(deliveryAddress), new BigDecimal("12.00"));

        ReflectionTestUtils.setField(customer, "id", 1L);
        ReflectionTestUtils.setField(shipment, "id", 1L);

        shipmentRequest.setCustomerId(1L);
        shipmentRequest.setPickupAddress(pickupAddress);
        shipmentRequest.setDeliveryAddress(deliveryAddress);
        shipmentRequest.setWeight(new BigDecimal("12.00"));

        when(shipmentService.create(any(CreateShipmentRequest.class))).thenReturn(shipment);

        String requestJson = jsonMapper.writeValueAsString(shipmentRequest);

        mockMvc.perform(post("/api/shipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.customerId").value(1))
                .andExpect(jsonPath("$.customerFirstName").value("Adrian"))
                .andExpect(jsonPath("$.customerLastName").value("Nowak"))
                .andExpect(jsonPath("$.pickupAddress.street").value("Odbiorowa"))
                .andExpect(jsonPath("$.deliveryAddress.street").value("Wysyłkowa"))
                .andExpect(jsonPath("$.weight").value(12.0))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.price").value(35.0));

        verify(shipmentService).create(any(CreateShipmentRequest.class));
    }

    @Test
    void shouldChangeStatusToReadyForPickup() throws Exception {
        Customer customer = new Customer(
                "Adrian",
                "Nowak",
                "adrian@nowak.com",
                "+48 782 230 124",
                createAddress()
        );

        Shipment shipment = createShipment(customer);
        ReflectionTestUtils.setField(shipment, "id", 1L);

        shipment.markAsReadyForPickup();

        when(shipmentService.markAsReadyForPickup(1L))
                .thenReturn(shipment);

        mockMvc.perform(patch("/api/shipments/1/ready-for-pickup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("READY_FOR_PICKUP"));

        verify(shipmentService).markAsReadyForPickup(1L);
    }

    @Test
    void shouldChangeStatusToInTransit() throws Exception {
        Customer customer = new Customer(
                "Adrian",
                "Nowak",
                "adrian@nowak.com",
                "+48 782 230 124",
                createAddress()
        );

        Shipment shipment = createShipment(customer);
        ReflectionTestUtils.setField(shipment, "id", 1L);

        shipment.markAsReadyForPickup();
        shipment.markAsInTransit();

        when(shipmentService.markAsInTransit(1L))
                .thenReturn(shipment);

        mockMvc.perform(patch("/api/shipments/1/in-transit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("IN_TRANSIT"));

        verify(shipmentService).markAsInTransit(1L);
    }

    @Test
    void shouldChangeStatusToDelivered() throws Exception {
        Customer customer = new Customer(
                "Adrian",
                "Nowak",
                "adrian@nowak.com",
                "+48 782 230 124",
                createAddress()
        );

        Shipment shipment = createShipment(customer);
        ReflectionTestUtils.setField(shipment, "id", 1L);

        shipment.markAsReadyForPickup();
        shipment.markAsInTransit();
        shipment.markAsDelivered();

        when(shipmentService.markAsDelivered(1L))
                .thenReturn(shipment);

        mockMvc.perform(patch("/api/shipments/1/delivered"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("DELIVERED"));

        verify(shipmentService).markAsDelivered(1L);
    }

    @Test
    void shouldChangeStatusToCancelled() throws Exception {
        Customer customer = new Customer(
                "Adrian",
                "Nowak",
                "adrian@nowak.com",
                "+48 782 230 124",
                createAddress()
        );

        Shipment shipment = createShipment(customer);
        ReflectionTestUtils.setField(shipment, "id", 1L);

        shipment.markAsReadyForPickup();
        shipment.markAsCancelled();

        when(shipmentService.markAsCancelled(1L))
                .thenReturn(shipment);

        mockMvc.perform(patch("/api/shipments/1/cancelled"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(shipmentService).markAsCancelled(1L);
    }

    @Test
    void shouldReturnNotFoundWhenShipmentDoesNotExist() throws Exception {
        when(shipmentService.findById(999L)).thenThrow(new ShipmentNotFoundException(999L));

        mockMvc.perform(get("/api/shipments/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Shipment with id 999 not found."))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(shipmentService).findById(999L);
    }

    @Test
    void shouldReturnConflictWhenShipmentHasInvalidStatus() throws Exception {
        when(shipmentService.markAsDelivered(1L)).thenThrow(new ShipmentInvalidStatusException("Only a shipment with status 'IN_TRANSIT' can be marked as 'DELIVERED'."));

        mockMvc.perform(patch("/api/shipments/1/delivered"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Only a shipment with status 'IN_TRANSIT' can be marked as 'DELIVERED'."))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(shipmentService).markAsDelivered(1L);
    }

    @Test
    void shouldReturnBadRequestWhenShipmentWeightExceedsLimit() throws Exception {
        CreateShipmentRequest shipmentRequest = new CreateShipmentRequest();

        shipmentRequest.setCustomerId(1L);
        shipmentRequest.setPickupAddress(createPickupAddressRequest());
        shipmentRequest.setDeliveryAddress(createDeliveryAddressRequest());
        shipmentRequest.setWeight(new BigDecimal("25.00"));

        String requestJson = jsonMapper.writeValueAsString(shipmentRequest);

        mockMvc.perform(post("/api/shipments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("weight: Weight cannot exceed 20 kg."))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(shipmentService);
    }

    public CreateAddressRequest createPickupAddressRequest() {
        CreateAddressRequest request = new CreateAddressRequest();

        request.setStreet("Odbiorowa");
        request.setBuildingNumber("10");
        request.setApartmentNumber("2");
        request.setCity("Odbiór");
        request.setPostalCode("10-120");
        request.setCountry("Poland");

        return request;
    }

    public CreateAddressRequest createDeliveryAddressRequest() {
        CreateAddressRequest request = new CreateAddressRequest();

        request.setStreet("Wysyłkowa");
        request.setBuildingNumber("5");
        request.setApartmentNumber("3");
        request.setCity("Wysyłka");
        request.setPostalCode("90-192");
        request.setCountry("Poland");

        return request;
    }

    public Address createAddress() {
        return new Address("Klientowa", "12", "23", "Poznań", "90-231", "Poland");
    }

    public Address mapToAddress(CreateAddressRequest request) {
        return new Address(
                request.getStreet(),
                request.getBuildingNumber(),
                request.getApartmentNumber(),
                request.getCity(),
                request.getPostalCode(),
                request.getCountry()
        );
    }

    public Shipment createShipment(Customer customer) {
        CreateAddressRequest requestPickupAddress = createPickupAddressRequest();
        CreateAddressRequest requestDeliveryAddress = createDeliveryAddressRequest();

        return new Shipment(customer, mapToAddress(requestPickupAddress), mapToAddress(requestDeliveryAddress), new BigDecimal("12.00"));

    }
}
