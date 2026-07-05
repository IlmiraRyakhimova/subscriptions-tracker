package com.example.subscriptions.repository;


import com.example.subscriptions.entity.Category;
import com.example.subscriptions.entity.Obligation;
import com.example.subscriptions.entity.Recurrence;
import com.example.subscriptions.entity.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)

class ObligationFilterIntegrationTest {

    @Autowired
    private ObligationRepository obligationRepository;
    private final Sort sort = Sort.by("nextPaymentDate").ascending();

    @BeforeEach
    void setUp() {
        obligationRepository.save(build("Netflix", Category.SUBSCRIPTION, Status.ACTIVE));
        obligationRepository.save(build("Старая подписка", Category.SUBSCRIPTION, Status.EXPIRED));
        obligationRepository.save(build("Счёт", Category.BILL, Status.ACTIVE));
    }

    private Obligation build(String title, Category category, Status status) {
        Obligation o = new Obligation();
        o.setTitle(title);
        o.setAmount(new BigDecimal("100"));
        o.setCurrency("RUB");
        o.setCategory(category);
        o.setRecurrence(Recurrence.MONTHLY);
        o.setNextPaymentDate(LocalDate.now().plusDays(10));
        o.setStatus(status);
        return o;
    }

    @Test
    void findAll_noFilters_returnsAll() {
        Specification<Obligation> spec = ObligationSpecifications.hasCategory(null)
                .and(ObligationSpecifications.hasStatus(null));

        List<Obligation> result = obligationRepository.findAll(spec, sort);

        assertThat(result).hasSize(3);
    }

    @Test
    void findAll_categoryFilter_returnsOnlySubscription() {
        Specification<Obligation> spec = ObligationSpecifications.hasCategory(Category.SUBSCRIPTION)
                .and(ObligationSpecifications.hasStatus(null));
        List<Obligation> result = obligationRepository.findAll(spec, sort);

        assertThat(result)
                .hasSize(2)
                .allMatch(o -> o.getCategory() == Category.SUBSCRIPTION);
    }

    @Test
    void findAll_statusFilter_returnsOnlyActive() {
        Specification<Obligation> spec = ObligationSpecifications.hasCategory(null)
                .and(ObligationSpecifications.hasStatus(Status.ACTIVE));
        List<Obligation> result = obligationRepository.findAll(spec, sort);

        assertThat(result)
                .hasSize(2)
                .allMatch(o -> o.getStatus() == Status.ACTIVE);
    }

    @Test
    void findAll_statusAndCategoryFilter_returnsOnlyBillAndOnlyActive() {
        Specification<Obligation> spec = ObligationSpecifications.hasCategory(Category.BILL)
                .and(ObligationSpecifications.hasStatus(Status.ACTIVE));
        List<Obligation> result = obligationRepository.findAll(spec, sort);

        assertThat(result)
                .hasSize(1)
                .allMatch(o ->
                    o.getCategory() == Category.BILL && o.getStatus() == Status.ACTIVE);
    }
}

