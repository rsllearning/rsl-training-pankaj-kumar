package com.bookstore.config;

import com.bookstore.model.Book;
import com.bookstore.repository.BookRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final BookRepository bookRepository;

    public DataInitializer(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public void run(String... args) {
        if (bookRepository.count() == 0) {
            Book book1 = new Book(
                    "bk_849201",
                    "Designing Data-Intensive Applications",
                    "Martin Kleppmann",
                    "The definitive guide to the architecture of data systems, storage engines, distributed consensus, and scalability.",
                    BigDecimal.valueOf(39.99),
                    25,
                    "978-1449373320",
                    "Technology & Architecture"
            );

            Book book2 = new Book(
                    "bk_110934",
                    "Clean Architecture: A Craftsman's Guide",
                    "Robert C. Martin",
                    "A comprehensive guide to software structure, component design principles, and decoupling architectures.",
                    BigDecimal.valueOf(49.50),
                    15,
                    "978-0134494166",
                    "Software Engineering"
            );

            Book book3 = new Book(
                    "bk_98723",
                    "The Pragmatic Programmer",
                    "David Thomas, Andrew Hunt",
                    "Your journey to mastery: modern software development best practices, refactoring, and craftsmanship.",
                    BigDecimal.valueOf(44.95),
                    30,
                    "978-0135957059",
                    "Programming"
            );

            Book book4 = new Book(
                    "bk_441209",
                    "Building Microservices: Designing Fine-Grained Systems",
                    "Sam Newman",
                    "Key architectural concepts for building, running, and scaling decoupled microservice architectures.",
                    BigDecimal.valueOf(52.00),
                    8,
                    "978-1492034025",
                    "Cloud & Distributed Systems"
            );

            Book book5 = new Book(
                    "bk_low_stock",
                    "Rare Limited Collector's Edition Book",
                    "Special Author",
                    "A limited edition book with strictly restricted warehouse inventory.",
                    BigDecimal.valueOf(99.00),
                    2, // Low stock for testing 409 conflict
                    "978-0000000001",
                    "Collector's Item"
            );

            bookRepository.saveAll(List.of(book1, book2, book3, book4, book5));
            System.out.println(">> Seeded Online Bookstore database with initial catalog data.");
        }
    }
}
