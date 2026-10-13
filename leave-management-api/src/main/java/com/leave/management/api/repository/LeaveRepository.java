package com.leave.management.api.repository;

import com.leave.management.api.model.LeaveRequest;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class LeaveRepository {

    private final Map<Long, LeaveRequest> storage = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    public LeaveRequest save(LeaveRequest leaveRequest) {
        if (leaveRequest.getId() == null) {
            leaveRequest.setId(idSequence.getAndIncrement());
        }
        storage.put(leaveRequest.getId(), leaveRequest);
        return leaveRequest;
    }

    public Optional<LeaveRequest> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<LeaveRequest> findAll() {
        return new ArrayList<>(storage.values());
    }

    public boolean existsById(Long id) {
        return storage.containsKey(id);
    }

    public boolean deleteById(Long id) {
        return storage.remove(id) != null;
    }

    public void deleteAll() {
        storage.clear();
        idSequence.set(1);
    }
}
