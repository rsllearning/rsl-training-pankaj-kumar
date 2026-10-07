package com.bookstore.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class CartItem {

    private Long id;
    private String userId;
    private String bookId;
    private String title;
    private Integer quantity;
    private BigDecimal unitPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CartItem() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public CartItem(String userId, String bookId, String title, Integer quantity, BigDecimal unitPrice) {
        this.userId = userId;
        this.bookId = bookId;
        this.title = title;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
