package com.bookstore.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    private Long id;
    private String bookId;
    private String title;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;

    public OrderItem(String bookId, String title, Integer quantity, BigDecimal unitPrice) {
        this.bookId = bookId;
        this.title = title;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        if (unitPrice != null && quantity != null) {
            this.lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        if (this.unitPrice != null && quantity != null) {
            this.lineTotal = this.unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        if (this.quantity != null && unitPrice != null) {
            this.lineTotal = unitPrice.multiply(BigDecimal.valueOf(this.quantity));
        }
    }
}
