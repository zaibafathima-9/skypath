package com.skypath.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Flight (
        String flightNumber,
        String airline,
        String origin,
        String destination,
        String departureTime,
        String arrivalTime,
        BigDecimal price,
        String artifact
)

{
    public String aircraft() {
        return "";
    }
}
