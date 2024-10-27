package com.demo.weather.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
@ConfigurationProperties(prefix ="weather")
public class WeatherConfig {

	private static final Logger logger = LoggerFactory.getLogger(WeatherConfig.class);
	private Long fetchInterval;
    private String apiKey;
    private double tempAlertThreshold; 
    private double feelsLikeAlertThreshold; 
    private String weatherCondition; 
    private double lowTempAlertThreshold;
    private String alertEmail;

    public double getLowTempAlertThreshold() {
		return lowTempAlertThreshold;
	}

	public void setLowTempAlertThreshold(double lowTempAlertThreshold) {
		this.lowTempAlertThreshold = lowTempAlertThreshold;
	}

	public String getAlertEmail() {
		return alertEmail;
	}

	public void setAlertEmail(String alertEmail) {
		this.alertEmail = alertEmail;
	}

	public String getWeatherCondition() {
		return weatherCondition;
	}

	public void setWeatherCondition(String weatherCondition) {
		this.weatherCondition = weatherCondition;
	}

    public Long getFetchInterval() {
        return fetchInterval;
    }

    public double getTempAlertThreshold() {
		return tempAlertThreshold;
	}

	public void setTempAlertThreshold(double tempAlertThreshold) {
		this.tempAlertThreshold = tempAlertThreshold;
	}

	public double getFeelsLikeAlertThreshold() {
		return feelsLikeAlertThreshold;
	}

	public void setFeelsLikeAlertThreshold(double feelsLikeAlertThreshold) {
		this.feelsLikeAlertThreshold = feelsLikeAlertThreshold;
	}

	public void setFetchInterval(Long fetchInterval) {
        this.fetchInterval = fetchInterval;
    }
    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
    	//logger.debug("Setting API Key: {}", apiKey);
        this.apiKey = apiKey;
    }

    
    
    @PostConstruct
    public void init() {
//        System.out.println("API Key: " + apiKey);
        System.out.println("Fetch Interval: " + fetchInterval);
    }
}
