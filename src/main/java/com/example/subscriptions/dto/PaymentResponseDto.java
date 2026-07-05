package com.example.subscriptions.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class PaymentResponseDto {
    private UUID id;
    private UUID obligationId;
    private BigDecimal amount;
    private String currency;
    private Instant paidAt;
}
