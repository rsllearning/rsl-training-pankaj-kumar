package com.bookstore.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDetailsResponse {

    private String transactionId;
    private String paymentMethod;
    private String cardLast4;
    private LocalDateTime paidAt;
}
