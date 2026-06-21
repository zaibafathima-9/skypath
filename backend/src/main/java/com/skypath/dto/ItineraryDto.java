package com.skypath.dto;

import java.math.BigDecimal;
import java.util.List;

public record ItineraryDto(
        List<FlightSegmentDto> segments,
        List<LayoverDto> layovers,
        long totalDurationMinutes,
        BigDecimal totalPrice
) {
}