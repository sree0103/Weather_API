package com.demo.weather.config;

import java.util.logging.Logger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoggerConfig {

	@Bean
     Logger logger() {
        return Logger.getLogger("WeatherServiceLogger");
    }
}
