package com.demo.weather.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "weather.apiKey=78cd2304c7e612af3da87e204a994b2f",
        "weather.fetchInterval=300000"
})
public class WeatherConfigTest {

    @Autowired
    private WeatherConfig weatherConfig;

    @Test
    void testApiKey() {
        assertEquals("78cd2304c7e612af3da87e204a994b2f", weatherConfig.getApiKey());
    }

    @Test
    void testFetchInterval() {
    	System.out.println("Fetch Interaval in test: " + weatherConfig.getFetchInterval());
        assertEquals(Long.valueOf(300000), weatherConfig.getFetchInterval());
    }
    
}
