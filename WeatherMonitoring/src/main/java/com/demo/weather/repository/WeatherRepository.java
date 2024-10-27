package com.demo.weather.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.demo.weather.model.WeatherData;

@Repository
public interface WeatherRepository  extends JpaRepository<WeatherData, Long> {
 
	Optional<WeatherData> findByCity(String city);
}
