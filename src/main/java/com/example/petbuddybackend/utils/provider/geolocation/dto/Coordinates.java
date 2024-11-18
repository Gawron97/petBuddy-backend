package com.example.petbuddybackend.utils.provider.geolocation.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record Coordinates(
        BigDecimal latitude,
        BigDecimal longitude
) {
}
