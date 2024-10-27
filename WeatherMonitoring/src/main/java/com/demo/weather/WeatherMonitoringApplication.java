package com.demo.weather;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.demo.weather.config.WeatherConfig;

@SpringBootApplication
@EnableConfigurationProperties(WeatherConfig.class)
@EnableScheduling
public class WeatherMonitoringApplication {

	public static void main(String[] args) {
		SpringApplication.run(WeatherMonitoringApplication.class, args);
	}

}
