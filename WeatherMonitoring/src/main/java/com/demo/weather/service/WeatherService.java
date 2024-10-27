package com.demo.weather.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.demo.weather.config.WeatherConfig;
import com.demo.weather.model.DailyWeatherSummary;
import com.demo.weather.model.WeatherData;
import com.demo.weather.repository.DailyWeatherSummaryRepository;
import com.demo.weather.repository.WeatherRepository;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

@Service
public class WeatherService {
	private final WeatherRepository weatherRepository;
	private final WeatherConfig weatherConfig;
	private SendGrid sendGrid;
	private final RestTemplate restTemplate;
	private final DailyWeatherSummaryRepository dailyWeatherSummaryRepository;
	private final JavaMailSender mailSender;
//    private final Logger logger = Logger.getLogger(WeatherService.class.getName());
	private final Logger logger;
	private final String API_URL = "http://api.openweathermap.org/data/2.5/weather?q={city}&appid={apiKey}";
	public final List<String> cities = Arrays.asList("Delhi", "Mumbai", "Chennai", "Bangalore", "Kolkata", "Hyderabad");

	@Autowired
	public WeatherService(WeatherConfig weatherConfig, Logger logger, SendGrid sendGrid, RestTemplate restTemplate,
			WeatherRepository weatherRepository, DailyWeatherSummaryRepository dailyWeatherSummaryRepository,
			JavaMailSender mailSender) {
		this.weatherConfig = weatherConfig;
		this.logger = logger;
		this.sendGrid = sendGrid;
		this.restTemplate = restTemplate;
		this.weatherRepository = weatherRepository;
		this.dailyWeatherSummaryRepository = dailyWeatherSummaryRepository;
		this.mailSender = mailSender;
	}

	public void setSendGrid(SendGrid sendGrid) {
		this.sendGrid = sendGrid;
	}

	public String getApiKey() {
		return weatherConfig.getApiKey();
	}

	public List<String> getCities() {
		return cities;
	}

	@Scheduled(fixedRateString = "${weather.fetchInterval}")
	public void fetchWeatherDataForCities() {
		logger.info("Fetching weather data for cities...");

		for (String city : cities) {
			try {
				WeatherData weatherData = fetchWeatherData(city);
				if (weatherData != null) {
					logger.info("Weather data for " + city + " saved successfully.");
				}
			} catch (Exception e) {
				logger.severe("Failed to fetch or save weather data for " + city + ": " + e.getMessage());
			}
		}
	}

	public List<WeatherData> fetchAllWeatherData() {
		return weatherRepository.findAll();
	}

	public List<DailyWeatherSummary> getDailyWeatherSummary(String city) {
		List<DailyWeatherSummary> summaries = dailyWeatherSummaryRepository.findByCity(city);
		logger.info("Retrieved summaries for city: " + city + " - " + summaries);
		for (DailyWeatherSummary summary : summaries) {
			logger.info("Summary Details - City: " + summary.getCity() + ", Date: " + summary.getDate() + ", Avg Temp: "
					+ summary.getAvgTemperature() + ", Max Temp: " + summary.getMaxTemperature() + ", Min Temp: "
					+ summary.getMinTemperature());
		}
		return summaries;
	}

	public WeatherData getWeatherData(String city) {
		return weatherRepository.findByCity(city).orElse(null);
	}

	public WeatherData fetchWeatherData(String city) {
		if (!isValidCity(city)) {
			logger.severe("Invalid city name: " + city);
			return null;
		}

		String apiKey = weatherConfig.getApiKey();
		try {
			String result = restTemplate.getForObject(API_URL, String.class, city, apiKey);
			if (result == null) {
				logger.severe("API Response for city " + city + " is null.");
				return null;
			}

			JSONObject json = new JSONObject(result);
			if (!json.has("main")) {
				logger.severe("Main weather data is missing for the city: " + city);
				return null;
			}

			WeatherData weatherData = parseWeatherData(json, city);
			if (weatherData == null) {
				logger.severe("Failed to parse weather data for city: " + city);
				return null;
			}

			WeatherData savedWeatherData = weatherRepository.save(weatherData);
			calculateDailySummary(savedWeatherData);
			checkAlerts(savedWeatherData);
			checkThresholds(savedWeatherData);

			return savedWeatherData;

		} catch (HttpClientErrorException e) {
			logger.severe("HTTP error fetching weather data for city " + city + ": " + e.getMessage());
			if (e.getStatusCode().value() == 404) {
				logger.warning("City not found: " + city);
			}
		} catch (ResourceAccessException e) {
			logger.severe("Network error while fetching weather data for city " + city + ": " + e.getMessage());
		} catch (JSONException e) {
			logger.severe("JSON parsing error for city " + city + ": " + e.getMessage());
		} catch (Exception e) {
			logger.severe("Unexpected error while fetching weather data for city " + city + ": " + e.getMessage());
		}

		return null;
	}

	public boolean checkThresholds(WeatherData weatherData) {
		double temperatureThreshold = weatherConfig.getTempAlertThreshold();
		String weatherConditionThreshold = weatherConfig.getWeatherCondition();

		logger.info("Checking temperature for city: " + weatherData.getCity() + " - Current: "
				+ weatherData.getTempCelsius() + ", Threshold: " + temperatureThreshold);

		boolean breached = false;
		if (weatherData.getTempCelsius() > temperatureThreshold) {
			logger.warning("Alert: Temperature exceeds threshold! Current: " + weatherData.getTempCelsius()
					+ ", Threshold: " + temperatureThreshold);
			triggerAlert(weatherData.getCity(), "Temperature exceeds threshold: " + weatherData.getTempCelsius());
			breached = true;
		}
		if (weatherData.getWeatherCondition() != null
				&& weatherData.getWeatherCondition().equalsIgnoreCase(weatherConditionThreshold)) {
			logger.warning("Alert: Weather condition matches the alert threshold! Current: "
					+ weatherData.getWeatherCondition());
			breached = true;
		}

		return breached;
	}

	public WeatherData parseWeatherData(JSONObject json, String city) {
		WeatherData weatherData = new WeatherData();

		try {
			// Extract temperature data and convert from Kelvin to Celsius
			JSONObject main = json.optJSONObject("main");
			if (main == null) {
				logger.severe("Main weather data is missing for the city: " + city);
				return null;
			}

			double tempKelvin = main.optDouble("temp", 0.0);
			double feelsLikeKelvin = main.optDouble("feels_like", tempKelvin);
			double tempMinKelvin = main.optDouble("temp_min", tempKelvin);
			double tempMaxKelvin = main.optDouble("temp_max", tempKelvin);
			JSONArray weatherArray = json.optJSONArray("weather");
			if (weatherArray == null || weatherArray.length() == 0) {
				logger.severe("Weather data is missing for the city: " + city);
				return null;
			}

			String weatherCondition = weatherArray.getJSONObject(0).optString("main", "Unknown");
			logger.info("Weather condition for city " + city + ": " + weatherCondition);
			weatherData.setCity(city);
			weatherData.setWeatherCondition(weatherCondition);
			weatherData.setTempCelsius(convertKelvinToCelsius(tempKelvin));
			weatherData.setFeelsLikeCelsius(convertKelvinToCelsius(feelsLikeKelvin));
			weatherData.setTempMinCelsius(convertKelvinToCelsius(tempMinKelvin));
			weatherData.setTempMaxCelsius(convertKelvinToCelsius(tempMaxKelvin));
			weatherData.setTimestamp(System.currentTimeMillis());

		} catch (JSONException e) {
			logger.severe("JSON parsing error for city " + city + ": " + e.getMessage());
			return null;
		} catch (Exception e) {
			logger.severe("Unexpected error parsing weather data for city " + city + ": " + e.getMessage());
			return null;
		}

		return weatherData;
	}

	public double convertKelvinToCelsius(double kelvin) {
		return kelvin - 273.15;
	}

	public boolean isValidCity(String city) {
		String normalizedCity = city.toLowerCase();
		return cities.stream().anyMatch(c -> c.toLowerCase().equals(normalizedCity));
	}

	public void calculateDailySummary(WeatherData weatherData) {
		LocalDate today = LocalDate.now();
		Optional<DailyWeatherSummary> optionalSummary = dailyWeatherSummaryRepository
				.findFirstByCityAndDate(weatherData.getCity(), today);

		DailyWeatherSummary summary;
		if (optionalSummary.isEmpty()) {
			summary = new DailyWeatherSummary(weatherData.getCity(), today);
		} else {
			summary = optionalSummary.get();
		}
		summary.updateSummary(weatherData);
		dailyWeatherSummaryRepository.save(summary);
	}

	private int consecutiveHighTempBreaches = 0;

	public boolean checkAlerts(WeatherData weatherData) {
		boolean breached = false;
		double temperatureThreshold = weatherConfig.getTempAlertThreshold();
		if (weatherData.getTempCelsius() > temperatureThreshold) {
			consecutiveHighTempBreaches++;
			if (consecutiveHighTempBreaches >= 2) { // Alert only after 2 consecutive breaches
				logger.warning("Alert: Temperature exceeds threshold! Current: " + weatherData.getTempCelsius()
						+ ", Threshold: " + temperatureThreshold);
				triggerAlert(weatherData.getCity(), "Temperature exceeds threshold: " + weatherData.getTempCelsius());
				breached = true;
			}
		} else {
			consecutiveHighTempBreaches = 0; // Reset counter if below threshold
		}
		double lowTemperatureThreshold = weatherConfig.getLowTempAlertThreshold();
		if (weatherData.getTempCelsius() < lowTemperatureThreshold) {
			logger.warning("Alert: Temperature below threshold! Current: " + weatherData.getTempCelsius()
					+ ", Threshold: " + lowTemperatureThreshold);
			triggerAlert(weatherData.getCity(), "Temperature below threshold: " + weatherData.getTempCelsius());
			breached = true;
		}
		String expectedWeatherCondition = weatherConfig.getWeatherCondition();
		if (weatherData.getWeatherCondition() != null
				&& weatherData.getWeatherCondition().equalsIgnoreCase(expectedWeatherCondition)) {
			logger.warning("Alert: Weather condition matches threshold! Current: " + weatherData.getWeatherCondition());
			triggerAlert(weatherData.getCity(),
					"Weather condition matches threshold: " + weatherData.getWeatherCondition());
			breached = true;
		}
		return breached;
	}

	public void triggerAlert(String city, String message) {
		logger.info("Sending alert for " + city + ": " + message);
		sendEmail(city, message);
	}

	public void sendEmail(String city, String message) {
		String alertEmail = weatherConfig.getAlertEmail();

		if (alertEmail == null || alertEmail.isEmpty()) {
			logger.severe("Alert email is null or empty, unable to send notification");
			return;
		}
		Email from = new Email("2021mca07@cuk.ac.in");
		String subject = "Weather Alert for " + city;
		Email to = new Email(alertEmail);
		Content content = new Content("text/plain", message);
		Mail mail = new Mail(from, subject, to, content);

		Request request = new Request();

		try {
			request.setMethod(Method.POST);
			request.setEndpoint("mail/send");
			request.setBody(mail.build());

			Response response = sendGrid.api(request);
			if (response != null) {
				logger.info("Email sent to " + alertEmail + " regarding " + city + ". Status code: "
						+ response.getStatusCode());
				logger.info("Response body: " + response.getBody());
			} else {
				logger.severe("Failed to send email alert: Response is null");
			}
		} catch (IOException e) {
			logger.severe("Failed to send email alert: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			logger.severe("An unexpected error occurred: " + e.getMessage());
			e.printStackTrace();
		}
	}

}
