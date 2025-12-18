package com.matibag.presentlast.api.models;

import java.util.List;

public class StudentGradesResponse {
    private boolean success;
    private String error;
    private List<SubjectGrade> subjects;
    private List<String> semesters;
    private List<String> years;

    public boolean isSuccess() { return success; }
    public String getError() { return error; }
    public List<SubjectGrade> getSubjects() { return subjects; }
    public List<String> getSemesters() { return semesters; }
    public List<String> getYears() { return years; }

    public static class SubjectGrade {
        private int id;
        private String name;
        private String code;
        private String sectionName;
        private String gradeLevelName;
        private String semesterName;
        private String schoolYear;
        private String instructors;
        private int totalTasks;
        private int submittedCount;
        private int gradedCount;
        private Double averageGrade;
        private Double totalPoints;
        private Double highestGrade;
        private Double lowestGrade;

        public int getId() { return id; }
        public String getName() { return name; }
        public String getCode() { return code; }
        public String getSectionName() { return sectionName; }
        public String getGradeLevelName() { return gradeLevelName; }
        public String getSemesterName() { return semesterName; }
        public String getSchoolYear() { return schoolYear; }
        public String getInstructors() { return instructors; }
        public int getTotalTasks() { return totalTasks; }
        public int getSubmittedCount() { return submittedCount; }
        public int getGradedCount() { return gradedCount; }
        public Double getAverageGrade() { return averageGrade; }
        public Double getTotalPoints() { return totalPoints; }
        public Double getHighestGrade() { return highestGrade; }
        public Double getLowestGrade() { return lowestGrade; }

        /**
         * Logic to convert numerical average to letter grade.
         * Returns "N/A" if the averageGrade is null.
         */
        public String getLetterGrade() {
            if (averageGrade == null) return "N/A";
            if (averageGrade >= 90) return "A";
            if (averageGrade >= 80) return "B";
            if (averageGrade >= 70) return "C";
            if (averageGrade >= 60) return "D";
            return "F";
        }
    }
}