package com.demo.weather.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.demo.weather.model.AlertThreshold;

@Repository
public interface AlertThresholdRepository extends JpaRepository<AlertThreshold, Long>  {

	Optional<AlertThreshold> findByCity(String city);
}
