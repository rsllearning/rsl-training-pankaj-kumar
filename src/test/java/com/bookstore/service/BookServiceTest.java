package com.bookstore.service;

import com.bookstore.dto.BookCreateRequest;
import com.bookstore.dto.BookPriceUpdateRequest;
import com.bookstore.dto.BookResponse;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.model.Book;
import com.bookstore.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookServiceTest {

    private BookRepository bookRepository;
    private BookService bookService;

    @BeforeEach
    void setUp() {
        bookRepository = new BookRepository();
        bookService = new BookService(bookRepository);

        // Seed initial book
        Book book = new Book(
                "bk_101",
                "Clean Code",
                "Robert C. Martin",
                "A Handbook of Agile Software Craftsmanship",
                BigDecimal.valueOf(35.00),
                10,
                "978-0132350884",
                "Software Engineering"
        );
        bookRepository.save(book);
    }

    @Test
    @DisplayName("getAllBooks - Should return all active books")
    void testGetAllBooks() {
        List<BookResponse> books = bookService.getAllBooks(null, null, null);
        assertNotNull(books);
        assertEquals(1, books.size());
        assertEquals("Clean Code", books.get(0).getTitle());
    }

    @Test
    @DisplayName("getAllBooks - Filter by genre")
    void testGetAllBooksByGenre() {
        List<BookResponse> books = bookService.getAllBooks("Software Engineering", null, null);
        assertEquals(1, books.size());

        List<BookResponse> empty = bookService.getAllBooks("Fiction", null, null);
        assertTrue(empty.isEmpty());
    }

    @Test
    @DisplayName("getBookById - Success")
    void testGetBookByIdSuccess() {
        BookResponse response = bookService.getBookById("bk_101");
        assertNotNull(response);
        assertEquals("bk_101", response.getId());
        assertEquals("Clean Code", response.getTitle());
    }

    @Test
    @DisplayName("getBookById - Throws ResourceNotFoundException for non-existent ID")
    void testGetBookByIdNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> bookService.getBookById("non_existing_id"));
    }

    @Test
    @DisplayName("createBook - Should create and save new book")
    void testCreateBook() {
        BookCreateRequest request = new BookCreateRequest();
        request.setTitle("Refactoring");
        request.setAuthor("Martin Fowler");
        request.setDescription("Improving the Design of Existing Code");
        request.setPrice(BigDecimal.valueOf(45.00));
        request.setStockQuantity(15);
        request.setIsbn("978-0134757599");
        request.setGenre("Software Engineering");

        BookResponse created = bookService.createBook(request);
        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("Refactoring", created.getTitle());
        assertEquals(BigDecimal.valueOf(45.00), created.getPrice());
    }

    @Test
    @DisplayName("updatePrice - Success")
    void testUpdatePrice() {
        BookPriceUpdateRequest updateReq = new BookPriceUpdateRequest(BigDecimal.valueOf(29.99));
        BookResponse updated = bookService.updatePrice("bk_101", updateReq);

        assertNotNull(updated);
        assertEquals(BigDecimal.valueOf(29.99), updated.getPrice());
    }

    @Test
    @DisplayName("deleteBook - Should soft delete book")
    void testDeleteBook() {
        bookService.deleteBook("bk_101");
        assertThrows(ResourceNotFoundException.class, () -> bookService.getBookById("bk_101"));
    }
}
