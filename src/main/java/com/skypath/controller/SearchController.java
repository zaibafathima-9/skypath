package com.skypath.controller;

import com.skypath.exception.ApiException;
import com.skypath.service.FlightDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@RestController
public class SearchController {

    private final FlightDataService flightDataService;

    public SearchController(FlightDataService flightDataService) {
        this.flightDataService = flightDataService;
    }

    @GetMapping("/api/search")
    public ResponseEntity<Map<String, Object>> search(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam String date
    ) {
        String normalizedOrigin = normalizeAirportCode(origin, "origin");
        String normalizedDestination = normalizeAirportCode(destination, "destination");
        LocalDate parsedDate = parseDate(date);

        validateDifferentAirports(normalizedOrigin, normalizedDestination);
        validateAirportExists(normalizedOrigin, "origin");
        validateAirportExists(normalizedDestination, "destination");

        Map<String, Object> response = Map.of(
                "origin", normalizedOrigin,
                "destination", normalizedDestination,
                "date", parsedDate.toString(),
                "count", 0,
                "itineraries", List.of()
        );

        return ResponseEntity.ok(response);
    }

    private String normalizeAirportCode(String airportCode, String fieldName) {
        if (airportCode == null || airportCode.isBlank()) {
            throw ApiException.badRequest(fieldName + " is required");
        }

        String normalizedCode = airportCode.trim().toUpperCase();

        if (!normalizedCode.matches("^[A-Z]{3}$")) {
            throw ApiException.badRequest(fieldName + " must be a valid 3-letter IATA airport code");
        }

        return normalizedCode;
    }

    private LocalDate parseDate(String date) {
        if (date == null || date.isBlank()) {
            throw ApiException.badRequest("date is required");
        }

        try {
            return LocalDate.parse(date.trim());
        } catch (DateTimeParseException exception) {
            throw ApiException.badRequest("date must be in ISO format YYYY-MM-DD");
        }
    }

    private void validateDifferentAirports(String origin, String destination) {
        if (origin.equals(destination)) {
            throw ApiException.badRequest("origin and destination cannot be the same");
        }
    }

    private void validateAirportExists(String airportCode, String fieldName) {
        if (!flightDataService.airportExists(airportCode)) {
            throw ApiException.badRequest("Invalid " + fieldName + " airport code: " + airportCode);
        }
    }
}