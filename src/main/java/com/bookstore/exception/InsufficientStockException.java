package com.bookstore.exception;

import java.util.List;
import com.bookstore.dto.InvalidItemDetail;

public class InsufficientStockException extends RuntimeException {

    private final List<InvalidItemDetail> invalidItems;

    public InsufficientStockException(String message, List<InvalidItemDetail> invalidItems) {
        super(message);
        this.invalidItems = invalidItems;
    }

    public List<InvalidItemDetail> getInvalidItems() {
        return invalidItems;
    }
}
