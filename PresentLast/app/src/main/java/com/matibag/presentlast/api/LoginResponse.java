package com.matibag.presentlast.api;

public class LoginResponse {
    private boolean success;
    private String error;
    private User user;

    public boolean isSuccess() {
        return success;
    }

    public String getError() {
        return error;
    }

    public User getUser() {
        return user;
    }

    public static class User {
        private int id;
        private String username;
        private String email;
        private String role;

        public int getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
    }
}