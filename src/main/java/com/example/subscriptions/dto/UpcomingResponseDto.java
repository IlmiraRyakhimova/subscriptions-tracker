package com.example.subscriptions.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class UpcomingResponseDto {
    private List<ObligationResponseDto> obligations;
    private Map<String, BigDecimal> totals;
    private List<RenewalAlertsDto> renewalAlerts;

}
