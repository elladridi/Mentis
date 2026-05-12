package models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

public class user {

    // Basic fields matching Symfony entity
    private int id;
    private String firstname;
    private String lastname;
    private String phone;
    private LocalDate dateofbirth;
    private String type;
    private String email;
    private String password;

    // Face recognition fields
    private String faceData;
    private boolean faceEnabled;
    private LocalDateTime faceRegisteredAt;

    // Additional fields from Symfony
    private LocalDateTime createdAt;
    private String gender;

    // Ban fields
    private boolean isBanned;
    private LocalDateTime bannedAt;
    private LocalDateTime bannedUntil;
    private String banReason;

    // Gson instance for JSON handling
    private static final Gson gson = new Gson();

    // Constructors
    public user() {
        this.id = 0;
        this.createdAt = LocalDateTime.now();
        this.faceEnabled = false;
        this.type = "Patient";
        this.isBanned = false;
    }

    // Constructor for new user (without ID)
    public user(String firstname, String lastname, String phone, LocalDate dateofbirth,
                String type, String email, String password) {
        this();
        this.firstname = firstname;
        this.lastname = lastname;
        this.phone = phone;
        this.dateofbirth = dateofbirth;
        this.type = type;
        this.email = email;
        this.password = password;
    }

    // Constructor for existing user (with ID from database)
    public user(int id, String firstname, String lastname, String phone, LocalDate dateofbirth,
                String type, String email, String password) {
        this(firstname, lastname, phone, dateofbirth, type, email, password);
        this.id = id;
    }

    // ==================== BASIC GETTERS & SETTERS ====================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getDateofbirth() {
        return dateofbirth;
    }

    public void setDateofbirth(LocalDate dateofbirth) {
        this.dateofbirth = dateofbirth;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // ==================== FACE RECOGNITION GETTERS & SETTERS ====================

    public String getFaceData() {
        return faceData;
    }

    public void setFaceData(String faceData) {
        this.faceData = faceData;
    }

    public boolean isFaceEnabled() {
        return faceEnabled;
    }

    public void setFaceEnabled(boolean faceEnabled) {
        this.faceEnabled = faceEnabled;
    }

    public LocalDateTime getFaceRegisteredAt() {
        return faceRegisteredAt;
    }

    public void setFaceRegisteredAt(LocalDateTime faceRegisteredAt) {
        this.faceRegisteredAt = faceRegisteredAt;
    }

    /**
     * Get face samples as list (converts JSON to list)
     */
    public List<String> getFaceSamples() {
        if (faceData == null || faceData.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            // Check if it's JSON (multiple samples)
            Type listType = new TypeToken<ArrayList<String>>(){}.getType();
            List<String> samples = gson.fromJson(faceData, listType);
            if (samples != null && !samples.isEmpty()) {
                return samples;
            }
        } catch (Exception e) {
            // Old format: single sample as string
            return List.of(faceData);
        }

        return new ArrayList<>();
    }

    /**
     * Store multiple face samples as JSON
     */
    public void setFaceSamples(List<String> samples) {
        if (samples == null || samples.isEmpty()) {
            this.faceData = null;
            this.faceEnabled = false;
            this.faceRegisteredAt = null;
        } else {
            this.faceData = gson.toJson(samples);
        }
    }

    /**
     * Add a single face sample to the collection
     */
    public void addFaceSample(String sample) {
        List<String> samples = getFaceSamples();
        samples.add(sample);
        setFaceSamples(samples);
    }

    /**
     * Remove a specific face sample by index
     */
    public void removeFaceSample(int index) {
        List<String> samples = getFaceSamples();
        if (index >= 0 && index < samples.size()) {
            samples.remove(index);
            setFaceSamples(samples);
        }
    }

    /**
     * Get the count of face samples
     */
    public int getFaceSamplesCount() {
        return getFaceSamples().size();
    }

    /**
     * Check if user has enough face samples (minimum 3)
     */
    public boolean hasEnoughFaceSamples(int required) {
        return getFaceSamplesCount() >= required;
    }

    public boolean hasEnoughFaceSamples() {
        return hasEnoughFaceSamples(3);
    }

    /**
     * Get the first face sample path (for display purposes)
     */
    public String getFirstFaceSamplePath() {
        List<String> samples = getFaceSamples();
        if (samples.isEmpty()) {
            return null;
        }
        return samples.get(0);
    }

    /**
     * Get the last face sample (most recently added)
     */
    public String getLastFaceSample() {
        List<String> samples = getFaceSamples();
        if (samples.isEmpty()) {
            return null;
        }
        return samples.get(samples.size() - 1);
    }

    /**
     * Enable face recognition and set registration timestamp
     */
    public void enableFaceRecognition() {
        this.faceEnabled = true;
        if (this.faceRegisteredAt == null) {
            this.faceRegisteredAt = LocalDateTime.now();
        }
    }

    /**
     * Disable face recognition and clear registration timestamp
     */
    public void disableFaceRecognition() {
        this.faceEnabled = false;
        this.faceRegisteredAt = null;
    }

    // ==================== BAN GETTERS & SETTERS ====================

    public boolean isBanned() {
        // Check if ban has expired
        if (isBanned && bannedUntil != null && bannedUntil.isBefore(LocalDateTime.now())) {
            isBanned = false;
            bannedUntil = null;
        }
        return isBanned;
    }

    public void setIsBanned(boolean isBanned) {
        this.isBanned = isBanned;
    }

    public LocalDateTime getBannedAt() {
        return bannedAt;
    }

    public void setBannedAt(LocalDateTime bannedAt) {
        this.bannedAt = bannedAt;
    }

    public LocalDateTime getBannedUntil() {
        return bannedUntil;
    }

    public void setBannedUntil(LocalDateTime bannedUntil) {
        this.bannedUntil = bannedUntil;
    }

    public String getBanReason() {
        return banReason;
    }

    public void setBanReason(String banReason) {
        this.banReason = banReason;
    }

    // ==================== ADDITIONAL GETTERS & SETTERS ====================

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    // ==================== HELPER METHODS ====================

    /**
     * Calculate user's age from date of birth
     */
    public Integer getAge() {
        if (dateofbirth == null) {
            return null;
        }
        return Period.between(dateofbirth, LocalDate.now()).getYears();
    }

    /**
     * Get formatted date of birth for display
     */
    public String getDateofbirthFormatted() {
        if (dateofbirth == null) {
            return "";
        }
        return dateofbirth.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * Get user's full name
     */
    public String getFullName() {
        return (firstname != null ? firstname : "") + " " + (lastname != null ? lastname : "");
    }

    /**
     * Check if user is an admin
     */
    public boolean isAdmin() {
        return type != null && type.equalsIgnoreCase("admin");
    }

    /**
     * Check if user is a psychologist
     */
    public boolean isPsychologist() {
        return type != null && type.equalsIgnoreCase("psychologist");
    }

    /**
     * Check if user is a patient
     */
    public boolean isPatient() {
        return type != null && type.equalsIgnoreCase("patient");
    }

    /**
     * Get roles for Spring Security (if using)
     */
    public List<String> getRoles() {
        List<String> roles = new ArrayList<>();
        roles.add("ROLE_USER");

        if (isAdmin()) {
            roles.add("ROLE_ADMIN");
            roles.add("ROLE_PSYCHOLOGIST");
        } else if (isPsychologist()) {
            roles.add("ROLE_PSYCHOLOGIST");
        }

        return roles;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", phone='" + phone + '\'' +
                ", dateofbirth=" + dateofbirth +
                ", type='" + type + '\'' +
                ", email='" + email + '\'' +
                ", faceEnabled=" + faceEnabled +
                ", isBanned=" + isBanned +
                ", createdAt=" + createdAt +
                ", gender='" + gender + '\'' +
                '}';
    }
}