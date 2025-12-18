package com.matibag.presentlast.api.models;

public class QRMarkAttendanceRequest {
    private String token;
    private int studentId;

    public QRMarkAttendanceRequest(String token, int studentId) {
        this.token = token;
        this.studentId = studentId;
    }

    public String getToken() { return token; }
    public int getStudentId() { return studentId; }
}