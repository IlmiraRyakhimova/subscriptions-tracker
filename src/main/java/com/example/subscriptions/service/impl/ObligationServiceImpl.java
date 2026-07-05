package com.example.subscriptions.service.impl;

import com.example.subscriptions.dto.*;
import com.example.subscriptions.entity.*;
import com.example.subscriptions.exception.InvalidObligationStateException;
import com.example.subscriptions.exception.ObligationNotFoundException;
import com.example.subscriptions.mapper.ObligationMapper;
import com.example.subscriptions.mapper.PaymentMapper;
import com.example.subscriptions.repository.ObligationRepository;
import com.example.subscriptions.repository.ObligationSpecifications;
import com.example.subscriptions.repository.PaymentRepository;
import com.example.subscriptions.service.ObligationService;
import com.example.subscriptions.service.SseService;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ObligationServiceImpl implements ObligationService {

    private final ObligationRepository obligationRepository;
    private final PaymentRepository paymentRepository;
    private final ObligationMapper obligationMapper;
    private final PaymentMapper paymentMapper;
    private final SseService sseService;
    private static final String DUPLICATE_WARNING = "Активное обязательство с таким названием уже существует";

    public ObligationServiceImpl(ObligationRepository obligationRepository, PaymentRepository paymentRepository, ObligationMapper obligationMapper, PaymentMapper paymentMapper, SseService sseService) {
        this.obligationRepository = obligationRepository;
        this.paymentRepository = paymentRepository;
        this.obligationMapper = obligationMapper;
        this.paymentMapper = paymentMapper;
        this.sseService = sseService;
    }

    @Override
    @Transactional
    public CreateObligationResponseDto createObligation(ObligationRequestDto request) {

        boolean duplicateExists = obligationRepository.existsByTitleIgnoreCaseAndStatus(request.getTitle(), Status.ACTIVE);

        Obligation obligation = obligationMapper.toEntity(request);
        Status status = request.getNextPaymentDate().isBefore(LocalDate.now())
                ? Status.EXPIRED
                : Status.ACTIVE;
        obligation.setStatus(status);

        Obligation saved = obligationRepository.save(obligation);

        ObligationResponseDto response = obligationMapper.toResponseDto(saved);

        String warning = duplicateExists
                ? DUPLICATE_WARNING
                : null;

        return new CreateObligationResponseDto(response, warning);
    }

    @Override
    @Transactional
    public List<ObligationResponseDto> getAllObligations(Category category, Status status) {

        obligationRepository.expireOverdueOneTimeObligations(LocalDate.now(), Status.ACTIVE, Status.EXPIRED);

        Specification<Obligation> spec = ObligationSpecifications.hasCategory(category)
                .and(ObligationSpecifications.hasStatus(status));

        Sort sort = Sort.by("nextPaymentDate").ascending();
        List<Obligation> obligations = obligationRepository.findAll(spec, sort);

        return obligations.stream()
                .map(obligationMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public PayResponseDto pay(UUID id) {
        Obligation obligation = obligationRepository.findById(id).orElseThrow(() -> new ObligationNotFoundException("Обязательство не найдено: " + id));
        if (obligation.getStatus() != Status.ACTIVE) {
            throw new InvalidObligationStateException("Оплатить можно только активное обязательство");
        }

        Payment payment = new Payment();
        payment.setObligation(obligation);
        payment.setAmount(obligation.getAmount());
        payment.setCurrency(obligation.getCurrency());
        payment.setPaidAt(Instant.now());
        Payment savedPayment = paymentRepository.save(payment);

        Recurrence recurrence = obligation.getRecurrence();
        if (recurrence == null) {
            obligation.setStatus(Status.CANCELLED);
        } else {
            LocalDate current = obligation.getNextPaymentDate();
            LocalDate newNextPaymentDate = switch (recurrence) {
                case MONTHLY -> current.plusMonths(1);
                case QUARTERLY -> current.plusMonths(3);
                case YEARLY -> current.plusYears(1);
            };
            obligation.setNextPaymentDate(newNextPaymentDate);
        }

        Obligation savedObligation = obligationRepository.save(obligation);

        ObligationResponseDto obligationDto = obligationMapper.toResponseDto(savedObligation);
        PaymentResponseDto paymentDto = paymentMapper.toResponseDto(savedPayment);

        return new PayResponseDto(obligationDto, paymentDto);
    }

    @Override
    @Transactional
    public UpcomingResponseDto getUpcoming(int days) {
        LocalDate today = LocalDate.now();
        LocalDate end = today.plusDays(days);

        List<Obligation> inWindow = obligationRepository.findByNextPaymentDateBetween(today, end);

        List<ObligationResponseDto> obligations = inWindow.stream()
                .map(obligationMapper::toResponseDto)
                .toList();

        Map<String, BigDecimal> totals = inWindow.stream()
                .collect(Collectors.groupingBy(
                        Obligation::getCurrency,
                        Collectors.reducing(BigDecimal.ZERO, Obligation::getAmount, BigDecimal::add)
                ));
        List<RenewalAlertsDto> renewalAlerts = inWindow.stream()
                .filter(o -> o.getCategory() == Category.SUBSCRIPTION && o.getRecurrence() != null)
                .map(obligationMapper::toRenewalAlert)
                .toList();

        return new UpcomingResponseDto(obligations, totals, renewalAlerts);
    }

    @Override
    @Transactional
    public ObligationResponseDto cancel(UUID id) {
        Obligation obligation = obligationRepository.findById(id).orElseThrow(() -> new ObligationNotFoundException("Обязательство не найдено: " + id));

        if (obligation.getStatus() != Status.ACTIVE) {
            throw new InvalidObligationStateException("Отменить можно только активное обязательство");
        }

        obligation.setStatus(Status.CANCELLED);
        Obligation saved = obligationRepository.save(obligation);
        return obligationMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Obligation obligation = obligationRepository
                .findById(id).orElseThrow(() -> new ObligationNotFoundException("Обязательство не найдено: " + id));
        paymentRepository.deleteByObligationId(id);
        obligationRepository.deleteById(id);
        sseService.broadcast(new ObligationDeletedEvent("obligation_deleted", id));

    }
}
