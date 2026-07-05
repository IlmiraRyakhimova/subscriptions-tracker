package com.example.subscriptions.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PayResponseDto {
    private ObligationResponseDto obligation;
    private PaymentResponseDto payment;
}
