package com.bookstore.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class Book {

    private String id;
    private String title;
    private String author;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String isbn;
    private String genre;
    private Boolean isActive = true;
    private Boolean isDeleted = false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Book() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isActive = true;
        this.isDeleted = false;
    }

    public Book(String id, String title, String author, String description, BigDecimal price, Integer stockQuantity, String isbn, String genre) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.isbn = isbn;
        this.genre = genre;
        this.isActive = true;
        this.isDeleted = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
