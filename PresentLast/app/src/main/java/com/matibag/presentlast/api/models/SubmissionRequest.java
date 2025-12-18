package com.matibag.presentlast.api.models;

import java.util.List;

public class SubmissionRequest {
    private int submissionId;
    private int studentId;
    private List<FileInfo> files;

    public SubmissionRequest(int submissionId, int studentId, List<FileInfo> files) {
        this.submissionId = submissionId;
        this.studentId = studentId;
        this.files = files;
    }

    public int getSubmissionId() { return submissionId; }
    public int getStudentId() { return studentId; }
    public List<FileInfo> getFiles() { return files; }

    public static class FileInfo {
        private String name;
        private String type;
        private String url;

        public FileInfo(String name, String type, String url) {
            this.name = name;
            this.type = type;
            this.url = url;
        }

        public String getName() { return name; }
        public String getType() { return type; }
        public String getUrl() { return url; }
    }
}