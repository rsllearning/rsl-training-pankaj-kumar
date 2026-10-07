package com.bookstore.service;

import com.bookstore.dto.InvalidItemDetail;
import com.bookstore.dto.OrderCreateRequest;
import com.bookstore.dto.OrderItemRequest;
import com.bookstore.dto.OrderResponse;
import com.bookstore.dto.ShippingAddressRequest;
import com.bookstore.exception.InsufficientStockException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.model.Address;
import com.bookstore.model.Book;
import com.bookstore.model.Order;
import com.bookstore.model.OrderItem;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final BookRepository bookRepository;
    private final Object lock = new Object();

    public OrderService(OrderRepository orderRepository, BookRepository bookRepository) {
        this.orderRepository = orderRepository;
        this.bookRepository = bookRepository;
    }

    public OrderResponse createOrder(OrderCreateRequest request) {
        List<InvalidItemDetail> inventoryConflicts = new ArrayList<>();
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        synchronized (lock) {
            int index = 0;
            for (OrderItemRequest itemReq : request.getItems()) {
                Book book = bookRepository.findByIdAndIsDeletedFalse(itemReq.getBookId())
                        .orElseThrow(() -> new ResourceNotFoundException("Book", "bookId", itemReq.getBookId()));

                // Verify stock availability
                if (book.getStockQuantity() < itemReq.getQuantity()) {
                    inventoryConflicts.add(new InvalidItemDetail(
                            "items[" + index + "].quantity",
                            book.getId(),
                            book.getTitle(),
                            itemReq.getQuantity(),
                            book.getStockQuantity(),
                            String.format("Requested quantity (%d) exceeds available warehouse stock (%d).",
                                    itemReq.getQuantity(), book.getStockQuantity())
                    ));
                } else {
                    // Authoritative pricing from catalog to prevent client tampering
                    BigDecimal unitPrice = book.getPrice();
                    OrderItem orderItem = new OrderItem(book.getId(), book.getTitle(), itemReq.getQuantity(), unitPrice);
                    orderItems.add(orderItem);

                    subtotal = subtotal.add(orderItem.getLineTotal());
                }
                index++;
            }

            // If any stock conflict was detected, reject order placement atomically
            if (!inventoryConflicts.isEmpty()) {
                throw new InsufficientStockException(
                        "One or more items in the order cannot be fulfilled due to inventory limitations.",
                        inventoryConflicts
                );
            }

            // Deduct stock for all items
            for (OrderItemRequest itemReq : request.getItems()) {
                Book book = bookRepository.findByIdAndIsDeletedFalse(itemReq.getBookId()).orElseThrow();
                book.setStockQuantity(book.getStockQuantity() - itemReq.getQuantity());
                bookRepository.save(book);
            }
        }

        // Calculate pricing summary (tax 8%, flat shipping $5.00)
        BigDecimal shippingFee = subtotal.compareTo(BigDecimal.valueOf(100)) > 0 ? BigDecimal.valueOf(0.00) : BigDecimal.valueOf(5.00);
        BigDecimal taxAmount = subtotal.multiply(BigDecimal.valueOf(0.08)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal totalAmount = subtotal.add(taxAmount).add(shippingFee).subtract(discountAmount);

        // Build Order entity
        Order order = new Order();
        String orderId = "ord_" + LocalDateTime.now().getYear() + "_" + UUID.randomUUID().toString().substring(0, 8);
        order.setId(orderId);
        order.setOrderNumber("ORD-" + LocalDateTime.now().getYear() + "-" + (10000 + (int)(Math.random() * 89999)));
        order.setCustomerId(request.getCustomerId());
        order.setOrderStatus("CONFIRMED");
        order.setPaymentStatus("PAID");
        order.setSubtotal(subtotal);
        order.setDiscountAmount(discountAmount);
        order.setShippingFee(shippingFee);
        order.setTaxAmount(taxAmount);
        order.setTotalAmount(totalAmount);
        order.setCurrency(request.getPayment().getCurrency() != null ? request.getPayment().getCurrency() : "USD");

        // Shipping address
        ShippingAddressRequest shipReq = request.getShippingAddress();
        Address shipAddr = new Address(
                shipReq.getFullName(),
                shipReq.getStreetAddress(),
                shipReq.getCity(),
                shipReq.getState(),
                shipReq.getPostalCode(),
                shipReq.getCountry(),
                shipReq.getPhoneNumber()
        );
        order.setShippingAddress(shipAddr);

        // Payment details
        order.setPaymentMethod(request.getPayment().getPaymentMethod());
        order.setPaymentTransactionId("txn_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        order.setCardLast4("4242");
        order.setNotes(request.getNotes());
        order.setEstimatedDeliveryDate(LocalDateTime.now().plusDays(5));

        // Add items to order
        for (OrderItem item : orderItems) {
            order.addItem(item);
        }

        Order saved = orderRepository.save(order);
        return OrderResponse.fromEntity(saved);
    }

    public OrderResponse getOrderById(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        return OrderResponse.fromEntity(order);
    }
    public List<OrderResponse> getOrdersByCustomerId(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            return Collections.emptyList();
        }
        List<Order> orders = orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        if (orders == null || orders.isEmpty()) {
            return Collections.emptyList();
        }
        List<OrderResponse> responseList = new ArrayList<>();
        for (Order order : orders) {
            responseList.add(OrderResponse.fromEntity(order));
        }
        return responseList;
    }

    public List<OrderResponse> getOrdersByUserId(String userId) {
        return getOrdersByCustomerId(userId);
    }
}
