package com.example.subscriptions.web.controller;

import com.example.subscriptions.dto.*;
import com.example.subscriptions.entity.Category;
import com.example.subscriptions.entity.Status;
import com.example.subscriptions.service.ObligationService;
import com.example.subscriptions.service.SseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/obligations")
public class ObligationController {
    private final ObligationService obligationService;
    private final SseService sseService;

    public ObligationController(ObligationService obligationService, SseService sseService) {
        this.obligationService = obligationService;
        this.sseService = sseService;
    }

    @PostMapping
    public ResponseEntity<CreateObligationResponseDto> createObligation(@RequestBody @Valid ObligationRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(obligationService.createObligation(request));
    }

    @GetMapping
    public ResponseEntity<List<ObligationResponseDto>> getAllObligations(
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) Status status
            ) {
        return ResponseEntity.ok(obligationService.getAllObligations(category, status));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<PayResponseDto> pay(@PathVariable UUID id) {
        return ResponseEntity.ok(obligationService.pay(id));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<UpcomingResponseDto> getUpcoming(@RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(obligationService.getUpcoming(days));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ObligationResponseDto> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(obligationService.cancel(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        obligationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/events")
    public SseEmitter events() {
        return sseService.subscribe();
    }


}
