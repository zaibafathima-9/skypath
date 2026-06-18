package com.skypath;

import com.skypath.service.FlightDataService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class FlightConnectionSearchEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlightConnectionSearchEngineApplication.class, args);
    }

    @Bean
    CommandLineRunner verifyDatasetLoaded(FlightDataService flightDataService) {
        return args -> {
            System.out.println("Airports loaded: " + flightDataService.getAirports().size());
            System.out.println("Flights loaded: " + flightDataService.getFlights().size());
        };
    }
}
