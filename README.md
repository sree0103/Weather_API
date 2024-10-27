# Weather_API


This is a real-time weather monitoring and alerting system that pulls data from the OpenWeatherMap API, stores it, performs various analyses, and sends alerts based on customizable weather thresholds. The system is designed with a modular structure using Spring Boot and includes scheduled data fetching, daily summaries, configurable alerts, and email notifications. Below is a detailed overview of each feature:

1. **Core Functionality**
   
Weather Data Fetching: Every 5 minutes, the system retrieves weather data for a predefined list of cities (Delhi, Mumbai, Chennai, Bangalore, Kolkata, Hyderabad) using the OpenWeatherMap API. It parses the response to extract temperature and weather conditions and stores the data in a relational database.
WeatherData Model: A class representing detailed weather data, storing attributes like temperature (in Celsius), weather conditions, and timestamps. This data is retrieved, processed, and used for analytics and alerting.


3. **Daily Summaries and Analytics**
   
DailyWeatherSummary Model: A class representing daily aggregated data for each city, including metrics like average, minimum, and maximum temperature.
Summarization Logic: Each time weather data is fetched, it updates the DailyWeatherSummary for that city and date, providing a historical view of weather trends.
Repository Layer: The DailyWeatherSummaryRepository handles storing and retrieving daily summaries, facilitating efficient data access and supporting analytics on daily weather patterns.


4. **Alerting and Thresholds**
   
Temperature and Condition Alerts: Configurable temperature thresholds (both high and low) and weather conditions can trigger alerts. Alerts only occur after a set number of consecutive threshold breaches to avoid false positives. The system compares current data to these thresholds each time new data is fetched.
Threshold Breach Detection: When thresholds are crossed, alerts are generated based on:
High/Low Temperature: Alerts when temperature exceeds or falls below specific thresholds.
Weather Condition Matching: Alerts if the weather condition matches a specified type (e.g., “Sunny”).
Alert Management: Tracks consecutive breaches for high temperatures and resets the counter once the data goes below the threshold.


5. **Email Notification with SendGrid**
   
Email Alerts: When thresholds are breached, the system triggers an email alert using SendGrid. The sendEmail method creates a formatted email detailing the breach, which is then sent to a configured alert email.
Error Handling: The email notification process includes error handling for cases where SendGrid is unavailable or configuration is incorrect, logging errors and ensuring robustness.


7. **Configuration and Customizability**
   
Configurable Parameters: Through a WeatherConfig class, the system provides properties like API keys, fetch intervals, temperature thresholds, weather condition thresholds, and alert email addresses.
Dynamic Fetch Interval: Uses @Scheduled annotation to manage data fetching intervals, allowing customization based on project needs.
City Validation: Ensures city names are valid before fetching data, reducing the likelihood of errors or invalid API calls.


9. **Logging and Error Handling**
    
Comprehensive Logging: Logs all major actions, including data fetching, summary calculations, threshold checks, and email notifications. Warnings and errors are recorded for threshold breaches and unexpected issues.
Error Resilience: Handles errors gracefully (e.g., network issues or API failures) by catching exceptions and logging them without interrupting the main application flow.


11. **Frontend and User Interface**
    
Dashboard with Thymeleaf: A user interface built with Thymeleaf allows users to select a city and view either the latest weather details or a summary. The data is dynamically loaded based on the selected city.
Separate JavaScript and CSS: CSS and JavaScript files are separated to enhance maintainability and manage presentation styles and interactive functionality on the frontend.
City-Specific Summaries: Displays daily weather summaries and historical trends for selected cities, showing users temperature metrics and alert triggers.


13. **Testing**
    
Unit Tests: Comprehensive testing covers core components, including weather data fetching, summary calculation, alert triggering, and email notifications. Tests include mock data, threshold scenarios, and resilience tests for network and JSON parsing errors.
LogCaptor for Log Testing: Uses LogCaptor to verify correct logging behavior, ensuring errors and alerts are correctly recorded.


15. **Project Structure**
    
Service Layer: Contains business logic (e.g., WeatherService), orchestrating data fetching, summaries, and alerts.
Controller Layer: Manages API endpoints for frontend interaction, exposing data-fetching methods to the UI.
Repository Layer: Interfaces with the database, supporting data persistence for weather data and daily summaries.
Resource Management: Structured resources, including external JavaScript and CSS files in designated folders (/static/css and /static/js), and HTML templates in /templates.


10.** Additional Features and Extensions**

Future Scalability: The project can expand to support more cities or additional data metrics.
Historical Data Analytics: Potentially allows for advanced analytics like trend forecasting, anomaly detection, or seasonal comparisons.
Additional Alerts: Can extend to support more complex alerting conditions, such as humidity or wind speed thresholds.


### Conclusion
This weather monitoring system effectively combines data fetching, storage, analytics, and alerting with a user-friendly frontend, providing users with timely insights and notifications on weather changes. The project is modular, scalable, and designed to handle real-time data efficiently, with robust error handling and logging, making it suitable for production-level deployment or further expansion.
