package com.skypath.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SearchControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnDirectAndConnectingFlightsForJfkToLax() throws Exception {
        mockMvc.perform(get("/api/search")
                        .param("origin", "JFK")
                        .param("destination", "LAX")
                        .param("date", "2024-03-15"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.origin", is("JFK")))
                .andExpect(jsonPath("$.destination", is("LAX")))
                .andExpect(jsonPath("$.date", is("2024-03-15")))
                .andExpect(jsonPath("$.count", greaterThan(0)))
                .andExpect(jsonPath("$.itineraries[0].segments", notNullValue()))
                .andExpect(jsonPath("$.itineraries[0].totalDurationMinutes", greaterThan(0)))
                .andExpect(jsonPath("$.itineraries[0].totalPrice", greaterThan(0.0)))
                .andExpect(jsonPath("$.itineraries[*].segments.length()", hasItem(1)))
                .andExpect(jsonPath("$.itineraries[*].segments.length()", hasItem(2)));
    }

    @Test
    void shouldReturnInternationalRouteOptionsForSfoToNrt() throws Exception {
        mockMvc.perform(get("/api/search")
                        .param("origin", "SFO")
                        .param("destination", "NRT")
                        .param("date", "2024-03-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.origin", is("SFO")))
                .andExpect(jsonPath("$.destination", is("NRT")))
                .andExpect(jsonPath("$.count", greaterThan(0)))
                .andExpect(jsonPath("$.itineraries[0].totalDurationMinutes", greaterThan(0)));
    }

    @Test
    void shouldReturnConnectingOptionsWhenNoDirectFlightExists() throws Exception {
        mockMvc.perform(get("/api/search")
                        .param("origin", "BOS")
                        .param("destination", "SEA")
                        .param("date", "2024-03-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.origin", is("BOS")))
                .andExpect(jsonPath("$.destination", is("SEA")))
                .andExpect(jsonPath("$.count", greaterThan(0)))
                .andExpect(jsonPath("$.itineraries[*].segments.length()", everyItem(greaterThanOrEqualTo(2))));
    }

    @Test
    void shouldRejectSameOriginAndDestination() throws Exception {
        mockMvc.perform(get("/api/search")
                        .param("origin", "JFK")
                        .param("destination", "JFK")
                        .param("date", "2024-03-15"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("origin and destination cannot be the same")));
    }

    @Test
    void shouldRejectInvalidAirportCode() throws Exception {
        mockMvc.perform(get("/api/search")
                        .param("origin", "XXX")
                        .param("destination", "LAX")
                        .param("date", "2024-03-15"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Invalid origin airport code: XXX")));
    }

    @Test
    void shouldHandleDateLineCrossingForSydToLax() throws Exception {
        mockMvc.perform(get("/api/search")
                        .param("origin", "SYD")
                        .param("destination", "LAX")
                        .param("date", "2024-03-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", greaterThan(0)))
                .andExpect(jsonPath("$.itineraries[0].totalDurationMinutes", greaterThan(0)))
                .andExpect(jsonPath("$.itineraries[0].segments[0].origin", is("SYD")))
                .andExpect(jsonPath("$.itineraries[0].segments[0].destination", is("LAX")));
    }
}