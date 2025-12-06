package com.matibag.presentlast;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class GradesPage extends Activity {
    TextView btnBack, txtCourseName, txtCourseInfo;
    TextView tabContent, tabAttendance, tabGrades;
    LinearLayout gradesContainer;

    int courseId;
    String courseName, courseCode, instructor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grades_page);

        btnBack = findViewById(R.id.btnBack);
        txtCourseName = findViewById(R.id.txtCourseName);
        txtCourseInfo = findViewById(R.id.txtCourseInfo);
        tabContent = findViewById(R.id.tabContent);
        tabAttendance = findViewById(R.id.tabAttendance);
        tabGrades = findViewById(R.id.tabGrades);
        gradesContainer = findViewById(R.id.gradesContainer);

        courseId = getIntent().getIntExtra("courseId", -1);
        courseName = getIntent().getStringExtra("courseName");
        courseCode = getIntent().getStringExtra("courseCode");
        instructor = getIntent().getStringExtra("instructor");

        txtCourseName.setText(courseName);
        txtCourseInfo.setText(courseCode + " • " + instructor);

        btnBack.setOnClickListener(view -> finish());

        // Tab navigation
        tabContent.setOnClickListener(view -> {
            Intent intent = new Intent(GradesPage.this, SubjectPage.class);
            intent.putExtra("courseId", courseId);
            intent.putExtra("courseName", courseName);
            intent.putExtra("courseCode", courseCode);
            intent.putExtra("instructor", instructor);
            startActivity(intent);
            finish();
        });

        tabAttendance.setOnClickListener(view -> {
            Intent intent = new Intent(GradesPage.this, attendance.class);
            intent.putExtra("courseId", courseId);
            intent.putExtra("courseName", courseName);
            intent.putExtra("courseCode", courseCode);
            intent.putExtra("instructor", instructor);
            startActivity(intent);
            finish();
        });

        tabGrades.setOnClickListener(view -> {
            // Already on grades page
        });


        // Set active tab
        setActiveTab(tabGrades);

        // Load grades
        loadSampleGrades();
    }

    private void setActiveTab(TextView activeTab) {
        tabContent.setTextColor(0xFF94A3B8);
        tabAttendance.setTextColor(0xFF94A3B8);
        tabGrades.setTextColor(0xFF94A3B8);

        activeTab.setTextColor(0xFF6366F1);
    }

    /**
     * SDFR14 - Load grades with submission status
     */
    private void loadSampleGrades() {
        // Sample data with task IDs and statuses
        addGradeCard(1, "Quiz 1: Functions", "QUIZ", 92, 100, "2024-12-15", "Submitted", "Graded");
        addGradeCard(2, "Seatwork 2: Arrays", "SEATWORK", 88, 100, "2024-12-09", "Submitted", "Graded");
        addGradeCard(3, "Homework 1: Loops", "HOMEWORK", 90, 100, "2024-12-05", "Submitted", "Graded");
        addGradeCard(4, "Quiz 2: Algorithms", "QUIZ", 0, 100, "2024-12-20", "Not Submitted", "Not Graded");
        addGradeCard(5, "Project Proposal", "PROJECT", 0, 100, "2024-12-22", "Submitted", "Not Graded");
    }

    /**
     * SDFR14 - Add grade card
     * Output: TaskTitle, Date, Submitted/NotSubmitted, Graded/NotGraded
     * Clickable to redirect to submission page
     */
    private void addGradeCard(int taskId, String taskTitle, String taskType, double grade,
                              double maxGrade, String gradedDate, String submitStatus, String gradeStatus) {
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

        // Top row: Title and Grade
        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);
        topRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Left section: Task info
        LinearLayout leftSection = new LinearLayout(this);
        leftSection.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        leftParams.weight = 1;
        leftSection.setLayoutParams(leftParams);

        // Task title
        TextView txtTitle = new TextView(this);
        txtTitle.setText(taskTitle);
        txtTitle.setTextColor(Color.WHITE);
        txtTitle.setTextSize(18);
        txtTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        leftSection.addView(txtTitle);

        // Date
        TextView txtDate = new TextView(this);
        txtDate.setText("Date: " + gradedDate);
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

        // Right section: Grade display
        if (gradeStatus.equals("Graded")) {
            TextView txtGrade = new TextView(this);

            double percentage = (grade / maxGrade) * 100;
            int gradeColor;
            if (percentage >= 90) {
                gradeColor = 0xFF6366F1;
            } else if (percentage >= 75) {
                gradeColor = 0xFF10B981;
            } else if (percentage >= 60) {
                gradeColor = 0xFFFBBF24;
            } else {
                gradeColor = 0xFFEF4444;
            }

            txtGrade.setText(String.format("%.0f", grade));
            txtGrade.setTextColor(gradeColor);
            txtGrade.setTextSize(36);
            txtGrade.setTypeface(null, android.graphics.Typeface.BOLD);
            txtGrade.setGravity(Gravity.END);
            topRow.addView(txtGrade);
        } else {
            TextView txtNoGrade = new TextView(this);
            txtNoGrade.setText("--");
            txtNoGrade.setTextColor(0xFF6B7280);
            txtNoGrade.setTextSize(36);
            txtNoGrade.setTypeface(null, android.graphics.Typeface.BOLD);
            topRow.addView(txtNoGrade);
        }

        card.addView(topRow);

        // Status row: Submission status and Grade status
        LinearLayout statusRow = new LinearLayout(this);
        statusRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams statusRowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        statusRowParams.setMargins(0, dpToPx(12), 0, 0);
        statusRow.setLayoutParams(statusRowParams);

        // Submission status badge
        TextView txtSubmitStatus = new TextView(this);
        txtSubmitStatus.setText(submitStatus);
        txtSubmitStatus.setTextColor(submitStatus.equals("Submitted") ? 0xFF10B981 : 0xFFFBBF24);
        txtSubmitStatus.setTextSize(12);
        txtSubmitStatus.setTypeface(null, android.graphics.Typeface.BOLD);
        txtSubmitStatus.setBackgroundColor(submitStatus.equals("Submitted") ? 0xFF064E3B : 0xFF78350F);
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

        // Grade status badge
        TextView txtGradeStatus = new TextView(this);
        txtGradeStatus.setText(gradeStatus);
        txtGradeStatus.setTextColor(gradeStatus.equals("Graded") ? 0xFF6366F1 : 0xFF9CA3B8);
        txtGradeStatus.setTextSize(12);
        txtGradeStatus.setTypeface(null, android.graphics.Typeface.BOLD);
        txtGradeStatus.setBackgroundColor(gradeStatus.equals("Graded") ? 0xFF1E3A8A : 0xFF374151);
        txtGradeStatus.setPadding(dpToPx(12), badgePadding, dpToPx(12), badgePadding);
        txtGradeStatus.setGravity(Gravity.CENTER);
        statusRow.addView(txtGradeStatus);

        card.addView(statusRow);

        // SDFR14 - Click listener to redirect to submission page
        card.setOnClickListener(view -> {
            // TODO: Create SubmissionPage.class for detailed view
            Toast.makeText(this, "Opening submission page for: " + taskTitle, Toast.LENGTH_SHORT).show();

            // Intent intent = new Intent(GradesPage.this, SubmissionPage.class);
            // intent.putExtra("taskId", taskId);
            // intent.putExtra("taskTitle", taskTitle);
            // intent.putExtra("taskType", taskType);
            // intent.putExtra("dueDate", gradedDate);
            // intent.putExtra("submitStatus", submitStatus);
            // intent.putExtra("gradeStatus", gradeStatus);
            // intent.putExtra("grade", grade);
            // intent.putExtra("maxGrade", maxGrade);
            // startActivity(intent);
        });

        gradesContainer.addView(card);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}