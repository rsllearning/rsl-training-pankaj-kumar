package com.bookstore.service;

import com.bookstore.dto.BookCreateRequest;
import com.bookstore.dto.BookPriceUpdateRequest;
import com.bookstore.dto.BookResponse;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.model.Book;
import com.bookstore.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<BookResponse> getAllBooks(String genre, String author, String search) {
        List<Book> books;
        if (genre != null && !genre.isBlank()) {
            books = bookRepository.findByGenreIgnoreCaseAndIsDeletedFalse(genre.trim());
        } else if (author != null && !author.isBlank()) {
            books = bookRepository.findByAuthorContainingIgnoreCaseAndIsDeletedFalse(author.trim());
        } else if (search != null && !search.isBlank()) {
            books = bookRepository.findByTitleContainingIgnoreCaseAndIsDeletedFalse(search.trim());
        } else {
            books = bookRepository.findAllByIsDeletedFalse();
        }

        if (books == null || books.isEmpty()) {
            return Collections.emptyList();
        }

        List<BookResponse> responseList = new ArrayList<>();
        for (Book book : books) {
            responseList.add(BookResponse.fromEntity(book));
        }
        return responseList;
    }

    public BookResponse getBookById(String id) {
        Book book = bookRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));
        return BookResponse.fromEntity(book);
    }

    public Book getBookEntityById(String id) {
        return bookRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));
    }

    public BookResponse createBook(BookCreateRequest request) {
        String bookId = request.getId();
        if (bookId == null || bookId.isBlank()) {
            bookId = "bk_" + UUID.randomUUID().toString().substring(0, 8);
        }

        Book book = new Book(
                bookId,
                request.getTitle(),
                request.getAuthor(),
                request.getDescription(),
                request.getPrice(),
                request.getStockQuantity(),
                request.getIsbn(),
                request.getGenre()
        );

        Book saved = bookRepository.save(book);
        return BookResponse.fromEntity(saved);
    }

    public BookResponse updatePrice(String id, BookPriceUpdateRequest request) {
        Book book = bookRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));

        book.setPrice(request.getPrice());
        Book updated = bookRepository.save(book);
        return BookResponse.fromEntity(updated);
    }

    public void deleteBook(String id) {
        Book book = bookRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));

        // Soft delete for auditability and historical order integrity
        book.setIsDeleted(true);
        book.setIsActive(false);
        bookRepository.save(book);
    }
}
