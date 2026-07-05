package com.example.subscriptions.mapper;

import com.example.subscriptions.dto.ObligationRequestDto;
import com.example.subscriptions.dto.ObligationResponseDto;
import com.example.subscriptions.dto.RenewalAlertsDto;
import com.example.subscriptions.entity.Obligation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ObligationMapper {

    ObligationResponseDto toResponseDto(Obligation obligation);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Obligation toEntity(ObligationRequestDto request);

    RenewalAlertsDto toRenewalAlert(Obligation obligation);
}
