package com.mbugajski.logistics.customer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
public class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void shouldReturnAllCustomers() throws Exception {
        Address address = new Address("Wschodnia", "130", "15", "Łódź", "90-266", "Poland");
        Customer customer1 = new Customer(1L, "Franciszek", "Cyprian", "franciszek@cyprian.com", "+48 777 222 333", address);
        Customer customer2 = new Customer(2L, "Adrian", "Nowak", "adrian@nowak.com", "+48 699 300 299", address);
        
        when(customerService.findAll()).thenReturn(List.of(customer1, customer2));

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].firstName").value("Franciszek"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].firstName").value("Adrian"));
    }

    @Test
    void shouldReturnCustomerById() throws Exception {
        Address address = new Address("Wschodnia", "130", "15", "Łódź", "90-266", "Poland");
        Customer customer1 = new Customer(1L, "Franciszek", "Cyprian", "franciszek@cyprian.com", "+48 777 222 333", address);

        when(customerService.findById(1L)).thenReturn(customer1);

        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Franciszek"))
                .andExpect(jsonPath("$.lastName").value("Cyprian"))
                .andExpect(jsonPath("$.email").value("franciszek@cyprian.com"))
                .andExpect(jsonPath("$.address.street").value("Wschodnia"))
                .andExpect(jsonPath("$.address.buildingNumber").value("130"))
                .andExpect(jsonPath("$.address.apartmentNumber").value("15"));
    }

    @Test
    void shouldReturnNotFoundWhenCustomerDoesNotExist() throws Exception {
        when(customerService.findById(1L)).thenThrow(new CustomerNotFoundException(1L));

        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateCustomer() throws Exception {
        Address address = new Address("Wschodnia", "130", "15", "Łódź", "90-266", "Poland");
        Customer customer1 = new Customer(1L, "Franciszek", "Cyprian", "franciszek@cyprian.com", "+48 777 222 333", address);
        CreateCustomerRequest customerRequest = createCustomerRequest("Franciszek", "Cyprian", "franciszek@cyprian.com", "+48 777 222 333");
        CreateAddressRequest addressRequest = createValidAddressRequest();

        customerRequest.setAddress(addressRequest);

        when(customerService.create(any(CreateCustomerRequest.class))).thenReturn(customer1);

        String requestJson = jsonMapper.writeValueAsString(customerRequest);

        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Franciszek"))
                .andExpect(jsonPath("$.lastName").value("Cyprian"))
                .andExpect(jsonPath("$.email").value("franciszek@cyprian.com"))
                .andExpect(jsonPath("$.phoneNumber").value("+48 777 222 333"))
                .andExpect(jsonPath("$.address.street").value("Wschodnia"))
                .andExpect(jsonPath("$.address.buildingNumber").value("130"))
                .andExpect(jsonPath("$.address.apartmentNumber").value("15"))
                .andExpect(jsonPath("$.address.city").value("Łódź"))
                .andExpect(jsonPath("$.address.postalCode").value("90-266"))
                .andExpect(jsonPath("$.address.country").value("Poland"));
    }

    @Test
    void shouldDeleteCustomer() throws Exception {
        mockMvc.perform(delete("/api/customers/1"))
                .andExpect(status().isNoContent());

        verify(customerService).deleteById(1L);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistingCustomer() throws Exception {
        doThrow(new CustomerNotFoundException(1L))
                .when(customerService).deleteById(1L);

        mockMvc.perform(delete("/api/customers/1"))
                .andExpect(status().isNotFound());

        verify(customerService).deleteById(1L);
    }

    @Test
    void shouldReturnBadRequestWhenCustomerRequestIsInvalid() throws Exception {
        CreateCustomerRequest customerRequest = createCustomerRequest("", "Cyprian", "franciszek@cyprian.com", "+48 777 222 333");
        CreateAddressRequest addressRequest = createValidAddressRequest();
        customerRequest.setAddress(addressRequest);

        String requestJson = jsonMapper.writeValueAsString(customerRequest);

        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest());

        verify(customerService, never()).create(any(CreateCustomerRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenAddressIsInvalid() throws Exception {
        CreateCustomerRequest customerRequest = createCustomerRequest("Franciszek", "Cyprian", "franciszek@cyprian.com", "+48 777 222 333");
        CreateAddressRequest invalidAddressRequest = createInvalidAddressRequest();
        customerRequest.setAddress(invalidAddressRequest);

        String requestJson = jsonMapper.writeValueAsString(customerRequest);

        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest());

        verify(customerService, never()).create(any(CreateCustomerRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenAddressIsMissing() throws Exception {
        CreateCustomerRequest customerRequest = createCustomerRequest("Franciszek", "Cyprian", "franciszek@cyprian.com", "+48 777 222 333");
        customerRequest.setAddress(null);

        String requestJson = jsonMapper.writeValueAsString(customerRequest);

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());

        verify(customerService, never()).create(any(CreateCustomerRequest.class));
    }

    private CreateCustomerRequest createCustomerRequest(String firstName, String lastName, String email, String phoneNumber) {
        CreateCustomerRequest customerRequest = new CreateCustomerRequest();

        customerRequest.setFirstName(firstName);
        customerRequest.setLastName(lastName);
        customerRequest.setEmail(email);
        customerRequest.setPhoneNumber(phoneNumber);

        return customerRequest;
    }

    private CreateAddressRequest createValidAddressRequest() {
        CreateAddressRequest addressRequest = new CreateAddressRequest();
        addressRequest.setStreet("Wschodnia");
        addressRequest.setBuildingNumber("130");
        addressRequest.setApartmentNumber("15");
        addressRequest.setCity("Łódź");
        addressRequest.setPostalCode("90-266");
        addressRequest.setCountry("Poland");

        return addressRequest;
    }

    private CreateAddressRequest createInvalidAddressRequest() {
        CreateAddressRequest addressRequest = new CreateAddressRequest();
        addressRequest.setStreet("  ");
        addressRequest.setBuildingNumber("130");
        addressRequest.setApartmentNumber("15");
        addressRequest.setCity("Łódź");
        addressRequest.setPostalCode("90-266");
        addressRequest.setCountry("Poland");

        return addressRequest;
    }
}
