package com.bookstore.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookPriceUpdateRequest {

    @NotNull(message = "Price field is required for price update")
    @DecimalMin(value = "0.01", message = "Price must be a positive decimal greater than 0")
    private BigDecimal price;
}
