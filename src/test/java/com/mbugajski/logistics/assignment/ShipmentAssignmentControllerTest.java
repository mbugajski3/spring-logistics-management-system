package com.mbugajski.logistics.assignment;

import com.mbugajski.logistics.address.entity.Address;
import com.mbugajski.logistics.assignment.controller.ShipmentAssignmentController;
import com.mbugajski.logistics.assignment.dto.request.CreateShipmentAssignmentRequest;
import com.mbugajski.logistics.assignment.entity.AssignmentStatus;
import com.mbugajski.logistics.assignment.entity.ShipmentAssignment;
import com.mbugajski.logistics.assignment.exception.ActiveAssignmentNotFoundException;
import com.mbugajski.logistics.assignment.exception.AssignmentParameterInvalidStatus;
import com.mbugajski.logistics.assignment.exception.AssignmentVehicleOutOfSpaceException;
import com.mbugajski.logistics.assignment.service.ShipmentAssignmentService;
import com.mbugajski.logistics.common.exception.GlobalExceptionHandler;
import com.mbugajski.logistics.courier.dto.request.CreateCourierRequest;
import com.mbugajski.logistics.courier.entity.Courier;
import com.mbugajski.logistics.courier.exception.CourierInvalidStateException;
import com.mbugajski.logistics.courier.exception.CourierNotFoundException;
import com.mbugajski.logistics.customer.entity.Customer;
import com.mbugajski.logistics.shipment.entity.Shipment;
import com.mbugajski.logistics.shipment.exception.ShipmentInvalidStatusException;
import com.mbugajski.logistics.shipment.exception.ShipmentNotFoundException;
import com.mbugajski.logistics.vehicle.entity.Vehicle;
import com.mbugajski.logistics.vehicle.entity.VehicleType;
import com.mbugajski.logistics.vehicle.exception.VehicleInvalidStateException;
import com.mbugajski.logistics.vehicle.exception.VehicleNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;

@WebMvcTest(ShipmentAssignmentController.class)
@Import(GlobalExceptionHandler.class)
public class ShipmentAssignmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShipmentAssignmentService shipmentAssignmentService;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void shouldAssignShipment() throws Exception {
        Shipment shipment = createShipment();
        Courier courier = createCourier();
        Vehicle vehicle = createVehicle();

        ShipmentAssignment shipmentAssignment = new ShipmentAssignment(shipment, courier, vehicle);

        ReflectionTestUtils.setField(shipmentAssignment, "id", 4L);

        CreateShipmentAssignmentRequest shipmentAssignmentRequest = new CreateShipmentAssignmentRequest();
        ReflectionTestUtils.setField(shipment, "id", 1L);
        ReflectionTestUtils.setField(courier, "id", 2L);
        ReflectionTestUtils.setField(vehicle, "id", 3L);
        shipmentAssignmentRequest.setCourierId(2L);
        shipmentAssignmentRequest.setVehicleId(3L);

        when(shipmentAssignmentService.assign(1L, 2L, 3L)).thenReturn(shipmentAssignment);

        String jsonRequest = jsonMapper.writeValueAsString(shipmentAssignmentRequest);

        mockMvc.perform(post("/api/shipments/1/assignment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.assignmentId").value(4L))
                .andExpect(jsonPath("$.shipmentId").value(1L))
                .andExpect(jsonPath("$.courierId").value(2L))
                .andExpect(jsonPath("$.vehicleId").value(3L))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.assignedAt").exists())
                .andExpect(jsonPath("$.finishedAt").isEmpty());

        verify(shipmentAssignmentService).assign(1L, 2L, 3L);
    }

    @Test
    void shouldReturnBadRequestWhenCourierIdIsNull() throws Exception {
        CreateShipmentAssignmentRequest shipmentAssignmentRequest = new CreateShipmentAssignmentRequest();
        shipmentAssignmentRequest.setCourierId(null);
        shipmentAssignmentRequest.setVehicleId(3L);

        String jsonRequest = jsonMapper.writeValueAsString(shipmentAssignmentRequest);

        mockMvc.perform(post("/api/shipments/1/assignment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.name()))
                .andExpect(jsonPath("$.message").value("courierId: Courier ID cannot be null."))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(shipmentAssignmentService);
    }

    @Test
    void shouldReturnBadRequestWhenCourierIdIsNotPositive () throws Exception {
        CreateShipmentAssignmentRequest shipmentAssignmentRequest = new CreateShipmentAssignmentRequest();
        shipmentAssignmentRequest.setCourierId(-1L);
        shipmentAssignmentRequest.setVehicleId(3L);

        String jsonRequest = jsonMapper.writeValueAsString(shipmentAssignmentRequest);

        mockMvc.perform(post("/api/shipments/1/assignment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.name()))
                .andExpect(jsonPath("$.message").value("courierId: Courier ID must be positive."))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(shipmentAssignmentService);
    }

    @Test
    void shouldReturnBadRequestWhenVehicleIdIsNull() throws Exception {
        CreateShipmentAssignmentRequest shipmentAssignmentRequest = new CreateShipmentAssignmentRequest();
        shipmentAssignmentRequest.setCourierId(1L);
        shipmentAssignmentRequest.setVehicleId(null);

        String jsonRequest = jsonMapper.writeValueAsString(shipmentAssignmentRequest);

        mockMvc.perform(post("/api/shipments/1/assignment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.name()))
                .andExpect(jsonPath("$.message").value("vehicleId: Vehicle ID cannot be null."))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(shipmentAssignmentService);
    }

    @Test
    void shouldReturnBadRequestWhenVehicleIdIsNotPositive () throws Exception {
        CreateShipmentAssignmentRequest shipmentAssignmentRequest = new CreateShipmentAssignmentRequest();
        shipmentAssignmentRequest.setCourierId(3L);
        shipmentAssignmentRequest.setVehicleId(-1L);

        String jsonRequest = jsonMapper.writeValueAsString(shipmentAssignmentRequest);

        mockMvc.perform(post("/api/shipments/1/assignment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.name()))
                .andExpect(jsonPath("$.message").value("vehicleId: Vehicle ID must be positive."))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(shipmentAssignmentService);
    }

    @Test
    void shouldReturnNotFoundWhenShipmentDoesNotExist() throws Exception {
        CreateShipmentAssignmentRequest shipmentAssignmentRequest = new CreateShipmentAssignmentRequest();
        shipmentAssignmentRequest.setCourierId(2L);
        shipmentAssignmentRequest.setVehicleId(3L);

        when(shipmentAssignmentService.assign(1L, 2L, 3L)).thenThrow(new ShipmentNotFoundException(1L));

        String jsonRequest = jsonMapper.writeValueAsString(shipmentAssignmentRequest);

        mockMvc.perform(post("/api/shipments/1/assignment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.error").value(HttpStatus.NOT_FOUND.name()))
                .andExpect(jsonPath("$.message").value("Shipment with id 1 not found."))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(shipmentAssignmentService).assign(1L, 2L, 3L);
    }

    @Test
    void shouldReturnNotFoundWhenCourierDoesNotExist() throws Exception {
        CreateShipmentAssignmentRequest shipmentAssignmentRequest = new CreateShipmentAssignmentRequest();
        shipmentAssignmentRequest.setCourierId(2L);
        shipmentAssignmentRequest.setVehicleId(3L);

        when(shipmentAssignmentService.assign(1L, 2L, 3L)).thenThrow(new CourierNotFoundException(2L));

        String jsonRequest = jsonMapper.writeValueAsString(shipmentAssignmentRequest);

        mockMvc.perform(post("/api/shipments/1/assignment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.error").value(HttpStatus.NOT_FOUND.name()))
                .andExpect(jsonPath("$.message").value("Courier with id 2 not found."))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(shipmentAssignmentService).assign(1L, 2L, 3L);
    }

    @Test
    void shouldReturnNotFoundWhenVehicleDoesNotExist() throws Exception {
        CreateShipmentAssignmentRequest shipmentAssignmentRequest = new CreateShipmentAssignmentRequest();
        shipmentAssignmentRequest.setCourierId(2L);
        shipmentAssignmentRequest.setVehicleId(3L);

        when(shipmentAssignmentService.assign(1L, 2L, 3L)).thenThrow(new VehicleNotFoundException(3L));

        String jsonRequest = jsonMapper.writeValueAsString(shipmentAssignmentRequest);

        mockMvc.perform(post("/api/shipments/1/assignment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.error").value(HttpStatus.NOT_FOUND.name()))
                .andExpect(jsonPath("$.message").value("Vehicle with id 3 not found."))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(shipmentAssignmentService).assign(1L, 2L, 3L);
    }

    @Test
    void shouldReturnConflictWhenShipmentCannotBeAssignedDueToStatus() throws Exception {
        CreateShipmentAssignmentRequest shipmentAssignmentRequest = new CreateShipmentAssignmentRequest();
        shipmentAssignmentRequest.setCourierId(2L);
        shipmentAssignmentRequest.setVehicleId(3L);

        when(shipmentAssignmentService.assign(1L, 2L, 3L)).thenThrow(new AssignmentParameterInvalidStatus("Shipment status must be 'CREATED' to assign."));

        String jsonRequest = jsonMapper.writeValueAsString(shipmentAssignmentRequest);

        mockMvc.perform(post("/api/shipments/1/assignment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()))
                .andExpect(jsonPath("$.error").value(HttpStatus.CONFLICT.name()))
                .andExpect(jsonPath("$.message").value("Shipment status must be 'CREATED' to assign."))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(shipmentAssignmentService).assign(1L, 2L, 3L);
    }

    @Test
    void shouldReturnConflictWhenVehicleOutOfSpace() throws Exception {
        CreateShipmentAssignmentRequest shipmentAssignmentRequest = new CreateShipmentAssignmentRequest();
        shipmentAssignmentRequest.setCourierId(2L);
        shipmentAssignmentRequest.setVehicleId(3L);

        when(shipmentAssignmentService.assign(1L, 2L, 3L)).thenThrow(new AssignmentVehicleOutOfSpaceException("Shipment weight is too big."));

        String jsonRequest = jsonMapper.writeValueAsString(shipmentAssignmentRequest);

        mockMvc.perform(post("/api/shipments/1/assignment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()))
                .andExpect(jsonPath("$.error").value(HttpStatus.CONFLICT.name()))
                .andExpect(jsonPath("$.message").value("Shipment weight is too big."))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(shipmentAssignmentService).assign(1L, 2L, 3L);
    }

    @Test
    void shouldReturnConflictWhenCourierHasInvalidStatus() throws Exception {
        CreateShipmentAssignmentRequest shipmentAssignmentRequest = new CreateShipmentAssignmentRequest();
        shipmentAssignmentRequest.setCourierId(2L);
        shipmentAssignmentRequest.setVehicleId(3L);

        when(shipmentAssignmentService.assign(1L, 2L, 3L)).thenThrow(new CourierInvalidStateException("Courier must be active and available to mark as busy."));

        String jsonRequest = jsonMapper.writeValueAsString(shipmentAssignmentRequest);

        mockMvc.perform(post("/api/shipments/1/assignment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()))
                .andExpect(jsonPath("$.error").value(HttpStatus.CONFLICT.name()))
                .andExpect(jsonPath("$.message").value("Courier must be active and available to mark as busy."))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(shipmentAssignmentService).assign(1L, 2L, 3L);
    }

    @Test
    void shouldReturnConflictWhenVehicleHasInvalidStatus() throws Exception {
        CreateShipmentAssignmentRequest shipmentAssignmentRequest = new CreateShipmentAssignmentRequest();
        shipmentAssignmentRequest.setCourierId(2L);
        shipmentAssignmentRequest.setVehicleId(3L);

        when(shipmentAssignmentService.assign(1L, 2L, 3L)).thenThrow(new VehicleInvalidStateException("Vehicle must be active and available to mark as busy."));

        String jsonRequest = jsonMapper.writeValueAsString(shipmentAssignmentRequest);

        mockMvc.perform(post("/api/shipments/1/assignment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()))
                .andExpect(jsonPath("$.error").value(HttpStatus.CONFLICT.name()))
                .andExpect(jsonPath("$.message").value("Vehicle must be active and available to mark as busy."))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(shipmentAssignmentService).assign(1L, 2L, 3L);
    }

    @Test
    void shouldReassignShipment() throws Exception {
        Shipment shipment = createShipment();
        Courier courier = createCourier();
        courier.markAsBusy();

        Vehicle vehicle = createVehicle();
        vehicle.markAsBusy();

        ShipmentAssignment shipmentAssignment = new ShipmentAssignment(shipment, courier, vehicle);
        ReflectionTestUtils.setField(shipmentAssignment, "id", 1L);
        ReflectionTestUtils.setField(shipment, "id", 1L);
        ReflectionTestUtils.setField(courier, "id", 1L);
        ReflectionTestUtils.setField(vehicle, "id", 1L);

        when(shipmentAssignmentService.reassign(1L)).thenReturn(shipmentAssignment);

        mockMvc.perform(patch("/api/shipments/1/reassign"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignmentId").value(1L))
                .andExpect(jsonPath("$.shipmentId").value(1L))
                .andExpect(jsonPath("$.courierId").value(1L))
                .andExpect(jsonPath("$.vehicleId").value(1L))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.assignedAt").exists())
                .andExpect(jsonPath("$.finishedAt").isEmpty());;

        verify(shipmentAssignmentService).reassign(1L);
    }

    @Test
    void shouldThrowWhenShipmentNotFound() throws Exception {
        when(shipmentAssignmentService.reassign(1L)).thenThrow(new ShipmentNotFoundException(1L));

        mockMvc.perform(patch("/api/shipments/1/reassign"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.error").value(HttpStatus.NOT_FOUND.name()))
                .andExpect(jsonPath("$.message").value("Shipment with id 1 not found."))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(shipmentAssignmentService).reassign(1L);
    }

    @Test
    void shouldThrowWhenShipmentHasInvalidStatus() throws Exception {
        when(shipmentAssignmentService.reassign(1L)).thenThrow(new ShipmentInvalidStatusException("Shipment status must be 'READY_FOR_PICKUP' to reassign."));

        mockMvc.perform(patch("/api/shipments/1/reassign"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()))
                .andExpect(jsonPath("$.error").value(HttpStatus.CONFLICT.name()))
                .andExpect(jsonPath("$.message").value("Shipment status must be 'READY_FOR_PICKUP' to reassign."))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(shipmentAssignmentService).reassign(1L);
    }

    @Test
    void shouldThrowWhenCourierNotFound() throws Exception {
        when(shipmentAssignmentService.reassign(1L)).thenThrow(new CourierNotFoundException());

        mockMvc.perform(patch("/api/shipments/1/reassign"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.error").value(HttpStatus.NOT_FOUND.name()))
                .andExpect(jsonPath("$.message").value("Available courier not found."))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(shipmentAssignmentService).reassign(1L);
    }

    @Test
    void shouldThrowWhenVehicleNotFound() throws Exception {
        when(shipmentAssignmentService.reassign(1L)).thenThrow(new VehicleNotFoundException(new BigDecimal("20.00")));

        mockMvc.perform(patch("/api/shipments/1/reassign"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.error").value(HttpStatus.NOT_FOUND.name()))
                .andExpect(jsonPath("$.message").value("No suitable vehicle available for shipment weighing 20.00 kg."))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(shipmentAssignmentService).reassign(1L);
    }

    @Test
    void shouldThrowWhenActiveAssignmentNotFound() throws Exception {
        when(shipmentAssignmentService.reassign(1L)).thenThrow(new ActiveAssignmentNotFoundException(1L));

        mockMvc.perform(patch("/api/shipments/1/reassign"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.error").value(HttpStatus.NOT_FOUND.name()))
                .andExpect(jsonPath("$.message").value("Active assignment for shipment with id 1 not found."))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(shipmentAssignmentService).reassign(1L);
    }

    @Test
    void shouldReturnAssignmentHistory() throws Exception {
        Shipment shipment = createShipment();
        Courier courier = createCourier();
        Vehicle vehicle = createVehicle();

        ShipmentAssignment assignment1 = new ShipmentAssignment(shipment, courier, vehicle);
        assignment1.reassign();

        ShipmentAssignment assignment2 = new ShipmentAssignment(shipment, courier, vehicle);
        assignment2.reassign();

        ShipmentAssignment assignment3 = new ShipmentAssignment(shipment, courier, vehicle);

        ReflectionTestUtils.setField(assignment1, "id", 1L);
        ReflectionTestUtils.setField(assignment2, "id", 2L);
        ReflectionTestUtils.setField(assignment3, "id", 3L);
        ReflectionTestUtils.setField(shipment, "id", 1L);
        ReflectionTestUtils.setField(courier, "id", 1L);
        ReflectionTestUtils.setField(vehicle, "id", 1L);

        List<ShipmentAssignment> shipmentAssignmentList = List.of(assignment1, assignment2, assignment3);

        when(shipmentAssignmentService.getAssignmentHistory(1L)).thenReturn(shipmentAssignmentList);

        mockMvc.perform(get("/api/shipments/1/assignment-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].assignmentId").value(1L))
                .andExpect(jsonPath("$[0].shipmentId").value(1L))
                .andExpect(jsonPath("$[0].courierId").value(1L))
                .andExpect(jsonPath("$[0].vehicleId").value(1L))
                .andExpect(jsonPath("$[0].status").value("REASSIGNED"))
                .andExpect(jsonPath("$[0].assignedAt").exists())
                .andExpect(jsonPath("$[0].finishedAt").exists())
                .andExpect(jsonPath("$[1].assignmentId").value(2L))
                .andExpect(jsonPath("$[1].shipmentId").value(1L))
                .andExpect(jsonPath("$[1].courierId").value(1L))
                .andExpect(jsonPath("$[1].vehicleId").value(1L))
                .andExpect(jsonPath("$[1].status").value("REASSIGNED"))
                .andExpect(jsonPath("$[1].assignedAt").exists())
                .andExpect(jsonPath("$[1].finishedAt").exists())
                .andExpect(jsonPath("$[2].assignmentId").value(3L))
                .andExpect(jsonPath("$[2].shipmentId").value(1L))
                .andExpect(jsonPath("$[2].courierId").value(1L))
                .andExpect(jsonPath("$[2].vehicleId").value(1L))
                .andExpect(jsonPath("$[2].status").value("ACTIVE"))
                .andExpect(jsonPath("$[2].assignedAt").exists())
                .andExpect(jsonPath("$[2].finishedAt").isEmpty());

        verify(shipmentAssignmentService).getAssignmentHistory(1L);
    }

    @Test
    void shouldReturnEmptyAssignmentHistoryList() throws Exception {
        when(shipmentAssignmentService.getAssignmentHistory(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/shipments/1/assignment-history"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(shipmentAssignmentService).getAssignmentHistory(1L);
    }

    @Test
    void shouldReturnNotFoundWhenGettingAssignmentHistoryForMissingShipment() throws Exception {
        when(shipmentAssignmentService.getAssignmentHistory(1L)).thenThrow(new ShipmentNotFoundException(1L));

        mockMvc.perform(get("/api/shipments/1/assignment-history"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.error").value(HttpStatus.NOT_FOUND.name()))
                .andExpect(jsonPath("$.message").value("Shipment with id 1 not found."))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(shipmentAssignmentService).getAssignmentHistory(1L);
    }

    private Shipment createShipment() {
        Address customerAddress = new Address("Adrianowa", "20", "14", "Kielce", "50-231", "Poland");
        Address deliveryAddress = new Address("Wysyłkowa", "13", "3", "Krakow", "60-123", "Poland");

        Customer customer = new Customer("Adrian", "Nowak", "adrian@nowak.com", "+48 553 214 532", customerAddress);

        return new Shipment(customer, customerAddress, deliveryAddress, new BigDecimal("5.00"));
    }

    private Shipment createOverweightShipment() {
        Address customerAddress = new Address("Adrianowa", "20", "14", "Kielce", "50-231", "Poland");
        Address deliveryAddress = new Address("Wysyłkowa", "13", "3", "Krakow", "60-123", "Poland");

        Customer customer = new Customer("Adrian", "Nowak", "adrian@nowak.com", "+48 553 214 532", customerAddress);

        return new Shipment(customer, customerAddress, deliveryAddress, new BigDecimal("800.00"));
    }

    private Courier createCourier() {
        return new Courier("Postman", "Pat", "+48 999 234 523");
    }

    private Vehicle createVehicle() {
        return new Vehicle("Ford", "Transport", "GD 9231L", VehicleType.VAN, new BigDecimal("700.00"));
    }

    private Vehicle createVehicleWithLowMaximumLoad() {
        return new Vehicle("Ford", "Transport", "GD 9231L", VehicleType.VAN, new BigDecimal("2.00"));
    }
}
