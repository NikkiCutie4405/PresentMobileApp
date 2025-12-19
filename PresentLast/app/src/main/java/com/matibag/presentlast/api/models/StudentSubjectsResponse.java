package com.matibag.presentlast.api.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class StudentSubjectsResponse {
    private boolean success;
    private String error;
    private List<Subject> subjects;
    private List<String> semesters;
    private List<String> years;

    public boolean isSuccess() { return success; }
    public String getError() { return error; }
    public List<Subject> getSubjects() { return subjects; }
    public List<String> getSemesters() { return semesters; }
    public List<String> getYears() { return years; }

    public static class Subject {
        private int id;
        private String name;
        private String code;
        private int sectionId;
        private String sectionName;
        private int gradeLevelId;
        private String gradeLevelName;
        private int semesterId;
        private String semesterName;
        private int schoolYearId;
        private String schoolYear;
        private String enrolledAt;
        private String instructors;
        private int totalSubmissions;
        private int completedSubmissions;

        public int getId() { return id; }
        public String getName() { return name; }
        public String getCode() { return code; }
        public int getSectionId() { return sectionId; }
        public String getSectionName() { return sectionName; }
        public int getGradeLevelId() { return gradeLevelId; }
        public String getGradeLevelName() { return gradeLevelName; }
        public int getSemesterId() { return semesterId; }
        public String getSemesterName() { return semesterName; }
        public int getSchoolYearId() { return schoolYearId; }
        public String getSchoolYear() { return schoolYear; }
        public String getEnrolledAt() { return enrolledAt; }
        public String getInstructors() { return instructors; }
        public int getTotalSubmissions() { return totalSubmissions; }
        public int getCompletedSubmissions() { return completedSubmissions; }

        /**
         * Calculates the percentage of completed assignments/tasks for this activity_subject.
         * Useful for displaying a ProgressBar in the Course List.
         */
        public int getProgress() {
            if (totalSubmissions == 0) return 0;
            return (int) ((completedSubmissions * 100.0) / totalSubmissions);
        }
    }
}