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
import com.matibag.presentlast.api.models.SubjectAttendanceDetailResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubjectAttendanceActivity extends AppCompatActivity {

    private static final String TAG = "SubjectAttendanceActivity";

    // Header views (from included layout)
    private TextView btnBack, txtCourseName, txtCourseInfo, txtAttendanceGrade;

    // Tab views (from included layout)
    private TextView tabContent, tabAttendance, tabGrades;

    // Content views
    private TextView txtSectionTitle;
    private LinearLayout attendanceContainer, legendContainer;
    private ProgressBar progressBar;

    // Data
    private AuthManager authManager;
    private int courseId;
    private String courseName, courseCode, instructor;
    private SubjectAttendanceDetailResponse attendanceDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_attendance);

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
        loadAttendanceDetail();
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
        txtSectionTitle = findViewById(R.id.txtSectionTitle);
        attendanceContainer = findViewById(R.id.attendanceContainer);
        legendContainer = findViewById(R.id.legendContainer);
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
        // Set Attendance tab as active
        setActiveTab(tabAttendance);

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
                setActiveTab(tabAttendance);
                // Already on attendance page
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

    private void loadAttendanceDetail() {
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

        ApiClient.getApiService().getSubjectAttendanceDetail(courseId, studentId)
                .enqueue(new Callback<SubjectAttendanceDetailResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<SubjectAttendanceDetailResponse> call,
                                           @NonNull Response<SubjectAttendanceDetailResponse> response) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            if (response.isSuccessful() && response.body() != null) {
                                SubjectAttendanceDetailResponse res = response.body();
                                if (res.isSuccess()) {
                                    attendanceDetail = res;
                                    updateHeaderFromResponse();
                                    displayAttendanceGrade();
                                    displayAttendanceRecords();
                                } else {
                                    showError(res.getError());
                                    displayNoAttendance();
                                }
                            } else {
                                showError("Failed to load attendance");
                                displayNoAttendance();
                            }
                        });
                    }

                    @Override
                    public void onFailure(@NonNull Call<SubjectAttendanceDetailResponse> call, @NonNull Throwable t) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            showError("Connection error: " + t.getMessage());
                            displayNoAttendance();
                        });
                    }
                });
    }

    private void updateHeaderFromResponse() {
        if (attendanceDetail == null || attendanceDetail.getSubject() == null) return;

        SubjectAttendanceDetailResponse.SubjectInfo subject = attendanceDetail.getSubject();

        if (txtCourseName != null) {
            txtCourseName.setText(subject.getName());
        }
        if (txtCourseInfo != null) {
            String info = (subject.getCode() != null ? subject.getCode() : "") +
                    (subject.getInstructors() != null ? " • " + subject.getInstructors() : "");
            txtCourseInfo.setText(info);
        }
    }

    private void displayAttendanceGrade() {
        if (txtAttendanceGrade == null || attendanceDetail == null ||
                attendanceDetail.getStats() == null) return;

        SubjectAttendanceDetailResponse.AttendanceStats stats = attendanceDetail.getStats();

        // Calculate attendance grade: (Present * 100 + Late * 50) / Total
        int total = stats.getTotalSessions();
        if (total == 0) {
            txtAttendanceGrade.setVisibility(View.GONE);
            return;
        }

        double gradePoints = (stats.getPresentCount() * 100.0) + (stats.getLateCount() * 50.0);
        double attendanceGrade = gradePoints / total;

        txtAttendanceGrade.setText(String.format("Attendance Grade: %.1f%%", attendanceGrade));
        txtAttendanceGrade.setTextColor(getGradeColor(attendanceGrade));
        txtAttendanceGrade.setVisibility(View.VISIBLE);
    }

    // ============================================================
    // DISPLAY ATTENDANCE RECORDS
    // ============================================================

    private void displayAttendanceRecords() {
        if (attendanceContainer == null) return;
        attendanceContainer.removeAllViews();

        if (attendanceDetail == null || attendanceDetail.getSessions() == null ||
                attendanceDetail.getSessions().isEmpty()) {
            displayNoAttendance();
            return;
        }

        List<SubjectAttendanceDetailResponse.AttendanceSession> sessions = attendanceDetail.getSessions();

        for (SubjectAttendanceDetailResponse.AttendanceSession session : sessions) {
            addAttendanceRow(session);
        }
    }

    private void addAttendanceRow(SubjectAttendanceDetailResponse.AttendanceSession session) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setBackgroundColor(0xFF2D2D2D);
        int padding = dpToPx(12);
        row.setPadding(padding, padding, padding, padding);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.setMargins(0, 0, 0, dpToPx(8));
        row.setLayoutParams(rowParams);

        // Date
        TextView txtDate = new TextView(this);
        String dateText = session.getDate();
        if (session.getTime() != null) {
            dateText += " " + session.getTime();
        }
        txtDate.setText(dateText);
        txtDate.setTextColor(Color.WHITE);
        txtDate.setTextSize(16);
        LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        txtDate.setLayoutParams(dateParams);
        row.addView(txtDate);

        // Status
        TextView txtStatus = new TextView(this);
        txtStatus.setTextSize(16);
        txtStatus.setTypeface(null, Typeface.BOLD);

        String status = session.getStatus() != null ? session.getStatus() : "absent";
        int gradePercentage;
        int statusColor;

        switch (status.toLowerCase()) {
            case "present":
                statusColor = 0xFF10B981;
                gradePercentage = 100;
                break;
            case "late":
                statusColor = 0xFFFFA500;
                gradePercentage = 50;
                break;
            case "excused":
                statusColor = 0xFF3B82F6;
                gradePercentage = 100; // Excused counts as present
                break;
            case "absent":
            default:
                statusColor = 0xFFEF4444;
                gradePercentage = 0;
                break;
        }

        String statusText = status.substring(0, 1).toUpperCase() + status.substring(1);
        txtStatus.setText(statusText + " (" + gradePercentage + "%)");
        txtStatus.setTextColor(statusColor);

        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        txtStatus.setLayoutParams(statusParams);
        row.addView(txtStatus);

        attendanceContainer.addView(row);
    }

    private void displayNoAttendance() {
        if (attendanceContainer == null) return;
        attendanceContainer.removeAllViews();

        TextView emptyText = new TextView(this);
        emptyText.setText("No attendance records available");
        emptyText.setTextColor(0xFF94A3B8);
        emptyText.setTextSize(16);
        emptyText.setGravity(Gravity.CENTER);
        emptyText.setBackgroundColor(0xFF1E293B);
        int padding = dpToPx(24);
        emptyText.setPadding(padding, padding, padding, padding);

        attendanceContainer.addView(emptyText);
    }

    private int getGradeColor(double grade) {
        if (grade >= 90) return 0xFF10B981; // Green
        if (grade >= 75) return 0xFFFFA500; // Orange
        return 0xFFEF4444; // Red
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