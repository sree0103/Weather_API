package com.demo.weather.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class WeatherData {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String city;
	private String weatherCondition;
	private double tempCelsius;
	private double feelsLikeCelsius;
	private double tempMinCelsius;
	private double tempMaxCelsius;
	private long timestamp;

	public WeatherData(String city, String weatherCondition, double tempCelsius, double feelsLikeCelsius,
			double tempMinCelsius, double tempMaxCelsius) {
		this.city = city;
		this.weatherCondition = weatherCondition;
		this.tempCelsius = tempCelsius;
		this.feelsLikeCelsius = feelsLikeCelsius;
		this.tempMinCelsius = tempMinCelsius;
		this.tempMaxCelsius = tempMaxCelsius;
	}

	public WeatherData() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getWeatherCondition() {
		return weatherCondition;
	}

	public void setWeatherCondition(String weatherCondition) {
		this.weatherCondition = weatherCondition;
	}

	public double getTempCelsius() {
		return tempCelsius;
	}

	public void setTempCelsius(double tempCelsius) {
		this.tempCelsius = tempCelsius;
	}

	public double getFeelsLikeCelsius() {
		return feelsLikeCelsius;
	}

	public void setFeelsLikeCelsius(double feelsLikeCelsius) {
		this.feelsLikeCelsius = feelsLikeCelsius;
	}

	public double getTempMinCelsius() {
		return tempMinCelsius;
	}

	public void setTempMinCelsius(double tempMinCelsius) {
		this.tempMinCelsius = tempMinCelsius;
	}

	public double getTempMaxCelsius() {
		return tempMaxCelsius;
	}

	public void setTempMaxCelsius(double tempMaxCelsius) {
		this.tempMaxCelsius = tempMaxCelsius;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	@Override
	public String toString() {
		return String.format(
				"WeatherData[id=%d, city='%s', condition='%s', temp=%.2f, feelsLike=%.2f, minTemp=%.2f, maxTemp=%.2f, timestamp=%d]",
				id, city, weatherCondition, tempCelsius, feelsLikeCelsius, tempMinCelsius, tempMaxCelsius, timestamp);
	}
}
