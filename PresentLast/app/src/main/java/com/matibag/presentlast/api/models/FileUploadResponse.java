package com.matibag.presentlast.api.models;

public class FileUploadResponse {
    private boolean success;
    private String error;
    private String details;
    private FileInfo file;

    public boolean isSuccess() { return success; }
    public String getError() { return error; }
    public String getDetails() { return details; }
    public FileInfo getFile() { return file; }

    public static class FileInfo {
        private String name;
        private String originalName;
        private String fileName;
        private String type;
        private long size;
        private String url;
        private String path;

        public String getName() { return name; }
        public String getOriginalName() { return originalName; }
        public String getFileName() { return fileName; }
        public String getType() { return type; }
        public long getSize() { return size; }
        public String getUrl() { return url; }
        public String getPath() { return path; }
    }
}