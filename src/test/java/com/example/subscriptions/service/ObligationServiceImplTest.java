package com.example.subscriptions.service;

import com.example.subscriptions.dto.CreateObligationResponseDto;
import com.example.subscriptions.dto.ObligationRequestDto;
import com.example.subscriptions.dto.PayResponseDto;
import com.example.subscriptions.dto.UpcomingResponseDto;
import com.example.subscriptions.entity.*;
import com.example.subscriptions.exception.InvalidObligationStateException;
import com.example.subscriptions.mapper.ObligationMapper;
import com.example.subscriptions.mapper.ObligationMapperImpl;
import com.example.subscriptions.mapper.PaymentMapper;
import com.example.subscriptions.mapper.PaymentMapperImpl;
import com.example.subscriptions.repository.ObligationRepository;
import com.example.subscriptions.repository.PaymentRepository;
import com.example.subscriptions.service.impl.ObligationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ObligationServiceImplTest {

    @Mock
    private ObligationRepository obligationRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private SseService sseService;


    private ObligationServiceImpl obligationService;

    private ObligationRequestDto request;

    @BeforeEach
    void setUp() {
        ObligationMapper obligationMapper = new ObligationMapperImpl();
        PaymentMapper paymentMapper = new PaymentMapperImpl();
        obligationService = new ObligationServiceImpl(obligationRepository, paymentRepository, obligationMapper, paymentMapper, sseService);

        request = new ObligationRequestDto();
        request.setTitle("Netflix");
        request.setAmount(new BigDecimal("9.99"));
        request.setCurrency("USD");
        request.setCategory(Category.SUBSCRIPTION);
        request.setRecurrence(Recurrence.MONTHLY);
        request.setNextPaymentDate(LocalDate.now().plusDays(10));
    }

    @Test
    void createObligation_futureDate_statusActive() {
        when(obligationRepository.save(any(Obligation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        CreateObligationResponseDto result = obligationService.createObligation(request);
        assertThat(result.getObligation().getStatus()).isEqualTo(Status.ACTIVE);
    }

    @Test
    void createObligation_pastDate_statusExpired() {
        request.setNextPaymentDate(LocalDate.now().minusDays(10));
        when(obligationRepository.save(any(Obligation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        CreateObligationResponseDto result = obligationService.createObligation(request);
        assertThat(result.getObligation().getStatus()).isEqualTo(Status.EXPIRED);
    }

    @Test
    void createObligation_noDuplicate_warningIsNull() {
        when(obligationRepository.existsByTitleIgnoreCaseAndStatus(any(), any()))
                .thenReturn(false);

        when(obligationRepository.save(any(Obligation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateObligationResponseDto result = obligationService.createObligation(request);
        assertThat(result.getWarning()).isNull();
    }

    @Test
    void createObligation_activeDuplicate_returnsWarning() {
        when(obligationRepository.existsByTitleIgnoreCaseAndStatus(any(), any()))
                .thenReturn(true);

        when(obligationRepository.save(any(Obligation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateObligationResponseDto result = obligationService.createObligation(request);
        assertThat(result.getWarning()).isNotNull();
    }

    @Test
    void createObligation_mapsAllFieldsCorrectly() {
        when(obligationRepository.save(any(Obligation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        CreateObligationResponseDto result = obligationService.createObligation(request);

        assertThat(result.getObligation().getTitle()).isEqualTo(request.getTitle());
        assertThat(result.getObligation().getAmount()).isEqualByComparingTo(request.getAmount());
        assertThat(result.getObligation().getCurrency()).isEqualTo(request.getCurrency());
        assertThat(result.getObligation().getCategory()).isEqualTo(request.getCategory());
        assertThat(result.getObligation().getRecurrence()).isEqualTo(request.getRecurrence());
        assertThat(result.getObligation().getNextPaymentDate()).isEqualTo(request.getNextPaymentDate());
    }

    @Test
    void pay_monthly_shiftsDateByOneMonthAndKeepsActive() {
        UUID id = UUID.randomUUID();
        Obligation obligation = new Obligation();
        obligation.setId(id);
        obligation.setTitle("Netflix");
        obligation.setAmount(new BigDecimal("9.99"));
        obligation.setCurrency("USD");
        obligation.setCategory(Category.SUBSCRIPTION);
        obligation.setRecurrence(Recurrence.MONTHLY);
        obligation.setNextPaymentDate(LocalDate.of(2026, 8, 15));
        obligation.setStatus(Status.ACTIVE);

        when(obligationRepository.findById(id)).thenReturn(Optional.of(obligation));
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(obligationRepository.save(any(Obligation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PayResponseDto result = obligationService.pay(id);

        assertThat(result.getObligation().getNextPaymentDate())
                .isEqualTo(LocalDate.of(2026, 9, 15));
        assertThat(result.getObligation().getStatus()).isEqualTo(Status.ACTIVE);
    }

    @Test
    void pay_quarterly_shiftsDateByThreeMonthsAndKeepsActive() {
        UUID id = UUID.randomUUID();
        Obligation obligation = new Obligation();
        obligation.setId(id);
        obligation.setTitle("Netflix");
        obligation.setAmount(new BigDecimal("9.99"));
        obligation.setCurrency("USD");
        obligation.setCategory(Category.SUBSCRIPTION);
        obligation.setRecurrence(Recurrence.QUARTERLY);
        obligation.setNextPaymentDate(LocalDate.of(2026, 8, 15));
        obligation.setStatus(Status.ACTIVE);

        when(obligationRepository.findById(id)).thenReturn(Optional.of(obligation));
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(obligationRepository.save(any(Obligation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PayResponseDto result = obligationService.pay(id);

        assertThat(result.getObligation().getNextPaymentDate())
                .isEqualTo(LocalDate.of(2026, 11, 15));
        assertThat(result.getObligation().getStatus()).isEqualTo(Status.ACTIVE);
    }

    @Test
    void pay_yearly_shiftsDateByOneYearAndKeepsActive() {
        UUID id = UUID.randomUUID();
        Obligation obligation = new Obligation();
        obligation.setId(id);
        obligation.setTitle("Netflix");
        obligation.setAmount(new BigDecimal("9.99"));
        obligation.setCurrency("USD");
        obligation.setCategory(Category.SUBSCRIPTION);
        obligation.setRecurrence(Recurrence.YEARLY);
        obligation.setNextPaymentDate(LocalDate.of(2026, 8, 15));
        obligation.setStatus(Status.ACTIVE);

        when(obligationRepository.findById(id)).thenReturn(Optional.of(obligation));
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(obligationRepository.save(any(Obligation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PayResponseDto result = obligationService.pay(id);

        assertThat(result.getObligation().getNextPaymentDate())
                .isEqualTo(LocalDate.of(2027, 8, 15));
        assertThat(result.getObligation().getStatus()).isEqualTo(Status.ACTIVE);
    }

    @Test
    void pay_oneTime_setsCancelled() {
        UUID id = UUID.randomUUID();
        Obligation obligation = new Obligation();
        obligation.setId(id);
        obligation.setTitle("Netflix");
        obligation.setAmount(new BigDecimal("9.99"));
        obligation.setCurrency("USD");
        obligation.setCategory(Category.BILL);
        obligation.setRecurrence(null);
        obligation.setNextPaymentDate(LocalDate.of(2026, 8, 15));
        obligation.setStatus(Status.ACTIVE);

        when(obligationRepository.findById(id)).thenReturn(Optional.of(obligation));
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(obligationRepository.save(any(Obligation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PayResponseDto result = obligationService.pay(id);

        assertThat(result.getObligation().getNextPaymentDate()).isEqualTo(LocalDate.of(2026, 8, 15));
        assertThat(result.getObligation().getStatus()).isEqualTo(Status.CANCELLED);
    }

    @Test
    void pay_monthly_endOfMonth_adjustsToLastDayOfFebruary() {
        UUID id = UUID.randomUUID();
        Obligation obligation = new Obligation();
        obligation.setId(id);
        obligation.setTitle("Netflix");
        obligation.setAmount(new BigDecimal("9.99"));
        obligation.setCurrency("USD");
        obligation.setCategory(Category.SUBSCRIPTION);
        obligation.setRecurrence(Recurrence.MONTHLY);
        obligation.setNextPaymentDate(LocalDate.of(2026, 1, 31));
        obligation.setStatus(Status.ACTIVE);

        when(obligationRepository.findById(id)).thenReturn(Optional.of(obligation));
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(obligationRepository.save(any(Obligation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PayResponseDto result = obligationService.pay(id);

        assertThat(result.getObligation().getNextPaymentDate())
                .isEqualTo(LocalDate.of(2026, 2, 28));
        assertThat(result.getObligation().getStatus()).isEqualTo(Status.ACTIVE);

    }

    @Test
    void pay_monthly_endOfMonthLeapYear_adjustsToLastDayOfFebruary() {
        UUID id = UUID.randomUUID();
        Obligation obligation = new Obligation();
        obligation.setId(id);
        obligation.setTitle("Netflix");
        obligation.setAmount(new BigDecimal("9.99"));
        obligation.setCurrency("USD");
        obligation.setCategory(Category.SUBSCRIPTION);
        obligation.setRecurrence(Recurrence.MONTHLY);
        obligation.setNextPaymentDate(LocalDate.of(2028, 1, 31));
        obligation.setStatus(Status.ACTIVE);

        when(obligationRepository.findById(id)).thenReturn(Optional.of(obligation));
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(obligationRepository.save(any(Obligation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PayResponseDto result = obligationService.pay(id);

        assertThat(result.getObligation().getNextPaymentDate())
                .isEqualTo(LocalDate.of(2028, 2, 29));
        assertThat(result.getObligation().getStatus()).isEqualTo(Status.ACTIVE);
    }

    @Test
    void pay_notActive_throwsException() {
        UUID id = UUID.randomUUID();
        Obligation obligation = new Obligation();
        obligation.setId(id);
        obligation.setTitle("Netflix");
        obligation.setAmount(new BigDecimal("9.99"));
        obligation.setCurrency("USD");
        obligation.setCategory(Category.SUBSCRIPTION);
        obligation.setRecurrence(Recurrence.MONTHLY);
        obligation.setNextPaymentDate(LocalDate.of(2026, 1, 31));
        obligation.setStatus(Status.CANCELLED);

        when(obligationRepository.findById(id)).thenReturn(Optional.of(obligation));

        assertThatThrownBy(() -> obligationService.pay(id))
                .isInstanceOf(InvalidObligationStateException.class);
    }

    @Test
    void getUpcoming_calculatesTotalsByCurrency() {
        Obligation rub1 = buildObligation("A", Category.BILL, null, new BigDecimal("100"), "RUB");
        Obligation rub2 = buildObligation("B", Category.BILL, null, new BigDecimal("400"), "RUB");
        Obligation usd = buildObligation("C", Category.SUBSCRIPTION, Recurrence.MONTHLY, new BigDecimal("9.99"), "USD");

        when(obligationRepository.findByNextPaymentDateBetween(any(), any()))
                .thenReturn(List.of(rub1, rub2, usd));

        UpcomingResponseDto result = obligationService.getUpcoming(7);

        assertThat(result.getTotals())
                .containsEntry("RUB", new BigDecimal("500"))
                .containsEntry("USD", new BigDecimal("9.99"));
    }
    @Test
    void getUpcoming_renewalAlertsContainsOnlySubscriptionsWithRecurrence() {
        Obligation rec = buildObligation("A", Category.SUBSCRIPTION, Recurrence.MONTHLY, new BigDecimal("100"), "RUB");
        Obligation noRec = buildObligation("B", Category.SUBSCRIPTION,null, new BigDecimal("400"), "RUB");
        Obligation noRec2 = buildObligation("C", Category.BILL, Recurrence.MONTHLY, new BigDecimal("9.99"), "USD");

        when(obligationRepository.findByNextPaymentDateBetween(any(),any())).thenReturn(List.of(rec, noRec, noRec2));

        UpcomingResponseDto result = obligationService.getUpcoming(7);
        assertThat(result.getRenewalAlerts()).hasSize(1);
        assertThat(result.getRenewalAlerts().get(0).getTitle()).isEqualTo("A");
    }

    private Obligation buildObligation(String title, Category category, Recurrence recurrence,
                                       BigDecimal amount, String currency) {
        Obligation o = new Obligation();
        o.setId(UUID.randomUUID());
        o.setTitle(title);
        o.setAmount(amount);
        o.setCurrency(currency);
        o.setCategory(category);
        o.setRecurrence(recurrence);
        o.setNextPaymentDate(LocalDate.now().plusDays(3));
        o.setStatus(Status.ACTIVE);
        return o;
    }




}
