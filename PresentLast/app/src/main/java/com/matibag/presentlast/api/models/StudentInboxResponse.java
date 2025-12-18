package com.matibag.presentlast.api.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class StudentInboxResponse {
    private boolean success;
    private String error;

    // In your JSON, tasks is an object containing lists
    private TasksContainer tasks;

    // Use @SerializedName to map the JSON key "recentGrades" to this variable
    @SerializedName("recentGrades")
    private List<GradeItem> grades;

    @SerializedName("recentAttendance")
    private List<AttendanceItem> attendance;

    public boolean isSuccess() { return success; }
    public String getError() { return error; }

    // These getters now exist directly on the Response object!
    public List<TaskItem> getTasks() {
        return tasks != null ? tasks.recent : null;
    }
    public List<GradeItem> getGrades() { return grades; }
    public List<AttendanceItem> getAttendance() { return attendance; }

    // Helper class to handle the tasks: { "recent": [...], "today": [...] } structure
    public static class TasksContainer {
        private List<TaskItem> overdue;
        private List<TaskItem> today;
        private List<TaskItem> upcoming;
        private List<TaskItem> recent;

        public List<TaskItem> getRecent() { return recent; }
    }

    public static class TaskItem {
        private int id;
        private String taskName;
        private String dueDate;
        private String createdAt;
        private String subjectName;
        private String subjectCode;
        private String status;
        private Integer grade;

        public int getId() { return id; }
        public String getTaskName() { return taskName; }
        public String getDueDate() { return dueDate; }
        public String getCreatedAt() { return createdAt; }
        public String getSubjectName() { return subjectName; }
        public String getSubjectCode() { return subjectCode; }
        public String getStatus() { return status; }
        public Integer getGrade() { return grade; }

        public boolean isGraded() { return "graded".equals(status); }
        public boolean isSubmitted() { return !"pending".equals(status); }
        public boolean isOverdue() { return "overdue".equals(status); }
    }

    public static class GradeItem {
        private String taskName;
        private int grade;
        public String getTaskName() { return taskName; }
        public int getGrade() { return grade; }
    }

    public static class AttendanceItem {
        private String subjectName;
        private String status;
        private String date; // Logs showed "date", not "sessionDate"
        private String time; // Logs showed "time", not "sessionTime"

        public String getSubjectName() { return subjectName; }
        public String getStatus() { return status; }
        public String getDate() { return date; }
        public String getTime() { return time; }
    }
}