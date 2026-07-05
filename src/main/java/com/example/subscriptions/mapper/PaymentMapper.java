package com.example.subscriptions.mapper;


import com.example.subscriptions.dto.PaymentResponseDto;
import com.example.subscriptions.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    @Mapping(target = "obligationId", source = "obligation.id")
    PaymentResponseDto toResponseDto(Payment payment);
}
