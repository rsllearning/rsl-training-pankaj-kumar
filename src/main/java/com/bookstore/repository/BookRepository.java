package com.bookstore.repository;

import com.bookstore.model.Book;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory repository backed by ConcurrentHashMap for thread-safe book inventory storage.
 */
@Repository
public class BookRepository {

    private final Map<String, Book> bookStore = new ConcurrentHashMap<>();

    public Optional<Book> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(bookStore.get(id));
    }

    public Optional<Book> findByIdAndIsDeletedFalse(String id) {
        if (id == null) return Optional.empty();
        Book book = bookStore.get(id);
        if (book != null && !Boolean.TRUE.equals(book.getIsDeleted())) {
            return Optional.of(book);
        }
        return Optional.empty();
    }

    public List<Book> findAllByIsDeletedFalse() {
        return bookStore.values().stream()
                .filter(b -> !Boolean.TRUE.equals(b.getIsDeleted()))
                .sorted(Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    public List<Book> findByGenreIgnoreCaseAndIsDeletedFalse(String genre) {
        if (genre == null) return Collections.emptyList();
        return bookStore.values().stream()
                .filter(b -> !Boolean.TRUE.equals(b.getIsDeleted()) &&
                             b.getGenre() != null &&
                             b.getGenre().equalsIgnoreCase(genre.trim()))
                .collect(Collectors.toList());
    }

    public List<Book> findByAuthorContainingIgnoreCaseAndIsDeletedFalse(String author) {
        if (author == null) return Collections.emptyList();
        return bookStore.values().stream()
                .filter(b -> !Boolean.TRUE.equals(b.getIsDeleted()) &&
                             b.getAuthor() != null &&
                             b.getAuthor().toLowerCase().contains(author.trim().toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Book> findByTitleContainingIgnoreCaseAndIsDeletedFalse(String title) {
        if (title == null) return Collections.emptyList();
        return bookStore.values().stream()
                .filter(b -> !Boolean.TRUE.equals(b.getIsDeleted()) &&
                             b.getTitle() != null &&
                             b.getTitle().toLowerCase().contains(title.trim().toLowerCase()))
                .collect(Collectors.toList());
    }

    public Book save(Book book) {
        if (book.getId() == null) {
            book.setId("bk_" + UUID.randomUUID().toString().substring(0, 8));
        }
        bookStore.put(book.getId(), book);
        return book;
    }

    public List<Book> saveAll(Iterable<Book> books) {
        List<Book> saved = new ArrayList<>();
        for (Book book : books) {
            saved.add(save(book));
        }
        return saved;
    }

    public void deleteById(String id) {
        if (id != null) {
            bookStore.remove(id);
        }
    }

    public long count() {
        return bookStore.values().stream()
                .filter(b -> !Boolean.TRUE.equals(b.getIsDeleted()))
                .count();
    }

    public void clear() {
        bookStore.clear();
    }
}
