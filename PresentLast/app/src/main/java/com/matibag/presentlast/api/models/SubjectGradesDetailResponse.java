package com.matibag.presentlast.api.models;

import java.util.List;

public class SubjectGradesDetailResponse {
    private boolean success;
    private String error;
    private SubjectInfo subject;
    private List<TaskGrade> tasks;
    private GradeStats stats;

    public boolean isSuccess() { return success; }
    public String getError() { return error; }
    public SubjectInfo getSubject() { return subject; }
    public List<TaskGrade> getTasks() { return tasks; }
    public GradeStats getStats() { return stats; }

    public static class SubjectInfo {
        private int id;
        private String name;
        private String code;
        private String sectionName;
        private String instructors;

        public int getId() { return id; }
        public String getName() { return name; }
        public String getCode() { return code; }
        public String getSectionName() { return sectionName; }
        public String getInstructors() { return instructors; }
    }

    public static class TaskGrade {
        private int id;
        private String name;
        private String description;
        private String dueDate;
        private String folderName;
        private Integer studentSubmissionId;
        private Integer grade;
        private String feedback;
        private String submittedAt;
        private String gradedAt;
        private String status;

        public int getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getDueDate() { return dueDate; }
        public String getFolderName() { return folderName; }
        public Integer getStudentSubmissionId() { return studentSubmissionId; }
        public Integer getGrade() { return grade; }
        public String getFeedback() { return feedback; }
        public String getSubmittedAt() { return submittedAt; }
        public String getGradedAt() { return gradedAt; }
        public String getStatus() { return status; }
    }

    public static class GradeStats {
        private int totalTasks;
        private int submittedCount;
        private int gradedCount;
        private Double averageGrade;
        private Double highestGrade;
        private Double lowestGrade;

        public int getTotalTasks() { return totalTasks; }
        public int getSubmittedCount() { return submittedCount; }
        public int getGradedCount() { return gradedCount; }
        public Double getAverageGrade() { return averageGrade; }
        public Double getHighestGrade() { return highestGrade; }
        public Double getLowestGrade() { return lowestGrade; }
    }
}