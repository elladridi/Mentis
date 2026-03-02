package services;

import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class WeatherService {

    private static final String API_KEY = "821520bc49714bb58b204909262402";
    private static final String BASE_URL = "http://api.weatherapi.com/v1/forecast.json";

    public String getWeatherForecast(String location, String date) {
        try {
            // Format location for URL (replace spaces with %20)
            String encodedLocation = location.replace(" ", "%20");

            // Build API URL
            String urlString = String.format(
                    "%s?key=%s&q=%s&dt=%s",
                    BASE_URL, API_KEY, encodedLocation, date
            );

            System.out.println("🌤️ Fetching weather for: " + location + " on " + date);

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                String inline = "";
                Scanner scanner = new Scanner(url.openStream());

                while (scanner.hasNext()) {
                    inline += scanner.nextLine();
                }
                scanner.close();

                // Parse JSON
                JSONObject data = new JSONObject(inline);

                // Get location info
                JSONObject location_data = data.getJSONObject("location");
                String city = location_data.getString("name");
                String country = location_data.getString("country");

                // Get forecast for the day
                JSONObject forecast = data.getJSONObject("forecast")
                        .getJSONArray("forecastday")
                        .getJSONObject(0)
                        .getJSONObject("day");

                double maxTemp = forecast.getDouble("maxtemp_c");
                double minTemp = forecast.getDouble("mintemp_c");
                double avgTemp = forecast.getDouble("avgtemp_c");
                double chanceOfRain = forecast.getDouble("daily_chance_of_rain");
                double chanceOfSnow = forecast.getDouble("daily_chance_of_snow");
                double humidity = forecast.getDouble("avghumidity");
                String condition = forecast.getJSONObject("condition").getString("text");

                // Get weather icon/emoji
                String weatherEmoji = getWeatherEmoji(condition);

                // Format the forecast nicely
                String forecastText = String.format(
                        "%s %s\n📍 %s, %s\n🌡️ Temperature: %.1f°C (%.1f°C - %.1f°C)\n💧 Humidity: %.0f%%\n☔ Rain chance: %.0f%%\n❄️ Snow chance: %.0f%%",
                        weatherEmoji, condition, city, country, avgTemp, minTemp, maxTemp, humidity, chanceOfRain, chanceOfSnow
                );

                return forecastText;

            } else {
                System.out.println("Weather API returned code: " + responseCode);
                return getSimulatedWeather(location);
            }

        } catch (Exception e) {
            System.out.println("Weather API error: " + e.getMessage());
            e.printStackTrace();
            return getSimulatedWeather(location);
        }
    }

    private String getSimulatedWeather(String location) {
        // Random weather for demo when API fails
        String[] conditions = {"☀️ Sunny", "⛅ Partly cloudy", "☁️ Cloudy", "🌧️ Rainy", "⛈️ Thunderstorms", "❄️ Snowy"};
        String[] temps = {"22°C - 28°C", "18°C - 24°C", "15°C - 20°C", "12°C - 16°C", "10°C - 14°C", "-2°C - 2°C"};
        String[] rain = {"10%", "30%", "50%", "90%", "95%", "80% snow"};

        int index = (int)(Math.random() * conditions.length);

        return String.format("%s in %s\n🌡️ %s\n☔ Rain chance: %s",
                conditions[index], location, temps[index], rain[index]);
    }

    private String getWeatherEmoji(String condition) {
        // Map weather conditions to emojis
        String lower = condition.toLowerCase();

        if (lower.contains("sunny") || lower.contains("clear")) return "☀️";
        if (lower.contains("partly cloudy")) return "⛅";
        if (lower.contains("cloudy") || lower.contains("overcast")) return "☁️";
        if (lower.contains("mist") || lower.contains("fog")) return "🌫️";
        if (lower.contains("rain") || lower.contains("drizzle")) return "🌧️";
        if (lower.contains("thunder") || lower.contains("storm")) return "⛈️";
        if (lower.contains("snow") || lower.contains("blizzard")) return "❄️";
        if (lower.contains("ice") || lower.contains("sleet")) return "🌨️";
        if (lower.contains("wind")) return "💨";

        return "☀️"; // Default
    }

    // Additional method to get just the emoji for small displays
    public String getWeatherEmojiOnly(String location, String date) {
        try {
            String encodedLocation = location.replace(" ", "%20");
            String urlString = String.format(
                    "%s?key=%s&q=%s&dt=%s",
                    BASE_URL, API_KEY, encodedLocation, date
            );

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                String inline = "";
                Scanner scanner = new Scanner(url.openStream());

                while (scanner.hasNext()) {
                    inline += scanner.nextLine();
                }
                scanner.close();

                JSONObject data = new JSONObject(inline);
                JSONObject forecast = data.getJSONObject("forecast")
                        .getJSONArray("forecastday")
                        .getJSONObject(0)
                        .getJSONObject("day");

                String condition = forecast.getJSONObject("condition").getString("text");
                return getWeatherEmoji(condition);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String[] emojis = {"☀️", "⛅", "☁️", "🌧️", "⛈️", "❄️"};
        return emojis[(int)(Math.random() * emojis.length)];
    }
}