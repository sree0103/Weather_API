package com.demo.weather.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.demo.weather.model.DailyWeatherSummary;

@Repository
public interface DailyWeatherSummaryRepository extends JpaRepository<DailyWeatherSummary, Long> {

	Optional<DailyWeatherSummary> findFirstByCityAndDate(String city, LocalDate date);
	Optional<DailyWeatherSummary> findByCityAndDate(String city, LocalDate date);
    List<DailyWeatherSummary> findByCity(String city);
    List<DailyWeatherSummary> findByDate(LocalDate date);
}
