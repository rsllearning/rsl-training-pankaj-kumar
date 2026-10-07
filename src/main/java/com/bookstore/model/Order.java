package com.bookstore.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class Order {

    private String id;
    private String orderNumber;
    private String customerId;
    private String orderStatus; // PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
    private String paymentStatus; // PENDING, PAID, FAILED, REFUNDED
    private List<OrderItem> items = new ArrayList<>();
    private BigDecimal subtotal;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private BigDecimal shippingFee = BigDecimal.ZERO;
    private BigDecimal taxAmount = BigDecimal.ZERO;
    private BigDecimal totalAmount;
    private String currency = "USD";
    private Address shippingAddress;
    private String paymentMethod;
    private String paymentTransactionId;
    private String cardLast4;
    private String notes;
    private LocalDateTime estimatedDeliveryDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Order() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.discountAmount = BigDecimal.ZERO;
        this.shippingFee = BigDecimal.ZERO;
        this.taxAmount = BigDecimal.ZERO;
        this.items = new ArrayList<>();
    }

    public void addItem(OrderItem item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(item);
    }
}
