package com.leave.management.api.service;

import com.leave.management.api.dto.LeaveCreateRequestDTO;
import com.leave.management.api.dto.LeaveResponseDTO;
import com.leave.management.api.dto.LeaveUpdateRequestDTO;
import com.leave.management.api.exception.InvalidLeaveDateException;
import com.leave.management.api.exception.ResourceNotFoundException;
import com.leave.management.api.model.LeaveRequest;
import com.leave.management.api.model.LeaveStatus;
import com.leave.management.api.repository.LeaveRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class LeaveService {

    private final LeaveRepository leaveRepository;

    public LeaveService(LeaveRepository leaveRepository) {
        this.leaveRepository = leaveRepository;
    }

    public List<LeaveResponseDTO> getAllLeaves() {
        return leaveRepository.findAll().stream()
                .sorted(Comparator.comparing(LeaveRequest::getId))
                .map(this::mapToResponseDTO)
                .toList();
    }

    public LeaveResponseDTO getLeaveById(Long id) {
        LeaveRequest leaveRequest = leaveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + id));
        return mapToResponseDTO(leaveRequest);
    }

    public LeaveResponseDTO createLeave(LeaveCreateRequestDTO createRequest) {
        validateDateRange(createRequest.getStartDate(), createRequest.getEndDate());

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employeeId(createRequest.getEmployeeId())
                .leaveType(createRequest.getLeaveType())
                .startDate(createRequest.getStartDate())
                .endDate(createRequest.getEndDate())
                .reason(createRequest.getReason())
                .status(LeaveStatus.PENDING)
                .build();

        LeaveRequest saved = leaveRepository.save(leaveRequest);
        return mapToResponseDTO(saved);
    }

    public LeaveResponseDTO updateLeave(Long id, LeaveUpdateRequestDTO updateRequest) {
        LeaveRequest existing = leaveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + id));

        LocalDate newStartDate = updateRequest.getStartDate() != null ? updateRequest.getStartDate() : existing.getStartDate();
        LocalDate newEndDate = updateRequest.getEndDate() != null ? updateRequest.getEndDate() : existing.getEndDate();

        validateDateRange(newStartDate, newEndDate);

        if (updateRequest.getLeaveType() != null) {
            existing.setLeaveType(updateRequest.getLeaveType());
        }
        if (updateRequest.getStartDate() != null) {
            existing.setStartDate(updateRequest.getStartDate());
        }
        if (updateRequest.getEndDate() != null) {
            existing.setEndDate(updateRequest.getEndDate());
        }
        if (updateRequest.getReason() != null && !updateRequest.getReason().trim().isEmpty()) {
            existing.setReason(updateRequest.getReason().trim());
        }
        if (updateRequest.getStatus() != null) {
            existing.setStatus(updateRequest.getStatus());
        }

        LeaveRequest updated = leaveRepository.save(existing);
        return mapToResponseDTO(updated);
    }

    public void deleteLeave(Long id) {
        if (!leaveRepository.existsById(id)) {
            throw new ResourceNotFoundException("Leave request not found with id: " + id);
        }
        leaveRepository.deleteById(id);
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new InvalidLeaveDateException("End date (" + endDate + ") cannot be before start date (" + startDate + ")");
        }
    }

    private LeaveResponseDTO mapToResponseDTO(LeaveRequest leaveRequest) {
        return LeaveResponseDTO.builder()
                .id(leaveRequest.getId())
                .employeeId(leaveRequest.getEmployeeId())
                .leaveType(leaveRequest.getLeaveType())
                .startDate(leaveRequest.getStartDate())
                .endDate(leaveRequest.getEndDate())
                .reason(leaveRequest.getReason())
                .status(leaveRequest.getStatus())
                .build();
    }
}
