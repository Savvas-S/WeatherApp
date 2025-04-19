package com.example.weatherapp.controller;

import com.example.weatherapp.model.DailyForecastSummary;
import com.example.weatherapp.model.WeatherResponse;
import com.example.weatherapp.services.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/weather")
public class WeatherController {

    private final WeatherService weatherService;

    @Autowired
    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping
    public WeatherResponse getWeather(@RequestParam String city) {
        return weatherService.getWeatherByCity(city);
    }

    @GetMapping("/cities")
    public List<WeatherResponse> getWeatherForCities(@RequestParam List<String> names){
        return weatherService.getWeatherForCities(names);
    }

    @GetMapping("/forecast")
    public ResponseEntity<List<DailyForecastSummary>> get5DayForecast(@RequestParam String city) {
        return ResponseEntity.ok(weatherService.get5DayForecast(city));
    }

    @GetMapping("/forecast/cities")
    public ResponseEntity<Map<String, Map<String, DailyForecastSummary>>> getMultipleForecast(@RequestParam List<String> cities) {
        return ResponseEntity.ok(weatherService.getMultipleForecast(cities));
    }
}
