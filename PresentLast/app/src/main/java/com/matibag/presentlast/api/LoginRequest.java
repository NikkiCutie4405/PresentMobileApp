package com.matibag.presentlast.api;

/**
 * Request body for activity_login API.
 * The 'username' field can accept either a username or an email address.
 * Note: The mobile app only allows student logins.
 * Teachers and admins should use the web portal.
 **/
public class LoginRequest {
    private String username;  // Acceptable as username OR email
    private String password;

    /**
     * Create a activity_login request
     * @param usernameOrEmail The user's username or email address
     * @param password The user's password
     **/
    public LoginRequest(String usernameOrEmail, String password) {
        this.username = usernameOrEmail;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}