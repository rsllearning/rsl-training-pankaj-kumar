package com.leave.management.api.dto;

import com.leave.management.api.model.LeaveStatus;
import com.leave.management.api.model.LeaveType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveUpdateRequestDTO {

    private LeaveType leaveType;

    private LocalDate startDate;

    private LocalDate endDate;

    @Size(max = 255, message = "Reason cannot exceed 255 characters")
    private String reason;

    private LeaveStatus status;
}
