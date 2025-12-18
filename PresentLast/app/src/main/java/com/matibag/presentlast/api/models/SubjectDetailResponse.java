package com.matibag.presentlast.api.models;

import java.util.List;

public class SubjectDetailResponse {
    private boolean success;
    private String error;
    private Subject subject;
    private List<Instructor> instructors;
    private List<Folder> folders;

    public boolean isSuccess() { return success; }
    public String getError() { return error; }
    public Subject getSubject() { return subject; }
    public List<Instructor> getInstructors() { return instructors; }
    public List<Folder> getFolders() { return folders; }

    public static class Subject {
        private int id;
        private String name;
        private String code;
        private String sectionName;
        private String gradeLevelName;
        private String semesterName;
        private String schoolYear;

        public int getId() { return id; }
        public String getName() { return name; }
        public String getCode() { return code; }
        public String getSectionName() { return sectionName; }
        public String getGradeLevelName() { return gradeLevelName; }
        public String getSemesterName() { return semesterName; }
        public String getSchoolYear() { return schoolYear; }
    }

    public static class Instructor {
        private int id;
        private String username;
        private String email;
        private String name;

        public int getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getName() { return name; }
    }

    public static class Folder {
        private int id;
        private String name;
        private String createdAt;
        private List<Submission> submissions;

        public int getId() { return id; }
        public String getName() { return name; }
        public String getCreatedAt() { return createdAt; }
        public List<Submission> getSubmissions() { return submissions; }
    }

    public static class Submission {
        private int id;
        private String name;
        private String description;
        private String dueDate;
        private String dueTime;
        private int maxAttempts;
        private boolean isVisible;
        private int attemptCount;
        private Integer studentSubmissionId;
        private Integer grade;
        private String status;

        public int getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getDueDate() { return dueDate; }
        public String getDueTime() { return dueTime; }
        public int getMaxAttempts() { return maxAttempts; }
        public boolean isVisible() { return isVisible; }
        public int getAttemptCount() { return attemptCount; }
        public Integer getStudentSubmissionId() { return studentSubmissionId; }
        public Integer getGrade() { return grade; }
        public String getStatus() { return status; }
    }
}