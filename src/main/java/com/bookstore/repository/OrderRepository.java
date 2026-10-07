package com.bookstore.repository;

import com.bookstore.model.Order;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory repository backed by ConcurrentHashMap for thread-safe purchase order storage.
 */
@Repository
public class OrderRepository {

    private final Map<String, Order> orderStore = new ConcurrentHashMap<>();

    public Optional<Order> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(orderStore.get(id));
    }

    public Optional<Order> findByOrderNumber(String orderNumber) {
        if (orderNumber == null) return Optional.empty();
        return orderStore.values().stream()
                .filter(o -> orderNumber.equals(o.getOrderNumber()))
                .findFirst();
    }

    public List<Order> findByCustomerIdOrderByCreatedAtDesc(String customerId) {
        if (customerId == null) return Collections.emptyList();
        return orderStore.values().stream()
                .filter(o -> customerId.equals(o.getCustomerId()))
                .sorted(Comparator.comparing(Order::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    public Order save(Order order) {
        if (order.getId() == null) {
            order.setId("ord_" + UUID.randomUUID().toString().substring(0, 8));
        }
        orderStore.put(order.getId(), order);
        return order;
    }

    public List<Order> findAll() {
        return new ArrayList<>(orderStore.values());
    }

    public long count() {
        return orderStore.size();
    }

    public void clear() {
        orderStore.clear();
    }
}
