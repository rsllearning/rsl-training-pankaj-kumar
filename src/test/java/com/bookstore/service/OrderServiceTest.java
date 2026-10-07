package com.bookstore.service;

import com.bookstore.dto.*;
import com.bookstore.exception.InsufficientStockException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.model.Book;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {

    private OrderRepository orderRepository;
    private BookRepository bookRepository;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderRepository = new OrderRepository();
        bookRepository = new BookRepository();
        orderService = new OrderService(orderRepository, bookRepository);

        Book book1 = new Book("bk_1", "Clean Code", "Robert C. Martin", "Desc", BigDecimal.valueOf(30.00), 10, "111", "Programming");
        Book book2 = new Book("bk_2", "Refactoring", "Martin Fowler", "Desc", BigDecimal.valueOf(40.00), 5, "222", "Programming");
        bookRepository.save(book1);
        bookRepository.save(book2);
    }

    @Test
    @DisplayName("createOrder - Successful placement deducts stock and calculates totals")
    void testCreateOrderSuccess() {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setCustomerId("usr_test_1");

        OrderItemRequest item1 = new OrderItemRequest("bk_1", 2, BigDecimal.valueOf(30.00));
        request.setItems(List.of(item1));

        ShippingAddressRequest shipping = new ShippingAddressRequest("Alice Smith", "123 Main St", "City", "State", "12345", "USA", "1234567890");
        request.setShippingAddress(shipping);

        PaymentRequest payment = new PaymentRequest("CREDIT_CARD", "tok_123", "USD");
        request.setPayment(payment);

        OrderResponse order = orderService.createOrder(request);

        assertNotNull(order);
        assertNotNull(order.getOrderId());
        assertEquals("CONFIRMED", order.getOrderStatus());
        assertEquals("PAID", order.getPaymentStatus());
        assertEquals(1, order.getItems().size());
        assertEquals(BigDecimal.valueOf(60.00), order.getPricingSummary().getSubtotal());

        // Verify stock was deducted: 10 - 2 = 8
        Book updatedBook = bookRepository.findById("bk_1").orElseThrow();
        assertEquals(8, updatedBook.getStockQuantity());
    }

    @Test
    @DisplayName("createOrder - Throws InsufficientStockException when quantity exceeds stock")
    void testCreateOrderInsufficientStock() {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setCustomerId("usr_test_1");

        // Request 15 units when only 10 in stock
        OrderItemRequest item = new OrderItemRequest("bk_1", 15, BigDecimal.valueOf(30.00));
        request.setItems(List.of(item));

        ShippingAddressRequest shipping = new ShippingAddressRequest("Alice Smith", "123 Main St", "City", "State", "12345", "USA", "1234567890");
        request.setShippingAddress(shipping);
        request.setPayment(new PaymentRequest("CREDIT_CARD", "tok_123", "USD"));

        InsufficientStockException ex = assertThrows(InsufficientStockException.class, () -> orderService.createOrder(request));
        assertNotNull(ex.getInvalidItems());
        assertEquals(1, ex.getInvalidItems().size());
        assertEquals("bk_1", ex.getInvalidItems().get(0).getBookId());

        // Verify stock was NOT deducted
        Book book = bookRepository.findById("bk_1").orElseThrow();
        assertEquals(10, book.getStockQuantity());
    }

    @Test
    @DisplayName("createOrder - Throws ResourceNotFoundException for unknown book")
    void testCreateOrderBookNotFound() {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setCustomerId("usr_test_1");

        OrderItemRequest item = new OrderItemRequest("bk_unknown", 1, BigDecimal.valueOf(10.00));
        request.setItems(List.of(item));

        ShippingAddressRequest shipping = new ShippingAddressRequest("Alice Smith", "123 Main St", "City", "State", "12345", "USA", "1234567890");
        request.setShippingAddress(shipping);
        request.setPayment(new PaymentRequest("CREDIT_CARD", "tok_123", "USD"));

        assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder(request));
    }

    @Test
    @DisplayName("getOrderById - Success and Not Found")
    void testGetOrderById() {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setCustomerId("usr_test_1");
        request.setItems(List.of(new OrderItemRequest("bk_1", 1, BigDecimal.valueOf(30.00))));
        request.setShippingAddress(new ShippingAddressRequest("Alice Smith", "123 Main St", "City", "State", "12345", "USA", "1234567890"));
        request.setPayment(new PaymentRequest("CREDIT_CARD", "tok_123", "USD"));

        OrderResponse created = orderService.createOrder(request);

        OrderResponse found = orderService.getOrderById(created.getOrderId());
        assertNotNull(found);
        assertEquals(created.getOrderId(), found.getOrderId());

        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderById("non_existent_order"));
    }

    @Test
    @DisplayName("getOrdersByCustomerId - Returns customer order history")
    void testGetOrdersByCustomerId() {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setCustomerId("usr_alice");
        request.setItems(List.of(new OrderItemRequest("bk_1", 1, BigDecimal.valueOf(30.00))));
        request.setShippingAddress(new ShippingAddressRequest("Alice Smith", "123 Main St", "City", "State", "12345", "USA", "1234567890"));
        request.setPayment(new PaymentRequest("CREDIT_CARD", "tok_123", "USD"));

        orderService.createOrder(request);

        List<OrderResponse> orders = orderService.getOrdersByCustomerId("usr_alice");
        assertNotNull(orders);
        assertEquals(1, orders.size());
        assertEquals("usr_alice", orders.get(0).getCustomerId());
    }
}
