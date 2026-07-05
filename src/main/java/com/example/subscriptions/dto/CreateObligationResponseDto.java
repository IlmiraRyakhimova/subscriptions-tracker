package com.example.subscriptions.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
public class CreateObligationResponseDto {
    private ObligationResponseDto obligation;
    private String warning;
}
