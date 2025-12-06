package com.matibag.presentlast;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class SubmissionPage extends Activity {
    TextView btnBack, txtSubmissionTitle, txtTaskCategory, txtDueDate;
    TextView txtSubmitStatus, txtLateStatus, txtAttempts, txtGrade, txtInstructions;
    LinearLayout uploadContainer, attachedFilesContainer;
    Button btnSubmit;

    int taskId;
    String taskTitle, dueDate, submitStatus, gradeStatus;
    int currentAttempts = 0;
    int maxAttempts = 3;
    boolean isLate = false;

    private static final int PICK_FILE_REQUEST = 1;
    private ArrayList<Uri> attachedFiles = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.submission);

        // Initialize views
        btnBack = findViewById(R.id.btnBack);
        txtSubmissionTitle = findViewById(R.id.txtSubmissionTitle);
        txtTaskCategory = findViewById(R.id.txtTaskCategory);
        txtDueDate = findViewById(R.id.txtDueDate);
        txtSubmitStatus = findViewById(R.id.txtSubmitStatus);
        txtLateStatus = findViewById(R.id.txtLateStatus);
        txtAttempts = findViewById(R.id.txtAttempts);
        txtGrade = findViewById(R.id.txtGrade);
        txtInstructions = findViewById(R.id.txtInstructions);
        uploadContainer = findViewById(R.id.uploadContainer);
        attachedFilesContainer = findViewById(R.id.attachedFilesContainer);
        btnSubmit = findViewById(R.id.btnSubmit);

        // Get task info from intent
        taskId = getIntent().getIntExtra("taskId", -1);
        taskTitle = getIntent().getStringExtra("taskTitle");
        dueDate = getIntent().getStringExtra("dueDate");
        submitStatus = getIntent().getStringExtra("submitStatus");
        gradeStatus = getIntent().getStringExtra("gradeStatus");

        // Set task details
        txtSubmissionTitle.setText(taskTitle);
        txtTaskCategory.setText("QUIZ"); // TODO: Get from database
        txtDueDate.setText(dueDate + " 11:59 PM");

        // Check if late
        checkIfLate();

        // Set status
        updateSubmissionStatus();

        // Set attempts
        txtAttempts.setText(currentAttempts + " / " + maxAttempts);

        // Set grade
        if (gradeStatus != null && gradeStatus.equals("Graded")) {
            txtGrade.setText("95 / 100"); // TODO: Get actual grade from database
            txtGrade.setTextColor(0xFF10B981);
        } else {
            txtGrade.setText("Not Graded");
            txtGrade.setTextColor(0xFF94A3B8);
        }

        // Set instructions
        txtInstructions.setText("Complete the quiz on functions and loops. Make sure to submit your answers before the due date. You can submit multiple times, but only the last submission will be graded.");

        // Back button
        btnBack.setOnClickListener(view -> finish());

        // Upload container click
        uploadContainer.setOnClickListener(view -> openFilePicker());

        // Submit button
        btnSubmit.setOnClickListener(view -> submitAssignment());

        // Update submit button based on status
        updateSubmitButton();
    }

    private void checkIfLate() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date dueDateTime = sdf.parse(dueDate);
            Date currentDate = new Date();

            if (currentDate.after(dueDateTime) && !submitStatus.equals("Submitted")) {
                isLate = true;
                txtLateStatus.setVisibility(View.VISIBLE);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void updateSubmissionStatus() {
        if (submitStatus != null && submitStatus.equals("Submitted")) {
            txtSubmitStatus.setText("Submitted");
            txtSubmitStatus.setTextColor(0xFF10B981); // Green
            if (isLate) {
                txtLateStatus.setVisibility(View.VISIBLE);
            }
        } else {
            txtSubmitStatus.setText("Not Submitted");
            txtSubmitStatus.setTextColor(0xFFFBBF24); // Yellow
        }
    }

    private void updateSubmitButton() {
        if (currentAttempts >= maxAttempts) {
            btnSubmit.setText("Maximum Attempts Reached");
            btnSubmit.setEnabled(false);
            btnSubmit.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF6B7280));
        } else if (submitStatus != null && submitStatus.equals("Submitted")) {
            btnSubmit.setText("⬆ Resubmit Assignment");
        } else {
            btnSubmit.setText("⬆ Submit Assignment");
        }

        if (attachedFiles.isEmpty()) {
            btnSubmit.setEnabled(false);
            btnSubmit.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF6B7280));
        } else {
            btnSubmit.setEnabled(true);
            btnSubmit.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF2563EB));
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimeTypes = {
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "image/*",
                "video/*"
        };
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(Intent.createChooser(intent, "Select file to upload"), PICK_FILE_REQUEST);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Please install a file manager", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                attachedFiles.add(fileUri);
                String fileName = getFileName(fileUri);
                addAttachedFileCard(fileName, fileUri);
                updateSubmitButton();
            }
        }
    }

    private String getFileName(Uri uri) {
        String fileName = "Unknown file";
        if (uri.getScheme().equals("content")) {
            android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                        if (nameIndex != -1) {
                            fileName = cursor.getString(nameIndex);
                        }
                    }
                } finally {
                    cursor.close();
                }
            }
        } else if (uri.getScheme().equals("file")) {
            fileName = new java.io.File(uri.getPath()).getName();
        }
        return fileName;
    }

    private void addAttachedFileCard(String fileName, Uri fileUri) {
        // File card container
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

        // File icon
        TextView fileIcon = new TextView(this);
        fileIcon.setText("📎");
        fileIcon.setTextSize(20);
        fileCard.addView(fileIcon);

        // File name
        TextView txtFileName = new TextView(this);
        txtFileName.setText(fileName);
        txtFileName.setTextColor(Color.WHITE);
        txtFileName.setTextSize(14);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        nameParams.weight = 1;
        nameParams.setMargins(dpToPx(12), 0, dpToPx(12), 0);
        txtFileName.setLayoutParams(nameParams);
        fileCard.addView(txtFileName);

        // Remove button
        TextView btnRemove = new TextView(this);
        btnRemove.setText("✕");
        btnRemove.setTextColor(0xFFEF4444);
        btnRemove.setTextSize(20);
        btnRemove.setClickable(true);
        btnRemove.setFocusable(true);
        btnRemove.setOnClickListener(view -> {
            attachedFilesContainer.removeView(fileCard);
            attachedFiles.remove(fileUri);
            updateSubmitButton();
        });
        fileCard.addView(btnRemove);

        attachedFilesContainer.addView(fileCard);
    }

    private void submitAssignment() {
        if (attachedFiles.isEmpty()) {
            Toast.makeText(this, "Please attach at least one file", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentAttempts >= maxAttempts) {
            Toast.makeText(this, "Maximum attempts reached", Toast.LENGTH_SHORT).show();
            return;
        }

        // Increment attempts
        currentAttempts++;
        txtAttempts.setText(currentAttempts + " / " + maxAttempts);

        // Update status
        submitStatus = "Submitted";
        updateSubmissionStatus();

        // Check if late and auto-grade to 0
        if (isLate) {
            txtGrade.setText("0 / 100 (Late Submission)");
            txtGrade.setTextColor(0xFFEF4444);
            gradeStatus = "Graded";
        }

        Toast.makeText(this, "Assignment submitted successfully!", Toast.LENGTH_LONG).show();

        // Update button
        updateSubmitButton();

        // Clear attached files (optional - depends on requirements)
        // attachedFiles.clear();
        // attachedFilesContainer.removeAllViews();

        // TODO: Save to database
        // db.execSQL("INSERT INTO submissions (task_id, student_id, submission_date, is_late, attempt_number) VALUES (?, ?, ?, ?, ?)",
        //           new Object[]{taskId, studentId, new Date(), isLate, currentAttempts});
        // for (Uri file : attachedFiles) {
        //     db.execSQL("INSERT INTO submission_files (submission_id, file_uri, file_name) VALUES (?, ?, ?)",
        //               new Object[]{submissionId, file.toString(), getFileName(file)});
        // }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}