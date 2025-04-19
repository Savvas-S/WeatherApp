package com.example.weatherapp.controller;

import com.example.weatherapp.model.WeatherResponse;
import com.example.weatherapp.services.WeatherService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.hamcrest.Matchers.*;

@WebMvcTest(WeatherController.class)
public class WeatherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeatherService weatherService;

    @Test
    void getWeatherForMultipleCities_shouldReturnJsonArray() throws Exception {
        // Arrange
        List<WeatherResponse> mockResponses = List.of(
                new WeatherResponse("London", "15.2°C", "clear sky"),
                new WeatherResponse("Paris", "18.3°C", "sunny")
        );

        Mockito.when(weatherService.getWeatherForCities(List.of("London", "Paris")))
                .thenReturn(mockResponses);

        // Act & Assert
        mockMvc.perform(get("/weather/cities?names=London,Paris"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].city", is("London")))
                .andExpect(jsonPath("$[0].temperature", is("15.2°C")))
                .andExpect(jsonPath("$[0].description", is("clear sky")))
                .andExpect(jsonPath("$[1].city", is("Paris")))
                .andExpect(jsonPath("$[1].temperature", is("18.3°C")))
                .andExpect(jsonPath("$[1].description", is("sunny")));
    }
}
