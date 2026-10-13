package com.leave.management.api.repository;

import com.leave.management.api.model.LeaveRequest;
import com.leave.management.api.model.LeaveStatus;
import com.leave.management.api.model.LeaveType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class LeaveRepositoryTest {

    private LeaveRepository repository;

    @BeforeEach
    void setUp() {
        repository = new LeaveRepository();
    }

    @Test
    @DisplayName("save should generate sequential IDs and store leaves")
    void testSaveAndFindById() {
        LeaveRequest leave = LeaveRequest.builder()
                .employeeId(200L)
                .leaveType(LeaveType.CASUAL)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .reason("Personal work")
                .status(LeaveStatus.PENDING)
                .build();

        LeaveRequest saved = repository.save(leave);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getId()).isEqualTo(1L);

        Optional<LeaveRequest> found = repository.findById(1L);
        assertThat(found).isPresent();
        assertThat(found.get().getEmployeeId()).isEqualTo(200L);
    }

    @Test
    @DisplayName("save should update an existing entity when ID is provided")
    void testSave_UpdateExisting() {
        LeaveRequest leave = LeaveRequest.builder()
                .employeeId(200L)
                .leaveType(LeaveType.CASUAL)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .reason("Initial reason")
                .status(LeaveStatus.PENDING)
                .build();

        LeaveRequest saved = repository.save(leave);
        Long id = saved.getId();

        saved.setReason("Updated reason");
        saved.setStatus(LeaveStatus.APPROVED);
        repository.save(saved);

        Optional<LeaveRequest> found = repository.findById(id);
        assertThat(found).isPresent();
        assertThat(found.get().getReason()).isEqualTo("Updated reason");
        assertThat(found.get().getStatus()).isEqualTo(LeaveStatus.APPROVED);
        assertThat(repository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("findById should return empty optional when ID does not exist")
    void testFindById_NotFound() {
        Optional<LeaveRequest> found = repository.findById(999L);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("findAll should return empty list when repository is empty")
    void testFindAll_Empty() {
        List<LeaveRequest> list = repository.findAll();
        assertThat(list).isEmpty();
    }

    @Test
    @DisplayName("findAll should return all stored requests")
    void testFindAll() {
        repository.save(LeaveRequest.builder().employeeId(1L).build());
        repository.save(LeaveRequest.builder().employeeId(2L).build());

        List<LeaveRequest> list = repository.findAll();
        assertThat(list).hasSize(2);
    }

    @Test
    @DisplayName("existsById should return false when entity does not exist")
    void testExistsById_NotFound() {
        boolean exists = repository.existsById(999L);
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("deleteById should remove entity")
    void testDeleteById() {
        LeaveRequest saved = repository.save(LeaveRequest.builder().employeeId(1L).build());
        Long id = saved.getId();

        assertThat(repository.existsById(id)).isTrue();
        boolean deleted = repository.deleteById(id);

        assertThat(deleted).isTrue();
        assertThat(repository.existsById(id)).isFalse();
        assertThat(repository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("deleteById should return false when entity does not exist")
    void testDeleteById_NotFound() {
        boolean deleted = repository.deleteById(999L);
        assertThat(deleted).isFalse();
    }

    @Test
    @DisplayName("deleteAll should clear all entities and reset sequence")
    void testDeleteAll() {
        repository.save(LeaveRequest.builder().employeeId(1L).build());
        repository.save(LeaveRequest.builder().employeeId(2L).build());
        assertThat(repository.findAll()).hasSize(2);

        repository.deleteAll();

        assertThat(repository.findAll()).isEmpty();
        LeaveRequest newLeave = repository.save(LeaveRequest.builder().employeeId(3L).build());
        assertThat(newLeave.getId()).isEqualTo(1L);
    }
}
