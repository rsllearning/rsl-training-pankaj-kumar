package com.leave.management.api.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import com.leave.management.api.dto.LeaveCreateRequestDTO;
import com.leave.management.api.dto.LeaveResponseDTO;
import com.leave.management.api.dto.LeaveUpdateRequestDTO;
import com.leave.management.api.exception.InvalidLeaveDateException;
import com.leave.management.api.exception.ResourceNotFoundException;
import com.leave.management.api.model.LeaveStatus;
import com.leave.management.api.model.LeaveType;
import com.leave.management.api.service.LeaveService;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LeaveController.class)
class LeaveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LeaveService leaveService;

    @Test
    @DisplayName("GET /v1/leaves - returns 200 with list of leaves")
    void testGetAllLeaves() throws Exception {
        LeaveResponseDTO response = LeaveResponseDTO.builder()
                .id(1L)
                .employeeId(101L)
                .leaveType(LeaveType.ANNUAL)
                .startDate(LocalDate.of(2026, 12, 1))
                .endDate(LocalDate.of(2026, 12, 5))
                .reason("Vacation")
                .status(LeaveStatus.APPROVED)
                .build();

        when(leaveService.getAllLeaves()).thenReturn(List.of(response));

        mockMvc.perform(get("/v1/leaves")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].employeeId", is(101)))
                .andExpect(jsonPath("$[0].leaveType", is("ANNUAL")))
                .andExpect(jsonPath("$[0].status", is("APPROVED")));

        verify(leaveService).getAllLeaves();
    }

    @Test
    @DisplayName("GET /v1/leaves/{id} - returns 200 when leave is found")
    void testGetLeaveById_Success() throws Exception {
        LeaveResponseDTO response = LeaveResponseDTO.builder()
                .id(1L)
                .employeeId(101L)
                .leaveType(LeaveType.ANNUAL)
                .startDate(LocalDate.of(2026, 12, 1))
                .endDate(LocalDate.of(2026, 12, 5))
                .reason("Vacation")
                .status(LeaveStatus.APPROVED)
                .build();

        when(leaveService.getLeaveById(1L)).thenReturn(response);

        mockMvc.perform(get("/v1/leaves/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.employeeId", is(101)))
                .andExpect(jsonPath("$.leaveType", is("ANNUAL")));

        verify(leaveService).getLeaveById(1L);
    }

    @Test
    @DisplayName("GET /v1/leaves/{id} - returns 404 when leave is not found")
    void testGetLeaveById_NotFound() throws Exception {
        when(leaveService.getLeaveById(99L))
                .thenThrow(new ResourceNotFoundException("Leave request not found with id: 99"));

        mockMvc.perform(get("/v1/leaves/99")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is("Leave request not found with id: 99")));

        verify(leaveService).getLeaveById(99L);
    }

    @Test
    @DisplayName("POST /v1/leaves - returns 201 when request is valid")
    void testCreateLeave_Success() throws Exception {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDate future = LocalDate.now().plusDays(4);

        LeaveCreateRequestDTO createDTO = LeaveCreateRequestDTO.builder()
                .employeeId(101L)
                .leaveType(LeaveType.SICK)
                .startDate(tomorrow)
                .endDate(future)
                .reason("Medical appointment")
                .build();

        LeaveResponseDTO response = LeaveResponseDTO.builder()
                .id(1L)
                .employeeId(101L)
                .leaveType(LeaveType.SICK)
                .startDate(tomorrow)
                .endDate(future)
                .reason("Medical appointment")
                .status(LeaveStatus.PENDING)
                .build();

        when(leaveService.createLeave(any(LeaveCreateRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/v1/leaves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.employeeId", is(101)))
                .andExpect(jsonPath("$.status", is("PENDING")));

        verify(leaveService).createLeave(any(LeaveCreateRequestDTO.class));
    }

    @Test
    @DisplayName("POST /v1/leaves - returns 400 when validation fails on required fields")
    void testCreateLeave_ValidationFailure() throws Exception {
        LeaveCreateRequestDTO invalidDTO = LeaveCreateRequestDTO.builder()
                .employeeId(null)
                .leaveType(null)
                .startDate(null)
                .endDate(null)
                .reason("")
                .build();

        mockMvc.perform(post("/v1/leaves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")));

        verify(leaveService, never()).createLeave(any());
    }

    @Test
    @DisplayName("POST /v1/leaves - returns 400 when employeeId is negative or zero")
    void testCreateLeave_NegativeEmployeeId_ValidationFailure() throws Exception {
        LeaveCreateRequestDTO invalidDTO = LeaveCreateRequestDTO.builder()
                .employeeId(-1L)
                .leaveType(LeaveType.ANNUAL)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .reason("Vacation")
                .build();

        mockMvc.perform(post("/v1/leaves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.details[0]", containsString("Employee ID must be positive")));

        verify(leaveService, never()).createLeave(any());
    }

    @Test
    @DisplayName("POST /v1/leaves - returns 400 when endDate is before startDate")
    void testCreateLeave_InvalidDateRange() throws Exception {
        LocalDate tomorrow = LocalDate.now().plusDays(2);
        LocalDate yesterday = LocalDate.now().plusDays(1);

        LeaveCreateRequestDTO createDTO = LeaveCreateRequestDTO.builder()
                .employeeId(101L)
                .leaveType(LeaveType.ANNUAL)
                .startDate(tomorrow)
                .endDate(yesterday)
                .reason("Trip")
                .build();

        when(leaveService.createLeave(any(LeaveCreateRequestDTO.class)))
                .thenThrow(new InvalidLeaveDateException("End date (" + yesterday + ") cannot be before start date (" + tomorrow + ")"));

        mockMvc.perform(post("/v1/leaves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("End date")));

        verify(leaveService).createLeave(any(LeaveCreateRequestDTO.class));
    }

    @Test
    @DisplayName("PUT /v1/leaves/{id} - returns 200 on successful update")
    void testUpdateLeave_Success() throws Exception {
        LeaveUpdateRequestDTO updateDTO = LeaveUpdateRequestDTO.builder()
                .reason("Updated plan")
                .status(LeaveStatus.APPROVED)
                .build();

        LeaveResponseDTO response = LeaveResponseDTO.builder()
                .id(1L)
                .employeeId(101L)
                .leaveType(LeaveType.ANNUAL)
                .startDate(LocalDate.now().plusDays(2))
                .endDate(LocalDate.now().plusDays(4))
                .reason("Updated plan")
                .status(LeaveStatus.APPROVED)
                .build();

        when(leaveService.updateLeave(eq(1L), any(LeaveUpdateRequestDTO.class))).thenReturn(response);

        mockMvc.perform(put("/v1/leaves/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reason", is("Updated plan")))
                .andExpect(jsonPath("$.status", is("APPROVED")));

        verify(leaveService).updateLeave(eq(1L), any(LeaveUpdateRequestDTO.class));
    }

    @Test
    @DisplayName("PUT /v1/leaves/{id} - returns 404 when leave not found")
    void testUpdateLeave_NotFound() throws Exception {
        LeaveUpdateRequestDTO updateDTO = LeaveUpdateRequestDTO.builder()
                .reason("Updated plan")
                .build();

        when(leaveService.updateLeave(eq(99L), any(LeaveUpdateRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Leave request not found with id: 99"));

        mockMvc.perform(put("/v1/leaves/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));

        verify(leaveService).updateLeave(eq(99L), any(LeaveUpdateRequestDTO.class));
    }

    @Test
    @DisplayName("GET /v1/leaves/{id} - returns 400 when id is not a number")
    void testGetLeaveById_TypeMismatch() throws Exception {
        mockMvc.perform(get("/v1/leaves/abc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", is("Invalid parameter type")));

        verify(leaveService, never()).getLeaveById(any());
    }

    @Test
    @DisplayName("POST /v1/leaves - returns 400 when request body contains malformed JSON")
    void testCreateLeave_MalformedJson() throws Exception {
        mockMvc.perform(post("/v1/leaves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeId\": \"not-a-number\""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")));

        verify(leaveService, never()).createLeave(any());
    }

    @Test
    @DisplayName("PUT /v1/leaves/{id} - returns 400 when endDate is before startDate")
    void testUpdateLeave_InvalidDateRange() throws Exception {
        LocalDate start = LocalDate.now().plusDays(5);
        LocalDate end = LocalDate.now().plusDays(2);
        LeaveUpdateRequestDTO updateDTO = LeaveUpdateRequestDTO.builder()
                .startDate(start)
                .endDate(end)
                .build();

        when(leaveService.updateLeave(eq(1L), any(LeaveUpdateRequestDTO.class)))
                .thenThrow(new InvalidLeaveDateException("End date (" + end + ") cannot be before start date (" + start + ")"));

        mockMvc.perform(put("/v1/leaves/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("End date")));

        verify(leaveService).updateLeave(eq(1L), any(LeaveUpdateRequestDTO.class));
    }

    @Test
    @DisplayName("DELETE /v1/leaves/{id} - returns 204 on successful deletion")
    void testDeleteLeave_Success() throws Exception {
        doNothing().when(leaveService).deleteLeave(1L);

        mockMvc.perform(delete("/v1/leaves/1"))
                .andExpect(status().isNoContent());

        verify(leaveService).deleteLeave(1L);
    }

    @Test
    @DisplayName("DELETE /v1/leaves/{id} - returns 404 when leave to delete does not exist")
    void testDeleteLeave_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Leave request not found with id: 99"))
                .when(leaveService).deleteLeave(99L);

        mockMvc.perform(delete("/v1/leaves/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));

        verify(leaveService).deleteLeave(99L);
    }
}
