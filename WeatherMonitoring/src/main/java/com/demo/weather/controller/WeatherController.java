package com.demo.weather.controller;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.demo.weather.model.DailyWeatherSummary;
import com.demo.weather.model.WeatherData;
import com.demo.weather.repository.DailyWeatherSummaryRepository;
import com.demo.weather.service.WeatherService;

import jakarta.annotation.PostConstruct;

@Controller
@RequestMapping("/weather")
public class WeatherController {

    @Autowired
    private WeatherService weatherService;

    @Autowired
    private DailyWeatherSummaryRepository dailyWeatherSummaryRepository;

    private static final Logger logger = Logger.getLogger(WeatherController.class.getName());
    private List<String> availableCities;

    @PostConstruct
    public void init() {
        this.availableCities = weatherService.cities.stream().map(String::toLowerCase).toList();
    }

    @GetMapping("/{city}")
    public String getWeatherByCity(@PathVariable String city, Model model) {
        logger.info("Fetching weather data for city: " + city);

        if (!isValidCity(city)) {
            return handleError(model, "Error: City '" + city + "' is not supported.");
        }

        WeatherData weatherData = weatherService.fetchWeatherData(city);
        if (weatherData != null) {
            model.addAttribute("city", city);
            model.addAttribute("weatherData", weatherData);
            return "weather";
        } else {
            return handleError(model, "Weather data not found for city: " + city);
        }
    }

    @GetMapping("/summary/{city}")
    public String getWeatherSummary(@PathVariable String city, Model model) {
        logger.info("Fetching summary for city: " + city);
        List<DailyWeatherSummary> summaries = weatherService.getDailyWeatherSummary(city);
        List<String> dates = summaries.stream()
                .map(summary -> summary.getDate().toString())
                .collect(Collectors.toList());
        List<Double> avgTemps = summaries.stream()
                .map(DailyWeatherSummary::getAvgTemperature)
                .collect(Collectors.toList());
        List<Double> maxTemps = summaries.stream()
                .map(DailyWeatherSummary::getMaxTemperature)
                .collect(Collectors.toList());
        List<Double> minTemps = summaries.stream()
                .map(DailyWeatherSummary::getMinTemperature)
                .collect(Collectors.toList());
        List<Boolean> breaches = summaries.stream()
                .map(summary -> {
                    WeatherData weatherData = convertToWeatherData(summary);
                    return weatherService.checkThresholds(weatherData);
                })
                .collect(Collectors.toList());
        model.addAttribute("city", city);
        model.addAttribute("dates", dates);
        model.addAttribute("avgTemps", avgTemps);
        model.addAttribute("maxTemps", maxTemps);
        model.addAttribute("minTemps", minTemps);
        model.addAttribute("summaries", summaries);
        model.addAttribute("breaches", breaches);
        return "weather-summary"; 
    }

    private WeatherData convertToWeatherData(DailyWeatherSummary summary) {
        WeatherData weatherData = new WeatherData();
        weatherData.setTempCelsius(summary.getAvgTemperature());
        weatherData.setTempMaxCelsius(summary.getMaxTemperature());
        weatherData.setTempMinCelsius(summary.getMinTemperature());
        return weatherData;
    }

    private boolean isValidCity(String city) {
        String normalizedCity = city.toLowerCase();
        logger.info("Validating city: " + normalizedCity);
        return availableCities.contains(normalizedCity);
    }

    private String handleError(Model model, String message) {
        model.addAttribute("errorMessage", message);
        return "error";
    }
}
