package com.mentalhealth.app.utils;

public class UserSession {

    private static UserSession instance;

    private int userId;
    private String userName;
    private String userEmail;
    private String userPhone;
    private String userType; // "admin", "psychologist", "patient"

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void login(int userId, String userName, String userEmail, String userPhone, String userType) {
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPhone = userPhone;
        this.userType = userType != null ? userType.toLowerCase() : "";
    }

    public void logout() {
        userId = 0;
        userName = "";
        userEmail = "";
        userPhone = "";
        userType = "";
    }

    // Role checks
    public boolean isAdmin() { return "admin".equals(userType); }
    public boolean isPsychologist() { return "psychologist".equals(userType); }
    public boolean isPatient() { return "patient".equals(userType); }
    public boolean canManageEvents() { return isAdmin() || isPsychologist(); }

    // Getters
    public int getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getUserEmail() { return userEmail; }
    public String getUserPhone() { return userPhone; }
    public String getUserType() { return userType; }
    public boolean isLoggedIn() { return userId > 0; }
}