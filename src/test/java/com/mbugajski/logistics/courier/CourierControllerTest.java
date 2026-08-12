package com.mbugajski.logistics.courier;

import com.mbugajski.logistics.courier.controller.CourierController;
import com.mbugajski.logistics.courier.dto.request.CreateCourierRequest;
import com.mbugajski.logistics.courier.entity.Courier;
import com.mbugajski.logistics.courier.service.CourierService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CourierController.class)
public class CourierControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CourierService courierService;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void shouldCreateCourier() throws Exception{
        Courier courier = new Courier("Adrian", "Nowak", "+48 534 234 432");
        CreateCourierRequest courierRequest = new CreateCourierRequest();

        ReflectionTestUtils.setField(courier, "id", 1L);

        courierRequest.setFirstName("Adrian");
        courierRequest.setLastName("Nowak");
        courierRequest.setPhoneNumber("+48 534 234 432");

        when(courierService.create(any(CreateCourierRequest.class))).thenReturn(courier);

        String jsonRequest = jsonMapper.writeValueAsString(courierRequest);

        mockMvc.perform(post("/api/couriers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Adrian"))
                .andExpect(jsonPath("$.lastName").value("Nowak"))
                .andExpect(jsonPath("$.phoneNumber").value("+48 534 234 432"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.available").value(true));

        verify(courierService).create(any(CreateCourierRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenFirstNameIsBlank() throws Exception {
        CreateCourierRequest courierRequest = new CreateCourierRequest();
        courierRequest.setFirstName("");
        courierRequest.setLastName("Nowak");
        courierRequest.setPhoneNumber("+48 534 234 432");

        String jsonRequest = jsonMapper.writeValueAsString(courierRequest);

        mockMvc.perform(post("/api/couriers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());

        verify(courierService, never()).create(any(CreateCourierRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenLastNameIsBlank() throws Exception {
        CreateCourierRequest courierRequest = new CreateCourierRequest();
        courierRequest.setFirstName("Adrian");
        courierRequest.setLastName(" ");
        courierRequest.setPhoneNumber("+48 534 234 432");

        String jsonRequest = jsonMapper.writeValueAsString(courierRequest);

        mockMvc.perform(post("/api/couriers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());

        verify(courierService, never()).create(any(CreateCourierRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenPhoneNumberIsBlank() throws Exception {
        CreateCourierRequest courierRequest = new CreateCourierRequest();
        courierRequest.setFirstName("Adrian");
        courierRequest.setLastName("Nowak");
        courierRequest.setPhoneNumber(" ");

        String jsonRequest = jsonMapper.writeValueAsString(courierRequest);

        mockMvc.perform(post("/api/couriers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());

        verify(courierService, never()).create(any(CreateCourierRequest.class));
    }
}
