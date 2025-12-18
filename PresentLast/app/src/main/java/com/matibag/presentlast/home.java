package com.matibag.presentlast;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.journeyapps.barcodescanner.ScanOptions;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;

public class home extends Activity {
    TextView tabRecent, tabToday, tabUpcoming;
    ImageView PROFILE;
    Button HOME, COURSE, GRADES, ATTENDANCE,btnScanQR; // Added COURSE button
    LinearLayout updatesContainer;
    private ActivityResultLauncher<ScanOptions> barcodeLauncher;
    //    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(
    //            new ScanContract(),
    //            result -> {
    //                if(result.getContents() != null) {
    //                    Toast.makeText(attendance.this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
    //                    markAttendance(result.getContents());
    //                }
    //            }
    //    );
    private String currentTab = "Recent";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        btnScanQR = findViewById(R.id.scanButton);
        // Initialize views
        tabRecent = findViewById(R.id.tabRecent);
        tabToday = findViewById(R.id.tabToday);
        tabUpcoming = findViewById(R.id.tabUpcoming);
        updatesContainer = findViewById(R.id.updatesContainer);
        HOME = findViewById(R.id.home);
        COURSE = findViewById(R.id.course); // Initialize COURSE button
        GRADES = findViewById(R.id.Grades);
        ATTENDANCE = findViewById(R.id.attendance);
        PROFILE = findViewById(R.id.imgLogo);

        PROFILE.setOnClickListener(view -> {
            Intent callMainT = new Intent(home.this, setting.class);
            startActivity(callMainT);
        });

        HOME.setOnClickListener(view -> {
            // Already on activity_home, no need to restart
            // Just refresh if needed
        });

        COURSE.setOnClickListener(view -> {
            Intent callMain = new Intent(home.this, course.class);
            startActivity(callMain);
            finish();
        });

        GRADES.setOnClickListener(view -> {
            Intent callMainT = new Intent(home.this, GradesOverView.class);
            startActivity(callMainT);
            finish();
        });

        ATTENDANCE.setOnClickListener(view -> {
            Intent callMainT = new Intent(home.this, AttendanceOverview.class);
            startActivity(callMainT);
            finish();
        });

        // Tab navigation
        tabRecent.setOnClickListener(view -> {
            setActiveTab(tabRecent, "Recent");
            loadUpdates("Recent");
        });

        tabToday.setOnClickListener(view -> {
            setActiveTab(tabToday, "Today");
            loadUpdates("Today");
        });

        tabUpcoming.setOnClickListener(view -> {
            setActiveTab(tabUpcoming, "Upcoming");
            loadUpdates("Upcoming");
        });

        // Load initial updates (Recent)
        loadUpdates("Recent");
    }

    private void setActiveTab(TextView activeTab, String tabName) {
        currentTab = tabName;

        // Reset all tabs
        tabRecent.setTextColor(0xFF94A3B8);
        tabRecent.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabToday.setTextColor(0xFF94A3B8);
        tabToday.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabUpcoming.setTextColor(0xFF94A3B8);
        tabUpcoming.setTypeface(null, android.graphics.Typeface.NORMAL);

        // Set active tab
        activeTab.setTextColor(0xFF6366F1);
        activeTab.setTypeface(null, android.graphics.Typeface.BOLD);
    }

    private void loadUpdates(String category) {
        updatesContainer.removeAllViews();

        // TODO: Load from database based on category
        // Sample data
        if (category.equals("Recent")) {
            addTaskUpdate("CS101", "Introduction to Programming", "Quiz 1: Functions and Loops",
                    "2024-12-15 11:59 PM", "task", "2 hours ago");
            addGradeUpdate("MATH201", "Calculus II", "Assignment 2: Derivatives",
                    "95/100", "A", "5 hours ago");
            addAttendanceUpdate("ENG102", "English Composition", "Present", "Today at 9:00 AM");
            addTaskUpdate("PHY301", "Physics III", "Lab Report 3",
                    "2024-12-18 11:59 PM", "task", "Yesterday");
            addGradeUpdate("CHEM101", "General Chemistry", "Midterm Exam",
                    "88/100", "B+", "2 days ago");
        } else if (category.equals("Today")) {
            addTaskUpdate("CS101", "Introduction to Programming", "Homework 5: Arrays",
                    "2024-12-15 11:59 PM", "task", "Due today");
            addAttendanceUpdate("MATH201", "Calculus II", "Present", "Today at 8:00 AM");
            addAttendanceUpdate("ENG102", "English Composition", "Present", "Today at 9:00 AM");
        } else if (category.equals("Upcoming")) {
            addTaskUpdate("CS101", "Introduction to Programming", "Final Project",
                    "2024-12-20 11:59 PM", "task", "Due in 5 days");
            addTaskUpdate("MATH201", "Calculus II", "Quiz 3: Integrals",
                    "2024-12-18 11:59 PM", "task", "Due in 3 days");
            addTaskUpdate("PHY301", "Physics III", "Lab Report 4",
                    "2024-12-22 11:59 PM", "task", "Due in 7 days");
        }
    }

    private void addTaskUpdate(String courseCode, String courseName, String taskTitle,
                               String dueDate, String type, String timeAgo) {
        // Update card container
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(0xFF1E293B);
        int padding = dpToPx(16);
        card.setPadding(padding, padding, padding, padding);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dpToPx(12));
        card.setLayoutParams(cardParams);
        card.setClickable(true);
        card.setFocusable(true);

        // Header row (icon + category + time)
        LinearLayout headerRow = new LinearLayout(this);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);
        headerRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Icon
        TextView icon = new TextView(this);
        icon.setText("📋");
        icon.setTextSize(20);
        headerRow.addView(icon);

        // Category label
        TextView lblCategory = new TextView(this);
        lblCategory.setText("  NEW TASK");
        lblCategory.setTextColor(0xFF6366F1);
        lblCategory.setTextSize(12);
        lblCategory.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams catParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        catParams.weight = 1;
        lblCategory.setLayoutParams(catParams);
        headerRow.addView(lblCategory);

        // Time ago
        TextView txtTimeAgo = new TextView(this);
        txtTimeAgo.setText(timeAgo);
        txtTimeAgo.setTextColor(0xFF94A3B8);
        txtTimeAgo.setTextSize(12);
        headerRow.addView(txtTimeAgo);

        card.addView(headerRow);

        // Subject name
        TextView txtSubject = new TextView(this);
        txtSubject.setText(courseCode + " - " + courseName);
        txtSubject.setTextColor(0xFF94A3B8);
        txtSubject.setTextSize(14);
        LinearLayout.LayoutParams subjectParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        subjectParams.setMargins(0, dpToPx(8), 0, dpToPx(4));
        txtSubject.setLayoutParams(subjectParams);
        card.addView(txtSubject);

        // Task title
        TextView txtTitle = new TextView(this);
        txtTitle.setText(taskTitle);
        txtTitle.setTextColor(Color.WHITE);
        txtTitle.setTextSize(18);
        txtTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(txtTitle);

        // Due date
        TextView txtDueDate = new TextView(this);
        txtDueDate.setText("Due: " + dueDate);
        txtDueDate.setTextColor(0xFFFBBF24);
        txtDueDate.setTextSize(14);
        LinearLayout.LayoutParams dueDateParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        dueDateParams.setMargins(0, dpToPx(8), 0, 0);
        txtDueDate.setLayoutParams(dueDateParams);
        card.addView(txtDueDate);

        // Click listener - redirect to submission page
        card.setOnClickListener(view -> {
            Intent intent = new Intent(home.this, SubmissionPage.class);
            intent.putExtra("taskId", 1); // TODO: Get actual task ID
            intent.putExtra("taskTitle", taskTitle);
            intent.putExtra("dueDate", dueDate);
            intent.putExtra("submitStatus", "Not Submitted");
            intent.putExtra("gradeStatus", "Not Graded");
            startActivity(intent);
        });

        updatesContainer.addView(card);
    }

    private void addGradeUpdate(String courseCode, String courseName, String taskTitle,
                                String grade, String letterGrade, String timeAgo) {
        // Update card container
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(0xFF1E293B);
        int padding = dpToPx(16);
        card.setPadding(padding, padding, padding, padding);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dpToPx(12));
        card.setLayoutParams(cardParams);

        // Header row
        LinearLayout headerRow = new LinearLayout(this);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);
        headerRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Icon
        TextView icon = new TextView(this);
        icon.setText("📊");
        icon.setTextSize(20);
        headerRow.addView(icon);

        // Category label
        TextView lblCategory = new TextView(this);
        lblCategory.setText("  NEW GRADE");
        lblCategory.setTextColor(0xFF10B981);
        lblCategory.setTextSize(12);
        lblCategory.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams catParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        catParams.weight = 1;
        lblCategory.setLayoutParams(catParams);
        headerRow.addView(lblCategory);

        // Time ago
        TextView txtTimeAgo = new TextView(this);
        txtTimeAgo.setText(timeAgo);
        txtTimeAgo.setTextColor(0xFF94A3B8);
        txtTimeAgo.setTextSize(12);
        headerRow.addView(txtTimeAgo);

        card.addView(headerRow);

        // Subject name
        TextView txtSubject = new TextView(this);
        txtSubject.setText(courseCode + " - " + courseName);
        txtSubject.setTextColor(0xFF94A3B8);
        txtSubject.setTextSize(14);
        LinearLayout.LayoutParams subjectParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        subjectParams.setMargins(0, dpToPx(8), 0, dpToPx(4));
        txtSubject.setLayoutParams(subjectParams);
        card.addView(txtSubject);

        // Task title
        TextView txtTitle = new TextView(this);
        txtTitle.setText(taskTitle);
        txtTitle.setTextColor(Color.WHITE);
        txtTitle.setTextSize(18);
        txtTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(txtTitle);

        // Grade
        TextView txtGrade = new TextView(this);
        txtGrade.setText("Grade: " + grade + " (" + letterGrade + ")");
        txtGrade.setTextColor(0xFF10B981);
        txtGrade.setTextSize(16);
        txtGrade.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams gradeParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        gradeParams.setMargins(0, dpToPx(8), 0, 0);
        txtGrade.setLayoutParams(gradeParams);
        card.addView(txtGrade);

        updatesContainer.addView(card);
    }

    private void addAttendanceUpdate(String courseCode, String courseName,
                                     String status, String timeAgo) {
        // Update card container
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(0xFF1E293B);
        int padding = dpToPx(16);
        card.setPadding(padding, padding, padding, padding);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dpToPx(12));
        card.setLayoutParams(cardParams);

        // Header row
        LinearLayout headerRow = new LinearLayout(this);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);
        headerRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Icon
        TextView icon = new TextView(this);
        icon.setText("✓");
        icon.setTextSize(20);
        icon.setTextColor(0xFF10B981);
        headerRow.addView(icon);

        // Category label
        TextView lblCategory = new TextView(this);
        lblCategory.setText("  ATTENDANCE");
        lblCategory.setTextColor(0xFF10B981);
        lblCategory.setTextSize(12);
        lblCategory.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams catParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        catParams.weight = 1;
        lblCategory.setLayoutParams(catParams);
        headerRow.addView(lblCategory);

        // Time ago
        TextView txtTimeAgo = new TextView(this);
        txtTimeAgo.setText(timeAgo);
        txtTimeAgo.setTextColor(0xFF94A3B8);
        txtTimeAgo.setTextSize(12);
        headerRow.addView(txtTimeAgo);

        card.addView(headerRow);

        // Subject name
        TextView txtSubject = new TextView(this);
        txtSubject.setText(courseCode + " - " + courseName);
        txtSubject.setTextColor(0xFF94A3B8);
        txtSubject.setTextSize(14);
        LinearLayout.LayoutParams subjectParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        subjectParams.setMargins(0, dpToPx(8), 0, dpToPx(4));
        txtSubject.setLayoutParams(subjectParams);
        card.addView(txtSubject);

        // Status
        TextView txtStatus = new TextView(this);
        txtStatus.setText("Marked as " + status);
        txtStatus.setTextColor(Color.WHITE);
        txtStatus.setTextSize(18);
        txtStatus.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(txtStatus);

        updatesContainer.addView(card);
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