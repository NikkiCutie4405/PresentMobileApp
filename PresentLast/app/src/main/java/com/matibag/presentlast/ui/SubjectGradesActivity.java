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
import com.matibag.presentlast.api.models.SubjectGradesDetailResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubjectGradesActivity extends AppCompatActivity {

    private static final String TAG = "SubjectGradesActivity";

    // Header views
    private TextView btnBack, txtCourseName, txtCourseInfo;

    // Tab views
    private TextView tabContent, tabAttendance, tabGrades;

    // Content views
    private LinearLayout gradesContainer;
    private ProgressBar progressBar;

    // Data
    private AuthManager authManager;
    private int courseId;
    private String courseName, courseCode, instructor;
    private SubjectGradesDetailResponse gradesDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_grades);

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
        loadGradesDetail();
    }

    // ============================================================
    // VIEW INITIALIZATION
    // ============================================================

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        txtCourseName = findViewById(R.id.txtCourseName);
        txtCourseInfo = findViewById(R.id.txtCourseInfo);

        tabContent = findViewById(R.id.tabContent);
        tabAttendance = findViewById(R.id.tabAttendance);
        tabGrades = findViewById(R.id.tabGrades);

        gradesContainer = findViewById(R.id.gradesContainer);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupHeader() {
        if (txtCourseName != null && courseName != null) {
            txtCourseName.setText(courseName);
        }
        if (txtCourseInfo != null) {
            String info = (courseCode != null ? courseCode : "") +
                    (instructor != null ? " • " + instructor : "");
            txtCourseInfo.setText(info);
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void setupTabs() {
        setActiveTab(tabGrades);

        if (tabContent != null) {
            tabContent.setOnClickListener(v -> {
                Intent intent = new Intent(this, SubjectPageActivity.class);
                intent.putExtra("courseId", courseId);
                intent.putExtra("courseName", courseName);
                intent.putExtra("courseCode", courseCode);
                intent.putExtra("instructor", instructor);
                startActivity(intent);
                finish();
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
                setActiveTab(tabGrades);
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

    private void loadGradesDetail() {
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

        ApiClient.getApiService().getSubjectGradesDetail(courseId, studentId)
                .enqueue(new Callback<SubjectGradesDetailResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<SubjectGradesDetailResponse> call,
                                           @NonNull Response<SubjectGradesDetailResponse> response) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            if (response.isSuccessful() && response.body() != null) {
                                SubjectGradesDetailResponse res = response.body();
                                if (res.isSuccess()) {
                                    gradesDetail = res;
                                    updateHeaderFromResponse();
                                    displayGrades();
                                } else {
                                    showError(res.getError());
                                    displayNoGrades();
                                }
                            } else {
                                showError("Failed to load grades");
                                displayNoGrades();
                            }
                        });
                    }

                    @Override
                    public void onFailure(@NonNull Call<SubjectGradesDetailResponse> call, @NonNull Throwable t) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            showError("Connection error: " + t.getMessage());
                            displayNoGrades();
                        });
                    }
                });
    }

    private void updateHeaderFromResponse() {
        if (gradesDetail == null || gradesDetail.getSubject() == null) return;

        SubjectGradesDetailResponse.SubjectInfo subject = gradesDetail.getSubject();

        if (txtCourseName != null && subject.getName() != null) {
            txtCourseName.setText(subject.getName());
        }
        if (txtCourseInfo != null) {
            String info = (subject.getCode() != null ? subject.getCode() : "") +
                    (subject.getInstructors() != null ? " • " + subject.getInstructors() : "");
            txtCourseInfo.setText(info);
        }
    }

    // ============================================================
    // DISPLAY GRADES
    // ============================================================

    private void displayGrades() {
        if (gradesContainer == null) return;
        gradesContainer.removeAllViews();

        if (gradesDetail == null || gradesDetail.getTasks() == null ||
                gradesDetail.getTasks().isEmpty()) {
            displayNoGrades();
            return;
        }

        List<SubjectGradesDetailResponse.TaskGrade> tasks = gradesDetail.getTasks();

        for (SubjectGradesDetailResponse.TaskGrade task : tasks) {
            addGradeCard(task);
        }
    }

    private void addGradeCard(SubjectGradesDetailResponse.TaskGrade task) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(0xFF1E293B);
        int padding = dpToPx(20);
        card.setPadding(padding, padding, padding, padding);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dpToPx(12));
        card.setLayoutParams(cardParams);
        card.setClickable(true);
        card.setFocusable(true);

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);
        topRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        LinearLayout leftSection = new LinearLayout(this);
        leftSection.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
        leftParams.weight = 1;
        leftSection.setLayoutParams(leftParams);

        TextView txtTitle = new TextView(this);
        txtTitle.setText(task.getName());
        txtTitle.setTextColor(Color.WHITE);
        txtTitle.setTextSize(18);
        txtTitle.setTypeface(null, Typeface.BOLD);
        leftSection.addView(txtTitle);

        TextView txtDate = new TextView(this);
        txtDate.setText("Due: " + (task.getDueDate() != null ? task.getDueDate() : "N/A"));
        txtDate.setTextColor(0xFF94A3B8);
        txtDate.setTextSize(14);
        LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        dateParams.setMargins(0, dpToPx(4), 0, 0);
        txtDate.setLayoutParams(dateParams);
        leftSection.addView(txtDate);

        topRow.addView(leftSection);

        boolean isGraded = task.getGrade() != null;
        if (isGraded) {
            TextView txtGrade = new TextView(this);
            int grade = task.getGrade();
            int gradeColor = getGradeColor(grade);

            txtGrade.setText(String.valueOf(grade));
            txtGrade.setTextColor(gradeColor);
            txtGrade.setTextSize(36);
            txtGrade.setTypeface(null, Typeface.BOLD);
            txtGrade.setGravity(Gravity.END);
            topRow.addView(txtGrade);
        } else {
            TextView txtNoGrade = new TextView(this);
            txtNoGrade.setText("--");
            txtNoGrade.setTextColor(0xFF6B7280);
            txtNoGrade.setTextSize(36);
            txtNoGrade.setTypeface(null, Typeface.BOLD);
            topRow.addView(txtNoGrade);
        }

        card.addView(topRow);

        LinearLayout statusRow = new LinearLayout(this);
        statusRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams statusRowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        statusRowParams.setMargins(0, dpToPx(12), 0, 0);
        statusRow.setLayoutParams(statusRowParams);

        boolean isSubmitted = task.getStudentSubmissionId() != null;
        String submitStatus = isSubmitted ? "Submitted" : "Not Submitted";

        TextView txtSubmitStatus = new TextView(this);
        txtSubmitStatus.setText(submitStatus);
        txtSubmitStatus.setTextColor(isSubmitted ? 0xFF10B981 : 0xFFFBBF24);
        txtSubmitStatus.setTextSize(12);
        txtSubmitStatus.setTypeface(null, Typeface.BOLD);
        txtSubmitStatus.setBackgroundColor(isSubmitted ? 0xFF064E3B : 0xFF78350F);
        int badgePadding = dpToPx(6);
        txtSubmitStatus.setPadding(dpToPx(12), badgePadding, dpToPx(12), badgePadding);
        txtSubmitStatus.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams submitParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        submitParams.setMargins(0, 0, dpToPx(8), 0);
        txtSubmitStatus.setLayoutParams(submitParams);
        statusRow.addView(txtSubmitStatus);

        String gradeStatus = isGraded ? "Graded" : "Not Graded";

        TextView txtGradeStatus = new TextView(this);
        txtGradeStatus.setText(gradeStatus);
        txtGradeStatus.setTextColor(isGraded ? 0xFF6366F1 : 0xFF9CA3B8);
        txtGradeStatus.setTextSize(12);
        txtGradeStatus.setTypeface(null, Typeface.BOLD);
        txtGradeStatus.setBackgroundColor(isGraded ? 0xFF1E3A8A : 0xFF374151);
        txtGradeStatus.setPadding(dpToPx(12), badgePadding, dpToPx(12), badgePadding);
        txtGradeStatus.setGravity(Gravity.CENTER);
        statusRow.addView(txtGradeStatus);

        card.addView(statusRow);

        card.setOnClickListener(v -> {
            Intent intent = new Intent(this, SubmissionPageActivity.class);
            intent.putExtra("taskId", task.getId());
            intent.putExtra("taskTitle", task.getName());
            intent.putExtra("taskDescription", task.getDescription());
            intent.putExtra("dueDate", task.getDueDate());
            intent.putExtra("subjectName", courseName);
            intent.putExtra("subjectCode", courseCode);
            intent.putExtra("folderName", task.getFolderName());
            startActivity(intent);
        });

        gradesContainer.addView(card);
    }

    private void displayNoGrades() {
        if (gradesContainer == null) return;
        gradesContainer.removeAllViews();

        TextView emptyText = new TextView(this);
        emptyText.setText("No grades available");
        emptyText.setTextColor(0xFF94A3B8);
        emptyText.setTextSize(16);
        emptyText.setGravity(Gravity.CENTER);
        emptyText.setBackgroundColor(0xFF1E293B);
        int padding = dpToPx(24);
        emptyText.setPadding(padding, padding, padding, padding);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dpToPx(16), 0, 0);
        emptyText.setLayoutParams(params);

        gradesContainer.addView(emptyText);
    }

    private int getGradeColor(int grade) {
        if (grade >= 90) return 0xFF10B981;
        if (grade >= 75) return 0xFF3B82F6;
        if (grade >= 60) return 0xFFFBBF24;
        return 0xFFEF4444;
    }

    // ============================================================
    // UTILITY METHODS
    // ============================================================

    private void setLoading(boolean loading) {
        if (progressBar != null) {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        } else if (loading && gradesContainer != null) {
            gradesContainer.removeAllViews();
            TextView loadingText = new TextView(this);
            loadingText.setText("Loading...");
            loadingText.setTextColor(0xFF94A3B8);
            loadingText.setTextSize(16);
            loadingText.setGravity(Gravity.CENTER);
            loadingText.setPadding(dpToPx(24), dpToPx(48), dpToPx(24), dpToPx(48));
            gradesContainer.addView(loadingText);
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