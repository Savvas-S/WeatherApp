package com.example.weatherapp.services;

import com.example.weatherapp.model.DailyForecastSummary;
import com.example.weatherapp.model.WeatherResponse;
import dataModel.ApiWeatherResponse;
import dataModel.ForecastEntry;
import dataModel.ForecastResponse;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WeatherService {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String baseUrl = "https://api.openweathermap.org/data/2.5/weather";
    private final String fiveDaysForecastBaseUrl = "https://api.openweathermap.org/data/2.5/forecast";

    public WeatherService(RestTemplate restTemplate, @Value("${weather.api.key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
    }

    public WeatherResponse getWeatherByCity(String city) {
        String uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("q", city)
                .queryParam("appid", apiKey)
                .queryParam("units", "metric")
                .toUriString();

        ApiWeatherResponse apiResponse = restTemplate.getForObject(uri, ApiWeatherResponse.class);

        if (apiResponse != null && apiResponse.getWeather() != null && !apiResponse.getWeather().isEmpty()) {
            String cityName = apiResponse.getName();
            double temp = apiResponse.getMain().getTemp();
            String description = apiResponse.getWeather().get(0).getDescription();
            return new WeatherResponse(cityName, String.format("%.1f°C", temp), description);
        } else {
            return new WeatherResponse(city, "N/A", "No data found");
        }
    }

    public List<WeatherResponse> getWeatherForCities(List<String> cities) {
        List<WeatherResponse> result = new ArrayList<>();
        for (String city : cities) {
            try {
                result.add(getWeatherByCity(city));
            } catch (Exception e) {
                result.add(new WeatherResponse(city, "N/A", "City not found or error"));
            }
        }
        return result;
    }

    public List<DailyForecastSummary> get5DayForecast(String city) {
        String uri = UriComponentsBuilder.fromHttpUrl(fiveDaysForecastBaseUrl)
                .queryParam("q", city)
                .queryParam("appid", apiKey)
                .queryParam("units", "metric")
                .toUriString();

        ForecastResponse response = restTemplate.getForObject(uri, ForecastResponse.class);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        Map<LocalDate, List<ForecastEntry>> groupedByDate = response.getList().stream()
                .collect(Collectors.groupingBy(entry ->
                        LocalDateTime.parse(entry.getDt_txt(), formatter).toLocalDate()
                ));

        return groupedByDate.entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<ForecastEntry> entries = entry.getValue();

                    double avgTemp = entries.stream()
                            .mapToDouble(e -> e.getMain().getTemp())
                            .average().orElse(0.0);

                    String description = entries.get(0).getWeather().get(0).getDescription();

                    return new DailyForecastSummary(date, avgTemp, description);
                })
                .sorted(Comparator.comparing(DailyForecastSummary::getDate))
                .toList();
    }

    public Map<String, Map<String, DailyForecastSummary>> getMultipleForecast(List<String> cities) {
        Map<String, Map<String, DailyForecastSummary>> results = new HashMap<>();

        for (String city : cities) {
            List<DailyForecastSummary> forecasts = get5DayForecast(city);

            Map<String, DailyForecastSummary> cityForecast = forecasts.stream()
                    .sorted(Comparator.comparing(DailyForecastSummary::getDate))
                    .collect(Collectors.toMap(
                            summary -> summary.getDate().getDayOfWeek().toString(),
                            summary -> summary,
                            (existing, replacement) -> existing,
                            LinkedHashMap::new
                    ));
            results.put(city, cityForecast);
        }
        return results;
    }
}
