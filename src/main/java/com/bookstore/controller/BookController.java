package com.bookstore.controller;

import com.bookstore.dto.ApiSuccessResponse;
import com.bookstore.dto.BookCreateRequest;
import com.bookstore.dto.BookPriceUpdateRequest;
import com.bookstore.dto.BookResponse;
import com.bookstore.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/v1/books")
@CrossOrigin(origins = "*")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * Requirement: Fetch list of all books
     * HTTP Method: GET
     * Safe: Yes, Idempotent: Yes
     */
    @GetMapping
    public ResponseEntity<ApiSuccessResponse<List<BookResponse>>> getAllBooks(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String search) {
        List<BookResponse> books = bookService.getAllBooks(genre, author, search);
        return ResponseEntity.ok(new ApiSuccessResponse<>("Fetched books successfully", books));
    }

    /**
     * Requirement: Fetch details of a specific book by ID
     * HTTP Method: GET
     * Safe: Yes, Idempotent: Yes
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiSuccessResponse<BookResponse>> getBookById(@PathVariable String id) {
        BookResponse book = bookService.getBookById(id);
        return ResponseEntity.ok(new ApiSuccessResponse<>("Fetched book details successfully", book));
    }

    /**
     * Requirement: Add a new book to the inventory
     * HTTP Method: POST
     * Safe: No, Idempotent: No
     */
    @PostMapping
    public ResponseEntity<ApiSuccessResponse<BookResponse>> createBook(@Valid @RequestBody BookCreateRequest request) {
        BookResponse created = bookService.createBook(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location)
                .body(new ApiSuccessResponse<>("Book added to inventory successfully", created));
    }

    /**
     * Requirement: Update price of an existing book
     * HTTP Method: PATCH
     * Safe: No, Idempotent: Yes
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ApiSuccessResponse<BookResponse>> updateBookPrice(
            @PathVariable String id,
            @Valid @RequestBody BookPriceUpdateRequest request) {
        BookResponse updated = bookService.updatePrice(id, request);
        return ResponseEntity.ok(new ApiSuccessResponse<>("Book price updated successfully", updated));
    }

    /**
     * Sub-resource endpoint variant for price update: PATCH /v1/books/{id}/price
     */
    @PatchMapping("/{id}/price")
    public ResponseEntity<ApiSuccessResponse<BookResponse>> updateBookPriceSubresource(
            @PathVariable String id,
            @Valid @RequestBody BookPriceUpdateRequest request) {
        BookResponse updated = bookService.updatePrice(id, request);
        return ResponseEntity.ok(new ApiSuccessResponse<>("Book price updated successfully", updated));
    }

    /**
     * Requirement: Delete a book record from inventory
     * HTTP Method: DELETE
     * Safe: No, Idempotent: Yes
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable String id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}
