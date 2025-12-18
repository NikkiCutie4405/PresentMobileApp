package com.matibag.presentlast.api.models;

import java.util.List;

public class StudentSubmissionResponse {
    private boolean success;
    private String error;
    private SubmissionDetail submission;

    public boolean isSuccess() { return success; }
    public String getError() { return error; }
    public SubmissionDetail getSubmission() { return submission; }

    public static class SubmissionDetail {
        private int id;
        private int submissionId;
        private int studentId;
        private int attemptNumber;
        private String submittedAt;
        private Integer grade;
        private String feedback;
        private String gradedAt;
        private List<FileInfo> files;

        public int getId() { return id; }
        public int getSubmissionId() { return submissionId; }
        public int getStudentId() { return studentId; }
        public int getAttemptNumber() { return attemptNumber; }
        public String getSubmittedAt() { return submittedAt; }
        public Integer getGrade() { return grade; }
        public String getFeedback() { return feedback; }
        public String getGradedAt() { return gradedAt; }
        public List<FileInfo> getFiles() { return files; }

        /**
         * Convenience method to check if the teacher has released a grade yet.
         */
        public boolean isGraded() {
            return grade != null;
        }
    }

    public static class FileInfo {
        private int id;
        private String fileName;
        private String fileType;
        private String fileUrl;

        public int getId() { return id; }
        public String getFileName() { return fileName; }
        public String getFileType() { return fileType; }
        public String getFileUrl() { return fileUrl; }
    }
}