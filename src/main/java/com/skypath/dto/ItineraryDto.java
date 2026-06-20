package com.skypath.dto;

import java.math.BigDecimal;
import java.util.List;

public record ItineraryDto(
        List<FlightSegmentDto> segments,
        long totalDurationMinutes,
        BigDecimal totalPrice
) {
}