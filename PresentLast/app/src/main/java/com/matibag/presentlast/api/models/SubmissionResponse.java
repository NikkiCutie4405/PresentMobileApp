package com.matibag.presentlast.api.models;

import java.util.List;

public class SubmissionResponse {
    private boolean success;
    private String error;
    private SubmissionData submission;

    public boolean isSuccess() { return success; }
    public String getError() { return error; }
    public SubmissionData getSubmission() { return submission; }

    public static class SubmissionData {
        private int id;
        private int submissionId;
        private int studentId;
        private int attemptNumber;
        private List<FileInfo> files;

        public int getId() { return id; }
        public int getSubmissionId() { return submissionId; }
        public int getStudentId() { return studentId; }
        public int getAttemptNumber() { return attemptNumber; }
        public List<FileInfo> getFiles() { return files; }
    }

    public static class FileInfo {
        private String name;
        private String type;
        private String url;

        public String getName() { return name; }
        public String getType() { return type; }
        public String getUrl() { return url; }
    }
}