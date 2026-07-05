package com.example.subscriptions.repository;

import com.example.subscriptions.entity.Obligation;
import com.example.subscriptions.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ObligationRepository extends JpaRepository<Obligation, UUID>, JpaSpecificationExecutor<Obligation> {
    boolean existsByTitleIgnoreCaseAndStatus(String title, Status status);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Obligation o SET o.status = :expired " +
            "WHERE o.status = :active " +
            "AND o.recurrence IS NULL " +
            "AND o.nextPaymentDate < :today")
    int expireOverdueOneTimeObligations(@Param("today") LocalDate today,
                                        @Param("active") Status active,
                                        @Param("expired") Status expired);
    List<Obligation> findByNextPaymentDateBetween(LocalDate start, LocalDate end);
}
