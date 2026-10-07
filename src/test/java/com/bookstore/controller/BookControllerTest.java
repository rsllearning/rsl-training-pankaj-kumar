package com.bookstore.controller;

import com.bookstore.dto.BookCreateRequest;
import com.bookstore.dto.BookPriceUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /v1/books - Should fetch list of all active books")
    void testGetAllBooks() throws Exception {
        mockMvc.perform(get("/v1/books")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].id", notNullValue()));
    }

    @Test
    @DisplayName("GET /v1/books/{id} - Should fetch details of a specific book by ID")
    void testGetBookById() throws Exception {
        mockMvc.perform(get("/v1/books/bk_849201")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.id", is("bk_849201")))
                .andExpect(jsonPath("$.data.title", is("Designing Data-Intensive Applications")));
    }

    @Test
    @DisplayName("POST /v1/books - Should create a new book and return 201 Created with Location header")
    void testCreateBook() throws Exception {
        BookCreateRequest request = new BookCreateRequest();
        request.setTitle("Domain-Driven Design");
        request.setAuthor("Eric Evans");
        request.setDescription("Tackling complexity in the heart of software.");
        request.setPrice(BigDecimal.valueOf(59.99));
        request.setStockQuantity(20);
        request.setIsbn("978-0321125217");
        request.setGenre("Software Engineering");

        mockMvc.perform(post("/v1/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.title", is("Domain-Driven Design")))
                .andExpect(jsonPath("$.data.price", is(59.99)));
    }

    @Test
    @DisplayName("PATCH /v1/books/{id} - Should update book price")
    void testUpdateBookPrice() throws Exception {
        BookPriceUpdateRequest request = new BookPriceUpdateRequest(BigDecimal.valueOf(34.99));

        mockMvc.perform(patch("/v1/books/bk_849201")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.price", is(34.99)));
    }

    @Test
    @DisplayName("DELETE /v1/books/{id} - Should delete book from inventory and return 204 No Content")
    void testDeleteBook() throws Exception {
        // Create a temporary book to delete
        BookCreateRequest request = new BookCreateRequest();
        request.setId("bk_temp_to_delete");
        request.setTitle("Temporary Book");
        request.setAuthor("Temp Author");
        request.setPrice(BigDecimal.valueOf(10.00));
        request.setStockQuantity(5);

        mockMvc.perform(post("/v1/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/v1/books/bk_temp_to_delete"))
                .andExpect(status().isNoContent());

        // Verifying it is now not found
        mockMvc.perform(get("/v1/books/bk_temp_to_delete"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode", is("ERR_NOT_FOUND")));
    }
}
