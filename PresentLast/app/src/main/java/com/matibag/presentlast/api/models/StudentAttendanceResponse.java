package com.matibag.presentlast.api.models;

import java.util.List;

public class StudentAttendanceResponse {
    private boolean success;
    private String error;
    private List<SubjectAttendance> subjects;
    private List<String> semesters;
    private List<String> years;

    public boolean isSuccess() { return success; }
    public String getError() { return error; }
    public List<SubjectAttendance> getSubjects() { return subjects; }
    public List<String> getSemesters() { return semesters; }
    public List<String> getYears() { return years; }

    public static class SubjectAttendance {
        private int id;
        private String name;
        private String code;
        private int totalSessions;
        private int presentCount;
        private int lateCount;
        private int absentCount;
        private int excusedCount;

        public int getId() { return id; }
        public String getName() { return name; }
        public String getCode() { return code; }
        public int getTotalSessions() { return totalSessions; }
        public int getPresentCount() { return presentCount; }
        public int getLateCount() { return lateCount; }
        public int getAbsentCount() { return absentCount; }
        public int getExcusedCount() { return excusedCount; }

        /**
         * Calculates attendance rate where Present and Late both count toward participation.
         */
        public int getAttendanceRate() {
            if (totalSessions == 0) return 0;
            return (int) (((presentCount + lateCount) * 100.0) / totalSessions);
        }
    }
}