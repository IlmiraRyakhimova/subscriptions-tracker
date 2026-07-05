package com.example.subscriptions.dto;

import com.example.subscriptions.entity.Category;
import com.example.subscriptions.entity.Recurrence;
import com.example.subscriptions.entity.Status;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class ObligationResponseDto {
    private UUID id;
    private String title;
    private BigDecimal amount;
    private String currency;
    private Category category;
    private Recurrence recurrence;
    private LocalDate nextPaymentDate;
    private Status status;
    private Instant createdAt;
    private Instant updatedAt;
}
