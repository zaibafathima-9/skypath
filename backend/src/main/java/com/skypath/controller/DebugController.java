package com.skypath.controller;

import com.skypath.model.Flight;
import com.skypath.service.FlightDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class DebugController {

    private final FlightDataService flightDataService;

    public DebugController(FlightDataService flightDataService) {
        this.flightDataService = flightDataService;
    }

    @GetMapping("/api/debug/dataset")
    public Map<String, Object> dataset() {
        return Map.of(
                "airports", flightDataService.getAirports().size(),
                "flights", flightDataService.getFlights().size(),
                "jfkFlights", flightDataService.getFlightsFromOrigin("JFK").size(),
                "laxFlights", flightDataService.getFlightsFromOrigin("LAX").size()
        );
    }

    @GetMapping("/api/debug/jfk")
    public List<Flight> jfkFlights() {
        return flightDataService.getFlightsFromOrigin("JFK");
    }

    @GetMapping("/api/debug/jfk-to-lax")
    public List<Flight> jfkToLaxFlights() {
        return flightDataService.getFlightsFromOrigin("JFK")
                .stream()
                .filter(flight -> flight.destination().equalsIgnoreCase("LAX"))
                .toList();
    }
}