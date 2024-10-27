package com.demo.weather.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DailyWeatherSummaryTest {

    private DailyWeatherSummary summary;

    @BeforeEach
    public void setUp() {
        summary = new DailyWeatherSummary();
    }

    @Test
    public void testGettersAndSetters() {
        summary.setId(1L);
        summary.setCity("London");
        summary.setDate(LocalDate.of(2024, 10, 20));
        summary.setAvgTemperature(15.5);
        summary.setMinTemperature(10.0);
        summary.setMaxTemperature(20.0);
        summary.setTimestamp(System.currentTimeMillis());
        assertNotNull(summary.getId());
        assertEquals(1L, summary.getId());
        assertEquals("London", summary.getCity());
        assertEquals(LocalDate.of(2024, 10, 20), summary.getDate());
        assertEquals(15.5, summary.getAvgTemperature(), 0.01);
        assertEquals(10.0, summary.getMinTemperature(), 0.01);
        assertEquals(20.0, summary.getMaxTemperature(), 0.01);
        assertNotNull(summary.getTimestamp());
    }

    @Test
    public void testDefaultConstructor() {
        assertNotNull(summary);
        assertEquals(0.0, summary.getAvgTemperature());
        assertEquals(Double.MAX_VALUE, summary.getMinTemperature());
        assertEquals(Double.MIN_VALUE, summary.getMaxTemperature());
        assertNotNull(summary.getDate());
    }
}
