package com.bookstore.service;

import com.bookstore.dto.CartItemRequest;
import com.bookstore.dto.CartItemResponse;
import com.bookstore.dto.CartResponse;
import com.bookstore.dto.InvalidItemDetail;
import com.bookstore.exception.InsufficientStockException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.model.Book;
import com.bookstore.model.CartItem;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.CartItemRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final BookRepository bookRepository;
    private final Object cartLock = new Object();

    public CartService(CartItemRepository cartItemRepository, BookRepository bookRepository) {
        this.cartItemRepository = cartItemRepository;
        this.bookRepository = bookRepository;
    }

    public CartResponse addItemToCart(CartItemRequest request) {
        String userId = (request.getUserId() != null && !request.getUserId().isBlank()) ? request.getUserId() : "usr_current_session";

        Book book = bookRepository.findByIdAndIsDeletedFalse(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", request.getBookId()));

        synchronized (cartLock) {
            Optional<CartItem> existingItemOpt = cartItemRepository.findByUserIdAndBookId(userId, request.getBookId());
            int currentQtyInCart = existingItemOpt.map(CartItem::getQuantity).orElse(0);
            int targetQty = currentQtyInCart + request.getQuantity();

            // Check inventory availability
            if (targetQty > book.getStockQuantity()) {
                InvalidItemDetail conflict = new InvalidItemDetail(
                        "quantity",
                        book.getId(),
                        book.getTitle(),
                        targetQty,
                        book.getStockQuantity(),
                        String.format("Adding %d units exceeds available stock (%d units currently in stock, %d already in cart).",
                                request.getQuantity(), book.getStockQuantity(), currentQtyInCart)
                );
                throw new InsufficientStockException("Requested quantity exceeds available stock.", Collections.singletonList(conflict));
            }

            // Upsert cart item
            CartItem cartItem;
            if (existingItemOpt.isPresent()) {
                cartItem = existingItemOpt.get();
                cartItem.setQuantity(targetQty);
                cartItem.setUnitPrice(book.getPrice()); // refresh latest price
            } else {
                cartItem = new CartItem(userId, book.getId(), book.getTitle(), request.getQuantity(), book.getPrice());
            }
            cartItemRepository.save(cartItem);
        }

        return getCart(userId);
    }

    public CartResponse getCart(String userId) {
        List<CartItem> items = cartItemRepository.findByUserId(userId);
        List<CartItemResponse> itemResponses = new ArrayList<>();
        int totalItems = 0;
        BigDecimal subtotal = BigDecimal.ZERO;

        if (items != null) {
            for (CartItem item : items) {
                CartItemResponse itemResp = new CartItemResponse(
                        item.getId(),
                        item.getBookId(),
                        item.getTitle(),
                        item.getQuantity(),
                        item.getUnitPrice()
                );
                itemResponses.add(itemResp);
                if (item.getQuantity() != null) {
                    totalItems += item.getQuantity();
                }
                if (itemResp.getLineTotal() != null) {
                    subtotal = subtotal.add(itemResp.getLineTotal());
                }
            }
        }

        return new CartResponse(userId, itemResponses, totalItems, subtotal, "USD");
    }

    public void clearCart(String userId) {
        cartItemRepository.deleteByUserId(userId);
    }
}
