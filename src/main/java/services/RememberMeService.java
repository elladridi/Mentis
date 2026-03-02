package services;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.prefs.Preferences;

public class RememberMeService {

    private static final String TOKEN_FILE = "mentis_token.dat";
    private static final String PREFS_KEY = "mentis_remember_token";
    private static final String SEPARATOR = "|";

    // Use Windows Registry (more secure)
    private Preferences prefs;

    // Simple encryption (for demo - use stronger encryption in production)
    private static final String SECRET_KEY = "MentisSecretKey2026";

    public RememberMeService() {
        // Windows Registry storage
        prefs = Preferences.userRoot().node("com/mentis/auth");
    }

    /**
     * Save user credentials securely with "Remember Me"
     */
    public void saveRememberMeToken(int userId, String email, String userType) {
        try {
            // Create token: userId|email|userType|expiry
            long expiryTime = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000); // 30 days
            String tokenData = userId + SEPARATOR + email + SEPARATOR + userType + SEPARATOR + expiryTime;

            // Encrypt the token
            String encryptedToken = encrypt(tokenData);

            // Save to Windows Registry
            prefs.put(PREFS_KEY, encryptedToken);

            // Also save to file as backup
            saveTokenToFile(encryptedToken);

            System.out.println("✅ Remember Me token saved for user: " + email);

        } catch (Exception e) {
            System.err.println("❌ Error saving remember me token: " + e.getMessage());
        }
    }

    /**
     * Retrieve and validate saved token
     */
    public RememberMeToken getRememberedUser() {
        try {
            // Try to get from Registry first
            String encryptedToken = prefs.get(PREFS_KEY, null);

            // If not in Registry, try file
            if (encryptedToken == null || encryptedToken.isEmpty()) {
                encryptedToken = readTokenFromFile();
            }

            if (encryptedToken == null || encryptedToken.isEmpty()) {
                return null;
            }

            // Decrypt token
            String tokenData = decrypt(encryptedToken);
            String[] parts = tokenData.split("\\" + SEPARATOR);

            if (parts.length == 4) {
                int userId = Integer.parseInt(parts[0]);
                String email = parts[1];
                String userType = parts[2];
                long expiryTime = Long.parseLong(parts[3]);

                // Check if token expired
                if (System.currentTimeMillis() > expiryTime) {
                    clearRememberMe();
                    return null;
                }

                return new RememberMeToken(userId, email, userType, expiryTime);
            }

        } catch (Exception e) {
            System.err.println("❌ Error reading remember me token: " + e.getMessage());
            clearRememberMe(); // Clear corrupted token
        }
        return null;
    }

    /**
     * Clear saved token (logout)
     */
    public void clearRememberMe() {
        try {
            prefs.remove(PREFS_KEY);
            deleteTokenFile();
            System.out.println("✅ Remember Me token cleared");
        } catch (Exception e) {
            System.err.println("❌ Error clearing remember me token: " + e.getMessage());
        }
    }

    /**
     * Check if user is remembered
     */
    public boolean isRemembered() {
        return getRememberedUser() != null;
    }

    // ================== ENCRYPTION METHODS ==================

    private String encrypt(String data) throws Exception {
        // Simple XOR encryption (for demo only - use AES in production)
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[dataBytes.length];

        for (int i = 0; i < dataBytes.length; i++) {
            result[i] = (byte) (dataBytes[i] ^ keyBytes[i % keyBytes.length]);
        }

        return Base64.getEncoder().encodeToString(result);
    }

    private String decrypt(String encryptedData) throws Exception {
        byte[] dataBytes = Base64.getDecoder().decode(encryptedData);
        byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[dataBytes.length];

        for (int i = 0; i < dataBytes.length; i++) {
            result[i] = (byte) (dataBytes[i] ^ keyBytes[i % keyBytes.length]);
        }

        return new String(result, StandardCharsets.UTF_8);
    }

    // ================== FILE STORAGE (Backup) ==================

    private void saveTokenToFile(String encryptedToken) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(TOKEN_FILE))) {
            oos.writeObject(encryptedToken);
        } catch (IOException e) {
            System.err.println("⚠️ Could not save token to file: " + e.getMessage());
        }
    }

    private String readTokenFromFile() {
        File file = new File(TOKEN_FILE);
        if (!file.exists()) return null;

        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(file))) {
            return (String) ois.readObject();
        } catch (Exception e) {
            return null;
        }
    }

    private void deleteTokenFile() {
        File file = new File(TOKEN_FILE);
        if (file.exists()) {
            file.delete();
        }
    }

    // ================== TOKEN CLASS ==================

    public static class RememberMeToken {
        private int userId;
        private String email;
        private String userType;
        private long expiryTime;

        public RememberMeToken(int userId, String email, String userType, long expiryTime) {
            this.userId = userId;
            this.email = email;
            this.userType = userType;
            this.expiryTime = expiryTime;
        }

        public int getUserId() { return userId; }
        public String getEmail() { return email; }
        public String getUserType() { return userType; }
        public long getExpiryTime() { return expiryTime; }
        public boolean isExpired() { return System.currentTimeMillis() > expiryTime; }
    }
}