package com.matibag.presentlast;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class SubjectPage extends Activity {
    TextView btnBack, txtCourseName, txtCourseInfo;
    TextView tabContent, tabAttendance, tabGrades;
    TextView btnJoinSession;
    LinearLayout tasksContainer;

    int courseId;
    String courseName, courseCode, instructor;
    boolean isSessionActive = false; // TODO: Get from database

    private static final int PICK_FILE_REQUEST = 1;
    private int currentTaskId = -1;
    private LinearLayout currentTaskCard = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subject);

        // Initialize views
        btnBack = findViewById(R.id.btnBack);
        txtCourseName = findViewById(R.id.txtCourseName);
        txtCourseInfo = findViewById(R.id.txtCourseInfo);
        btnJoinSession = findViewById(R.id.btnJoinSession);
        tabContent = findViewById(R.id.tabContent);
        tabAttendance = findViewById(R.id.tabAttendance);
        tabGrades = findViewById(R.id.tabGrades);
        tasksContainer = findViewById(R.id.tasksContainer);

        // Get course info from intent
        courseId = getIntent().getIntExtra("courseId", -1);
        courseName = getIntent().getStringExtra("courseName");
        courseCode = getIntent().getStringExtra("courseCode");
        instructor = getIntent().getStringExtra("instructor");

        // Set header info
        txtCourseName.setText(courseName);
        txtCourseInfo.setText(courseCode + " • " + instructor);

        // Check if session is active
        checkSessionStatus();

        // Back button
        btnBack.setOnClickListener(view -> finish());

        // SDFR7 - Join Session button
        btnJoinSession.setOnClickListener(view -> {
            if (isSessionActive) {
                // Redirect to Session Page
                Intent intent = new Intent(SubjectPage.this, attendance.class);
                intent.putExtra("courseId", courseId);
                intent.putExtra("courseName", courseName);
                startActivity(intent);
            } else {
                Toast.makeText(this, "No Session At This Moment!", Toast.LENGTH_SHORT).show();
            }
        });

        // SDFR6 - Tab 1: Content
        tabContent.setOnClickListener(view -> {
            setActiveTab(tabContent);
            loadTaskFolders();
        });

        // SDFR6 - Tab 2: Attendance
        tabAttendance.setOnClickListener(view -> {
            setActiveTab(tabAttendance);
            Intent intent = new Intent(SubjectPage.this, attendance.class);
            intent.putExtra("courseId", courseId);
            intent.putExtra("courseName", courseName);
            intent.putExtra("courseCode", courseCode);
            intent.putExtra("instructor", instructor);
            startActivity(intent);
            finish();
        });

        // SDFR6 & SDFR14 - Tab 3: Grades
        tabGrades.setOnClickListener(view -> {
            setActiveTab(tabGrades);
            Intent intent = new Intent(SubjectPage.this, GradesPage.class);
            intent.putExtra("courseId", courseId);
            intent.putExtra("courseName", courseName);
            intent.putExtra("courseCode", courseCode);
            intent.putExtra("instructor", instructor);
            startActivity(intent);
            finish();
        });


        // Load initial content
        loadTaskFolders();
    }

    /**
     * SDFR7 - Check if instructor has started a session
     */
    private void checkSessionStatus() {
        // TODO: Query database for active session
        // For now, using sample data
        isSessionActive = false; // Change to true to test

        if (isSessionActive) {
            btnJoinSession.setText("📱 Join Session! (Active)");
            btnJoinSession.setBackgroundColor(0xFF10B981); // Green
        } else {
            btnJoinSession.setText("📱 No Session Available");
            btnJoinSession.setBackgroundColor(0xFF6B7280); // Gray
        }
    }

    private void setActiveTab(TextView activeTab) {
        tabContent.setTextColor(0xFF94A3B8);
        tabAttendance.setTextColor(0xFF94A3B8);
        tabGrades.setTextColor(0xFF94A3B8);

        activeTab.setTextColor(0xFF6366F1);
    }

    /**
     * SDFR8 & SDFR9 - Load task folders with dropdown functionality
     */
    private void loadTaskFolders() {
        tasksContainer.removeAllViews();

        // Sample folder structure
        String[][] folders = {
                {"Module 1 - Introduction", "5"},
                {"Module 2 - Data Structures", "3"},
                {"Module 3 - Algorithms", "4"},
                {"Final Project", "2"}
        };

        for (String[] folder : folders) {
            addFolderCard(folder[0], Integer.parseInt(folder[1]));
        }
    }

    /**
     * SDFR9 - Add folder card with dropdown
     */
    private void addFolderCard(String folderName, int taskCount) {
        // Folder container
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
        txtFolderName.setText(folderName);
        txtFolderName.setTextColor(Color.WHITE);
        txtFolderName.setTextSize(18);
        txtFolderName.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        nameParams.weight = 1;
        nameParams.setMargins(dpToPx(12), 0, 0, 0);
        txtFolderName.setLayoutParams(nameParams);
        headerRow.addView(txtFolderName);

        // Task count badge
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

        // Sample tasks in folder
        addTaskSubmissionTab(tasksInFolder, 1, "Quiz 1: Functions", "2024-12-15", "Submitted", "Graded");
        addTaskSubmissionTab(tasksInFolder, 2, "Homework 1: Arrays", "2024-12-18", "Not Submitted", "Not Graded");
        addTaskSubmissionTab(tasksInFolder, 3, "Seatwork 1: Loops", "2024-12-20", "Submitted", "Not Graded");

        folderCard.addView(tasksInFolder);

        // Toggle dropdown
        headerRow.setClickable(true);
        headerRow.setOnClickListener(view -> {
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

    /**
     * SDFR10 - Add task submission tab
     * Shows: Submission Title, Due Date/Time, Submitted/NotSubmitted, Graded/NotGraded
     */
    private void addTaskSubmissionTab(LinearLayout container, int taskId, String title,
                                      String dueDate, String submitStatus, String gradeStatus) {
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
        txtTitle.setText(title);
        txtTitle.setTextColor(Color.WHITE);
        txtTitle.setTextSize(16);
        txtTitle.setTypeface(null, android.graphics.Typeface.BOLD);
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
        TextView txtDueDate = new TextView(this);
        txtDueDate.setText("Due: " + dueDate);
        txtDueDate.setTextColor(0xFF94A3B8);
        txtDueDate.setTextSize(12);
        detailsRow.addView(txtDueDate);

        // Submit status
        TextView txtSubmitStatus = new TextView(this);
        txtSubmitStatus.setText(" • " + submitStatus);
        txtSubmitStatus.setTextColor(submitStatus.equals("Submitted") ? 0xFF10B981 : 0xFFFBBF24);
        txtSubmitStatus.setTextSize(12);
        txtSubmitStatus.setTypeface(null, android.graphics.Typeface.BOLD);
        detailsRow.addView(txtSubmitStatus);

        // Grade status
        TextView txtGradeStatus = new TextView(this);
        txtGradeStatus.setText(" • " + gradeStatus);
        txtGradeStatus.setTextColor(gradeStatus.equals("Graded") ? 0xFF6366F1 : 0xFF94A3B8);
        txtGradeStatus.setTextSize(12);
        txtGradeStatus.setTypeface(null, android.graphics.Typeface.BOLD);
        detailsRow.addView(txtGradeStatus);

        taskTab.addView(detailsRow);

        // SDFR10 - Click to open detailed submission page
        taskTab.setOnClickListener(view -> {
            Intent intent = new Intent(SubjectPage.this, SubmissionPage.class);
            intent.putExtra("taskId", taskId);
            intent.putExtra("taskTitle", title);
            intent.putExtra("dueDate", dueDate);
            intent.putExtra("submitStatus", submitStatus);
            intent.putExtra("gradeStatus", gradeStatus);
            startActivity(intent);
        });

        container.addView(taskTab);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}