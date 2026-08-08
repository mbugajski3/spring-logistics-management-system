package com.mbugajski.logistics.customer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
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
        Customer customer1 = new Customer("Franciszek", "Cyprian", "franciszek@cyprian.com", "+48 777 222 333", address);
        Customer customer2 = new Customer("Adrian", "Nowak", "adrian@nowak.com", "+48 699 300 299", address);

        ReflectionTestUtils.setField(customer1, "id", 1L);
        ReflectionTestUtils.setField(customer2, "id", 2L);

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
        Customer customer1 = new Customer("Franciszek", "Cyprian", "franciszek@cyprian.com", "+48 777 222 333", address);

        ReflectionTestUtils.setField(customer1, "id", 1L);

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
        Customer customer1 = new Customer("Franciszek", "Cyprian", "franciszek@cyprian.com", "+48 777 222 333", address);
        CreateCustomerRequest customerRequest = createCustomerRequest("Franciszek", "Cyprian", "franciszek@cyprian.com", "+48 777 222 333");
        CreateAddressRequest addressRequest = createValidAddressRequest();

        customerRequest.setAddress(addressRequest);

        ReflectionTestUtils.setField(customer1, "id", 1L);

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

    @Test
    void shouldReturnConflictWhenEmailIsTaken() throws Exception {
        CreateCustomerRequest customerRequest = createCustomerRequest("Franciszek", "Cyprian", "franciszek@cyprian.com", "+48 777 222 333");
        customerRequest.setAddress(createValidAddressRequest());

        when(customerService.create(any(CreateCustomerRequest.class))).thenThrow(new CustomerEmailAlreadyExistsException("franciszek@cyprian.com"));

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(customerRequest)))
                        .andExpect(status().isConflict());

        verify(customerService).create(any(CreateCustomerRequest.class));
    }

    @Test
    void shouldReturnOkWhenCustomerIsPartiallyUpdated() throws Exception {
        Address address = new Address("Wschodnia", "130", "15", "Łódź", "90-266", "Poland");
        Customer updatedCustomer = new Customer("Franciszek", "Kowalski", "franciszek@cyprian.com", "+48 777 222 333", address);

        UpdateCustomerRequest updateRequest = new UpdateCustomerRequest();
        updateRequest.setLastName("Kowalski");

        ReflectionTestUtils.setField(updatedCustomer, "id", 1L);

        when(customerService.update(eq(1L), any(UpdateCustomerRequest.class))).thenReturn(updatedCustomer);

        String updateJson = jsonMapper.writeValueAsString(updateRequest);

        mockMvc.perform(patch("/api/customers/{customerId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(1))
                        .andExpect(jsonPath("$.firstName").value("Franciszek"))
                        .andExpect(jsonPath("$.lastName").value("Kowalski"))
                        .andExpect(jsonPath("$.email").value("franciszek@cyprian.com"))
                        .andExpect(jsonPath("$.phoneNumber").value("+48 777 222 333"))
                        .andExpect(jsonPath("$.address.street").value("Wschodnia"))
                        .andExpect(jsonPath("$.address.buildingNumber").value("130"))
                        .andExpect(jsonPath("$.address.apartmentNumber").value("15"))
                        .andExpect(jsonPath("$.address.city").value("Łódź"))
                        .andExpect(jsonPath("$.address.postalCode").value("90-266"))
                        .andExpect(jsonPath("$.address.country").value("Poland"));

        verify(customerService).update(eq(1L), any(UpdateCustomerRequest.class));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistingCustomer() throws Exception {
        UpdateCustomerRequest customerRequest = new UpdateCustomerRequest();
        customerRequest.setLastName("Kowalski");

        when(customerService.update(eq(999L), any(UpdateCustomerRequest.class))).thenThrow(new CustomerNotFoundException(999L));

        String updateJson = jsonMapper.writeValueAsString(customerRequest);

        mockMvc.perform(patch("/api/customers/{customerId}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                        .andExpect(status().isNotFound());

        verify(customerService).update(eq(999L), any(UpdateCustomerRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenUpdatedFirstNameIsBlank() throws Exception {
        UpdateCustomerRequest customerRequest = new UpdateCustomerRequest();
        customerRequest.setFirstName("   ");

        String updateJson = jsonMapper.writeValueAsString(customerRequest);

        mockMvc.perform(patch("/api/customers/{customerId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                        .andExpect(status().isBadRequest());

        verifyNoInteractions(customerService);
    }

    @Test
    void shouldReturnBadRequestWhenUpdatedAddressIsInvalid() throws Exception {
        UpdateCustomerRequest customerRequest = new UpdateCustomerRequest();
        CreateAddressRequest invalidAddress = createInvalidAddressRequest();

        customerRequest.setAddress(invalidAddress);

        String updateJson = jsonMapper.writeValueAsString(customerRequest);

        mockMvc.perform(patch("/api/customers/{customerId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                        .andExpect(status().isBadRequest());

        verifyNoInteractions(customerService);
    }

    @Test
    void shouldReturnBadRequestWhenNoUpdateFieldsAreProvided() throws Exception {
        UpdateCustomerRequest customerRequest = new UpdateCustomerRequest();

        when(customerService.update(eq(1L), any(UpdateCustomerRequest.class))).thenThrow(EmptyCustomerUpdateException.class);

        String updateJson = jsonMapper.writeValueAsString(customerRequest);

        mockMvc.perform(patch("/api/customers/{customerId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                        .andExpect(status().isBadRequest());

        verify(customerService).update(eq(1L), any(UpdateCustomerRequest.class));
    }

    @Test
    void shouldReturnOkWhenCustomerEmailIsUpdated() throws Exception {
        Address address = new Address("Wschodnia", "130", "15", "Łódź", "90-266", "Poland");
        Customer updatedCustomer = new Customer("Franciszek", "Kowalski", "franciszek@cyprian.com", "+48 777 222 333", address);

        UpdateCustomerRequest updateRequest = new UpdateCustomerRequest();
        updateRequest.setEmail("franciszek@cyprian.com");

        ReflectionTestUtils.setField(updatedCustomer, "id", 1L);

        when(customerService.update(eq(1L), any(UpdateCustomerRequest.class))).thenReturn(updatedCustomer);

        String updateJson = jsonMapper.writeValueAsString(updateRequest);

        mockMvc.perform(patch("/api/customers/{customerId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(1))
                        .andExpect(jsonPath("$.firstName").value("Franciszek"))
                        .andExpect(jsonPath("$.lastName").value("Kowalski"))
                        .andExpect(jsonPath("$.email").value("franciszek@cyprian.com"))
                        .andExpect(jsonPath("$.phoneNumber").value("+48 777 222 333"))
                        .andExpect(jsonPath("$.address.street").value("Wschodnia"))
                        .andExpect(jsonPath("$.address.buildingNumber").value("130"))
                        .andExpect(jsonPath("$.address.apartmentNumber").value("15"))
                        .andExpect(jsonPath("$.address.city").value("Łódź"))
                        .andExpect(jsonPath("$.address.postalCode").value("90-266"))
                        .andExpect(jsonPath("$.address.country").value("Poland"));

        verify(customerService).update(eq(1L), any(UpdateCustomerRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenUpdatedEmailHasInvalidFormat() throws Exception {
        UpdateCustomerRequest customerRequest = new UpdateCustomerRequest();
        customerRequest.setEmail("wrong-email");

        String updateJson = jsonMapper.writeValueAsString(customerRequest);

        mockMvc.perform(patch("/api/customers/{customerId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                        .andExpect(status().isBadRequest());

        verifyNoInteractions(customerService);
    }

    @Test
    void shouldReturnConflictWhenUpdatedEmailIsAlreadyUsed() throws Exception {
        UpdateCustomerRequest customerRequest = new UpdateCustomerRequest();
        customerRequest.setEmail("piotr@kowalski.com");

        when(customerService.update(eq(1L), any(UpdateCustomerRequest.class))).thenThrow(new CustomerEmailAlreadyExistsException("piotr@kowalski.com"));

        String updateJson = jsonMapper.writeValueAsString(customerRequest);

        mockMvc.perform(patch("/api/customers/{customerId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                        .andExpect(status().isConflict());

        verify(customerService).update(eq(1L), any(UpdateCustomerRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenUpdatedEmailIsBlank() throws Exception {
        UpdateCustomerRequest customerRequest = new UpdateCustomerRequest();
        customerRequest.setEmail("   ");

        String updateJson = jsonMapper.writeValueAsString(customerRequest);

        mockMvc.perform(patch("/api/customers/{customerId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                        .andExpect(status().isBadRequest());

        verifyNoInteractions(customerService);
    }

    @Test
    void shouldReturnOkWhenCustomerIsActivated() throws Exception {
        Address address = new Address("Wschodnia", "130", "15", "Łódź", "90-266", "Poland");
        Customer activeCustomer = new Customer("Franciszek", "Kowalski", "franciszek@cyprian.com", "+48 777 222 333", address);

        ReflectionTestUtils.setField(activeCustomer, "id", 1L);

        when(customerService.activate(1L)).thenReturn(activeCustomer);

        mockMvc.perform(patch("/api/customers/{customerId}/activate", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.active").value(true));

        verify(customerService).activate(1L);
    }

    @Test
    void shouldReturnConflictWhenActivatingAlreadyActiveCustomer() throws Exception {
        when(customerService.activate(1L)).thenThrow(new CustomerAlreadyActiveException());

        mockMvc.perform(patch("/api/customers/{customerId}/activate", 1L))
                .andExpect(status().isConflict());

        verify(customerService).activate(1L);
    }

    @Test
    void shouldReturnOkWhenCustomerIsDeactivated() throws Exception {
        Address address = new Address("Wschodnia", "130", "15", "Łódź", "90-266", "Poland");
        Customer inactiveCustomer = new Customer("Franciszek", "Kowalski", "franciszek@cyprian.com", "+48 777 222 333", address);
        inactiveCustomer.deactivate();

        ReflectionTestUtils.setField(inactiveCustomer, "id", 1L);

        when(customerService.deactivate(1L)).thenReturn(inactiveCustomer);

        mockMvc.perform(patch("/api/customers/{customerId}/deactivate", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.active").value(false));

        verify(customerService).deactivate(1L);
    }

    @Test
    void shouldReturnConflictWhenDeactivatingAlreadyInactiveCustomer() throws Exception {
        when(customerService.deactivate(1L)).thenThrow(new CustomerAlreadyInactiveException());

        mockMvc.perform(patch("/api/customers/{customerId}/deactivate", 1L))
                .andExpect(status().isConflict());

        verify(customerService).deactivate(1L);
    }

    @Test
    void shouldReturnConflictWhenDeactivatingCustomerWithDebt() throws Exception {
        when(customerService.deactivate(1L)).thenThrow(new CustomerHasOutstandingDebtException());

        mockMvc.perform(patch("/api/customers/{customerId}/deactivate", 1L))
                .andExpect(status().isConflict());

        verify(customerService).deactivate(1L);
    }

    @Test
    void shouldReturnNotFoundWhenActivatingNonExistingCustomer() throws Exception {
        when(customerService.activate(999L)).thenThrow(new CustomerNotFoundException(999L));

        mockMvc.perform(patch("/api/customers/{customerId}/activate", 999L))
                .andExpect(status().isNotFound());

        verify(customerService).activate(999L);
    }

    @Test
    void shouldReturnNotFoundWhenDeactivatingNonExistingCustomer() throws Exception {
        when(customerService.deactivate(999L)).thenThrow(new CustomerNotFoundException(999L));

        mockMvc.perform(patch("/api/customers/{customerId}/deactivate", 999L))
                .andExpect(status().isNotFound());

        verify(customerService).deactivate(999L);
    }

    @Test
    void shouldReturnNoContentWhenCustomerIsDeleted() throws Exception {
        mockMvc.perform(delete("/api/customers/1"))
                .andExpect(status().isNoContent());

        verify(customerService).deleteById(1L);
    }

    @Test
    void shouldReturnConflictWhenActiveCustomerCannotBeDeleted() throws Exception {
        doThrow(new ActiveCustomerDeletionException())
                .when(customerService)
                .deleteById(1L);

        mockMvc.perform(delete("/api/customers/1"))
                .andExpect(status().isConflict());

        verify(customerService).deleteById(1L);
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
