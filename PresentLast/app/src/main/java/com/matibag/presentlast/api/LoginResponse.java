package com.matibag.presentlast.api;

import com.google.gson.annotations.SerializedName;

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

    /**
     * Check if the logged-in user is a student
     */
    public boolean isStudent() {
        return user != null && user.isStudent();
    }

    /**
     * Only students are allowed to use the mobile app
     */
    public boolean isAllowedRole() {
        return isStudent();
    }

    public static class User {
        private int id;
        private String username;
        private String email;
        private String role;

        @SerializedName("fullName")
        private String fullName;

        @SerializedName("firstName")
        private String firstName;

        @SerializedName("lastName")
        private String lastName;

        public int getId() { return id; }

        public String getUsername() { return username; }

        public String getEmail() { return email; }

        public String getRole() { return role; }

        public String getFullName() {
            // Priority 1: Use fullName if provided by the API
            if (fullName != null && !fullName.trim().isEmpty()) {
                return fullName;
            }

            // Priority 2: Construct from First + Last name
            StringBuilder nameBuilder = new StringBuilder();
            if (firstName != null && !firstName.trim().isEmpty()) {
                nameBuilder.append(firstName.trim());
            }
            if (lastName != null && !lastName.trim().isEmpty()) {
                if (nameBuilder.length() > 0) nameBuilder.append(" ");
                nameBuilder.append(lastName.trim());
            }

            // Priority 3: Fallback to username
            String constructedName = nameBuilder.toString();
            return constructedName.isEmpty() ? username : constructedName;
        }

        public String getFirstName() { return firstName; }

        public String getLastName() { return lastName; }

        public boolean isStudent() {
            return "student".equalsIgnoreCase(role);
        }

        public boolean isTeacher() {
            return "teacher".equalsIgnoreCase(role) || "instructor".equalsIgnoreCase(role);
        }

        public boolean isAdmin() {
            return "admin".equalsIgnoreCase(role);
        }
    }
}