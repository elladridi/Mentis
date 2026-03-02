package services;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class GeolocationService {

    /**
     * Check if the risk level is critical and show emergency resources
     */
    public static void checkAndShowEmergencyResources(String riskLevel, String country) {
        if (isCriticalRisk(riskLevel)) {
            Platform.runLater(() -> showEmergencyMap());
        }
    }

    /**
     * Determine if risk level is critical
     */
    public static boolean isCriticalRisk(String riskLevel) {
        if (riskLevel == null) return false;
        String lower = riskLevel.toLowerCase();
        return lower.contains("severe") || lower.contains("critical") ||
                lower.contains("high") || lower.contains("emergency");
    }

    /**
     * Show emergency map window
     */
    private static void showEmergencyMap() {
        Stage mapStage = new Stage();
        mapStage.initModality(Modality.NONE);
        mapStage.setTitle("🚨 Emergency Resources - Find Help Near You");
        mapStage.setMinWidth(950);
        mapStage.setMinHeight(750);

        BorderPane root = new BorderPane();

        // ── Top warning banner ──────────────────────────────────────────────
        Label banner = new Label("⚠️  CRITICAL RISK DETECTED — EMERGENCY RESOURCES NEAR YOU  ⚠️");
        banner.setMaxWidth(Double.MAX_VALUE);
        banner.setAlignment(Pos.CENTER);
        banner.setStyle(
                "-fx-background-color: #d32f2f;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 14px;"
        );

        // ── WebView ─────────────────────────────────────────────────────────
        WebView webView = new WebView();
        webView.setContextMenuEnabled(false);
        WebEngine engine = webView.getEngine();

        // Allow JS and set user-agent so Nominatim accepts requests
        engine.setUserAgent("Mozilla/5.0 MentisMentalHealthApp/1.0");
        engine.setJavaScriptEnabled(true);

        engine.loadContent(buildMapHtml());

        // ── Bottom button row ────────────────────────────────────────────────
        Button contactsBtn = new Button("📞 Emergency Numbers");
        contactsBtn.setStyle(
                "-fx-background-color: #d32f2f; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-font-size: 13px;" +
                        "-fx-padding: 10 22; -fx-cursor: hand; -fx-background-radius: 5;"
        );
        contactsBtn.setOnAction(e -> showEmergencyContacts());

        Button closeBtn = new Button("Close");
        closeBtn.setStyle(
                "-fx-background-color: #757575; -fx-text-fill: white;" +
                        "-fx-font-size: 13px; -fx-padding: 10 22;" +
                        "-fx-cursor: hand; -fx-background-radius: 5;"
        );
        closeBtn.setOnAction(e -> mapStage.close());

        HBox bottomBar = new HBox(20, contactsBtn, closeBtn);
        bottomBar.setAlignment(Pos.CENTER);
        bottomBar.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 14px;");

        root.setTop(banner);
        root.setCenter(webView);
        root.setBottom(bottomBar);

        mapStage.setScene(new Scene(root, 950, 750));
        mapStage.show();
    }

    // ── HTML + Leaflet map ───────────────────────────────────────────────────
    private static String buildMapHtml() {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "  <meta charset='utf-8'/>\n" +
                "  <title>Emergency Resources Map</title>\n" +
                "  <link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>\n" +
                "  <script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>\n" +
                "  <style>\n" +
                "    * { margin:0; padding:0; box-sizing:border-box; }\n" +
                "    body { font-family: Arial, sans-serif; display:flex; flex-direction:column; height:100vh; }\n" +
                "    #toolbar {\n" +
                "      display:flex; gap:10px; align-items:center; flex-wrap:wrap;\n" +
                "      padding:10px 14px; background:#fff3f3; border-bottom:2px solid #d32f2f;\n" +
                "    }\n" +
                "    #toolbar button {\n" +
                "      padding:8px 16px; border:none; border-radius:5px;\n" +
                "      cursor:pointer; font-weight:bold; font-size:13px;\n" +
                "    }\n" +
                "    #btn-hospital  { background:#e53935; color:#fff; }\n" +
                "    #btn-mental    { background:#1976d2; color:#fff; }\n" +
                "    #btn-pharmacy  { background:#388e3c; color:#fff; }\n" +
                "    #status { font-size:13px; color:#555; margin-left:auto; }\n" +
                "    #map { flex:1; }\n" +
                "    #results {\n" +
                "      max-height:160px; overflow-y:auto;\n" +
                "      border-top:1px solid #ddd; background:#fafafa;\n" +
                "    }\n" +
                "    .result-item {\n" +
                "      padding:8px 14px; border-bottom:1px solid #eee;\n" +
                "      cursor:pointer; font-size:13px;\n" +
                "    }\n" +
                "    .result-item:hover { background:#fce4ec; }\n" +
                "    .result-title { font-weight:bold; color:#c62828; }\n" +
                "    .popup-title { font-weight:bold; color:#c62828; font-size:14px; margin-bottom:4px; }\n" +
                "    .popup-addr  { font-size:12px; color:#444; }\n" +
                "    .popup-badge {\n" +
                "      display:inline-block; margin-top:6px;\n" +
                "      background:#d32f2f; color:#fff;\n" +
                "      padding:2px 8px; border-radius:3px; font-size:11px;\n" +
                "    }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "\n" +
                "<div id='toolbar'>\n" +
                "  <button id='btn-hospital' onclick='searchType(\"hospital\")'>🏥 Hospitals</button>\n" +
                "  <button id='btn-mental'   onclick='searchType(\"mental_health\")'>🧠 Mental Health Centers</button>\n" +
                "  <button id='btn-pharmacy' onclick='searchType(\"pharmacy\")'>💊 Pharmacies</button>\n" +
                "  <span id='status'>Detecting your location…</span>\n" +
                "</div>\n" +
                "\n" +
                "<div id='map'></div>\n" +
                "<div id='results'></div>\n" +
                "\n" +
                "<script>\n" +
                "// ── Map setup ──────────────────────────────────────────────────────\n" +
                "var map = L.map('map').setView([20, 0], 2);\n" +
                "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {\n" +
                "  attribution: '© OpenStreetMap contributors',\n" +
                "  maxZoom: 19\n" +
                "}).addTo(map);\n" +
                "\n" +
                "var userMarker = null;\n" +
                "var markers = [];\n" +
                "var userLat = null, userLon = null;\n" +
                "\n" +
                "// ── Step 1: get coordinates via ip-api.com (no browser permission needed) ──\n" +
                "function locateByIP() {\n" +
                "  setStatus('Detecting your location via IP…');\n" +
                "  fetch('http://ip-api.com/json/?fields=status,lat,lon,city,country')\n" +
                "    .then(r => r.json())\n" +
                "    .then(d => {\n" +
                "      if (d.status === 'success') {\n" +
                "        setStatus('Located: ' + d.city + ', ' + d.country);\n" +
                "        onLocationFound(d.lat, d.lon);\n" +
                "      } else {\n" +
                "        setStatus('IP location failed — using browser geolocation…');\n" +
                "        locateByBrowser();\n" +
                "      }\n" +
                "    })\n" +
                "    .catch(() => {\n" +
                "      setStatus('Trying browser geolocation…');\n" +
                "      locateByBrowser();\n" +
                "    });\n" +
                "}\n" +
                "\n" +
                "// ── Step 2 (fallback): browser geolocation ─────────────────────────\n" +
                "function locateByBrowser() {\n" +
                "  if (!navigator.geolocation) { onLocationFailed(); return; }\n" +
                "  navigator.geolocation.getCurrentPosition(\n" +
                "    p => onLocationFound(p.coords.latitude, p.coords.longitude),\n" +
                "    ()  => onLocationFailed(),\n" +
                "    { timeout: 8000 }\n" +
                "  );\n" +
                "}\n" +
                "\n" +
                "function onLocationFound(lat, lon) {\n" +
                "  userLat = lat; userLon = lon;\n" +
                "  map.setView([lat, lon], 14);\n" +
                "\n" +
                "  if (userMarker) map.removeLayer(userMarker);\n" +
                "  userMarker = L.circleMarker([lat, lon], {\n" +
                "    radius: 10, color: '#1565c0', fillColor: '#42a5f5',\n" +
                "    fillOpacity: 0.9, weight: 3\n" +
                "  }).addTo(map).bindPopup('<b>📍 Your Location</b>').openPopup();\n" +
                "\n" +
                "  // Auto-load hospitals\n" +
                "  searchType('hospital');\n" +
                "}\n" +
                "\n" +
                "function onLocationFailed() {\n" +
                "  setStatus('Could not detect location. Showing global view.');\n" +
                "  map.setView([48.8566, 2.3522], 12);  // Paris as default\n" +
                "  userLat = 48.8566; userLon = 2.3522;\n" +
                "  searchType('hospital');\n" +
                "}\n" +
                "\n" +
                "// ── Overpass API search ────────────────────────────────────────────\n" +
                "function searchType(type) {\n" +
                "  if (userLat === null) { setStatus('Location not ready yet…'); return; }\n" +
                "  clearMarkers();\n" +
                "  document.getElementById('results').innerHTML = '';\n" +
                "\n" +
                "  var radius = 10000;  // 10 km\n" +
                "  var overpassQuery = '';\n" +
                "  var label = '';\n" +
                "  var emoji = '';\n" +
                "\n" +
                "  if (type === 'hospital') {\n" +
                "    label = 'Hospital'; emoji = '🏥';\n" +
                "    overpassQuery =\n" +
                "      '[out:json][timeout:20];' +\n" +
                "      '(' +\n" +
                "        'node[\"amenity\"=\"hospital\"](around:' + radius + ',' + userLat + ',' + userLon + ');' +\n" +
                "        'way[\"amenity\"=\"hospital\"](around:' + radius + ',' + userLat + ',' + userLon + ');' +\n" +
                "      ');' +\n" +
                "      'out center 20;';\n" +
                "  } else if (type === 'mental_health') {\n" +
                "    label = 'Mental Health Center'; emoji = '🧠';\n" +
                "    overpassQuery =\n" +
                "      '[out:json][timeout:20];' +\n" +
                "      '(' +\n" +
                "        'node[\"healthcare\"=\"psychotherapist\"](around:' + radius + ',' + userLat + ',' + userLon + ');' +\n" +
                "        'node[\"healthcare\"=\"psychiatrist\"](around:' + radius + ',' + userLat + ',' + userLon + ');' +\n" +
                "        'node[\"amenity\"=\"social_facility\"](around:' + radius + ',' + userLat + ',' + userLon + ');' +\n" +
                "        'node[\"healthcare\"=\"mental_health\"](around:' + radius + ',' + userLat + ',' + userLon + ');' +\n" +
                "        'way[\"healthcare\"~\"psychotherapist|psychiatrist|mental_health\"](around:' + radius + ',' + userLat + ',' + userLon + ');' +\n" +
                "      ');' +\n" +
                "      'out center 20;';\n" +
                "  } else if (type === 'pharmacy') {\n" +
                "    label = 'Pharmacy'; emoji = '💊';\n" +
                "    overpassQuery =\n" +
                "      '[out:json][timeout:20];' +\n" +
                "      '(' +\n" +
                "        'node[\"amenity\"=\"pharmacy\"](around:' + radius + ',' + userLat + ',' + userLon + ');' +\n" +
                "        'way[\"amenity\"=\"pharmacy\"](around:' + radius + ',' + userLat + ',' + userLon + ');' +\n" +
                "      ');' +\n" +
                "      'out center 20;';\n" +
                "  }\n" +
                "\n" +
                "  setStatus('Searching for nearby ' + label + 's…');\n" +
                "\n" +
                "  var url = 'https://overpass-api.de/api/interpreter?data=' + encodeURIComponent(overpassQuery);\n" +
                "\n" +
                "  fetch(url)\n" +
                "    .then(r => r.json())\n" +
                "    .then(data => {\n" +
                "      var elements = data.elements || [];\n" +
                "      setStatus('Found ' + elements.length + ' ' + label + '(s) within 10 km');\n" +
                "      if (elements.length === 0) {\n" +
                "        setStatus('No ' + label + 's found nearby — try expanding area or different type.');\n" +
                "        return;\n" +
                "      }\n" +
                "      renderResults(elements, label, emoji);\n" +
                "    })\n" +
                "    .catch(err => {\n" +
                "      console.error(err);\n" +
                "      setStatus('Search failed. Check your internet connection.');\n" +
                "    });\n" +
                "}\n" +
                "\n" +
                "// ── Render markers + result list ───────────────────────────────────\n" +
                "function renderResults(elements, label, emoji) {\n" +
                "  var resultsDiv = document.getElementById('results');\n" +
                "  var bounds = [];\n" +
                "\n" +
                "  elements.forEach(function(el) {\n" +
                "    var lat = el.lat || (el.center && el.center.lat);\n" +
                "    var lon = el.lon || (el.center && el.center.lon);\n" +
                "    if (!lat || !lon) return;\n" +
                "\n" +
                "    var name = (el.tags && (el.tags.name || el.tags['name:en'])) || label;\n" +
                "    var phone = (el.tags && el.tags.phone) || '';\n" +
                "    var addr = '';\n" +
                "    if (el.tags) {\n" +
                "      addr = [el.tags['addr:housenumber'], el.tags['addr:street'], el.tags['addr:city']]\n" +
                "             .filter(Boolean).join(', ');\n" +
                "    }\n" +
                "\n" +
                "    var distKm = haversine(userLat, userLon, lat, lon).toFixed(2);\n" +
                "\n" +
                "    // Custom icon\n" +
                "    var icon = L.divIcon({\n" +
                "      className: '',\n" +
                "      html: '<div style=\"font-size:22px;line-height:1;filter:drop-shadow(1px 1px 2px #000)\">' + emoji + '</div>',\n" +
                "      iconSize: [28, 28], iconAnchor: [14, 14]\n" +
                "    });\n" +
                "\n" +
                "    var popupHtml =\n" +
                "      '<div style=\"min-width:200px\">' +\n" +
                "      '<div class=\"popup-title\">' + emoji + ' ' + name + '</div>' +\n" +
                "      (addr ? '<div class=\"popup-addr\">📍 ' + addr + '</div>' : '') +\n" +
                "      (phone ? '<div class=\"popup-addr\">📞 ' + phone + '</div>' : '') +\n" +
                "      '<div class=\"popup-addr\">🚶 ' + distKm + ' km away</div>' +\n" +
                "      '<div class=\"popup-badge\">Emergency Support</div>' +\n" +
                "      '</div>';\n" +
                "\n" +
                "    var m = L.marker([lat, lon], { icon: icon })\n" +
                "              .addTo(map)\n" +
                "              .bindPopup(popupHtml);\n" +
                "    markers.push(m);\n" +
                "    bounds.push([lat, lon]);\n" +
                "\n" +
                "    // Result list item\n" +
                "    var item = document.createElement('div');\n" +
                "    item.className = 'result-item';\n" +
                "    item.innerHTML =\n" +
                "      '<span class=\"result-title\">' + emoji + ' ' + name + '</span>' +\n" +
                "      (addr ? ' — <small>' + addr + '</small>' : '') +\n" +
                "      ' <small style=\"color:#888\">(' + distKm + ' km)</small>';\n" +
                "    item.onclick = function() {\n" +
                "      map.setView([lat, lon], 16);\n" +
                "      m.openPopup();\n" +
                "    };\n" +
                "    resultsDiv.appendChild(item);\n" +
                "  });\n" +
                "\n" +
                "  // Fit map to show all results + user\n" +
                "  if (bounds.length > 0) {\n" +
                "    bounds.push([userLat, userLon]);\n" +
                "    map.fitBounds(bounds, { padding: [40, 40] });\n" +
                "  }\n" +
                "}\n" +
                "\n" +
                "function clearMarkers() {\n" +
                "  markers.forEach(m => map.removeLayer(m));\n" +
                "  markers = [];\n" +
                "}\n" +
                "\n" +
                "function setStatus(msg) {\n" +
                "  document.getElementById('status').textContent = msg;\n" +
                "}\n" +
                "\n" +
                "// ── Haversine distance formula (km) ───────────────────────────────\n" +
                "function haversine(lat1, lon1, lat2, lon2) {\n" +
                "  var R = 6371;\n" +
                "  var dLat = (lat2 - lat1) * Math.PI / 180;\n" +
                "  var dLon = (lon2 - lon1) * Math.PI / 180;\n" +
                "  var a = Math.sin(dLat/2) * Math.sin(dLat/2) +\n" +
                "          Math.cos(lat1 * Math.PI/180) * Math.cos(lat2 * Math.PI/180) *\n" +
                "          Math.sin(dLon/2) * Math.sin(dLon/2);\n" +
                "  return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));\n" +
                "}\n" +
                "\n" +
                "// ── Start ──────────────────────────────────────────────────────────\n" +
                "locateByIP();\n" +
                "</script>\n" +
                "</body>\n" +
                "</html>";
    }

    /**
     * Show emergency contact numbers dialog
     */
    private static void showEmergencyContacts() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("📞 Emergency Contacts");
        alert.setHeaderText("Mental Health Crisis Resources");
        alert.setContentText(
                "🌍 INTERNATIONAL EMERGENCY NUMBERS\n" +
                        "----------------------------------------\n" +
                        "🚑 Emergency (Ambulance/Fire/Police):\n" +
                        "  USA/Canada: 911\n" +
                        "  UK/Europe:  112\n" +
                        "  Australia:  000\n" +
                        "  Japan:      119\n" +
                        "  India:      112\n\n" +

                        "🧠 Mental Health Crisis Lines:\n" +
                        "  USA:         988 (Suicide & Crisis Lifeline)\n" +
                        "  UK:          111 (NHS Mental Health)\n" +
                        "  Canada:      1-833-456-4566\n" +
                        "  Australia:   13 11 14\n" +
                        "  New Zealand: 1737\n" +
                        "  France:      3114\n" +
                        "  Germany:     0800 111 0 111\n\n" +

                        "📱 Crisis Text Lines:\n" +
                        "  USA/Canada: Text HOME to 741741\n" +
                        "  UK:         Text SHOUT to 85258\n" +
                        "  Ireland:    Text HELP to 50808\n\n" +

                        "💚 Additional Support:\n" +
                        "  Befrienders: https://www.befrienders.org\n" +
                        "  IASP:        https://www.iasp.info\n\n" +

                        "⚠️ If in immediate danger, call your local emergency number NOW."
        );
        alert.getDialogPane().setPrefWidth(500);
        alert.showAndWait();
    }
}