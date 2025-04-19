package com.example.weatherapp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

// DailyForecastSummary.java
@Data
@AllArgsConstructor
public class DailyForecastSummary {
    private LocalDate date;
    private double avgTemp;
    private String description;

    // 🆕 Add this to expose day name in the JSON
    @JsonProperty("day")
    public String getDayOfWeekName() {
        return date.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH);
    }
}

