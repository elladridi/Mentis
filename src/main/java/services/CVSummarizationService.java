package services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.*;

public class CVSummarizationService {

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String API_KEY = System.getenv("OPENAI_API_KEY");
    private final OkHttpClient client;
    private final Gson gson;
    private final boolean useMockMode;

    public CVSummarizationService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();

        this.useMockMode = (API_KEY == null || API_KEY.isEmpty() || API_KEY.equals("your-api-key-here"));
        if (useMockMode) {
            System.out.println("⚠️ OPENAI_API_KEY not set. Using MOCK MODE for CV summarization.");
        }
    }

    /**
     * Extract information from CV that matches the user table schema
     */
    public CVSummary summarizeCV(String cvText) {
        if (useMockMode) {
            return getMockSummary(cvText);
        }

        try {
            String prompt = buildPrompt(cvText);
            String aiResponse = callAI(prompt);

            if (aiResponse == null) {
                System.out.println("❌ AI response is null, falling back to mock data");
                return getMockSummary(cvText);
            }

            return parseAIResponse(aiResponse);
        } catch (Exception e) {
            System.err.println("❌ Error in AI summarization: " + e.getMessage());
            e.printStackTrace();
            return getMockSummary(cvText);
        }
    }

    private String buildPrompt(String cvText) {
        String truncatedText = cvText.length() > 3000 ? cvText.substring(0, 3000) + "..." : cvText;

        return String.format("""
            Extract the person's information from this CV/resume.
            
            NAME EXTRACTION - LOOK FOR THESE PATTERNS (in order of priority):
            1. Large text at the VERY TOP of the document (header)
            2. After "Name:" or "Full Name:" labels
            3. After "Personal Information" or "Personal Details" sections
            4. The first line that looks like a person's name (2-4 words, no numbers, not a section title)
            
            COMMON NAME FORMATS TO RECOGNIZE:
            - "JOHN DOE" (all caps)
            - "John Doe" (title case)
            - "Doe, John" (last name first)
            - "Dr. John Doe" (with title)
            - "Prof. Jane Smith" (with academic title)
            
            COMMON SECTION TITLES TO IGNORE:
            - "PROFILE INFO", "SUMMARY", "EDUCATION", "EXPERIENCE", "SKILLS"
            - "CURRICULUM VITAE", "RESUME", "CV", "BIOGRAPHY"
            - "CONTACT", "PERSONAL INFORMATION", "ABOUT ME"
            
            Required fields:
            - firstname: Person's first name (extract intelligently)
            - lastname: Person's last name (extract intelligently)
            - phone: Phone number (with country code if available)
            - email: Email address
            - dateofbirth: Date of birth in YYYY-MM-DD format (if available)
            
            CV Text:
            %s
            
            Return ONLY valid JSON:
            {
                "firstname": "...",
                "lastname": "...",
                "phone": "...",
                "email": "...", 
                "dateofbirth": "..."
            }
            """, truncatedText);
    }

    private String callAI(String prompt) {
        try {
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", "gpt-3.5-turbo");

            JsonArray messages = new JsonArray();
            JsonObject userMessage = new JsonObject();
            userMessage.addProperty("role", "user");
            userMessage.addProperty("content", prompt);
            messages.add(userMessage);

            requestBody.add("messages", messages);
            requestBody.addProperty("temperature", 0.3);
            requestBody.addProperty("max_tokens", 500);

            Request request = new Request.Builder()
                    .url(API_URL)
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json")
                    .post(RequestBody.create(
                            gson.toJson(requestBody),
                            MediaType.parse("application/json")
                    ))
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    System.err.println("❌ API call failed: " + response.code());
                    return null;
                }

                if (response.body() != null) {
                    return response.body().string();
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Network error calling AI: " + e.getMessage());
        }
        return null;
    }

    private CVSummary parseAIResponse(String aiResponse) {
        try {
            JsonObject response = JsonParser.parseString(aiResponse).getAsJsonObject();

            if (!response.has("choices") || response.getAsJsonArray("choices").size() == 0) {
                return getMockSummary("");
            }

            String content = response.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();

            content = content.replaceAll("```json\\n|```", "").trim();
            return gson.fromJson(content, CVSummary.class);
        } catch (Exception e) {
            System.err.println("❌ Error parsing AI response: " + e.getMessage());
            return getMockSummary("");
        }
    }

    /**
     * Intelligent mock extraction that handles multiple CV formats
     */
    private CVSummary getMockSummary(String cvText) {
        CVSummary mock = new CVSummary();

        // Try multiple name extraction strategies
        String fullName = extractNameMultiStrategy(cvText);

        if (!fullName.isEmpty()) {
            // Handle different name formats
            NameParts nameParts = parseNameFormat(fullName);
            mock.setFirstname(nameParts.firstName);
            mock.setLastname(nameParts.lastName);
        }

        mock.setEmail(extractEmailFromText(cvText));
        mock.setPhone(extractPhoneFromText(cvText));
        mock.setDateofbirth(extractDateOfBirthFromText(cvText));

        return mock;
    }

    /**
     * Try multiple strategies to extract name from different CV formats
     */
    private String extractNameMultiStrategy(String text) {
        String[] lines = text.split("\n");

        // Strategy 1: Look for ALL CAPS at the top (like "ARIJ BOUHLILA")
        for (int i = 0; i < Math.min(10, lines.length); i++) {
            String line = lines[i].trim();
            if (isValidNameLine(line) && line.matches("^[A-Z\\s]+$")) {
                return line;
            }
        }

        // Strategy 2: Look for "Name:" or "Full Name:" labels
        Pattern nameLabelPattern = Pattern.compile(
                "(?i)(?:name|full name|姓名|nombre)[:\\s]+([A-Za-z\\s]+)"
        );
        Matcher matcher = nameLabelPattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        // Strategy 3: Look for name after "Personal Information" section
        Pattern personalInfoPattern = Pattern.compile(
                "(?i)(?:personal information|personal details|about me)[\\s\\n]+([A-Za-z\\s]+)"
        );
        matcher = personalInfoPattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        // Strategy 4: Look for name in first few lines (excluding common headers)
        for (int i = 0; i < Math.min(15, lines.length); i++) {
            String line = lines[i].trim();
            if (isValidNameLine(line)) {
                return line;
            }
        }

        return "";
    }

    /**
     * Check if a line could be a valid name
     */
    private boolean isValidNameLine(String line) {
        if (line.isEmpty() || line.length() > 50) return false;

        // Common section titles to exclude
        String[] excludedTerms = {
                "profile", "summary", "education", "experience", "skills",
                "curriculum", "vitae", "resume", "cv", "contact", "objective",
                "work", "project", "internship", "training", "certification",
                "language", "reference", "hobby", "interest", "achievement"
        };

        String lowerLine = line.toLowerCase();
        for (String term : excludedTerms) {
            if (lowerLine.contains(term)) {
                return false;
            }
        }

        // Should have at least one letter and no excessive numbers
        return line.matches(".*[A-Za-z].*") && !line.matches(".*\\d{4,}.*");
    }

    /**
     * Parse different name formats into first/last name
     */
    private NameParts parseNameFormat(String fullName) {
        NameParts parts = new NameParts();

        // Remove titles
        String name = fullName.replaceAll("(?i)^(dr|prof|mr|mrs|ms|miss)\\s+", "");

        // Handle "Last, First" format
        if (name.contains(",")) {
            String[] split = name.split(",");
            parts.lastName = split[0].trim();
            parts.firstName = split.length > 1 ? split[1].trim() : "";
        }
        // Handle multiple words
        else {
            String[] words = name.split("\\s+");

            if (words.length == 1) {
                parts.firstName = words[0];
                parts.lastName = "";
            } else if (words.length == 2) {
                parts.firstName = words[0];
                parts.lastName = words[1];
            } else {
                // For names with multiple words (e.g., "Jean Pierre Dubois")
                parts.firstName = words[0] + " " + words[1];
                parts.lastName = words[words.length - 1];
            }
        }

        // Clean up (remove punctuation, convert from all caps if needed)
        parts.firstName = cleanNamePart(parts.firstName);
        parts.lastName = cleanNamePart(parts.lastName);

        return parts;
    }

    private String cleanNamePart(String name) {
        if (name.isEmpty()) return "";

        // Remove punctuation
        name = name.replaceAll("[.,;:]", "");

        // Convert from ALL CAPS to Title Case
        if (name.matches("[A-Z\\s]+")) {
            StringBuilder titleCase = new StringBuilder();
            String[] words = name.split("\\s+");
            for (String word : words) {
                if (word.length() > 0) {
                    titleCase.append(word.charAt(0))
                            .append(word.substring(1).toLowerCase())
                            .append(" ");
                }
            }
            name = titleCase.toString().trim();
        }

        return name;
    }

    private String extractEmailFromText(String text) {
        Pattern pattern = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group() : "";
    }

    private String extractPhoneFromText(String text) {
        Pattern pattern = Pattern.compile("\\+?[0-9\\-\\s()]{10,20}");
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group().trim() : "";
    }

    private String extractDateOfBirthFromText(String text) {
        // Multiple date formats
        Pattern[] patterns = {
                Pattern.compile("\\b\\d{4}[-/]\\d{2}[-/]\\d{2}\\b"),
                Pattern.compile("\\b\\d{2}[-/]\\d{2}[-/]\\d{4}\\b"),
                Pattern.compile("(?i)(?:dob|date of birth|born)[:\\s]*(\\d{1,2}[-/]\\d{1,2}[-/]\\d{2,4})")
        };

        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String date = matcher.groupCount() > 0 ? matcher.group(1) : matcher.group();
                return date.replace('/', '-');
            }
        }
        return "";
    }

    /**
     * Helper class for name parsing
     */
    private static class NameParts {
        String firstName = "";
        String lastName = "";
    }

    /**
     * CV Summary data class
     */
    public static class CVSummary {
        private String firstname;
        private String lastname;
        private String phone;
        private String email;
        private String dateofbirth;

        public CVSummary() {
            this.firstname = "";
            this.lastname = "";
            this.phone = "";
            this.email = "";
            this.dateofbirth = "";
        }

        public String getFirstname() { return firstname != null ? firstname : ""; }
        public void setFirstname(String firstname) { this.firstname = firstname; }

        public String getLastname() { return lastname != null ? lastname : ""; }
        public void setLastname(String lastname) { this.lastname = lastname; }

        public String getPhone() { return phone != null ? phone : ""; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getEmail() { return email != null ? email : ""; }
        public void setEmail(String email) { this.email = email; }

        public String getDateofbirth() { return dateofbirth != null ? dateofbirth : ""; }
        public void setDateofbirth(String dateofbirth) { this.dateofbirth = dateofbirth; }
    }
}