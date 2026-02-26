package services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class YouTubeRecommendationService {

    // ── PASTE YOUR KEY HERE ───────────────────────────────────────────────────
    private static final String API_KEY = "AIzaSyBpLEeIXjXyD4sS8vehVJ_VKATHwntd3Bk";
    // ─────────────────────────────────────────────────────────────────────────

    private static final String BASE_URL =
            "https://www.googleapis.com/youtube/v3/search";

    // Video result model
    public static class VideoResult {
        public final String videoId;
        public final String title;
        public final String channelTitle;
        public final String thumbnail;   // URL
        public final String description;
        public final String watchUrl;

        public VideoResult(String videoId, String title, String channelTitle,
                           String thumbnail, String description) {
            this.videoId      = videoId;
            this.title        = title;
            this.channelTitle = channelTitle;
            this.thumbnail    = thumbnail;
            this.description  = description;
            this.watchUrl     = "https://www.youtube.com/watch?v=" + videoId;
        }
    }

    /**
     * Build a search query based on assessment type and risk level.
     */
    public static String buildQuery(String assessmentType, String riskLevel) {
        String base;
        if (assessmentType == null) assessmentType = "";
        switch (assessmentType.toLowerCase()) {
            case "depression":
                base = riskLevel != null && riskLevel.toLowerCase().contains("high")
                        ? "depression relief guided meditation therapy"
                        : "uplifting music for depression mood boost";
                break;
            case "anxiety":
                base = "anxiety relief breathing exercises calm meditation";
                break;
            case "stress":
                base = "stress relief relaxation music nature sounds";
                break;
            case "wellness":
                base = "mindfulness meditation wellness self care";
                break;
            case "sleep":
                base = "sleep meditation relaxing music bedtime";
                break;
            default:
                base = "mental health relaxation therapy guided meditation";
        }

        // Add urgency keywords for high risk
        if (riskLevel != null) {
            String lower = riskLevel.toLowerCase();
            if (lower.contains("high") || lower.contains("severe") || lower.contains("critical")) {
                base += " crisis support mental health";
            }
        }
        return base;
    }

    /**
     * Fetch up to maxResults videos. Returns empty list on any error.
     */
    public static List<VideoResult> fetchVideos(String assessmentType,
                                                String riskLevel,
                                                int maxResults) {
        List<VideoResult> results = new ArrayList<>();
        try {
            String query = buildQuery(assessmentType, riskLevel);
            String encoded = URLEncoder.encode(query, "UTF-8");

            String urlStr = BASE_URL
                    + "?part=snippet"
                    + "&type=video"
                    + "&videoEmbeddable=true"
                    + "&safeSearch=strict"
                    + "&relevanceLanguage=en"
                    + "&maxResults=" + maxResults
                    + "&q=" + encoded
                    + "&key=" + API_KEY;

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);
            conn.setRequestProperty("Accept", "application/json");

            int status = conn.getResponseCode();
            if (status != 200) {
                System.err.println("[YouTube] HTTP " + status);
                return results;
            }

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();

            JSONObject root = new JSONObject(sb.toString());
            JSONArray items = root.optJSONArray("items");
            if (items == null) return results;

            for (int i = 0; i < items.length(); i++) {
                JSONObject item    = items.getJSONObject(i);
                JSONObject idObj   = item.optJSONObject("id");
                JSONObject snippet = item.optJSONObject("snippet");
                if (idObj == null || snippet == null) continue;

                String videoId      = idObj.optString("videoId", "");
                String title        = snippet.optString("title", "Untitled");
                String channelTitle = snippet.optString("channelTitle", "");
                String description  = snippet.optString("description", "");

                // Prefer medium thumbnail, fall back to default
                String thumbnail = "";
                JSONObject thumbs = snippet.optJSONObject("thumbnails");
                if (thumbs != null) {
                    JSONObject medium = thumbs.optJSONObject("medium");
                    JSONObject high   = thumbs.optJSONObject("high");
                    JSONObject def    = thumbs.optJSONObject("default");
                    if (high   != null) thumbnail = high.optString("url", "");
                    else if (medium != null) thumbnail = medium.optString("url", "");
                    else if (def    != null) thumbnail = def.optString("url", "");
                }

                if (!videoId.isEmpty()) {
                    results.add(new VideoResult(videoId, title,
                            channelTitle, thumbnail, description));
                }
            }

        } catch (Exception e) {
            System.err.println("[YouTube] Error fetching videos: " + e.getMessage());
        }
        return results;
    }
}