package com.demo.weather.repository;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import com.demo.weather.model.WeatherData;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
	    "spring.datasource.url=jdbc:mysql://localhost/weatherdb",
	    "spring.datasource.username=root",
	    "spring.datasource.password=Keerthi@0103",
	    "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
	    "spring.jpa.hibernate.ddl-auto=create-drop"
	})
public class WeatherRepositoryTest {

	@Autowired
    private WeatherRepository weatherRepository;

    @Test
    public void testSaveWeatherData() {
        WeatherData weatherData = new WeatherData();
        weatherData.setCity("Delhi");
        weatherData.setWeatherCondition("Sunny");
        weatherData.setTempCelsius(30.0);
        weatherData.setFeelsLikeCelsius(31.0);
        weatherData.setTimestamp(System.currentTimeMillis());

        WeatherData savedData = weatherRepository.save(weatherData);
        assertNotNull(savedData.getId(), "Saved weather data should have a generated ID");
    }
	
}
