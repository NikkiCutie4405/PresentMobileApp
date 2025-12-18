package com.matibag.presentlast.api.models;

public class QRValidateResponse {
    private boolean success;
    private String error;
    private SessionInfo session;
    private String willBeMarkedAs;
    private String expiresAt;

    public boolean isSuccess() { return success; }
    public String getError() { return error; }
    public SessionInfo getSession() { return session; }
    public String getWillBeMarkedAs() { return willBeMarkedAs; }
    public String getExpiresAt() { return expiresAt; }

    /**
     * Helper to check if the student is scanning past the 'on-time' threshold.
     */
    public boolean willBeLate() {
        return "late".equalsIgnoreCase(willBeMarkedAs);
    }

    public static class SessionInfo {
        private int id;
        private int subjectId;
        private String subjectName;
        private String subjectCode;
        private String sectionName;
        private String gradeLevelName;
        private String date;
        private String time;

        public int getId() { return id; }
        public int getSubjectId() { return subjectId; }
        public String getSubjectName() { return subjectName; }
        public String getSubjectCode() { return subjectCode; }
        public String getSectionName() { return sectionName; }
        public String getGradeLevelName() { return gradeLevelName; }
        public String getDate() { return date; }
        public String getTime() { return time; }
    }
}