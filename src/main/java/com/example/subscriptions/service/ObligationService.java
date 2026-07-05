package com.example.subscriptions.service;

import com.example.subscriptions.dto.*;
import com.example.subscriptions.entity.Category;
import com.example.subscriptions.entity.Status;

import java.util.List;
import java.util.UUID;

public interface ObligationService {

    CreateObligationResponseDto createObligation(ObligationRequestDto request);
    List<ObligationResponseDto> getAllObligations(Category category, Status status);
    PayResponseDto pay(UUID id);
    UpcomingResponseDto getUpcoming(int days);
    ObligationResponseDto cancel(UUID id);
    void delete(UUID id);
}
