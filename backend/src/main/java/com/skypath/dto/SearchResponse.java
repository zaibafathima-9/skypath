package com.skypath.dto;

import java.util.List;

public record SearchResponse(
        String origin,
        String destination,
        String date,
        int count,
        List<ItineraryDto> itineraries
) {
}