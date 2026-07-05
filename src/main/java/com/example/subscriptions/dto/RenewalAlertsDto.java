package com.example.subscriptions.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class RenewalAlertsDto {
    private UUID id;
    private String title;
    private LocalDate nextPaymentDate;
    private BigDecimal amount;
    private String currency;
}
