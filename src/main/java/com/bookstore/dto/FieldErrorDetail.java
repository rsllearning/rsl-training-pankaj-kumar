package com.bookstore.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FieldErrorDetail {

    private String field;
    private Object rejectedValue;
    private String rule;
    private String message;
}
