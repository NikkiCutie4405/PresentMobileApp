package com.matibag.presentlast.api.models;

import java.util.List;

public class StudentInboxResponse {
    private boolean success;
    private String error;
    private InboxData data;

    public boolean isSuccess() { return success; }
    public String getError() { return error; }
    public InboxData getData() { return data; }

    public static class InboxData {
        private List<TaskItem> tasks;
        private List<GradeItem> grades;
        private List<AttendanceItem> attendance;

        public List<TaskItem> getTasks() { return tasks; }
        public List<GradeItem> getGrades() { return grades; }
        public List<AttendanceItem> getAttendance() { return attendance; }
    }

    public static class TaskItem {
        private int id;
        private String taskName;
        private String description;
        private String dueDate;
        private String dueTime;
        private String createdAt;
        private int subjectId;
        private String subjectName;
        private String subjectCode;
        private String folderName;
        private String category; // "overdue", "today", "upcoming", "recent"
        private String status;   // "pending", "submitted", "graded", "overdue"
        private String instructors;
        private Integer grade;
        private String submittedAt;

        public int getId() { return id; }
        public String getTaskName() { return taskName; }
        public String getDescription() { return description; }
        public String getDueDate() { return dueDate; }
        public String getDueTime() { return dueTime; }
        public String getCreatedAt() { return createdAt; }
        public int getSubjectId() { return subjectId; }
        public String getSubjectName() { return subjectName; }
        public String getSubjectCode() { return subjectCode; }
        public String getFolderName() { return folderName; }
        public String getCategory() { return category; }
        public String getStatus() { return status; }
        public String getInstructors() { return instructors; }
        public Integer getGrade() { return grade; }
        public String getSubmittedAt() { return submittedAt; }

        public boolean isGraded() {
            return "graded".equals(status);
        }

        public boolean isSubmitted() {
            return "submitted".equals(status) || "graded".equals(status);
        }

        public boolean isOverdue() {
            return "overdue".equals(category) || "overdue".equals(status);
        }
    }

    public static class GradeItem {
        private int id;
        private String taskName;
        private int subjectId;
        private String subjectName;
        private String subjectCode;
        private int grade;
        private String feedback;
        private String gradedAt;
        private String instructors;

        public int getId() { return id; }
        public String getTaskName() { return taskName; }
        public int getSubjectId() { return subjectId; }
        public String getSubjectName() { return subjectName; }
        public String getSubjectCode() { return subjectCode; }
        public int getGrade() { return grade; }
        public String getFeedback() { return feedback; }
        public String getGradedAt() { return gradedAt; }
        public String getInstructors() { return instructors; }

        public String getLetterGrade() {
            if (grade >= 90) return "A";
            if (grade >= 80) return "B";
            if (grade >= 70) return "C";
            if (grade >= 60) return "D";
            return "F";
        }
    }

    public static class AttendanceItem {
        private int id;
        private int subjectId;
        private String subjectName;
        private String subjectCode;
        private String sessionDate;
        private String sessionTime;
        private String status; // "present", "late", "absent", "excused"
        private String markedAt;

        public int getId() { return id; }
        public int getSubjectId() { return subjectId; }
        public String getSubjectName() { return subjectName; }
        public String getSubjectCode() { return subjectCode; }
        public String getSessionDate() { return sessionDate; }
        public String getSessionTime() { return sessionTime; }
        public String getStatus() { return status; }
        public String getMarkedAt() { return markedAt; }
    }
}