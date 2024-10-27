package com.demo.weather.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.demo.weather.config.WeatherConfig;
import com.demo.weather.model.DailyWeatherSummary;
import com.demo.weather.model.WeatherData;
import com.demo.weather.repository.DailyWeatherSummaryRepository;
import com.demo.weather.repository.WeatherRepository;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;

//@ExtendWith(MockitoExtension.class)
public class WeatherServiceTest {

	private static final String API_URL = "http://api.openweathermap.org/data/2.5/weather?q={city}&appid={apiKey}";
	
	@InjectMocks
    private WeatherService weatherService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private WeatherRepository weatherRepository;

    @Mock
    private WeatherConfig weatherConfig;
   
//    @Mock
//    private JavaMailSender mailSender;
    
    @Mock
    private Logger logger;
    
    @Mock
    private SendGrid sendGrid;
    
    @Mock
    private DailyWeatherSummaryRepository dailyWeatherSummaryRepository;

    @BeforeEach
    public void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        logger = mock(Logger.class);
        when(weatherConfig.getApiKey()).thenReturn("testApiKey");
        when(weatherConfig.getLowTempAlertThreshold()).thenReturn(5.0);
        when(weatherConfig.getTempAlertThreshold()).thenReturn(35.0);
        when(weatherConfig.getWeatherCondition()).thenReturn("Rain");
        when(sendGrid.api(any(Request.class))).thenReturn(new Response(202, "Email sent", null));
    }
      
    @Test
    public void testSystemSetup() {
        assertNotNull(weatherService);
        assertEquals("testApiKey", weatherConfig.getApiKey());
    }
    
    @Test
    void testDataRetrieval() {
        String city = "Some City";
        WeatherData mockWeatherData = new WeatherData();
        mockWeatherData.setCity(city);
        mockWeatherData.setWeatherCondition("Sunny");
        when(weatherRepository.findByCity(city)).thenReturn(Optional.of(mockWeatherData));
        WeatherData retrievedData = weatherService.getWeatherData(city); 
        assertNotNull(retrievedData, "Weather data should not be null");
        assertEquals(city, retrievedData.getCity(), "City should match");
    }
    
    
    @Test
    public void testTemperatureConversion() {
        double tempKelvin = 300.0;
        double tempCelsius = weatherService.convertKelvinToCelsius(tempKelvin);
        assertEquals(26.85, tempCelsius, 0.01);
    }

    @Test
    public void testDailyWeatherSummary() {
        LocalDate today = LocalDate.now();
        DailyWeatherSummary expectedSummary = new DailyWeatherSummary("Delhi", today, 31.0, 32.0, 30.0);
        List<DailyWeatherSummary> mockSummaries = Collections.singletonList(expectedSummary);
        when(dailyWeatherSummaryRepository.findByCity("Delhi")).thenReturn(mockSummaries);
        List<DailyWeatherSummary> summaries = weatherService.getDailyWeatherSummary("Delhi");
        assertNotNull(summaries, "The summaries list should not be null.");
        assertFalse(summaries.isEmpty(), "The summaries list should not be empty.");
        assertEquals(1, summaries.size());
        DailyWeatherSummary summary = summaries.get(0);
        assertEquals("Delhi", summary.getCity());
        assertEquals(today, summary.getDate());
        assertEquals(31.0, summary.getAvgTemperature(), 0.01);
        assertEquals(32.0, summary.getMaxTemperature(), 0.01);
        assertEquals(30.0, summary.getMinTemperature(), 0.01);
    }
    @Test
    public void testAlertingThresholds() {
        when(weatherConfig.getTempAlertThreshold()).thenReturn(34.0);
        when(weatherConfig.getWeatherCondition()).thenReturn("Rain");
        WeatherData weatherData = new WeatherData("Delhi", "Clear", 35.0, 36.0, 34.0, 37.0);
        boolean isBreached = weatherService.checkThresholds(weatherData);
        assertTrue(isBreached); 
        weatherData.setWeatherCondition("Rain");
        isBreached = weatherService.checkThresholds(weatherData);
        assertTrue(isBreached);
    }
    
    @Test
    public void testApplicationStartsAndConnectsToApi() {
        String apiKey = weatherService.getApiKey(); 
        assertNotNull(apiKey, "API key should not be null");
    }
    
  
    @Test
    public void testFetchWeatherData_ValidCity() {
        // Arrange
        String city = "Delhi";
        String apiResponse = "{ \"main\": { \"temp\": 300.15, \"feels_like\": 305.15 }, \"weather\": [{ \"main\": \"Sunny\" }] }";
        
        when(weatherConfig.getApiKey()).thenReturn("dummy-api-key");
        when(restTemplate.getForObject(API_URL, String.class, city, weatherConfig.getApiKey()))
            .thenReturn(apiResponse); 
        WeatherData mockWeatherData = new WeatherData();
        mockWeatherData.setCity(city);
        mockWeatherData.setTempCelsius(27.0);
        mockWeatherData.setWeatherCondition("Sunny");
        when(weatherRepository.save(any())).thenReturn(mockWeatherData);
        when(weatherRepository.findByCity(city)).thenReturn(Optional.of(mockWeatherData));
        WeatherData result = weatherService.fetchWeatherData(city);
        assertNotNull(result, "Expected WeatherData should not be null");
        assertEquals(mockWeatherData.getCity(), result.getCity(), "City should match");
        assertNotNull(result.getWeatherCondition(), "Weather condition should not be null");
        assertNotNull(result.getTempCelsius(), "Temperature should not be null");
    }
    @Test
    public void testParseWeatherData_ConvertTemperatureFromKelvinToCelsius() throws Exception {
        String city = "Delhi";
        String jsonResponse = "{"
            + "\"main\": {"
            + "\"temp\": 300.15,"  
            + "\"feels_like\": 302.15," 
            + "\"temp_min\": 299.15,"  
            + "\"temp_max\": 301.15" 
            + "},"
            + "\"weather\": [{"
            + "\"main\": \"Clear\""
            + "}]"
            + "}";

        JSONObject jsonObject = new JSONObject(jsonResponse);
        WeatherData weatherData = weatherService.parseWeatherData(jsonObject, city);

        // Assert that the temperatures are converted correctly
        assertEquals(27.0, weatherData.getTempCelsius(), 0.001);
        assertEquals(29.0, weatherData.getFeelsLikeCelsius(), 0.001);
        assertEquals(26.0, weatherData.getTempMinCelsius(), 0.001);
        assertEquals(28.0, weatherData.getTempMaxCelsius(), 0.001);
    }

    
    @Test
    public void testCalculateDailySummary() {
        WeatherData weatherData = createWeatherData("Delhi", 30.0, LocalDate.now());
        DailyWeatherSummary expectedSummary = new DailyWeatherSummary("Delhi", LocalDate.now());
        expectedSummary.setAvgTemperature(30.0);
        expectedSummary.setMaxTemperature(30.0);
        expectedSummary.setMinTemperature(30.0);
        expectedSummary.setReadingCount(1);
        when(dailyWeatherSummaryRepository.findFirstByCityAndDate("Delhi", LocalDate.now()))
            .thenReturn(Optional.of(expectedSummary));
        weatherService.calculateDailySummary(weatherData);
        DailyWeatherSummary summary = dailyWeatherSummaryRepository.findFirstByCityAndDate("Delhi", LocalDate.now()).orElse(null);
        if (summary == null) {
            System.out.println("DailyWeatherSummary not found for Delhi on " + LocalDate.now());
        } else {
            System.out.println("Summary found: Avg Temp = " + summary.getAvgTemperature());
            assertEquals(30.0, summary.getAvgTemperature(), 0.01);
            assertEquals(30.0, summary.getMaxTemperature());
            assertEquals(30.0, summary.getMinTemperature());
        }
    }

    private WeatherData createWeatherData(String city, double temperature, LocalDate date) {
        WeatherData weatherData = new WeatherData();
        weatherData.setCity(city);
        weatherData.setTempCelsius(temperature);
        weatherData.setTimestamp(date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli());
        return weatherData;
    }
    
    @Test
    void testCalculateDailySummaryForNewDay() {
        WeatherData weatherData = new WeatherData();
        weatherData.setCity("Delhi");
        weatherData.setTempCelsius(25);
        weatherData.setTempMaxCelsius(27);
        weatherData.setTempMinCelsius(23);
        weatherData.setWeatherCondition("Rainy");
        weatherData.setTimestamp(System.currentTimeMillis());
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        when(dailyWeatherSummaryRepository.findFirstByCityAndDate("Delhi", tomorrow))
            .thenReturn(Optional.empty());
        weatherService.calculateDailySummary(weatherData);
        verify(dailyWeatherSummaryRepository, times(1)).save(any(DailyWeatherSummary.class));
    }
   
    
    @Test
    public void testCheckAlerts_LowTemperatureBreach() {
        WeatherData weatherData = new WeatherData();
        weatherData.setTempCelsius(3.0);
        weatherData.setCity("Delhi");
        weatherData.setWeatherCondition("Clear");
        when(weatherConfig.getTempAlertThreshold()).thenReturn(25.0); 
        when(weatherConfig.getLowTempAlertThreshold()).thenReturn(5.0); 
        WeatherService spyWeatherService = spy(weatherService);
        boolean isBreached = spyWeatherService.checkAlerts(weatherData);
        assertTrue(isBreached); 
        verify(spyWeatherService, times(1)).triggerAlert(eq("Delhi"), anyString()); 
    }
    @Test
    public void testCheckAlerts_WeatherConditionMatchesThreshold() {
        WeatherData weatherData = createWeatherData("Mumbai", 20.0, "Rain");
        when(weatherConfig.getTempAlertThreshold()).thenReturn(35.0); 
        when(weatherConfig.getWeatherCondition()).thenReturn("Rain");
        when(weatherConfig.getLowTempAlertThreshold()).thenReturn(10.0);
        WeatherService spyWeatherService = spy(weatherService);
        boolean isBreached = spyWeatherService.checkAlerts(weatherData);
        assertTrue(isBreached, "Expected alert due to weather condition breach");
        verify(spyWeatherService, times(1)).triggerAlert(eq("Mumbai"), anyString());
    }
   
    @Test
    public void testCheckAlerts_AtTemperatureThreshold() {
        WeatherData weatherData = createWeatherData("Delhi", 25.0, "Clear");
        when(weatherConfig.getTempAlertThreshold()).thenReturn(25.0);
        assertFalse(weatherService.checkAlerts(weatherData)); 
    }

    @Test
    public void testCheckAlerts_NullWeatherData() {
        assertThrows(NullPointerException.class, () -> weatherService.checkAlerts(null));
    }  

    private WeatherData createWeatherData(String city, double temperature, String condition) {
        WeatherData weatherData = new WeatherData();
        weatherData.setCity(city);
        weatherData.setTempCelsius(temperature);
        weatherData.setWeatherCondition(condition);
        return weatherData;
    }
    @Test
    public void testFetchWeatherData_InvalidCity() {
        String city = "london"; 
        WeatherData result = weatherService.fetchWeatherData(city);
        assertNull(result);
        }

    @Test
    public void testFetchWeatherData_NetworkError() {
        String city = "London";
        when(weatherConfig.getApiKey()).thenReturn("dummy-api-key");
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenThrow(new ResourceAccessException("Network error"));
        WeatherData result = weatherService.fetchWeatherData(city);
        assertNull(result); 
    }
  
    
    void testFetchWeatherData_NullApiResponse() {
        // Arrange
        String city = "Delhi";
        when(weatherConfig.getApiKey()).thenReturn("dummy-api-key");
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(null);
        WeatherData result = weatherService.fetchWeatherData(city);
        assertNull(result); 
        verify(logger).severe(contains("API Response for city " + city + " is null."));
    }

    @Test
    void testCalculateDailySummary_NoExistingSummary() {
        WeatherData weatherData = new WeatherData();
        weatherData.setCity("Delhi");
        weatherData.setTempCelsius(35.0);
        when(dailyWeatherSummaryRepository.findByDate(any())).thenReturn(Collections.emptyList());
        weatherService.calculateDailySummary(weatherData);
        verify(dailyWeatherSummaryRepository).save(any(DailyWeatherSummary.class));
    }

    @Test
    void testCalculateDailySummary_ExistingSummary() {
        WeatherData weatherData = new WeatherData();
        weatherData.setCity("Delhi");
        weatherData.setTempCelsius(35.0); 
        DailyWeatherSummary existingSummary = new DailyWeatherSummary("Delhi", LocalDate.now());
        existingSummary.setAvgTemperature(30.0); 
        existingSummary.setMaxTemperature(32.0);
        existingSummary.setMinTemperature(28.0); 
        existingSummary.setReadingCount(1);

        when(dailyWeatherSummaryRepository.findFirstByCityAndDate("Delhi", LocalDate.now()))
                .thenReturn(Optional.of(existingSummary));
        weatherService.calculateDailySummary(weatherData);
        ArgumentCaptor<DailyWeatherSummary> captor = ArgumentCaptor.forClass(DailyWeatherSummary.class);
        verify(dailyWeatherSummaryRepository).save(captor.capture());
        DailyWeatherSummary savedSummary = captor.getValue();
        assertNotNull(savedSummary);
        assertEquals("Delhi", savedSummary.getCity());
        assertEquals(32.5, savedSummary.getAvgTemperature(), 0.01); 
       
    }
	    @Test
	    void testIsValidCity() {
	        assertTrue(weatherService.isValidCity("Delhi"));
	        assertFalse(weatherService.isValidCity("InvalidCity"));
	    }
    
    @Test
    public void testCheckAlerts_TemperatureExceedsThresholdTwice() throws Exception {
        when(weatherConfig.getTempAlertThreshold()).thenReturn(10.0);
        when(weatherConfig.getAlertEmail()).thenReturn("recipient@example.com"); 
        SendGrid mockSendGrid = mock(SendGrid.class);
        Response mockResponse = mock(Response.class);
        when(mockSendGrid.api(any(Request.class))).thenReturn(mockResponse);
        when(mockResponse.getStatusCode()).thenReturn(202);
        weatherService.setSendGrid(mockSendGrid);
        WeatherData highTempWeather1 = new WeatherData();
        highTempWeather1.setTempCelsius(15.0);
        highTempWeather1.setCity("Delhi");
        WeatherData highTempWeather2 = new WeatherData();
        highTempWeather2.setTempCelsius(12.0); 
        highTempWeather2.setCity("Delhi");
        weatherService.checkAlerts(highTempWeather1); 
        boolean result = weatherService.checkAlerts(highTempWeather2);
        assertTrue(result, "Temperature breach should trigger alert.");
        verify(mockSendGrid, times(1)).api(any(Request.class));
    }

    @Test
    public void testCheckAlerts_NoThresholdBreached() {
        WeatherData weatherData = new WeatherData();
        weatherData.setTempCelsius(30.0); 
        weatherData.setWeatherCondition("Sunny"); 
        weatherData.setCity("Bangalore");
        boolean isBreached = weatherService.checkAlerts(weatherData);
        assertFalse(isBreached); 
        verify(spy(weatherService), never()).triggerAlert(anyString(), anyString()); 
    } 
    
    
    @Test
    public void testCheckAlerts_LowTemperatureAlert() {
        WeatherData weatherData = new WeatherData();
        weatherData.setTempCelsius(2.0); 
        weatherData.setCity("Delhi");
        weatherData.setWeatherCondition("Clear");
        when(weatherConfig.getLowTempAlertThreshold()).thenReturn(5.0);
        WeatherService spyWeatherService = spy(weatherService); 
        boolean isBreached = spyWeatherService.checkAlerts(weatherData);
        assertTrue(isBreached, "Expected alert due to low temperature breach");
        verify(spyWeatherService, times(1)).triggerAlert(eq("Delhi"), anyString()); 
    }
    
    @Test
    public void testCheckAlerts_LowTemperatureExceedsThreshold() {
        WeatherData weatherData = new WeatherData();
        weatherData.setTempCelsius(5.0);
        weatherData.setWeatherCondition("Cloudy");
        weatherData.setCity("Chennai");
        when(weatherConfig.getLowTempAlertThreshold()).thenReturn(10.0); 
        WeatherService spyWeatherService = spy(weatherService); 
        boolean isBreached = spyWeatherService.checkAlerts(weatherData);
        assertTrue(isBreached, "Expected alert to be triggered for low temperature."); 
        verify(spyWeatherService, times(1)).triggerAlert(eq("Chennai"), anyString());
    }

}