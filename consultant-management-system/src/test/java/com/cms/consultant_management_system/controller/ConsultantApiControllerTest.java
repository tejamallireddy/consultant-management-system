package com.cms.consultant_management_system.controller;

import com.cms.consultant_management_system.entity.Consultant;
import com.cms.consultant_management_system.entity.Status;
import com.cms.consultant_management_system.exception.ApiExceptionHandler;
import com.cms.consultant_management_system.exception.ConsultantNotFoundException;
import com.cms.consultant_management_system.exception.DuplicateEmailException;
import com.cms.consultant_management_system.service.ConsultantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConsultantApiController.class)
@Import(ApiExceptionHandler.class)
@DisplayName("ConsultantApiController")
class ConsultantApiControllerTest {

    private static final String VALID_JSON = """
            {
              "name": "John Doe",
              "email": "john@email.com",
              "phone": "9876543210",
              "technology": "Java, Spring Boot",
              "experience": 5,
              "status": "ACTIVE"
            }
            """;

    private static final String INVALID_JSON = """
            {
              "name": "",
              "email": "not-an-email",
              "phone": "123",
              "technology": "",
              "experience": -1,
              "status": "ACTIVE"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConsultantService service;

    private Consultant sample;

    @BeforeEach
    void setUp() {
        sample = new Consultant("John Doe", "john@email.com", "9876543210",
                "Java, Spring Boot", 5, Status.ACTIVE);
        sample.setId(1L);
    }

    @Test
    @DisplayName("GET /api/consultants/{id} returns 200 with the consultant")
    void getOneReturnsConsultant() throws Exception {
        when(service.findById(1L)).thenReturn(sample);

        mockMvc.perform(get("/api/consultants/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@email.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("GET /api/consultants/{id} returns 404 when missing")
    void getOneReturns404() throws Exception {
        when(service.findById(99L)).thenThrow(new ConsultantNotFoundException(99L));

        mockMvc.perform(get("/api/consultants/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/consultants/99"));
    }

    @Test
    @DisplayName("POST /api/consultants returns 201 with a Location header")
    void createReturns201() throws Exception {
        when(service.create(any(Consultant.class))).thenReturn(sample);

        mockMvc.perform(post("/api/consultants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/consultants/1"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("POST with invalid data returns 400 and field errors")
    void createReturns400OnInvalidInput() throws Exception {
        mockMvc.perform(post("/api/consultants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(INVALID_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.phone").exists());

        verify(service, never()).create(any());
    }

    @Test
    @DisplayName("POST with a duplicate email returns 409")
    void createReturns409OnDuplicate() throws Exception {
        when(service.create(any(Consultant.class)))
                .thenThrow(new DuplicateEmailException("john@email.com"));

        mockMvc.perform(post("/api/consultants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("DELETE /api/consultants/{id} returns 204")
    void deleteReturns204() throws Exception {
        doNothing().when(service).delete(1L);

        mockMvc.perform(delete("/api/consultants/1"))
                .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }

    @Test
    @DisplayName("GET /api/consultants/{id} returns 400 for a non-numeric id")
    void getOneReturns400ForBadId() throws Exception {
        mockMvc.perform(get("/api/consultants/abc"))
                .andExpect(status().isBadRequest());

        verify(service, never()).findById(anyLong());
    }

    @Test
    @DisplayName("GET /api/consultants/stats returns the dashboard counters")
    void statsReturnsCounters() throws Exception {
        when(service.countTotal()).thenReturn(12L);
        when(service.countActive()).thenReturn(10L);
        when(service.countInactive()).thenReturn(2L);
        when(service.countNewThisMonth()).thenReturn(3L);

        mockMvc.perform(get("/api/consultants/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(12))
                .andExpect(jsonPath("$.active").value(10))
                .andExpect(jsonPath("$.inactive").value(2))
                .andExpect(jsonPath("$.newThisMonth").value(3));
    }
}