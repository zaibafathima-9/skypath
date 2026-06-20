package com.skypath.controller;

import com.skypath.dto.SearchResponse;
import com.skypath.exception.ApiException;
import com.skypath.service.FlightDataService;
import com.skypath.service.FlightSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@RestController
public class SearchController {

    private final FlightDataService flightDataService;
    private final FlightSearchService flightSearchService;

    public SearchController(
            FlightDataService flightDataService,
            FlightSearchService flightSearchService
    ) {
        this.flightDataService = flightDataService;
        this.flightSearchService = flightSearchService;
    }

    @GetMapping("/api/search")
    public ResponseEntity<SearchResponse> search(
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

        SearchResponse response = flightSearchService.searchDirectFlights(
                normalizedOrigin,
                normalizedDestination,
                parsedDate
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