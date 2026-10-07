package com.bookstore.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemRequest {

    private String userId;

    @NotBlank(message = "Book ID is required to add an item to the cart")
    private String bookId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be a positive integer greater than or equal to 1")
    private Integer quantity;
}
