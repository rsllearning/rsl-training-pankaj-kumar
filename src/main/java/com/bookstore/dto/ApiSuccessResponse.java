package com.bookstore.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiSuccessResponse<T> {

    private String status = "success";
    private String message;
    private T data;

    public ApiSuccessResponse(T data) {
        this.status = "success";
        this.data = data;
    }

    public ApiSuccessResponse(String message, T data) {
        this.status = "success";
        this.message = message;
        this.data = data;
    }
}
