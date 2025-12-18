package com.matibag.presentlast.api.models;

public class StudentProfileResponse {
    private boolean success;
    private String error;
    private Profile profile;
    private Stats stats;

    public boolean isSuccess() { return success; }
    public String getError() { return error; }
    public Profile getProfile() { return profile; }
    public Stats getStats() { return stats; }

    public static class Profile {
        private int id;
        private String username;
        private String email;
        private String role;
        private String firstName;
        private String middleName;
        private String lastName;
        private String fullName;
        private String studentNumber;
        private String department;
        private String phone;
        private String address;
        private String createdAt;

        public int getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
        public String getFirstName() { return firstName; }
        public String getMiddleName() { return middleName; }
        public String getLastName() { return lastName; }
        public String getFullName() { return fullName; }
        public String getStudentNumber() { return studentNumber; }
        public String getDepartment() { return department; }
        public String getPhone() { return phone; }
        public String getAddress() { return address; }
        public String getCreatedAt() { return createdAt; }
    }

    public static class Stats {
        private int enrolledSubjects;
        private Integer attendanceRate;

        public int getEnrolledSubjects() { return enrolledSubjects; }
        public Integer getAttendanceRate() { return attendanceRate; }
    }
}