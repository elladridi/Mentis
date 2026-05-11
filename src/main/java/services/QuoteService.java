package services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

public class QuoteService {
    public static String getDailyQuote() {
        try {
            URL url = new URL("https://zenquotes.io/api/random");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();

                JSONArray jsonArray = new JSONArray(response.toString());
                JSONObject firstQuote = jsonArray.getJSONObject(0);
                String quote = firstQuote.getString("q");
                String author = firstQuote.getString("a");
                return "\"" + quote + "\" — " + author;
            }
        } catch (Exception e) {
            System.err.println("Erreur API : " + e.getMessage());
        }
        return "Gardez espoir, chaque jour est une nouvelle chance.";
    }
}
