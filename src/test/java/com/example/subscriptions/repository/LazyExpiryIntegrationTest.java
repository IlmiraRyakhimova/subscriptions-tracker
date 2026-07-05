package com.example.subscriptions.repository;

import com.example.subscriptions.entity.Category;
import com.example.subscriptions.entity.Obligation;
import com.example.subscriptions.entity.Recurrence;
import com.example.subscriptions.entity.Status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LazyExpiryIntegrationTest {

    @Autowired
    private ObligationRepository obligationRepository;

    @Test
    void expireOverdueOneTimeObligations_expiresOneTimeButKeepsRecurring() {

        Obligation oneTime = new Obligation();
        oneTime.setTitle("Разовый просроченный");
        oneTime.setAmount(new BigDecimal("100"));
        oneTime.setCurrency("RUB");
        oneTime.setCategory(Category.BILL);
        oneTime.setRecurrence(null);
        oneTime.setNextPaymentDate(LocalDate.now().minusDays(5));
        oneTime.setStatus(Status.ACTIVE);

        Obligation recurring = new Obligation();
        recurring.setTitle("Рекуррентный просроченный");
        recurring.setAmount(new BigDecimal("299"));
        recurring.setCurrency("RUB");
        recurring.setCategory(Category.SUBSCRIPTION);
        recurring.setRecurrence(Recurrence.MONTHLY);
        recurring.setNextPaymentDate(LocalDate.now().minusDays(5));
        recurring.setStatus(Status.ACTIVE);

        oneTime = obligationRepository.save(oneTime);
        recurring = obligationRepository.save(recurring);
        obligationRepository.expireOverdueOneTimeObligations(LocalDate.now(), Status.ACTIVE, Status.EXPIRED);

        UUID oneTimeId = oneTime.getId();
        UUID recurringId = recurring.getId();

        Obligation reloadedOneTime = obligationRepository.findById(oneTimeId).orElseThrow();
        Obligation reloadedRecurring = obligationRepository.findById(recurringId).orElseThrow();

        assertThat(reloadedOneTime.getStatus()).isEqualTo(Status.EXPIRED);
        assertThat(reloadedRecurring.getStatus()).isEqualTo(Status.ACTIVE);


    }
}
