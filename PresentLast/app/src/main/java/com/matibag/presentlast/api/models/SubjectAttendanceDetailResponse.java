package com.matibag.presentlast.api.models;

import java.util.List;

public class SubjectAttendanceDetailResponse {
    private boolean success;
    private String error;
    private SubjectInfo subject;
    private List<AttendanceSession> sessions;
    private AttendanceStats stats;

    public boolean isSuccess() { return success; }
    public String getError() { return error; }
    public SubjectInfo getSubject() { return subject; }
    public List<AttendanceSession> getSessions() { return sessions; }
    public AttendanceStats getStats() { return stats; }

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

    public static class AttendanceSession {
        private int id;
        private String date;
        private String time;
        private String status; // "present", "late", "absent", "excused"
        private String markedAt;

        public int getId() { return id; }
        public String getDate() { return date; }
        public String getTime() { return time; }
        public String getStatus() { return status; }
        public String getMarkedAt() { return markedAt; }
    }

    public static class AttendanceStats {
        private int totalSessions;
        private int presentCount;
        private int lateCount;
        private int absentCount;
        private int excusedCount;
        private int attendanceRate;

        public int getTotalSessions() { return totalSessions; }
        public int getPresentCount() { return presentCount; }
        public int getLateCount() { return lateCount; }
        public int getAbsentCount() { return absentCount; }
        public int getExcusedCount() { return excusedCount; }
        public int getAttendanceRate() { return attendanceRate; }
    }
}