package com.skypath.service;

import com.skypath.dto.FlightSegmentDto;
import com.skypath.dto.LayoverDto;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class FlightSearchService {

    private static final int MAX_SEGMENTS = 3;

    private static final long MIN_DOMESTIC_LAYOVER_MINUTES = 45;
    private static final long MIN_INTERNATIONAL_LAYOVER_MINUTES = 90;
    private static final long MAX_LAYOVER_MINUTES = 6 * 60;
    private static final int MAX_RESULTS = 100;

    private final FlightDataService flightDataService;

    public FlightSearchService(FlightDataService flightDataService) {
        this.flightDataService = flightDataService;
    }

    public SearchResponse searchFlights(String origin, String destination, LocalDate date) {
        List<ItineraryDto> itineraries = new ArrayList<>();

        List<Flight> startingFlights = flightDataService.getFlightsFromOrigin(origin)
                .stream()
                .filter(flight -> isDepartureOnRequestedDate(flight, date))
                .toList();

        for (Flight flight : startingFlights) {
            List<Flight> currentPath = new ArrayList<>();
            Set<String> visitedAirports = new HashSet<>();

            currentPath.add(flight);
            visitedAirports.add(origin);
            visitedAirports.add(flight.destination());

            searchConnections(
                    flight,
                    destination,
                    currentPath,
                    visitedAirports,
                    itineraries
            );
        }

        List<ItineraryDto> sortedItineraries = itineraries.stream()
                .sorted(Comparator.comparingLong(ItineraryDto::totalDurationMinutes))
                .limit(MAX_RESULTS)
                .toList();

        return new SearchResponse(
                origin,
                destination,
                date.toString(),
                sortedItineraries.size(),
                sortedItineraries
        );
    }

    private void searchConnections(
            Flight currentFlight,
            String finalDestination,
            List<Flight> currentPath,
            Set<String> visitedAirports,
            List<ItineraryDto> results
    ) {
        if (currentFlight.destination().equalsIgnoreCase(finalDestination)) {
            results.add(buildItinerary(currentPath));
            return;
        }

        if (currentPath.size() >= MAX_SEGMENTS) {
            return;
        }

        List<Flight> nextFlights = flightDataService.getFlightsFromOrigin(currentFlight.destination());

        for (Flight nextFlight : nextFlights) {
            if (visitedAirports.contains(nextFlight.destination())) {
                continue;
            }

            if (!isValidConnection(currentFlight, nextFlight)) {
                continue;
            }

            currentPath.add(nextFlight);
            visitedAirports.add(nextFlight.destination());

            searchConnections(
                    nextFlight,
                    finalDestination,
                    currentPath,
                    visitedAirports,
                    results
            );

            currentPath.remove(currentPath.size() - 1);
            visitedAirports.remove(nextFlight.destination());
        }
    }

    private boolean isValidConnection(Flight arrivingFlight, Flight departingFlight) {
        Duration layover = Duration.between(
                getArrivalInstant(arrivingFlight),
                getDepartureInstant(departingFlight)
        );

        if (layover.isNegative() || layover.isZero()) {
            return false;
        }

        long layoverMinutes = layover.toMinutes();

        long minimumLayoverMinutes = isDomesticConnection(arrivingFlight, departingFlight)
                ? MIN_DOMESTIC_LAYOVER_MINUTES
                : MIN_INTERNATIONAL_LAYOVER_MINUTES;

        return layoverMinutes >= minimumLayoverMinutes
                && layoverMinutes <= MAX_LAYOVER_MINUTES;
    }

    private boolean isDomesticConnection(Flight arrivingFlight, Flight departingFlight) {
        String arrivingOriginCountry = getAirportCountry(arrivingFlight.origin());
        String arrivingDestinationCountry = getAirportCountry(arrivingFlight.destination());
        String departingOriginCountry = getAirportCountry(departingFlight.origin());
        String departingDestinationCountry = getAirportCountry(departingFlight.destination());

        return arrivingOriginCountry.equals(arrivingDestinationCountry)
                && arrivingOriginCountry.equals(departingOriginCountry)
                && arrivingOriginCountry.equals(departingDestinationCountry);
    }

    private String getAirportCountry(String airportCode) {
        return flightDataService.getAirportByCode(airportCode)
                .orElseThrow(() -> new IllegalStateException("Airport not found: " + airportCode))
                .country();
    }

    private ItineraryDto buildItinerary(List<Flight> flights) {
        List<FlightSegmentDto> segments = flights.stream()
                .map(this::buildSegment)
                .toList();

        List<LayoverDto> layovers = buildLayovers(flights);

        Instant firstDeparture = getDepartureInstant(flights.get(0));
        Instant finalArrival = getArrivalInstant(flights.get(flights.size() - 1));

        long totalDurationMinutes = Duration
                .between(firstDeparture, finalArrival)
                .toMinutes();

        BigDecimal totalPrice = flights.stream()
                .map(flight -> flight.price() == null ? BigDecimal.ZERO : flight.price())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ItineraryDto(
                segments,
                layovers,
                totalDurationMinutes,
                totalPrice
        );
    }

    private List<LayoverDto> buildLayovers(List<Flight> flights) {
        List<LayoverDto> layovers = new ArrayList<>();

        for (int i = 0; i < flights.size() - 1; i++) {
            Flight arrivingFlight = flights.get(i);
            Flight departingFlight = flights.get(i + 1);

            long layoverMinutes = Duration.between(
                    getArrivalInstant(arrivingFlight),
                    getDepartureInstant(departingFlight)
            ).toMinutes();

            String connectionType = isDomesticConnection(arrivingFlight, departingFlight)
                    ? "domestic"
                    : "international";

            layovers.add(new LayoverDto(
                    arrivingFlight.destination(),
                    layoverMinutes,
                    connectionType
            ));
        }

        return layovers;
    }


    private FlightSegmentDto buildSegment(Flight flight) {
        long durationMinutes = Duration
                .between(getDepartureInstant(flight), getArrivalInstant(flight))
                .toMinutes();

        return new FlightSegmentDto(
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
    }

    private boolean isDepartureOnRequestedDate(Flight flight, LocalDate requestedDate) {
        LocalDate departureDate = LocalDateTime
                .parse(flight.departureTime())
                .toLocalDate();

        return departureDate.equals(requestedDate);
    }

    private Instant getDepartureInstant(Flight flight) {
        return toInstant(flight.departureTime(), flight.origin());
    }

    private Instant getArrivalInstant(Flight flight) {
        return toInstant(flight.arrivalTime(), flight.destination());
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