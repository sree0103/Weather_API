package com.demo.weather.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "daily_weather_summary")
public class DailyWeatherSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double avgTemperature;
    private String city;
    private LocalDate date;
    private String dominantWeatherCondition; 
    private double maxTemperature = Double.NEGATIVE_INFINITY; 
    private double minTemperature = Double.POSITIVE_INFINITY;
    private int readingCount;
    private long timestamp; 
    private String weatherCondition;

    public String getWeatherCondition() {
		return weatherCondition;
	}

    public DailyWeatherSummary(String city, LocalDate date, double avgTemperature, double maxTemperature, double minTemperature) {
        this.city = city;
        this.date = date;
        this.avgTemperature = avgTemperature;
        this.maxTemperature = maxTemperature;
        this.minTemperature = minTemperature;
    }

	public void setWeatherCondition(String weatherCondition) {
		this.weatherCondition = weatherCondition;
	}


	// Constructor
    public DailyWeatherSummary(String city, LocalDate date) {
        this.city = city;
        this.date = date;
    }

    
    public void setId(Long id) {
		this.id = id;
	}


	public void setAvgTemperature(double avgTemperature) {
		this.avgTemperature = avgTemperature;
	}


	public void setCity(String city) {
		this.city = city;
	}


	public void setDate(LocalDate date) {
		this.date = date;
	}


	public void setDominantWeatherCondition(String dominantWeatherCondition) {
		this.dominantWeatherCondition = dominantWeatherCondition;
	}


	public void setMaxTemperature(double maxTemperature) {
		this.maxTemperature = maxTemperature;
	}


	public void setMinTemperature(double minTemperature) {
		this.minTemperature = minTemperature;
	}


	public void setReadingCount(int readingCount) {
		this.readingCount = readingCount;
	}


	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public DailyWeatherSummary(LocalDate date) {
		this.date = date;
	}

	public DailyWeatherSummary() {
	    this.date = LocalDate.now();
	}


	// Update summary based on the fetched weather data
	public void updateSummary(WeatherData weatherData) {
	    double newAvgTemp = (this.avgTemperature * this.readingCount + weatherData.getTempCelsius()) / (this.readingCount + 1);
	    this.avgTemperature = newAvgTemp;
	    this.maxTemperature = Math.max(this.maxTemperature, weatherData.getTempCelsius());
	    this.minTemperature = Math.min(this.minTemperature, weatherData.getTempCelsius());
	    this.readingCount++;
	    this.setDominantWeatherCondition(weatherData.getWeatherCondition());
	}
    public Long getId() {
        return id;
    }

    public double getAvgTemperature() {
        return avgTemperature;
    }

    public String getCity() {
        return city;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getDominantWeatherCondition() {
        return dominantWeatherCondition;
    }

    public double getMaxTemperature() {
        return maxTemperature;
    }

    public double getMinTemperature() {
        return minTemperature;
    }

    public int getReadingCount() {
        return readingCount;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
