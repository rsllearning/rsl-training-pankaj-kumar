package com.leave.management.api.controller;

import com.leave.management.api.dto.LeaveCreateRequestDTO;
import com.leave.management.api.dto.LeaveResponseDTO;
import com.leave.management.api.dto.LeaveUpdateRequestDTO;
import com.leave.management.api.service.LeaveService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1")
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @GetMapping("/leaves")
    public ResponseEntity<List<LeaveResponseDTO>> getAllLeaves() {
        List<LeaveResponseDTO> leaves = leaveService.getAllLeaves();
        return ResponseEntity.ok(leaves);
    }

    @GetMapping("/leaves/{id}")
    public ResponseEntity<LeaveResponseDTO> getLeaveById(@PathVariable Long id) {
        LeaveResponseDTO leave = leaveService.getLeaveById(id);
        return ResponseEntity.ok(leave);
    }

    @PostMapping("/leaves")
    public ResponseEntity<LeaveResponseDTO> createLeave(@Valid @RequestBody LeaveCreateRequestDTO createRequest) {
        LeaveResponseDTO created = leaveService.createLeave(createRequest);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/leaves/{id}")
    public ResponseEntity<LeaveResponseDTO> updateLeave(
            @PathVariable Long id,
            @Valid @RequestBody LeaveUpdateRequestDTO updateRequest) {
        LeaveResponseDTO updated = leaveService.updateLeave(id, updateRequest);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/leaves/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteLeave(@PathVariable Long id) {
        leaveService.deleteLeave(id);
        return ResponseEntity.noContent().build();
    }
}
