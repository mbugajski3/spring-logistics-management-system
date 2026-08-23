package com.mbugajski.logistics.shipment;

import com.mbugajski.logistics.address.dto.request.CreateAddressRequest;
import com.mbugajski.logistics.address.entity.Address;
import com.mbugajski.logistics.customer.entity.Customer;
import com.mbugajski.logistics.shipment.controller.ShipmentController;
import com.mbugajski.logistics.shipment.dto.request.CreateShipmentRequest;
import com.mbugajski.logistics.shipment.entity.Shipment;
import com.mbugajski.logistics.shipment.entity.ShipmentStatus;
import com.mbugajski.logistics.shipment.exception.ShipmentInvalidStatusException;
import com.mbugajski.logistics.shipment.exception.ShipmentNotFoundException;
import com.mbugajski.logistics.shipment.repository.ShipmentSortBy;
import com.mbugajski.logistics.shipment.service.ShipmentService;
import org.hibernate.query.SortDirection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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

    @Test
    void shouldReturnShipmentsPage() throws Exception {
        Page<Shipment> shipmentPage = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 20),
                0
        );

        when(shipmentService.findAllByPageNumber(0, 20,null, null, null, null)).thenReturn(shipmentPage);

        mockMvc.perform(get("/api/shipments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.hasPrevious").value(false));

        verify(shipmentService).findAllByPageNumber(0, 20, null, null, null, null);
    }

    @Test
    void shouldReturnShipmentsPageUsingQueryParams() throws Exception {
        Page<Shipment> shipmentPage = new PageImpl<>(
                List.of(),
                PageRequest.of(2, 10),
                35
        );

        when(shipmentService.findAllByPageNumber(2, 10, null, null, null, null)).thenReturn(shipmentPage);

        mockMvc.perform(get("/api/shipments?page=2&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(35))
                .andExpect(jsonPath("$.totalPages").value(4))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.hasPrevious").value(true));

        verify(shipmentService).findAllByPageNumber(2, 10, null, null, null, null);
    }

    @Test
    void shouldReturnPageWithShipment() throws Exception {
        Customer customer = new Customer(
                "Adrian",
                "Nowak",
                "adrian@nowak.com",
                "+48 782 230 124",
                createAddress()
        );

        Shipment shipment = createShipment(customer);

        ReflectionTestUtils.setField(customer, "id", 1L);
        ReflectionTestUtils.setField(shipment, "id", 2L);

        Page<Shipment> shipmentPage = new PageImpl<>(
                List.of(shipment),
                PageRequest.of(2, 10),
                35
        );

        when(shipmentService.findAllByPageNumber(2, 10, null, null, null, null)).thenReturn(shipmentPage);

        mockMvc.perform(get("/api/shipments?page=2&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(2L))
                .andExpect(jsonPath("$.content[0].customerId").value(1L))
                .andExpect(jsonPath("$.content[0].customerFirstName").value("Adrian"))
                .andExpect(jsonPath("$.content[0].customerLastName").value("Nowak"))
                .andExpect(jsonPath("$.content[0].pickupAddress.street").value("Odbiorowa"))
                .andExpect(jsonPath("$.content[0].pickupAddress.buildingNumber").value("10"))
                .andExpect(jsonPath("$.content[0].pickupAddress.apartmentNumber").value("2"))
                .andExpect(jsonPath("$.content[0].pickupAddress.city").value("Odbiór"))
                .andExpect(jsonPath("$.content[0].pickupAddress.postalCode").value("10-120"))
                .andExpect(jsonPath("$.content[0].pickupAddress.country").value("Poland"))
                .andExpect(jsonPath("$.content[0].deliveryAddress.street").value("Wysyłkowa"))
                .andExpect(jsonPath("$.content[0].deliveryAddress.buildingNumber").value("5"))
                .andExpect(jsonPath("$.content[0].deliveryAddress.apartmentNumber").value("3"))
                .andExpect(jsonPath("$.content[0].deliveryAddress.city").value("Wysyłka"))
                .andExpect(jsonPath("$.content[0].deliveryAddress.postalCode").value("90-192"))
                .andExpect(jsonPath("$.content[0].deliveryAddress.country").value("Poland"))
                .andExpect(jsonPath("$.content[0].weight").value(12.0))
                .andExpect(jsonPath("$.content[0].status").value("CREATED"))
                .andExpect(jsonPath("$.content[0].createdAt").exists())
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(35))
                .andExpect(jsonPath("$.totalPages").value(4))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.hasPrevious").value(true))
                .andExpect(jsonPath("$.pageable").doesNotExist())
                .andExpect(jsonPath("$.sort").doesNotExist());

        verify(shipmentService).findAllByPageNumber(2, 10, null, null, null, null);
    }

    @Test
    void shouldThrowWhenPageQueryParamIsInvalid() throws Exception {
        when(shipmentService.findAllByPageNumber(-1, 20, null, null, null, null))
                .thenThrow(new IllegalArgumentException("Page number cannot be negative."));

        mockMvc.perform(get("/api/shipments?page=-1&size=20"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Page number cannot be negative."))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(shipmentService).findAllByPageNumber(-1, 20, null, null, null, null);
    }

    @Test
    void shouldThrowWhenQueryPageSizeParamIsZero() throws Exception {
        when(shipmentService.findAllByPageNumber(0, 0, null, null, null, null))
                .thenThrow(new IllegalArgumentException("Page size must be greater than 0."));

        mockMvc.perform(get("/api/shipments?page=0&size=0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Page size must be greater than 0."))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(shipmentService).findAllByPageNumber(0, 0, null, null, null, null);
    }

    @Test
    void shouldThrowWhenQueryPageSizeParamIsOverLimit() throws Exception {
        when(shipmentService.findAllByPageNumber(0, 101, null, null, null, null))
                .thenThrow(new IllegalArgumentException("Page size cannot be more than 100."));

        mockMvc.perform(get("/api/shipments?page=0&size=101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Page size cannot be more than 100."))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(shipmentService).findAllByPageNumber(0, 101, null, null, null, null);
    }

    @Test
    void shouldReturnEmptyContentWhenPageIsNotInRange() throws Exception {
        Page<Shipment> shipmentPage = new PageImpl<>(
                List.of(),
                PageRequest.of(999, 20),
                35
        );

        when(shipmentService.findAllByPageNumber(999, 20, null, null, null, null)).thenReturn(shipmentPage);

        mockMvc.perform(get("/api/shipments?page=999&size=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());

        verify(shipmentService).findAllByPageNumber(999, 20, null, null, null, null);
    }

    @Test
    void shouldReturnShipmentsWithStatusCreated() throws Exception {
        Customer customer = new Customer(
                "Adrian",
                "Nowak",
                "adrian@nowak.com",
                "+48 782 230 124",
                createAddress()
        );

        Shipment shipment = createShipment(customer);

        Page<Shipment> shipmentPage = new PageImpl<>(
                List.of(shipment),
                PageRequest.of(0, 20),
                1
        );

        ReflectionTestUtils.setField(shipment, "id", 1L);
        ReflectionTestUtils.setField(customer, "id", 2L);


        when(shipmentService.findAllByPageNumber(0, 20, ShipmentStatus.CREATED, null, null, null)).thenReturn(shipmentPage);

        mockMvc.perform(get("/api/shipments?status=CREATED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].customerId").value(2L))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.hasPrevious").value(false))
                .andExpect(jsonPath("$.pageable").doesNotExist())
                .andExpect(jsonPath("$.sort").doesNotExist());

        verify(shipmentService).findAllByPageNumber(0, 20, ShipmentStatus.CREATED, null, null, null);
    }

    @Test
    void shouldReturnShipmentsByCustomerId() throws Exception {
        Customer customer = new Customer(
                "Adrian",
                "Nowak",
                "adrian@nowak.com",
                "+48 782 230 124",
                createAddress()
        );

        Shipment shipment = createShipment(customer);

        Page<Shipment> shipmentPage = new PageImpl<>(
                List.of(shipment),
                PageRequest.of(0, 20),
                1
        );

        ReflectionTestUtils.setField(shipment, "id", 1L);
        ReflectionTestUtils.setField(customer, "id", 2L);


        when(shipmentService.findAllByPageNumber(0, 20, null, 2L, null, null)).thenReturn(shipmentPage);

        mockMvc.perform(get("/api/shipments?customerId=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].customerId").value(2L))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.hasPrevious").value(false))
                .andExpect(jsonPath("$.pageable").doesNotExist())
                .andExpect(jsonPath("$.sort").doesNotExist());

        verify(shipmentService).findAllByPageNumber(0, 20, null, 2L, null, null);
    }

    @Test
    void shouldReturnShipmentsByCustomerIdAndStatusCreated() throws Exception {
        Customer customer = new Customer(
                "Adrian",
                "Nowak",
                "adrian@nowak.com",
                "+48 782 230 124",
                createAddress()
        );

        Shipment shipment = createShipment(customer);

        Page<Shipment> shipmentPage = new PageImpl<>(
                List.of(shipment),
                PageRequest.of(0, 20),
                1
        );

        ReflectionTestUtils.setField(shipment, "id", 1L);
        ReflectionTestUtils.setField(customer, "id", 2L);


        when(shipmentService.findAllByPageNumber(0, 20, ShipmentStatus.CREATED, 2L, null, null)).thenReturn(shipmentPage);

        mockMvc.perform(get("/api/shipments?status=CREATED&customerId=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].customerId").value(2L))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.hasPrevious").value(false))
                .andExpect(jsonPath("$.pageable").doesNotExist())
                .andExpect(jsonPath("$.sort").doesNotExist());

        verify(shipmentService).findAllByPageNumber(0, 20, ShipmentStatus.CREATED, 2L, null, null);
    }

    @Test
    void shouldReturnShipmentsSortedByWeightDescending() throws Exception {
        Customer customer = new Customer(
                "Adrian",
                "Nowak",
                "adrian@nowak.com",
                "+48 782 230 124",
                createAddress()
        );

        Shipment shipment = createShipment(customer);

        ReflectionTestUtils.setField(shipment, "id", 1L);
        ReflectionTestUtils.setField(customer, "id", 2L);

        Page<Shipment> shipmentPage = new PageImpl<>(
                List.of(shipment),
                PageRequest.of(0, 20),
                1
        );

        when(shipmentService.findAllByPageNumber(0, 20, null, null, ShipmentSortBy.weight, "desc")).thenReturn(shipmentPage);

        mockMvc.perform(get("/api/shipments?sortBy=weight&direction=desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].customerId").value(2L))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.hasPrevious").value(false));

        verify(shipmentService).findAllByPageNumber(0, 20, null, null, ShipmentSortBy.weight, "desc");
    }

    @Test
    void shouldReturnShipmentsSortedByPriceAscending() throws Exception {
        Customer customer = new Customer(
                "Adrian",
                "Nowak",
                "adrian@nowak.com",
                "+48 782 230 124",
                createAddress()
        );

        Shipment shipment = createShipment(customer);

        ReflectionTestUtils.setField(shipment, "id", 1L);
        ReflectionTestUtils.setField(customer, "id", 2L);

        Page<Shipment> shipmentPage = new PageImpl<>(
                List.of(shipment),
                PageRequest.of(0, 20),
                1
        );

        when(shipmentService.findAllByPageNumber(0, 20, ShipmentStatus.CREATED, null, ShipmentSortBy.price, "asc")).thenReturn(shipmentPage);

        mockMvc.perform(get("/api/shipments?status=CREATED&sortBy=price&direction=asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].customerId").value(2L))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.hasPrevious").value(false));

        verify(shipmentService).findAllByPageNumber(0, 20, ShipmentStatus.CREATED, null, ShipmentSortBy.price, "asc");
    }

    @Test
    void shouldThrowWhenSortParamGivenWithoutDirection() throws Exception {
        when(shipmentService.findAllByPageNumber(0, 20, null, null, ShipmentSortBy.weight, null)).thenThrow(new IllegalArgumentException("If sortBy exists, then direction cannot be null."));

        mockMvc.perform(get("/api/shipments?sortBy=weight"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("If sortBy exists, then direction cannot be null."));

        verify(shipmentService).findAllByPageNumber(0, 20, null, null, ShipmentSortBy.weight, null);
    }

    @Test
    void shouldThrowWhenDirectionGivenWithoutSortParam() throws Exception {
        when(shipmentService.findAllByPageNumber(0, 20, null, null, null, "desc")).thenThrow(new IllegalArgumentException("If sortBy dont exist, then direction is useless."));

        mockMvc.perform(get("/api/shipments?direction=desc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("If sortBy dont exist, then direction is useless."));

        verify(shipmentService).findAllByPageNumber(0, 20, null, null, null, "desc");
    }

    @Test
    void shouldThrowWhenDirectionGivenIsInvalid() throws Exception {
        when(shipmentService.findAllByPageNumber(0, 20, null, null, null, "sideways"))
                .thenThrow(new IllegalArgumentException("Direction can be only asc or desc."));

        mockMvc.perform(get("/api/shipments?direction=SIDEWAYS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Direction can be only asc or desc."));

        verify(shipmentService).findAllByPageNumber(0, 20, null, null, null, "sideways");
    }

    @Test
    void shouldThrowWhenSortParamIsInvalid() throws Exception {
        mockMvc.perform(get("/api/shipments?sortBy=banana&direction=desc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").exists());

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
