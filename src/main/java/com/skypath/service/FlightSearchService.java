package com.skypath.service;

import com.skypath.dto.FlightSegmentDto;
import com.skypath.dto.ItineraryDto;
import com.skypath.dto.SearchResponse;
import com.skypath.model.Airport;
import com.skypath.model.Flight;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

@Service
public class FlightSearchService {

    private final FlightDataService flightDataService;

    public FlightSearchService(FlightDataService flightDataService) {
        this.flightDataService = flightDataService;
    }

    public SearchResponse searchDirectFlights(String origin, String destination, LocalDate date) {
        List<ItineraryDto> itineraries = flightDataService.getFlightsFromOrigin(origin)
                .stream()
                .filter(flight -> flight.destination().equalsIgnoreCase(destination))
                .filter(flight -> isDepartureOnRequestedDate(flight, date))
                .map(this::buildDirectItinerary)
                .sorted(Comparator.comparingLong(ItineraryDto::totalDurationMinutes))
                .toList();

        return new SearchResponse(
                origin,
                destination,
                date.toString(),
                itineraries.size(),
                itineraries
        );
    }

    private ItineraryDto buildDirectItinerary(Flight flight) {
        long durationMinutes = calculateFlightDurationMinutes(flight);

        FlightSegmentDto segment = new FlightSegmentDto(
                flight.flightNumber(),
                flight.airline(),
                flight.origin(),
                flight.destination(),
                flight.departureTime(),
                flight.arrivalTime(),
                flight.price(),
                flight.aircraft(),
                durationMinutes
        );

        return new ItineraryDto(
                List.of(segment),
                durationMinutes,
                flight.price() == null ? BigDecimal.ZERO : flight.price()
        );
    }

    private boolean isDepartureOnRequestedDate(Flight flight, LocalDate requestedDate) {
        LocalDate departureDate = LocalDateTime
                .parse(flight.departureTime())
                .toLocalDate();

        return departureDate.equals(requestedDate);
    }

    private long calculateFlightDurationMinutes(Flight flight) {
        Instant departureInstant = toInstant(
                flight.departureTime(),
                flight.origin()
        );

        Instant arrivalInstant = toInstant(
                flight.arrivalTime(),
                flight.destination()
        );

        return Duration.between(departureInstant, arrivalInstant).toMinutes();
    }

    private Instant toInstant(String localDateTime, String airportCode) {
        Airport airport = flightDataService.getAirportByCode(airportCode)
                .orElseThrow(() -> new IllegalStateException("Airport not found: " + airportCode));

        return LocalDateTime
                .parse(localDateTime)
                .atZone(ZoneId.of(airport.timezone()))
                .toInstant();
    }
}
