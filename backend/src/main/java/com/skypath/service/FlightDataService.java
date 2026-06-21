package com.skypath.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skypath.model.Airport;
import com.skypath.model.Flight;
import com.skypath.model.FlightDataset;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FlightDataService {

    private static final Logger log = LoggerFactory.getLogger(FlightDataService.class);

    private final ObjectMapper objectMapper;

    private List<Airport> airports = Collections.emptyList();
    private List<Flight> flights = Collections.emptyList();
    private Map<String, Airport> airportByCode = Collections.emptyMap();
    private Map<String, List<Flight>> flightsByOrigin = Collections.emptyMap();

    public FlightDataService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void loadDataset() {
        try {
            ClassPathResource resource = new ClassPathResource("static/flights.json");

            if (!resource.exists()) {
                throw new IllegalStateException("flights.json was not found in src/main/resources/static");
            }

            try (InputStream inputStream = resource.getInputStream()) {
                FlightDataset dataset = objectMapper.readValue(inputStream, FlightDataset.class);

                if (dataset.airports() == null || dataset.flights() == null) {
                    throw new IllegalStateException("Invalid flights.json: airports or flights field is missing");
                }

                this.airports = List.copyOf(dataset.airports());
                this.flights = List.copyOf(dataset.flights());

                this.airportByCode = this.airports.stream()
                        .collect(Collectors.toUnmodifiableMap(
                                airport -> airport.code().toUpperCase(),
                                airport -> airport
                        ));

                this.flightsByOrigin = this.flights.stream()
                        .collect(Collectors.groupingBy(
                                flight -> flight.origin().toUpperCase(),
                                Collectors.collectingAndThen(Collectors.toList(), List::copyOf)
                        ));

                log.info("Loaded {} airports and {} flights", airports.size(), flights.size());
            }

        } catch (Exception e) {
            throw new IllegalStateException("Failed to load flights.json dataset", e);
        }
    }

    public List<Airport> getAirports() {
        return airports;
    }

    public List<Flight> getFlights() {
        return flights;
    }

    public Optional<Airport> getAirportByCode(String code) {
        if (code == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(airportByCode.get(code.toUpperCase()));
    }

    public boolean airportExists(String code) {
        if (code == null) {
            return false;
        }

        return airportByCode.containsKey(code.toUpperCase());
    }

    public List<Flight> getFlightsFromOrigin(String origin) {
        if (origin == null) {
            return Collections.emptyList();
        }

        return flightsByOrigin.getOrDefault(origin.toUpperCase(), Collections.emptyList());
    }
}