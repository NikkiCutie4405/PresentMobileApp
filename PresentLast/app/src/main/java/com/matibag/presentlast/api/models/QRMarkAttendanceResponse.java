package com.matibag.presentlast.api.models;

public class QRMarkAttendanceResponse {
    private boolean success;
    private String error;
    private String message;
    private String status;
    private int sessionId;
    private boolean alreadyMarked;

    public boolean isSuccess() { return success; }
    public String getError() { return error; }
    public String getMessage() { return message; }
    public String getStatus() { return status; }
    public int getSessionId() { return sessionId; }
    public boolean isAlreadyMarked() { return alreadyMarked; }

    public boolean isPresent() {
        return "present".equalsIgnoreCase(status);
    }

    public boolean isLate() {
        return "late".equalsIgnoreCase(status);
    }
}