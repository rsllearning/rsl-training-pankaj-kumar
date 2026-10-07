package com.bookstore.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InvalidItemDetail {

    private String field;
    private String bookId;
    private String bookTitle;
    private Integer requestedQuantity;
    private Integer availableStock;
    private String message;
}
