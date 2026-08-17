package com.mbugajski.logistics.vehicle;

import com.mbugajski.logistics.common.exception.GlobalExceptionHandler;
import com.mbugajski.logistics.shipment.dto.request.CreateShipmentRequest;
import com.mbugajski.logistics.shipment.exception.ShipmentInvalidStatusException;
import com.mbugajski.logistics.vehicle.controller.VehicleController;
import com.mbugajski.logistics.vehicle.dto.request.CreateVehicleRequest;
import com.mbugajski.logistics.vehicle.dto.request.UpdateVehicleStatusRequest;
import com.mbugajski.logistics.vehicle.entity.Vehicle;
import com.mbugajski.logistics.vehicle.entity.VehicleStatus;
import com.mbugajski.logistics.vehicle.entity.VehicleType;
import com.mbugajski.logistics.vehicle.exception.VehicleInvalidStateException;
import com.mbugajski.logistics.vehicle.exception.VehicleNotFoundException;
import com.mbugajski.logistics.vehicle.exception.VehicleRegistrationNumberAlreadyExistsException;
import com.mbugajski.logistics.vehicle.service.VehicleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VehicleController.class)
@Import(GlobalExceptionHandler.class)
public class VehicleControllerTest {

    @Autowired
    public MockMvc mockMvc;

    @MockitoBean
    public VehicleService vehicleService;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void shouldReturnAllVehicles() throws Exception {
        Vehicle vehicle1 = new Vehicle("Ford", "Ducato", "GD 8032D", VehicleType.VAN, new BigDecimal("120.00"));
        Vehicle vehicle2 = new Vehicle("Fiat", "Transport", "GD 0324C", VehicleType.VAN, new BigDecimal("120.00"));

        ReflectionTestUtils.setField(vehicle1, "id", 1L);
        ReflectionTestUtils.setField(vehicle2, "id", 2L);

        List<Vehicle> vehiclesList = List.of(vehicle1, vehicle2);

        when(vehicleService.findAll()).thenReturn(vehiclesList);

        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].brand").value("Ford"))
                .andExpect(jsonPath("$[0].model").value("Ducato"))
                .andExpect(jsonPath("$[0].registrationNumber").value("GD 8032D"))
                .andExpect(jsonPath("$[0].vehicleType").value("VAN"))
                .andExpect(jsonPath("$[0].maximumLoad").value(new BigDecimal("120.0")))
                .andExpect(jsonPath("$[0].active").value(true))
                .andExpect(jsonPath("$[0].available").value(true))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].brand").value("Fiat"))
                .andExpect(jsonPath("$[1].model").value("Transport"))
                .andExpect(jsonPath("$[1].registrationNumber").value("GD 0324C"))
                .andExpect(jsonPath("$[1].vehicleType").value("VAN"))
                .andExpect(jsonPath("$[1].maximumLoad").value(new BigDecimal("120.0")))
                .andExpect(jsonPath("$[1].active").value(true))
                .andExpect(jsonPath("$[1].available").value(true));

        verify(vehicleService).findAll();
    }

    @Test
    void shouldReturnVehicleById() throws Exception {
        Vehicle vehicle = new Vehicle("Ford", "Ducato", "GD 8032D", VehicleType.VAN, new BigDecimal("120.00"));

        ReflectionTestUtils.setField(vehicle, "id", 1L);

        when(vehicleService.findById(1L)).thenReturn(vehicle);

        mockMvc.perform(get("/api/vehicles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.brand").value("Ford"))
                .andExpect(jsonPath("$.model").value("Ducato"))
                .andExpect(jsonPath("$.registrationNumber").value("GD 8032D"))
                .andExpect(jsonPath("$.vehicleType").value("VAN"))
                .andExpect(jsonPath("$.maximumLoad").value(new BigDecimal("120.0")))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.available").value(true));

        verify(vehicleService).findById(1L);
    }

    @Test
    void shouldCreateVehicle() throws Exception {
        Vehicle vehicle = new Vehicle("Ford", "Ducato", "GD 8032D", VehicleType.VAN, new BigDecimal("120.00"));

        ReflectionTestUtils.setField(vehicle, "id", 1L);

        CreateVehicleRequest vehicleRequest = new CreateVehicleRequest();
        vehicleRequest.setBrand("Ford");
        vehicleRequest.setModel("Ducato");
        vehicleRequest.setRegistrationNumber("GD 8032D");
        vehicleRequest.setVehicleType(VehicleType.VAN);
        vehicleRequest.setMaximumLoad(new BigDecimal("120.0"));

        when(vehicleService.create(any(CreateVehicleRequest.class))).thenReturn(vehicle);

        String requestJson = jsonMapper.writeValueAsString(vehicleRequest);

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.brand").value("Ford"))
                .andExpect(jsonPath("$.model").value("Ducato"))
                .andExpect(jsonPath("$.registrationNumber").value("GD 8032D"))
                .andExpect(jsonPath("$.vehicleType").value("VAN"))
                .andExpect(jsonPath("$.maximumLoad").value(120.0))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.available").value(true));

        verify(vehicleService).create(any(CreateVehicleRequest.class));
    }

    @Test
    void shouldThrowWhenBrandIsBlank() throws Exception {
        CreateVehicleRequest vehicleRequest = new CreateVehicleRequest();
        vehicleRequest.setBrand(" ");
        vehicleRequest.setModel("Ducato");
        vehicleRequest.setRegistrationNumber("GD 8032D");
        vehicleRequest.setVehicleType(VehicleType.VAN);
        vehicleRequest.setMaximumLoad(new BigDecimal("120.0"));

        String requestJson = jsonMapper.writeValueAsString(vehicleRequest);

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("brand: Brand name cannot be null or empty."))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(vehicleService, never()).create(any(CreateVehicleRequest.class));
    }

    @Test
    void shouldThrowWhenModelIsBlank() throws Exception {
        CreateVehicleRequest vehicleRequest = new CreateVehicleRequest();
        vehicleRequest.setBrand("Ford");
        vehicleRequest.setModel(" ");
        vehicleRequest.setRegistrationNumber("GD 8032D");
        vehicleRequest.setVehicleType(VehicleType.VAN);
        vehicleRequest.setMaximumLoad(new BigDecimal("120.0"));

        String requestJson = jsonMapper.writeValueAsString(vehicleRequest);

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("model: Model name cannot be null or empty."))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(vehicleService, never()).create(any(CreateVehicleRequest.class));
    }

    @Test
    void shouldThrowWhenRegistrationNumberIsBlank() throws Exception {
        CreateVehicleRequest vehicleRequest = new CreateVehicleRequest();
        vehicleRequest.setBrand("Ford");
        vehicleRequest.setModel("Ducato");
        vehicleRequest.setRegistrationNumber(" ");
        vehicleRequest.setVehicleType(VehicleType.VAN);
        vehicleRequest.setMaximumLoad(new BigDecimal("120.0"));

        String requestJson = jsonMapper.writeValueAsString(vehicleRequest);

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("registrationNumber: Registration number cannot be null or empty."))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(vehicleService, never()).create(any(CreateVehicleRequest.class));
    }

    @Test
    void shouldThrowWhenVehicleTypeIsNull() throws Exception {
        CreateVehicleRequest vehicleRequest = new CreateVehicleRequest();
        vehicleRequest.setBrand("Ford");
        vehicleRequest.setModel("Ducato");
        vehicleRequest.setRegistrationNumber("GD 8032D");
        vehicleRequest.setVehicleType(null);
        vehicleRequest.setMaximumLoad(new BigDecimal("120.0"));

        String requestJson = jsonMapper.writeValueAsString(vehicleRequest);

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("vehicleType: Vehicle type cannot be null."))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(vehicleService, never()).create(any(CreateVehicleRequest.class));
    }

    @Test
    void shouldThrowWhenMaximumLoadIsNull() throws Exception {
        CreateVehicleRequest vehicleRequest = new CreateVehicleRequest();
        vehicleRequest.setBrand("Ford");
        vehicleRequest.setModel("Ducato");
        vehicleRequest.setRegistrationNumber("GD 8032D");
        vehicleRequest.setVehicleType(VehicleType.VAN);
        vehicleRequest.setMaximumLoad(null);

        String requestJson = jsonMapper.writeValueAsString(vehicleRequest);

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("maximumLoad: Maximum load cannot be null."))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(vehicleService, never()).create(any(CreateVehicleRequest.class));
    }

    @Test
    void shouldThrowWhenMaximumLoadIsBelowMinimum() throws Exception {
        CreateVehicleRequest vehicleRequest = new CreateVehicleRequest();
        vehicleRequest.setBrand("Ford");
        vehicleRequest.setModel("Ducato");
        vehicleRequest.setRegistrationNumber("GD 8032D");
        vehicleRequest.setVehicleType(VehicleType.VAN);
        vehicleRequest.setMaximumLoad(new BigDecimal("-5"));

        String requestJson = jsonMapper.writeValueAsString(vehicleRequest);

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("maximumLoad: Maximum load must be more than 0 kg."))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(vehicleService, never()).create(any(CreateVehicleRequest.class));
    }

    @Test
    void shouldThrowWhenMaximumLoadIsAboveMaximum() throws Exception {
        CreateVehicleRequest vehicleRequest = new CreateVehicleRequest();
        vehicleRequest.setBrand("Ford");
        vehicleRequest.setModel("Ducato");
        vehicleRequest.setRegistrationNumber("GD 8032D");
        vehicleRequest.setVehicleType(VehicleType.VAN);
        vehicleRequest.setMaximumLoad(new BigDecimal("40000.00"));

        String requestJson = jsonMapper.writeValueAsString(vehicleRequest);

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("maximumLoad: Maximum load cannot be more than 1600 kg."))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(vehicleService, never()).create(any(CreateVehicleRequest.class));
    }

    @Test
    void shouldReturnNotFoundWhenVehicleNotExists() throws Exception {
        when(vehicleService.findById(1L)).thenThrow(new VehicleNotFoundException(1L));

        mockMvc.perform(get("/api/vehicles/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Vehicle with id 1 not found."))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(vehicleService).findById(1L);
    }

    @Test
    void shouldReturnConflictWhenRegistrationNumberExists() throws Exception {
        CreateVehicleRequest vehicleRequest = new CreateVehicleRequest();
        vehicleRequest.setBrand("Ford");
        vehicleRequest.setModel("Ducato");
        vehicleRequest.setRegistrationNumber("GD 8032D");
        vehicleRequest.setVehicleType(VehicleType.VAN);
        vehicleRequest.setMaximumLoad(new BigDecimal("120.00"));

        when(vehicleService.create(any(CreateVehicleRequest.class))).thenThrow(new VehicleRegistrationNumberAlreadyExistsException("GD 8032D"));

        String jsonRequest = jsonMapper.writeValueAsString(vehicleRequest);

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("GD 8032D is already assigned to another vehicle."))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(vehicleService).create(any(CreateVehicleRequest.class));
    }

    @Test
    void shouldChangeStatusFromAvailableToBusy() throws Exception {
        UpdateVehicleStatusRequest updateVehicleStatusRequest = new UpdateVehicleStatusRequest();
        updateVehicleStatusRequest.setStatus(VehicleStatus.BUSY);

        Vehicle vehicle = new Vehicle("Ford", "Ducato", "GD 8032D", VehicleType.VAN, new BigDecimal("120.00"));
        vehicle.markAsBusy();
        ReflectionTestUtils.setField(vehicle, "id", 1L);

        when(vehicleService.updateStatus(eq(1L), any(UpdateVehicleStatusRequest.class))).thenReturn(vehicle);

        String jsonRequest = jsonMapper.writeValueAsString(updateVehicleStatusRequest);

        mockMvc.perform(patch("/api/vehicles/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.brand").value("Ford"))
                .andExpect(jsonPath("$.model").value("Ducato"))
                .andExpect(jsonPath("$.registrationNumber").value("GD 8032D"))
                .andExpect(jsonPath("$.vehicleType").value("VAN"))
                .andExpect(jsonPath("$.maximumLoad").value(120.0))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.available").value(false));

        verify(vehicleService).updateStatus(eq(1L), any(UpdateVehicleStatusRequest.class));
    }

    @Test
    void shouldChangeStatusFromBusyToAvailable() throws Exception {
        UpdateVehicleStatusRequest updateVehicleStatusRequest = new UpdateVehicleStatusRequest();
        updateVehicleStatusRequest.setStatus(VehicleStatus.AVAILABLE);

        Vehicle vehicle = new Vehicle("Ford", "Ducato", "GD 8032D", VehicleType.VAN, new BigDecimal("120.00"));
        ReflectionTestUtils.setField(vehicle, "id", 1L);

        when(vehicleService.updateStatus(eq(1L), any(UpdateVehicleStatusRequest.class))).thenReturn(vehicle);

        String jsonRequest = jsonMapper.writeValueAsString(updateVehicleStatusRequest);

        mockMvc.perform(patch("/api/vehicles/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.brand").value("Ford"))
                .andExpect(jsonPath("$.model").value("Ducato"))
                .andExpect(jsonPath("$.registrationNumber").value("GD 8032D"))
                .andExpect(jsonPath("$.vehicleType").value("VAN"))
                .andExpect(jsonPath("$.maximumLoad").value(120.0))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.available").value(true));

        verify(vehicleService).updateStatus(eq(1L), any(UpdateVehicleStatusRequest.class));
    }

    @Test
    void shouldChangeStatusFromActiveToInactive() throws Exception {
        UpdateVehicleStatusRequest updateVehicleStatusRequest = new UpdateVehicleStatusRequest();
        updateVehicleStatusRequest.setStatus(VehicleStatus.INACTIVE);

        Vehicle vehicle = new Vehicle("Ford", "Ducato", "GD 8032D", VehicleType.VAN, new BigDecimal("120.00"));
        vehicle.deactivate();

        ReflectionTestUtils.setField(vehicle, "id", 1L);

        when(vehicleService.updateStatus(eq(1L), any(UpdateVehicleStatusRequest.class))).thenReturn(vehicle);

        String jsonRequest = jsonMapper.writeValueAsString(updateVehicleStatusRequest);

        mockMvc.perform(patch("/api/vehicles/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.brand").value("Ford"))
                .andExpect(jsonPath("$.model").value("Ducato"))
                .andExpect(jsonPath("$.registrationNumber").value("GD 8032D"))
                .andExpect(jsonPath("$.vehicleType").value("VAN"))
                .andExpect(jsonPath("$.maximumLoad").value(120.0))
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.available").value(false));

        verify(vehicleService).updateStatus(eq(1L), any(UpdateVehicleStatusRequest.class));
    }

    @Test
    void shouldReturnConflictWhenChangingBusyVehicleToInactive() throws Exception {
        UpdateVehicleStatusRequest updateVehicleStatusRequest = new UpdateVehicleStatusRequest();
        updateVehicleStatusRequest.setStatus(VehicleStatus.INACTIVE);

        when(vehicleService.updateStatus(eq(1L), any(UpdateVehicleStatusRequest.class))).thenThrow(new VehicleInvalidStateException("Busy vehicle cannot be deactivated."));

        String jsonRequest = jsonMapper.writeValueAsString(updateVehicleStatusRequest);

        mockMvc.perform(patch("/api/vehicles/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Busy vehicle cannot be deactivated."))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(vehicleService).updateStatus(eq(1L), any(UpdateVehicleStatusRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenVehicleStatusIsNull() throws Exception {
        UpdateVehicleStatusRequest request = new UpdateVehicleStatusRequest();
        request.setStatus(null);

        String jsonRequest = jsonMapper.writeValueAsString(request);

        mockMvc.perform(patch("/api/vehicles/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("status: Vehicle status cannot be null."))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(vehicleService, never()).updateStatus(eq(1L), any(UpdateVehicleStatusRequest.class));
    }
}
