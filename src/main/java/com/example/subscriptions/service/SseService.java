package com.example.subscriptions.service;

import com.example.subscriptions.dto.ObligationDeletedEvent;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseService {
    SseEmitter subscribe();
    void broadcast(ObligationDeletedEvent event);
}