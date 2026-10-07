package com.bookstore.service;

import com.bookstore.dto.CartItemRequest;
import com.bookstore.dto.CartResponse;
import com.bookstore.exception.InsufficientStockException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.model.Book;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.CartItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CartServiceTest {

    private CartItemRepository cartItemRepository;
    private BookRepository bookRepository;
    private CartService cartService;

    @BeforeEach
    void setUp() {
        cartItemRepository = new CartItemRepository();
        bookRepository = new BookRepository();
        cartService = new CartService(cartItemRepository, bookRepository);

        Book book = new Book("bk_1", "The Pragmatic Programmer", "Andy Hunt", "Desc", BigDecimal.valueOf(40.00), 5, "333", "Software");
        bookRepository.save(book);
    }

    @Test
    @DisplayName("addItemToCart - Successfully adds new item and calculates subtotal")
    void testAddItemToCartSuccess() {
        CartItemRequest request = new CartItemRequest("usr_1", "bk_1", 2);
        CartResponse cart = cartService.addItemToCart(request);

        assertNotNull(cart);
        assertEquals("usr_1", cart.getUserId());
        assertEquals(1, cart.getItems().size());
        assertEquals(2, cart.getTotalItems());
        assertEquals(BigDecimal.valueOf(80.00), cart.getSubtotal());
    }

    @Test
    @DisplayName("addItemToCart - Incrementing existing item quantity in cart")
    void testAddItemToCartIncrement() {
        cartService.addItemToCart(new CartItemRequest("usr_1", "bk_1", 2));
        CartResponse cart = cartService.addItemToCart(new CartItemRequest("usr_1", "bk_1", 1));

        assertEquals(1, cart.getItems().size());
        assertEquals(3, cart.getTotalItems());
        assertEquals(BigDecimal.valueOf(120.00), cart.getSubtotal());
    }

    @Test
    @DisplayName("addItemToCart - Throws InsufficientStockException when adding more than stock")
    void testAddItemToCartExceedStock() {
        // Stock is 5, attempting to add 6
        CartItemRequest request = new CartItemRequest("usr_1", "bk_1", 6);
        assertThrows(InsufficientStockException.class, () -> cartService.addItemToCart(request));
    }

    @Test
    @DisplayName("addItemToCart - Throws ResourceNotFoundException when book not found")
    void testAddItemToCartBookNotFound() {
        CartItemRequest request = new CartItemRequest("usr_1", "bk_invalid", 1);
        assertThrows(ResourceNotFoundException.class, () -> cartService.addItemToCart(request));
    }

    @Test
    @DisplayName("getCart and clearCart - Verifies cart retrieval and reset")
    void testGetAndClearCart() {
        cartService.addItemToCart(new CartItemRequest("usr_1", "bk_1", 1));
        CartResponse cart = cartService.getCart("usr_1");
        assertEquals(1, cart.getTotalItems());

        cartService.clearCart("usr_1");
        CartResponse emptyCart = cartService.getCart("usr_1");
        assertEquals(0, emptyCart.getTotalItems());
        assertTrue(emptyCart.getItems().isEmpty());
    }
}
