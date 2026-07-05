package com.example.subscriptions.dto;

import com.example.subscriptions.entity.Category;
import com.example.subscriptions.entity.Recurrence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ObligationRequestDto {

    @NotBlank
    private String title;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotBlank
    @Size(min = 3, max = 3)
    private String currency;

    @NotNull
    private Category category;
    private Recurrence recurrence;

    @NotNull
    private LocalDate nextPaymentDate;
}
