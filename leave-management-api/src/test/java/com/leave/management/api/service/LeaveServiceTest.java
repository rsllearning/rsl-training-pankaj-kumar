package com.leave.management.api.service;

import com.leave.management.api.dto.LeaveCreateRequestDTO;
import com.leave.management.api.dto.LeaveResponseDTO;
import com.leave.management.api.dto.LeaveUpdateRequestDTO;
import com.leave.management.api.exception.InvalidLeaveDateException;
import com.leave.management.api.exception.ResourceNotFoundException;
import com.leave.management.api.model.LeaveRequest;
import com.leave.management.api.model.LeaveStatus;
import com.leave.management.api.model.LeaveType;
import com.leave.management.api.repository.LeaveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaveServiceTest {

    @Mock
    private LeaveRepository leaveRepository;

    @InjectMocks
    private LeaveService leaveService;

    private LeaveRequest sampleLeaveRequest;

    @BeforeEach
    void setUp() {
        sampleLeaveRequest = LeaveRequest.builder()
                .id(1L)
                .employeeId(101L)
                .leaveType(LeaveType.ANNUAL)
                .startDate(LocalDate.of(2026, 11, 1))
                .endDate(LocalDate.of(2026, 11, 5))
                .reason("Vacation trip")
                .status(LeaveStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("getAllLeaves should return sorted list of response DTOs")
    void testGetAllLeaves() {
        LeaveRequest secondRequest = LeaveRequest.builder()
                .id(2L)
                .employeeId(102L)
                .leaveType(LeaveType.SICK)
                .startDate(LocalDate.of(2026, 11, 6))
                .endDate(LocalDate.of(2026, 11, 7))
                .reason("Doctor appointment")
                .status(LeaveStatus.APPROVED)
                .build();

        when(leaveRepository.findAll()).thenReturn(Arrays.asList(secondRequest, sampleLeaveRequest));

        List<LeaveResponseDTO> results = leaveService.getAllLeaves();

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getId()).isEqualTo(1L);
        assertThat(results.get(1).getId()).isEqualTo(2L);
        verify(leaveRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getLeaveById should return leave when ID exists")
    void testGetLeaveById_Success() {
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(sampleLeaveRequest));

        LeaveResponseDTO response = leaveService.getLeaveById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmployeeId()).isEqualTo(101L);
        assertThat(response.getLeaveType()).isEqualTo(LeaveType.ANNUAL);
        assertThat(response.getStatus()).isEqualTo(LeaveStatus.PENDING);
        verify(leaveRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("getLeaveById should throw ResourceNotFoundException when ID does not exist")
    void testGetLeaveById_NotFound() {
        when(leaveRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> leaveService.getLeaveById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Leave request not found with id: 99");

        verify(leaveRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("createLeave should persist and return new leave request with PENDING status")
    void testCreateLeave_Success() {
        LeaveCreateRequestDTO createDTO = LeaveCreateRequestDTO.builder()
                .employeeId(101L)
                .leaveType(LeaveType.ANNUAL)
                .startDate(LocalDate.of(2026, 11, 1))
                .endDate(LocalDate.of(2026, 11, 5))
                .reason("Vacation trip")
                .build();

        when(leaveRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> {
            LeaveRequest req = invocation.getArgument(0);
            req.setId(10L);
            return req;
        });

        LeaveResponseDTO result = leaveService.createLeave(createDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getEmployeeId()).isEqualTo(101L);
        assertThat(result.getStatus()).isEqualTo(LeaveStatus.PENDING);
        verify(leaveRepository, times(1)).save(any(LeaveRequest.class));
    }

    @Test
    @DisplayName("createLeave should throw InvalidLeaveDateException when endDate is before startDate")
    void testCreateLeave_InvalidDateRange() {
        LeaveCreateRequestDTO createDTO = LeaveCreateRequestDTO.builder()
                .employeeId(101L)
                .leaveType(LeaveType.ANNUAL)
                .startDate(LocalDate.of(2026, 11, 5))
                .endDate(LocalDate.of(2026, 11, 1))
                .reason("Invalid dates")
                .build();

        assertThatThrownBy(() -> leaveService.createLeave(createDTO))
                .isInstanceOf(InvalidLeaveDateException.class)
                .hasMessageContaining("End date");

        verify(leaveRepository, never()).save(any(LeaveRequest.class));
    }

    @Test
    @DisplayName("updateLeave should update fields and save successfully")
    void testUpdateLeave_Success() {
        LeaveUpdateRequestDTO updateDTO = LeaveUpdateRequestDTO.builder()
                .reason("Updated personal reason")
                .status(LeaveStatus.APPROVED)
                .build();

        when(leaveRepository.findById(1L)).thenReturn(Optional.of(sampleLeaveRequest));
        when(leaveRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LeaveResponseDTO updated = leaveService.updateLeave(1L, updateDTO);

        assertThat(updated.getReason()).isEqualTo("Updated personal reason");
        assertThat(updated.getStatus()).isEqualTo(LeaveStatus.APPROVED);
        verify(leaveRepository, times(1)).findById(1L);
        verify(leaveRepository, times(1)).save(sampleLeaveRequest);
    }

    @Test
    @DisplayName("updateLeave should throw InvalidLeaveDateException when updated dates are invalid")
    void testUpdateLeave_InvalidDateRange() {
        LeaveUpdateRequestDTO updateDTO = LeaveUpdateRequestDTO.builder()
                .startDate(LocalDate.of(2026, 12, 10))
                .endDate(LocalDate.of(2026, 12, 5))
                .build();

        when(leaveRepository.findById(1L)).thenReturn(Optional.of(sampleLeaveRequest));

        assertThatThrownBy(() -> leaveService.updateLeave(1L, updateDTO))
                .isInstanceOf(InvalidLeaveDateException.class)
                .hasMessageContaining("End date");

        verify(leaveRepository, times(1)).findById(1L);
        verify(leaveRepository, never()).save(any(LeaveRequest.class));
    }

    @Test
    @DisplayName("updateLeave should throw ResourceNotFoundException when leave does not exist")
    void testUpdateLeave_NotFound() {
        LeaveUpdateRequestDTO updateDTO = LeaveUpdateRequestDTO.builder()
                .reason("Does not matter")
                .build();

        when(leaveRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> leaveService.updateLeave(99L, updateDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Leave request not found with id: 99");

        verify(leaveRepository, never()).save(any(LeaveRequest.class));
    }

    @Test
    @DisplayName("deleteLeave should delete when leave exists")
    void testDeleteLeave_Success() {
        when(leaveRepository.existsById(1L)).thenReturn(true);

        leaveService.deleteLeave(1L);

        verify(leaveRepository, times(1)).existsById(1L);
        verify(leaveRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("deleteLeave should throw ResourceNotFoundException when leave does not exist")
    void testDeleteLeave_NotFound() {
        when(leaveRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> leaveService.deleteLeave(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Leave request not found with id: 99");

        verify(leaveRepository, never()).deleteById(99L);
    }
}
