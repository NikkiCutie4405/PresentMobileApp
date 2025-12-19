package com.matibag.presentlast.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.matibag.presentlast.R;
import com.matibag.presentlast.api.ApiClient;
import com.matibag.presentlast.api.AuthManager;
import com.matibag.presentlast.api.FileUploadHelper;
import com.matibag.presentlast.api.models.FileUploadResponse;
import com.matibag.presentlast.api.models.StudentSubmissionResponse;
import com.matibag.presentlast.api.models.SubmissionRequest;
import com.matibag.presentlast.api.models.SubmissionResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubmissionPageActivity extends AppCompatActivity {

    private static final String TAG = "SubmissionPageActivity";

    // Views
    private TextView btnBack, txtSubmissionTitle, txtSubjectInfo, txtFolderName;
    private TextView txtDueDate, txtLateStatus, txtSubmitStatus, txtAttempts, txtGrade;
    private TextView txtInstructions, txtFeedback, lblTeacherFiles, lblYourSubmission;
    private TextView lblPreviousSubmission;
    private LinearLayout feedbackSection, uploadContainer, attachedFilesContainer;
    private LinearLayout teacherFilesContainer, previousFilesContainer;
    private ProgressBar progressBar;
    private Button btnSubmit;

    // Data from Intent
    private AuthManager authManager;
    private int taskId = -1;
    private String taskTitle;
    private String taskDescription;
    private String dueDate;
    private String dueTime;
    private int maxAttempts = 1;
    private String subjectName;
    private String subjectCode;
    private String folderName;

    // Data from API
    private StudentSubmissionResponse.SubmissionDetail existingSubmission;
    private int currentAttempts = 0;
    private boolean canSubmit = true;
    private boolean isLate = false;

    // File handling
    private ArrayList<Uri> attachedFiles = new ArrayList<>();
    private ArrayList<FileUploadResponse.FileInfo> uploadedFiles = new ArrayList<>();

    // File picker
    private final ActivityResultLauncher<String[]> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenMultipleDocuments(),
            uris -> {
                if (uris != null && !uris.isEmpty()) {
                    for (Uri uri : uris) {
                        try {
                            getContentResolver().takePersistableUriPermission(uri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (SecurityException e) {
                            // Ignore if permission already taken
                        }

                        if (!attachedFiles.contains(uri)) {
                            attachedFiles.add(uri);
                            addAttachedFileCard(getFileName(uri), uri);
                        }
                    }
                    updateSubmitButton();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submission);

        authManager = AuthManager.getInstance(this);

        if (!authManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        extractIntentData();

        if (taskId == -1) {
            showError("Invalid task");
            finish();
            return;
        }

        initViews();
        setupClickListeners();
        displayTaskDetails();
        loadExistingSubmission();
    }

    private void extractIntentData() {
        Intent intent = getIntent();
        taskId = intent.getIntExtra("taskId", -1);
        taskTitle = intent.getStringExtra("taskTitle");
        taskDescription = intent.getStringExtra("taskDescription");
        dueDate = intent.getStringExtra("dueDate");
        dueTime = intent.getStringExtra("dueTime");
        maxAttempts = intent.getIntExtra("maxAttempts", 1);
        subjectName = intent.getStringExtra("subjectName");
        subjectCode = intent.getStringExtra("subjectCode");
        folderName = intent.getStringExtra("folderName");

        if (taskTitle == null) {
            taskTitle = intent.getStringExtra("taskName");
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        txtSubmissionTitle = findViewById(R.id.txtSubmissionTitle);
        txtSubjectInfo = findViewById(R.id.txtSubjectInfo);
        txtFolderName = findViewById(R.id.txtFolderName);
        txtDueDate = findViewById(R.id.txtDueDate);
        txtLateStatus = findViewById(R.id.txtLateStatus);
        txtSubmitStatus = findViewById(R.id.txtSubmitStatus);
        txtAttempts = findViewById(R.id.txtAttempts);
        txtGrade = findViewById(R.id.txtGrade);
        txtInstructions = findViewById(R.id.txtInstructions);
        txtFeedback = findViewById(R.id.txtFeedback);
        feedbackSection = findViewById(R.id.feedbackSection);
        uploadContainer = findViewById(R.id.uploadContainer);
        attachedFilesContainer = findViewById(R.id.attachedFilesContainer);
        teacherFilesContainer = findViewById(R.id.teacherFilesContainer);
        previousFilesContainer = findViewById(R.id.previousFilesContainer);
        lblTeacherFiles = findViewById(R.id.lblTeacherFiles);
        lblYourSubmission = findViewById(R.id.lblYourSubmission);
        lblPreviousSubmission = findViewById(R.id.lblPreviousSubmission);
        progressBar = findViewById(R.id.progressBar);
        btnSubmit = findViewById(R.id.btnSubmit);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        uploadContainer.setOnClickListener(v -> {
            if (canSubmit) {
                openFilePicker();
            } else {
                showError("Cannot submit - maximum attempts reached");
            }
        });

        btnSubmit.setOnClickListener(v -> handleSubmit());
    }

    private void displayTaskDetails() {
        if (taskTitle != null && !taskTitle.isEmpty()) {
            txtSubmissionTitle.setText(taskTitle);
        } else {
            txtSubmissionTitle.setText("Task #" + taskId);
        }

        if (subjectCode != null || subjectName != null) {
            String info = "";
            if (subjectCode != null) info += subjectCode;
            if (subjectName != null) info += (info.isEmpty() ? "" : " - ") + subjectName;
            txtSubjectInfo.setText(info);
            txtSubjectInfo.setVisibility(View.VISIBLE);
        } else {
            txtSubjectInfo.setVisibility(View.GONE);
        }

        if (folderName != null && !folderName.isEmpty()) {
            txtFolderName.setText("📁 " + folderName);
            txtFolderName.setVisibility(View.VISIBLE);
        } else {
            txtFolderName.setVisibility(View.GONE);
        }

        txtDueDate.setText(formatDueDate(dueDate, dueTime));
        checkIfLate();

        if (taskDescription != null && !taskDescription.isEmpty()) {
            txtInstructions.setText(taskDescription);
        } else {
            txtInstructions.setText("No instructions provided.");
        }

        txtAttempts.setText("0 / " + maxAttempts);
        txtSubmitStatus.setText("Not Submitted");
        txtSubmitStatus.setTextColor(0xFFFBBF24);
        txtGrade.setText("Not Graded");
        txtGrade.setTextColor(0xFF94A3B8);

        updateSubmitButton();
    }

    private void loadExistingSubmission() {
        int studentId = authManager.getCurrentUserId();
        if (studentId == -1) {
            navigateToLogin();
            return;
        }

        setLoading(true);

        ApiClient.getApiService().getStudentSubmission(taskId, studentId)
                .enqueue(new Callback<StudentSubmissionResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<StudentSubmissionResponse> call,
                                           @NonNull Response<StudentSubmissionResponse> response) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            if (response.isSuccessful() && response.body() != null) {
                                StudentSubmissionResponse res = response.body();
                                if (res.isSuccess() && res.getSubmission() != null) {
                                    existingSubmission = res.getSubmission();
                                    displayExistingSubmission();
                                }
                            }
                            updateSubmitButton();
                        });
                    }

                    @Override
                    public void onFailure(@NonNull Call<StudentSubmissionResponse> call, @NonNull Throwable t) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            updateSubmitButton();
                        });
                    }
                });
    }

    private void displayExistingSubmission() {
        if (existingSubmission == null) return;

        currentAttempts = existingSubmission.getAttemptNumber();
        txtAttempts.setText(currentAttempts + " / " + maxAttempts);

        if (existingSubmission.isGraded()) {
            txtSubmitStatus.setText("Graded");
            txtSubmitStatus.setTextColor(0xFF10B981);
        } else {
            txtSubmitStatus.setText("Submitted");
            txtSubmitStatus.setTextColor(0xFF3B82F6);
        }

        if (existingSubmission.getGrade() != null) {
            int grade = existingSubmission.getGrade();
            txtGrade.setText(grade + " / 100");
            txtGrade.setTextColor(getGradeColor(grade));
        }

        if (existingSubmission.getFeedback() != null && !existingSubmission.getFeedback().isEmpty()) {
            txtFeedback.setText(existingSubmission.getFeedback());
            feedbackSection.setVisibility(View.VISIBLE);
        }

        List<StudentSubmissionResponse.FileInfo> files = existingSubmission.getFiles();
        if (files != null && !files.isEmpty()) {
            displayPreviousFiles(files);
        }

        canSubmit = currentAttempts < maxAttempts;
        updateSubmitButton();
    }

    private void displayPreviousFiles(List<StudentSubmissionResponse.FileInfo> files) {
        lblPreviousSubmission.setVisibility(View.VISIBLE);
        previousFilesContainer.setVisibility(View.VISIBLE);
        previousFilesContainer.removeAllViews();

        for (StudentSubmissionResponse.FileInfo file : files) {
            addFileDisplayCard(previousFilesContainer, file.getFileName(), file.getFileUrl());
        }
    }

    private void openFilePicker() {
        String[] mimeTypes = {
                "application/pdf", "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "image/*", "video/*", "text/plain"
        };
        filePickerLauncher.launch(mimeTypes);
    }

    private String getFileName(Uri uri) {
        String fileName = "Unknown file";
        if ("content".equals(uri.getScheme())) {
            try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        fileName = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if ("file".equals(uri.getScheme()) && uri.getPath() != null) {
            fileName = new java.io.File(uri.getPath()).getName();
        }
        return fileName;
    }

    private void addAttachedFileCard(String fileName, Uri fileUri) {
        LinearLayout fileCard = new LinearLayout(this);
        fileCard.setOrientation(LinearLayout.HORIZONTAL);
        fileCard.setBackgroundColor(0xFF334155);
        int padding = dpToPx(12);
        fileCard.setPadding(padding, padding, padding, padding);
        fileCard.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dpToPx(8));
        fileCard.setLayoutParams(cardParams);

        TextView fileIcon = new TextView(this);
        fileIcon.setText(getFileIcon(fileName));
        fileIcon.setTextSize(24);
        fileCard.addView(fileIcon);

        TextView txtFileName = new TextView(this);
        txtFileName.setText(fileName);
        txtFileName.setTextColor(Color.WHITE);
        txtFileName.setTextSize(14);
        txtFileName.setMaxLines(1);
        txtFileName.setEllipsize(android.text.TextUtils.TruncateAt.MIDDLE);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        nameParams.setMargins(dpToPx(12), 0, dpToPx(12), 0);
        txtFileName.setLayoutParams(nameParams);
        fileCard.addView(txtFileName);

        TextView btnRemove = new TextView(this);
        btnRemove.setText("✕");
        btnRemove.setTextColor(0xFFEF4444);
        btnRemove.setTextSize(20);
        btnRemove.setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4));
        btnRemove.setOnClickListener(v -> {
            attachedFilesContainer.removeView(fileCard);
            attachedFiles.remove(fileUri);
            updateSubmitButton();
        });
        fileCard.addView(btnRemove);

        attachedFilesContainer.addView(fileCard);
    }

    private void addFileDisplayCard(LinearLayout container, String fileName, String fileUrl) {
        LinearLayout fileCard = new LinearLayout(this);
        fileCard.setOrientation(LinearLayout.HORIZONTAL);
        fileCard.setBackgroundColor(0xFF1E293B);
        int padding = dpToPx(12);
        fileCard.setPadding(padding, padding, padding, padding);
        fileCard.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dpToPx(8));
        fileCard.setLayoutParams(cardParams);

        TextView fileIcon = new TextView(this);
        fileIcon.setText(getFileIcon(fileName));
        fileIcon.setTextSize(24);
        fileCard.addView(fileIcon);

        TextView txtFileName = new TextView(this);
        txtFileName.setText(fileName != null ? fileName : "File");
        txtFileName.setTextColor(Color.WHITE);
        txtFileName.setTextSize(14);
        txtFileName.setMaxLines(1);
        txtFileName.setEllipsize(android.text.TextUtils.TruncateAt.MIDDLE);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        nameParams.setMargins(dpToPx(12), 0, dpToPx(12), 0);
        txtFileName.setLayoutParams(nameParams);
        fileCard.addView(txtFileName);

        if (fileUrl != null && !fileUrl.isEmpty()) {
            TextView btnOpen = new TextView(this);
            btnOpen.setText("📥");
            btnOpen.setTextSize(24);
            btnOpen.setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4));
            btnOpen.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl));
                    startActivity(intent);
                } catch (Exception e) {
                    showError("Cannot open file");
                }
            });
            fileCard.addView(btnOpen);
        }

        container.addView(fileCard);
    }

    private String getFileIcon(String fileName) {
        if (fileName == null) return "📄";
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".pdf")) return "📕";
        if (lower.endsWith(".doc") || lower.endsWith(".docx")) return "📘";
        if (lower.endsWith(".ppt") || lower.endsWith(".pptx")) return "📙";
        if (lower.endsWith(".xls") || lower.endsWith(".xlsx")) return "📗";
        if (lower.matches(".*\\.(jpg|jpeg|png|gif)$")) return "🖼️";
        if (lower.matches(".*\\.(mp4|mov|avi)$")) return "🎬";
        if (lower.matches(".*\\.(mp3|wav)$")) return "🎵";
        if (lower.matches(".*\\.(zip|rar)$")) return "📦";
        return "📄";
    }

    private void handleSubmit() {
        if (attachedFiles.isEmpty()) {
            showError("Please attach at least one file");
            return;
        }

        if (!canSubmit) {
            showError("Cannot submit - maximum attempts reached");
            return;
        }

        String message = "Are you sure you want to submit this assignment?";
        if (isLate) message += "\n\n⚠️ Warning: This submission will be marked as LATE.";
        if (currentAttempts > 0) message += "\n\nAttempt " + (currentAttempts + 1) + " of " + maxAttempts + ".";

        new AlertDialog.Builder(this)
                .setTitle("Submit Assignment")
                .setMessage(message)
                .setPositiveButton("Submit", (dialog, which) -> uploadAndSubmit())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void uploadAndSubmit() {
        setLoading(true);
        btnSubmit.setEnabled(false);
        btnSubmit.setText("Uploading...");
        uploadContainer.setEnabled(false);

        uploadedFiles.clear();
        uploadNextFile(0);
    }

    private void uploadNextFile(int index) {
        if (index >= attachedFiles.size()) {
            submitToApi();
            return;
        }

        Uri fileUri = attachedFiles.get(index);
        int studentId = authManager.getCurrentUserId();

        runOnUiThread(() -> btnSubmit.setText("Uploading " + (index + 1) + "/" + attachedFiles.size() + "..."));

        FileUploadHelper.uploadFile(this, fileUri, studentId, taskId,
                new FileUploadHelper.UploadCallback() {
                    @Override
                    public void onSuccess(FileUploadResponse.FileInfo fileInfo) {
                        uploadedFiles.add(fileInfo);
                        uploadNextFile(index + 1);
                    }

                    @Override
                    public void onProgress(int percentage) {
                        runOnUiThread(() ->
                                btnSubmit.setText("Uploading " + (index + 1) + "/" + attachedFiles.size() + " (" + percentage + "%)"));
                    }

                    @Override
                    public void onError(String errorMessage) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            uploadContainer.setEnabled(true);
                            showError("Upload failed: " + errorMessage);
                            updateSubmitButton();
                        });
                    }
                });
    }

    private void submitToApi() {
        runOnUiThread(() -> btnSubmit.setText("Submitting..."));
        int studentId = authManager.getCurrentUserId();

        // Convert FileUploadResponse.FileInfo to SubmissionRequest.FileInfo
        List<SubmissionRequest.FileInfo> fileInfoList = new ArrayList<>();
        for (FileUploadResponse.FileInfo uploadedFile : uploadedFiles) {
            fileInfoList.add(new SubmissionRequest.FileInfo(
                    uploadedFile.getFileName() != null ? uploadedFile.getFileName() : uploadedFile.getName(), // Use fileName or name
                    uploadedFile.getType(), // Fixed: getType() not getFileType()
                    uploadedFile.getUrl()
            ));
        }

        // Create request with correct constructor: (submissionId, studentId, List<FileInfo>)
        SubmissionRequest request = new SubmissionRequest(taskId, studentId, fileInfoList);

        ApiClient.getApiService().submitAssignment(request)
                .enqueue(new Callback<SubmissionResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<SubmissionResponse> call, @NonNull Response<SubmissionResponse> response) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            uploadContainer.setEnabled(true);
                            if (response.isSuccessful() && response.body() != null) {
                                SubmissionResponse res = response.body();
                                if (res.isSuccess()) {
                                    showSuccess("Assignment submitted successfully!");
                                    attachedFiles.clear();
                                    attachedFilesContainer.removeAllViews();
                                    uploadedFiles.clear();
                                    loadExistingSubmission();
                                } else {
                                    showError(res.getError() != null ? res.getError() : "Submission failed");
                                    updateSubmitButton();
                                }
                            } else {
                                showError("Submission failed - server error");
                                updateSubmitButton();
                            }
                        });
                    }

                    @Override
                    public void onFailure(@NonNull Call<SubmissionResponse> call, @NonNull Throwable t) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            uploadContainer.setEnabled(true);
                            showError("Connection error: " + t.getMessage());
                            updateSubmitButton();
                        });
                    }
                });
    }

    private void checkIfLate() {
        if (dueDate == null || dueDate.isEmpty()) return;

        try {
            String dateTimeStr = dueDate;
            SimpleDateFormat sdf;

            if (dueTime != null && !dueTime.isEmpty()) {
                dateTimeStr += " " + dueTime;
                sdf = new SimpleDateFormat(dueTime.length() == 5 ? "yyyy-MM-dd HH:mm" : "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            } else {
                sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            }

            Date dueDateTime = sdf.parse(dateTimeStr);
            if (dueDateTime != null && new Date().after(dueDateTime)) {
                isLate = true;
                txtLateStatus.setVisibility(View.VISIBLE);
                txtDueDate.setTextColor(0xFFEF4444);
            }
        } catch (ParseException e) { /* Ignore */ }
    }

    private String formatDueDate(String date, String time) {
        if (date == null || date.isEmpty()) return "No due date";
        return date + (time != null && !time.isEmpty() ? " at " + time : "");
    }

    private void updateSubmitButton() {
        if (!canSubmit || currentAttempts >= maxAttempts) {
            btnSubmit.setText("Maximum Attempts Reached");
            btnSubmit.setEnabled(false);
            btnSubmit.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF6B7280));
            uploadContainer.setAlpha(0.5f);
            uploadContainer.setClickable(false);
        } else {
            String text = currentAttempts > 0 ? "Resubmit Assignment" : "Submit Assignment";
            btnSubmit.setText("📤 " + text);
            boolean hasFiles = !attachedFiles.isEmpty();
            btnSubmit.setEnabled(hasFiles);
            btnSubmit.setBackgroundTintList(android.content.res.ColorStateList.valueOf(hasFiles ? 0xFF2563EB : 0xFF6B7280));
            uploadContainer.setAlpha(1f);
            uploadContainer.setClickable(true);
        }
    }

    private int getGradeColor(int grade) {
        if (grade >= 90) return 0xFF10B981;
        if (grade >= 75) return 0xFF3B82F6;
        if (grade >= 60) return 0xFFFBBF24;
        return 0xFFEF4444;
    }

    private void setLoading(boolean loading) {
        if (progressBar != null) progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        if (message != null) Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showSuccess(String message) {
        Toast.makeText(this, "✓ " + message, Toast.LENGTH_LONG).show();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}