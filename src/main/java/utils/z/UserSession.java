package utils;

public final class UserSession {
    private static final UserSession INSTANCE = new UserSession();

    private int userId;
    private String userName;
    private String userRole;
    private boolean loggedIn;

    private UserSession() {}

    public static UserSession getInstance() {
        return INSTANCE;
    }

    public void setUser(int userId, String userName, String userRole) {
        this.userId = userId;
        this.userName = userName;
        this.userRole = userRole;
        this.loggedIn = true;
    }

    public int getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserRole() {
        return userRole;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void clear() {
        userId = 0;
        userName = null;
        userRole = null;
        loggedIn = false;
    }
}
