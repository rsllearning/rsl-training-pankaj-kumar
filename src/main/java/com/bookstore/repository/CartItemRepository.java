package com.bookstore.repository;

import com.bookstore.model.CartItem;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory repository backed by ConcurrentHashMap for thread-safe shopping
 * cart sessions.
 */
@Repository
public class CartItemRepository {

    // Key: userId -> Map<bookId, CartItem>
    private final Map<String, Map<String, CartItem>> userCarts = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public List<CartItem> findByUserId(String userId) {
        if (userId == null)
            return Collections.emptyList();
        Map<String, CartItem> cart = userCarts.get(userId);
        if (cart == null)
            return Collections.emptyList();
        return new ArrayList<>(cart.values());
    }

    public Optional<CartItem> findByUserIdAndBookId(String userId, String bookId) {
        if (userId == null || bookId == null)
            return Optional.empty();
        Map<String, CartItem> cart = userCarts.get(userId);
        if (cart == null)
            return Optional.empty();
        return Optional.ofNullable(cart.get(bookId));
    }

    public CartItem save(CartItem cartItem) {
        if (cartItem.getId() == null) {
            cartItem.setId(idGenerator.getAndIncrement());
        }
        userCarts.computeIfAbsent(cartItem.getUserId(), k -> new ConcurrentHashMap<>())
                .put(cartItem.getBookId(), cartItem);
        return cartItem;
    }

    public void deleteByUserId(String userId) {
        if (userId != null) {
            userCarts.remove(userId);
        }
    }

    public void delete(CartItem cartItem) {
        if (cartItem != null && cartItem.getUserId() != null && cartItem.getBookId() != null) {
            Map<String, CartItem> cart = userCarts.get(cartItem.getUserId());
            if (cart != null) {
                cart.remove(cartItem.getBookId());
            }
        }
    }

    public void clear() {
        userCarts.clear();
    }
}
