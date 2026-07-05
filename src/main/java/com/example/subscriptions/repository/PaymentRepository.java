package com.example.subscriptions.repository;

import com.example.subscriptions.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    void deleteByObligationId(UUID obligationId);
}
