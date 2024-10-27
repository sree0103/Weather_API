package com.demo.weather.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WeatherDataTest {

    @Test
    void testWeatherDataGettersAndSetters() {
        WeatherData weatherData = new WeatherData();
        
        weatherData.setId(1L);
        weatherData.setCity("Test City");
        weatherData.setWeatherCondition("Sunny");
        weatherData.setTempCelsius(25.0);
        weatherData.setFeelsLikeCelsius(26.0);
        weatherData.setTempMinCelsius(20.0);
        weatherData.setTempMaxCelsius(30.0);
        weatherData.setTimestamp(System.currentTimeMillis());

        assertEquals(1L, weatherData.getId());
        assertEquals("Test City", weatherData.getCity());
        assertEquals("Sunny", weatherData.getWeatherCondition());
        assertEquals(25.0, weatherData.getTempCelsius());
        assertEquals(26.0, weatherData.getFeelsLikeCelsius());
        assertEquals(20.0, weatherData.getTempMinCelsius());
        assertEquals(30.0, weatherData.getTempMaxCelsius());
    }

    @Test
    void testToString() {
        WeatherData weatherData = new WeatherData();
        weatherData.setId(1L);
        weatherData.setCity("Test City");
        weatherData.setWeatherCondition("Sunny");
        weatherData.setTempCelsius(25.0);
        weatherData.setFeelsLikeCelsius(26.0);
        weatherData.setTempMinCelsius(20.0);
        weatherData.setTempMaxCelsius(30.0);
        weatherData.setTimestamp(System.currentTimeMillis());

        String expectedString = String.format("WeatherData[id=%d, city='%s', condition='%s', temp=%.2f, feelsLike=%.2f, minTemp=%.2f, maxTemp=%.2f, timestamp=%d]",
                1L, "Test City", "Sunny", 25.0, 26.0, 20.0, 30.0, weatherData.getTimestamp());

        assertEquals(expectedString, weatherData.toString());
    }
}
