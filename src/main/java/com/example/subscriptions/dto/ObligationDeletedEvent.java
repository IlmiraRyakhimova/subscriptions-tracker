package com.example.subscriptions.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ObligationDeletedEvent {
    private String type;
    private UUID id;
}
