package com.demo.weather.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sendgrid.SendGrid;

@Configuration
public class SendGridConfig {

	@Value("${sendgrid.apiKey}")
    private String apiKey;
	@Bean
    SendGrid sendGrid() {
	  		if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("SendGrid API Key not found in environment variables");
        }
        return new SendGrid(apiKey);
    }
}
