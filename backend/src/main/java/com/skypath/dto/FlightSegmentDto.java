package com.skypath.dto;

import java.math.BigDecimal;

public record FlightSegmentDto(
        String flightNumber,
        String airline,
        String origin,
        String destination,
        String departureTime,
        String arrivalTime,
        BigDecimal price,
        String aircraft,
        long durationMinutes
) {
}