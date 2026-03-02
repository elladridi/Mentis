package com.mentalhealth.app.services;

import javafx.scene.image.Image;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class GoogleMapsService {

    // ⚠️ PUT YOUR GOOGLE MAPS API KEY HERE
    private static final String API_KEY = "AIzaSyA4o0MJhwT0YzMMt7MUp9GdDFrlkCaKrlg";  // ← Change this!

    private static final String STATIC_MAP_URL = "https://maps.googleapis.com/maps/api/staticmap";
    private static final int DEFAULT_WIDTH = 400;
    private static final int DEFAULT_HEIGHT = 250;
    private static final int DEFAULT_ZOOM = 15;

    /**
     * Generate a static map image URL for a location
     */
    public static String getMapUrl(String location) {
        return getMapUrl(location, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_ZOOM);
    }

    /**
     * Generate a static map image URL with custom size and zoom
     */
    public static String getMapUrl(String location, int width, int height, int zoom) {
        try {
            String encodedLocation = URLEncoder.encode(location, StandardCharsets.UTF_8);

            return String.format(
                    "%s?center=%s&zoom=%d&size=%dx%d&markers=color:red%%7C%s&key=%s",
                    STATIC_MAP_URL,
                    encodedLocation,
                    zoom,
                    width,
                    height,
                    encodedLocation,
                    API_KEY
            );
        } catch (Exception e) {
            System.err.println("Error generating map URL: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get map as JavaFX Image
     */
    public static Image getMapImage(String location) {
        return getMapImage(location, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_ZOOM);
    }

    /**
     * Get map as JavaFX Image with custom size
     */
    public static Image getMapImage(String location, int width, int height, int zoom) {
        try {
            String mapUrl = getMapUrl(location, width, height, zoom);
            if (mapUrl == null) return null;

            System.out.println("📍 Loading map for: " + location);
            return new Image(mapUrl, true);  // true = load in background

        } catch (Exception e) {
            System.err.println("Error loading map image: " + e.getMessage());
            return null;
        }
    }

    /**
     * Check if API key is configured
     */
    public static boolean isConfigured() {
        return API_KEY != null &&
                !API_KEY.isEmpty() &&
                !API_KEY.equals("YOUR_GOOGLE_MAPS_API_KEY");
    }

    /**
     * Get a directions URL (opens in browser)
     */
    public static String getDirectionsUrl(String location) {
        try {
            String encodedLocation = URLEncoder.encode(location, StandardCharsets.UTF_8);
            return "https://www.google.com/maps/dir/?api=1&destination=" + encodedLocation;
        } catch (Exception e) {
            return "https://www.google.com/maps";
        }
    }

    /**
     * Get a search URL for the location (opens in browser)
     */
    public static String getSearchUrl(String location) {
        try {
            String encodedLocation = URLEncoder.encode(location, StandardCharsets.UTF_8);
            return "https://www.google.com/maps/search/?api=1&query=" + encodedLocation;
        } catch (Exception e) {
            return "https://www.google.com/maps";
        }
    }
}