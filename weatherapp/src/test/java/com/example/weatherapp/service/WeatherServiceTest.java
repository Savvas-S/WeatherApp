package com.example.weatherapp.service;

import com.example.weatherapp.model.DailyForecastSummary;
import com.example.weatherapp.model.WeatherResponse;
import com.example.weatherapp.services.WeatherService;
import dataModel.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Spy
    @InjectMocks
    private WeatherService weatherService;

    @Test
    void getWeatherByCity_shouldReturnWeatherResponse() {
        Main main = new Main();
        main.setTemp(17.5);

        Weather weather = new Weather();
        weather.setDescription("partly cloudy");

        ApiWeatherResponse apiResponse = new ApiWeatherResponse();
        apiResponse.setName("London");
        apiResponse.setMain(main);
        apiResponse.setWeather(List.of(weather));

        when(restTemplate.getForObject(contains("London"), eq(ApiWeatherResponse.class)))
                .thenReturn(apiResponse);

        WeatherResponse result = weatherService.getWeatherByCity("London");

        assertEquals("London", result.getCity());
        assertEquals("17.5°C", result.getTemperature());
        assertEquals("partly cloudy", result.getDescription());
    }

    @Test
    void testForecastWeatherForCity() {
        String city = "Limassol";

        ForecastEntry entry1 = new ForecastEntry();
        entry1.setDt_txt("2025-04-20 09:00:00");
        Main main1 = new Main();
        main1.setTemp(20.0);
        Weather weather1 = new Weather();
        weather1.setDescription("clear sky");
        entry1.setMain(main1);
        entry1.setWeather(List.of(weather1));

        ForecastEntry entry2 = new ForecastEntry();
        entry2.setDt_txt("2025-04-20 15:00:00");
        Main main2 = new Main();
        main2.setTemp(22.0);
        Weather weather2 = new Weather();
        weather2.setDescription("clear sky");
        entry2.setMain(main2);
        entry2.setWeather(List.of(weather2));

        ForecastEntry entry3 = new ForecastEntry();
        entry3.setDt_txt("2025-04-21 09:00:00");
        Main main3 = new Main();
        main3.setTemp(24.0);
        Weather weather3 = new Weather();
        weather3.setDescription("cloudy sky");
        entry3.setMain(main3);
        entry3.setWeather(List.of(weather3));

        ForecastResponse mockResponse = new ForecastResponse();
        mockResponse.setList(List.of(entry1, entry2, entry3));

        when(restTemplate.getForObject(contains(city), eq(ForecastResponse.class)))
                .thenReturn(mockResponse);

        List<DailyForecastSummary> result = weatherService.get5DayForecast(city);

        DailyForecastSummary sunday = result.stream()
                .filter(d -> d.getDate().getDayOfWeek().toString().equals("SUNDAY"))
                .findFirst().orElseThrow();

        assertEquals(21.0, sunday.getAvgTemp());
        assertEquals("clear sky", sunday.getDescription());

        DailyForecastSummary monday = result.stream()
                .filter(d -> d.getDate().getDayOfWeek().toString().equals("MONDAY"))
                .findFirst().orElseThrow();

        assertEquals(24.0, monday.getAvgTemp());
        assertEquals("cloudy sky", monday.getDescription());
    }


    @Test
    void testGetWeatherForCities() {
        List<String> cities = List.of("Limassol", "Larnaca");

        WeatherResponse response1 = new WeatherResponse("Limassol", "25.0°C", "clear sky");
        WeatherResponse response2 = new WeatherResponse("Larnaca", "24.0°C", "cloudy");

        doReturn(response1).when(weatherService).getWeatherByCity("Limassol");
        doReturn(response2).when(weatherService).getWeatherByCity("Larnaca");

        List<WeatherResponse> results = weatherService.getWeatherForCities(cities);

        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(r -> r.getCity().equals("Limassol")));
        assertTrue(results.stream().anyMatch(r -> r.getCity().equals("Larnaca")));
    }

    @Test
    void testGetMultipleForecast() {
        List<String> cities = List.of("Limassol", "Larnaca");
        LocalDate forecastDate = LocalDate.of(2025, 4, 20);
        String weekdayKey = forecastDate.getDayOfWeek().toString();

        List<DailyForecastSummary> limassolForecast = List.of(
                new DailyForecastSummary(forecastDate, 21.0, "clear sky")
        );
        List<DailyForecastSummary> larnacaForecast = List.of(
                new DailyForecastSummary(forecastDate, 23.0, "cloudy")
        );

        doReturn(limassolForecast).when(weatherService).get5DayForecast("Limassol");
        doReturn(larnacaForecast).when(weatherService).get5DayForecast("Larnaca");

        Map<String, Map<String, DailyForecastSummary>> result = weatherService.getMultipleForecast(cities);

        assertEquals(2, result.size());

        assertNotNull(result.get("Limassol").get(weekdayKey), "Expected weekday key missing for Limassol");
        assertEquals(21.0, result.get("Limassol").get(weekdayKey).getAvgTemp());
        assertEquals("clear sky", result.get("Limassol").get(weekdayKey).getDescription());

        assertNotNull(result.get("Larnaca").get(weekdayKey), "Expected weekday key missing for Larnaca");
        assertEquals(23.0, result.get("Larnaca").get(weekdayKey).getAvgTemp());
        assertEquals("cloudy", result.get("Larnaca").get(weekdayKey).getDescription());
    }
}
