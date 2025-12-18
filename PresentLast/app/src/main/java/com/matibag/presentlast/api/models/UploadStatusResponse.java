package com.matibag.presentlast.api.models;

import java.util.List;

public class UploadStatusResponse {
    private String status;
    private boolean r2Available;
    private List<String> allowedTypes;
    private int maxSizeMB;
    private String usage;
    private String error;

    public String getStatus() { return status; }
    public boolean isR2Available() { return r2Available; }
    public List<String> getAllowedTypes() { return allowedTypes; }
    public int getMaxSizeMB() { return maxSizeMB; }
    public String getUsage() { return usage; }
    public String getError() { return error; }

    /**
     * Checks if the upload service and the underlying storage (R2) are both functional.
     */
    public boolean isActive() {
        return "active".equals(status) && r2Available;
    }
}