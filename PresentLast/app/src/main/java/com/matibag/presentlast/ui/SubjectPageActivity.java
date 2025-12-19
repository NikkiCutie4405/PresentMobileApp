package com.matibag.presentlast.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.matibag.presentlast.R;
import com.matibag.presentlast.api.ApiClient;
import com.matibag.presentlast.api.AuthManager;
import com.matibag.presentlast.api.models.SubjectDetailResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubjectPageActivity extends AppCompatActivity {

    private static final String TAG = "SubjectPageActivity";

    // Header views (from included layout)
    private TextView btnBack, txtCourseName, txtCourseInfo, txtAttendanceGrade;

    // Tab views (from included layout)
    private TextView tabContent, tabAttendance, tabGrades;

    // Content views
    private TextView btnJoinSession, txtSectionTitle;
    private LinearLayout tasksContainer;
    private ProgressBar progressBar;

    // Data
    private AuthManager authManager;
    private int courseId;
    private String courseName, courseCode, instructor;
    private SubjectDetailResponse subjectDetail;
    private boolean isSessionActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject);

        authManager = AuthManager.getInstance(this);

        if (!authManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        // Get course info from intent
        courseId = getIntent().getIntExtra("courseId", -1);
        courseName = getIntent().getStringExtra("courseName");
        courseCode = getIntent().getStringExtra("courseCode");
        instructor = getIntent().getStringExtra("instructor");

        initViews();
        setupHeader();
        setupTabs();
        loadSubjectDetail();
    }

    // ============================================================
    // VIEW INITIALIZATION
    // ============================================================

    private void initViews() {
        // Course Header (from included layout)
        View headerLayout = findViewById(R.id.layoutCourseHeader);
        if (headerLayout != null) {
            btnBack = headerLayout.findViewById(R.id.btnBack);
            txtCourseName = headerLayout.findViewById(R.id.txtCourseName);
            txtCourseInfo = headerLayout.findViewById(R.id.txtCourseInfo);
            txtAttendanceGrade = headerLayout.findViewById(R.id.txtAttendanceGrade);
        }

        // Tab Navigation (from included layout)
        View tabsLayout = findViewById(R.id.layoutCourseTabs);
        if (tabsLayout != null) {
            tabContent = tabsLayout.findViewById(R.id.tabContent);
            tabAttendance = tabsLayout.findViewById(R.id.tabAttendance);
            tabGrades = tabsLayout.findViewById(R.id.tabGrades);
        }

        // Content views
        btnJoinSession = findViewById(R.id.btnJoinSession);
        txtSectionTitle = findViewById(R.id.txtSectionTitle);
        tasksContainer = findViewById(R.id.tasksContainer);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupHeader() {
        // Set header info
        if (txtCourseName != null && courseName != null) {
            txtCourseName.setText(courseName);
        }
        if (txtCourseInfo != null) {
            String info = (courseCode != null ? courseCode : "") +
                    (instructor != null ? " • " + instructor : "");
            txtCourseInfo.setText(info);
        }

        // Hide SubjectAttendanceActivity grade on this page
        if (txtAttendanceGrade != null) {
            txtAttendanceGrade.setVisibility(View.GONE);
        }

        // Back button
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Join Session button
        if (btnJoinSession != null) {
            btnJoinSession.setOnClickListener(v -> handleJoinSession());
            updateSessionButton();
        }
    }

    private void setupTabs() {
        // Set Content tab as active
        setActiveTab(tabContent);

        if (tabContent != null) {
            tabContent.setOnClickListener(v -> {
                setActiveTab(tabContent);
                // Already on content page
            });
        }

        if (tabAttendance != null) {
            tabAttendance.setOnClickListener(v -> {
                Intent intent = new Intent(this, SubjectAttendanceActivity.class);
                intent.putExtra("courseId", courseId);
                intent.putExtra("courseName", courseName);
                intent.putExtra("courseCode", courseCode);
                intent.putExtra("instructor", instructor);
                startActivity(intent);
                finish();
            });
        }

        if (tabGrades != null) {
            tabGrades.setOnClickListener(v -> {
                Intent intent = new Intent(this, SubjectGradesActivity.class);
                intent.putExtra("courseId", courseId);
                intent.putExtra("courseName", courseName);
                intent.putExtra("courseCode", courseCode);
                intent.putExtra("instructor", instructor);
                startActivity(intent);
                finish();
            });
        }
    }

    private void setActiveTab(TextView activeTab) {
        if (tabContent != null) tabContent.setTextColor(0xFF94A3B8);
        if (tabAttendance != null) tabAttendance.setTextColor(0xFF94A3B8);
        if (tabGrades != null) tabGrades.setTextColor(0xFF94A3B8);

        if (activeTab != null) activeTab.setTextColor(0xFF6366F1);
    }

    // ============================================================
    // API DATA LOADING
    // ============================================================

    private void loadSubjectDetail() {
        if (courseId == -1) {
            showError("Invalid course");
            return;
        }

        int studentId = authManager.getCurrentUserId();
        if (studentId == -1) {
            navigateToLogin();
            return;
        }

        setLoading(true);

        ApiClient.getApiService().getSubjectDetail(courseId, studentId)
                .enqueue(new Callback<SubjectDetailResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<SubjectDetailResponse> call,
                                           @NonNull Response<SubjectDetailResponse> response) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            if (response.isSuccessful() && response.body() != null) {
                                SubjectDetailResponse res = response.body();
                                if (res.isSuccess()) {
                                    subjectDetail = res;
                                    updateHeaderFromResponse();
                                    displayFolders();
                                } else {
                                    showError(res.getError());
                                    displayNoContent();
                                }
                            } else {
                                showError("Failed to load subject details");
                                displayNoContent();
                            }
                        });
                    }

                    @Override
                    public void onFailure(@NonNull Call<SubjectDetailResponse> call, @NonNull Throwable t) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            showError("Connection error: " + t.getMessage());
                            displayNoContent();
                        });
                    }
                });
    }

    private void updateHeaderFromResponse() {
        if (subjectDetail == null || subjectDetail.getSubject() == null) return;

        SubjectDetailResponse.Subject subject = subjectDetail.getSubject();

        if (txtCourseName != null) {
            txtCourseName.setText(subject.getName());
        }

        if (txtCourseInfo != null) {
            // Build instructor string from list
            String instructorNames = "";
            if (subjectDetail.getInstructors() != null && !subjectDetail.getInstructors().isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (SubjectDetailResponse.Instructor inst : subjectDetail.getInstructors()) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(inst.getName());
                }
                instructorNames = sb.toString();
            }
            String info = (subject.getCode() != null ? subject.getCode() : "") +
                    (!instructorNames.isEmpty() ? " • " + instructorNames : "");
            txtCourseInfo.setText(info);
        }
    }

    // ============================================================
    // DISPLAY FOLDERS & TASKS
    // ============================================================

    private void displayFolders() {
        if (tasksContainer == null) return;
        tasksContainer.removeAllViews();

        if (subjectDetail == null || subjectDetail.getFolders() == null ||
                subjectDetail.getFolders().isEmpty()) {
            displayNoContent();
            return;
        }

        List<SubjectDetailResponse.Folder> folders = subjectDetail.getFolders();

        for (SubjectDetailResponse.Folder folder : folders) {
            addFolderCard(folder);
        }
    }

    private void addFolderCard(SubjectDetailResponse.Folder folder) {
        LinearLayout folderCard = new LinearLayout(this);
        folderCard.setOrientation(LinearLayout.VERTICAL);
        folderCard.setBackgroundColor(0xFF1E293B);
        int padding = dpToPx(16);
        folderCard.setPadding(padding, padding, padding, padding);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dpToPx(12));
        folderCard.setLayoutParams(cardParams);

        // Folder header row
        LinearLayout headerRow = new LinearLayout(this);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);
        headerRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Folder icon
        TextView folderIcon = new TextView(this);
        folderIcon.setText("📁");
        folderIcon.setTextSize(24);
        headerRow.addView(folderIcon);

        // Folder name
        TextView txtFolderName = new TextView(this);
        txtFolderName.setText(folder.getName());
        txtFolderName.setTextColor(Color.WHITE);
        txtFolderName.setTextSize(18);
        txtFolderName.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
        nameParams.weight = 1;
        nameParams.setMargins(dpToPx(12), 0, 0, 0);
        txtFolderName.setLayoutParams(nameParams);
        headerRow.addView(txtFolderName);

        // Task count
        int taskCount = folder.getSubmissions() != null ? folder.getSubmissions().size() : 0;
        TextView txtTaskCount = new TextView(this);
        txtTaskCount.setText(taskCount + " tasks");
        txtTaskCount.setTextColor(0xFF94A3B8);
        txtTaskCount.setTextSize(14);
        LinearLayout.LayoutParams countParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        countParams.setMargins(dpToPx(12), 0, 0, 0);
        txtTaskCount.setLayoutParams(countParams);
        headerRow.addView(txtTaskCount);

        // Dropdown arrow
        TextView dropdownArrow = new TextView(this);
        dropdownArrow.setText("▼");
        dropdownArrow.setTextColor(0xFF94A3B8);
        dropdownArrow.setTextSize(16);
        LinearLayout.LayoutParams arrowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        arrowParams.setMargins(dpToPx(12), 0, 0, 0);
        dropdownArrow.setLayoutParams(arrowParams);
        headerRow.addView(dropdownArrow);

        folderCard.addView(headerRow);

        // Tasks container (initially hidden)
        LinearLayout tasksInFolder = new LinearLayout(this);
        tasksInFolder.setOrientation(LinearLayout.VERTICAL);
        tasksInFolder.setVisibility(View.GONE);
        LinearLayout.LayoutParams tasksParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        tasksParams.setMargins(0, dpToPx(12), 0, 0);
        tasksInFolder.setLayoutParams(tasksParams);

        // Add submissions/tasks
        if (folder.getSubmissions() != null) {
            for (SubjectDetailResponse.Submission submission : folder.getSubmissions()) {
                addTaskCard(tasksInFolder, submission);
            }
        }

        folderCard.addView(tasksInFolder);

        // Toggle dropdown
        headerRow.setClickable(true);
        headerRow.setOnClickListener(v -> {
            if (tasksInFolder.getVisibility() == View.GONE) {
                tasksInFolder.setVisibility(View.VISIBLE);
                dropdownArrow.setText("▲");
            } else {
                tasksInFolder.setVisibility(View.GONE);
                dropdownArrow.setText("▼");
            }
        });

        tasksContainer.addView(folderCard);
    }

    private void addTaskCard(LinearLayout container, SubjectDetailResponse.Submission submission) {
        LinearLayout taskTab = new LinearLayout(this);
        taskTab.setOrientation(LinearLayout.VERTICAL);
        taskTab.setBackgroundColor(0xFF0F172A);
        int padding = dpToPx(12);
        taskTab.setPadding(padding, padding, padding, padding);

        LinearLayout.LayoutParams tabParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        tabParams.setMargins(0, 0, 0, dpToPx(8));
        taskTab.setLayoutParams(tabParams);
        taskTab.setClickable(true);
        taskTab.setFocusable(true);

        // Task title
        TextView txtTitle = new TextView(this);
        txtTitle.setText(submission.getName());
        txtTitle.setTextColor(Color.WHITE);
        txtTitle.setTextSize(16);
        txtTitle.setTypeface(null, Typeface.BOLD);
        taskTab.addView(txtTitle);

        // Details row
        LinearLayout detailsRow = new LinearLayout(this);
        detailsRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams detailsParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        detailsParams.setMargins(0, dpToPx(8), 0, 0);
        detailsRow.setLayoutParams(detailsParams);

        // Due date
        String dueText = submission.getDueDate() != null ? "Due: " + submission.getDueDate() : "No due date";
        if (submission.getDueTime() != null) {
            dueText += " " + submission.getDueTime();
        }
        TextView txtDueDate = new TextView(this);
        txtDueDate.setText(dueText);
        txtDueDate.setTextColor(0xFF94A3B8);
        txtDueDate.setTextSize(12);
        detailsRow.addView(txtDueDate);

        // Status
        String status = submission.getStatus() != null ? submission.getStatus() : "not_submitted";
        boolean isSubmitted = submission.getStudentSubmissionId() != null;
        boolean isGraded = submission.getGrade() != null;

        TextView txtSubmitStatus = new TextView(this);
        txtSubmitStatus.setText(" • " + (isSubmitted ? "Submitted" : "Not Submitted"));
        txtSubmitStatus.setTextColor(isSubmitted ? 0xFF10B981 : 0xFFFBBF24);
        txtSubmitStatus.setTextSize(12);
        txtSubmitStatus.setTypeface(null, Typeface.BOLD);
        detailsRow.addView(txtSubmitStatus);

        TextView txtGradeStatus = new TextView(this);
        txtGradeStatus.setText(" • " + (isGraded ? "Graded" : "Not Graded"));
        txtGradeStatus.setTextColor(isGraded ? 0xFF6366F1 : 0xFF94A3B8);
        txtGradeStatus.setTextSize(12);
        txtGradeStatus.setTypeface(null, Typeface.BOLD);
        detailsRow.addView(txtGradeStatus);

        taskTab.addView(detailsRow);

        // Click to open submission page - pass ALL data
        taskTab.setOnClickListener(v -> {
            Intent intent = new Intent(this, SubmissionPageActivity.class);
            intent.putExtra("taskId", submission.getId());
            intent.putExtra("taskTitle", submission.getName());
            intent.putExtra("taskDescription", submission.getDescription());
            intent.putExtra("dueDate", submission.getDueDate());
            intent.putExtra("dueTime", submission.getDueTime());
            intent.putExtra("maxAttempts", submission. getMaxAttempts());

            // Pass subject info from the loaded detail
            if (subjectDetail != null && subjectDetail.getSubject() != null) {
                intent.putExtra("subjectName", subjectDetail.getSubject().getName());
                intent.putExtra("subjectCode", subjectDetail.getSubject().getCode());
            }

            startActivity(intent);
        });

        container.addView(taskTab);
    }

    private void displayNoContent() {
        if (tasksContainer == null) return;
        tasksContainer.removeAllViews();

        TextView emptyText = new TextView(this);
        emptyText.setText("No tasks or assignments available");
        emptyText.setTextColor(0xFF94A3B8);
        emptyText.setTextSize(16);
        emptyText.setGravity(Gravity.CENTER);
        emptyText.setBackgroundColor(0xFF1E293B);
        int padding = dpToPx(24);
        emptyText.setPadding(padding, padding, padding, padding);

        tasksContainer.addView(emptyText);
    }

    // ============================================================
    // SESSION HANDLING
    // ============================================================

    private void updateSessionButton() {
        if (btnJoinSession == null) return;

        // TODO: Check API for active session
        if (isSessionActive) {
            btnJoinSession.setText("📱 Join Session! (Active)");
            btnJoinSession.setBackgroundColor(0xFF10B981);
            btnJoinSession.setVisibility(View.VISIBLE);
        } else {
            btnJoinSession.setVisibility(View.GONE);
        }
    }

    private void handleJoinSession() {
        if (isSessionActive) {
            Intent intent = new Intent(this, SubjectAttendanceActivity.class);
            intent.putExtra("courseId", courseId);
            intent.putExtra("courseName", courseName);
            intent.putExtra("courseCode", courseCode);
            intent.putExtra("instructor", instructor);
            startActivity(intent);
        } else {
            showError("No active session at this moment!");
        }
    }

    // ============================================================
    // UTILITY METHODS
    // ============================================================

    private void setLoading(boolean loading) {
        if (progressBar != null) {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    private void showError(String message) {
        if (message != null) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
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