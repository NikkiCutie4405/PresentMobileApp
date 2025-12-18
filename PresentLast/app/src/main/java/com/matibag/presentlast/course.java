package com.matibag.presentlast;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.matibag.presentlast.ui.HomeActivity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class course extends Activity {
    ImageView PROFILE;
    Button HOME, COURSE, GRADES, ATTENDANCE,btnScanQR;
    LinearLayout coursesContainer;
    Spinner spinnerSemester, spinnerYear;

    // Array of vibrant colors for course cards
    private final int[] cardColors = {
            0xFF2563EB, // Blue
            0xFF9333EA, // Purple
            0xFF10B981, // Green
            0xFFEF4444, // Red
            0xFFF59E0B, // Orange
            0xFF06B6D4, // Cyan
            0xFFEC4899, // Pink
            0xFF8B5CF6  // Violet
    };

    // Store all courses data
    private List<CourseData> allCourses = new ArrayList<>();
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
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.courses);
        btnScanQR = findViewById(R.id.scanButton);
        HOME = findViewById(R.id.home);
        COURSE = findViewById(R.id.course); // Initialize COURSE button
        GRADES = findViewById(R.id.Grades);
        ATTENDANCE = findViewById(R.id.attendance);
        coursesContainer = findViewById(R.id.coursesContainer);
        spinnerSemester = findViewById(R.id.spinnerSemester);
        spinnerYear = findViewById(R.id.spinnerYear);
        PROFILE = findViewById(R.id.imgLogo);
        PROFILE.setOnClickListener(view -> {
            Intent callMainT = new Intent(course.this, setting.class);
            startActivity(callMainT);
        });
        // Setup spinners with listeners
        setupSpinners();
        btnScanQR.setOnClickListener(view -> {
            ScanOptions options = new ScanOptions();
            options.setPrompt("Scan QR Code to Mark Attendance");
            options.setBeepEnabled(true);
            options.setBarcodeImageEnabled(true);
            barcodeLauncher.launch(options);
        });
        HOME.setOnClickListener(view -> {
            Intent callMainT = new Intent(course.this, HomeActivity.class);
            startActivity(callMainT);
            finish();
        });

        GRADES.setOnClickListener(view -> {
            Intent callMainT = new Intent(course.this, GradesOverView.class);
            startActivity(callMainT);
            finish();
        });

        ATTENDANCE.setOnClickListener(view -> {
            Intent callMainT = new Intent(course.this, AttendanceOverview.class);
            startActivity(callMainT);
            finish();
        });

        // Load sample courses data
        loadSampleCourses();

        // Display all courses initially
        displayCourses("All Semesters", "All Years");
    }

    private void setupSpinners() {
        // Setup semester spinner
        String[] semesters = {"All Semesters", "1st Semester", "2nd Semester", "Summer"};
        ArrayAdapter<String> semesterAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, semesters);
        semesterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSemester.setAdapter(semesterAdapter);

        // Setup year spinner
        String[] years = {"All Years", "2023", "2024", "2025"};
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);

        // Add listeners for filtering
        spinnerSemester.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterCourses();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterCourses();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    /**
     * Load sample courses with semester and year data
     * TODO: Replace with actual database query
     */
    private void loadSampleCourses() {
        allCourses.clear();

        // Sample data with semester and year
        allCourses.add(new CourseData(1, "CS 201", "Data Structures", "Dr. Maria Santos", 85, "1st Semester", "2024"));
        allCourses.add(new CourseData(2, "CS 202", "Database Systems", "Prof. Juan Dela Cruz", 72, "1st Semester", "2024"));
        allCourses.add(new CourseData(3, "CS 203", "Web Development", "Engr. Ana Reyes", 90, "2nd Semester", "2024"));
        allCourses.add(new CourseData(4, "MA 301", "Discrete Mathematics", "Dr. Ramon Lopez", 78, "1st Semester", "2024"));
        allCourses.add(new CourseData(5, "CS 301", "Software Engineering", "Engr. Maria Garcia", 88, "2nd Semester", "2024"));
        allCourses.add(new CourseData(6, "CS 101", "Introduction to Programming", "Dr. Smith", 95, "1st Semester", "2023"));
    }

    /**
     * Filter courses based on selected semester and year
     */
    private void filterCourses() {
        String selectedSemester = spinnerSemester.getSelectedItem().toString();
        String selectedYear = spinnerYear.getSelectedItem().toString();

        displayCourses(selectedSemester, selectedYear);
    }

    /**
     * Display courses filtered by semester and year
     */
    private void displayCourses(String semester, String year) {
        coursesContainer.removeAllViews();

        int colorIndex = 0;
        int displayedCount = 0;

        for (CourseData courseData : allCourses) {
            // Check if course matches filter criteria
            boolean semesterMatch = semester.equals("All Semesters") || courseData.semester.equals(semester);
            boolean yearMatch = year.equals("All Years") || courseData.year.equals(year);

            if (semesterMatch && yearMatch) {
                int cardColor = cardColors[colorIndex % cardColors.length];
                addCourseCard(courseData, cardColor);
                colorIndex++;
                displayedCount++;
            }
        }

        // Show message if no courses found
        if (displayedCount == 0) {
            TextView emptyText = new TextView(this);
            emptyText.setText("No courses found for selected filters");
            emptyText.setTextColor(0xFF9CA3AF);
            emptyText.setTextSize(16);
            emptyText.setGravity(Gravity.CENTER);
            emptyText.setPadding(16, 64, 16, 16);
            coursesContainer.addView(emptyText);
        }
    }

    private void addCourseCard(CourseData courseData, int backgroundColor) {
        // Create main card container
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(backgroundColor);
        int padding = dpToPx(20);
        card.setPadding(padding, padding, padding, padding);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dpToPx(16));
        card.setLayoutParams(cardParams);
        card.setClickable(true);
        card.setFocusable(true);

        // Top section (Course name + arrow)
        LinearLayout topSection = new LinearLayout(this);
        topSection.setOrientation(LinearLayout.HORIZONTAL);
        topSection.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Left side (course info)
        LinearLayout courseInfo = new LinearLayout(this);
        courseInfo.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        infoParams.weight = 1;
        courseInfo.setLayoutParams(infoParams);

        // Course name
        TextView txtCourseName = new TextView(this);
        txtCourseName.setText(courseData.courseName);
        txtCourseName.setTextColor(Color.WHITE);
        txtCourseName.setTextSize(22);
        txtCourseName.setTypeface(null, android.graphics.Typeface.BOLD);
        courseInfo.addView(txtCourseName);

        // Course code
        TextView txtCourseCode = new TextView(this);
        txtCourseCode.setText(courseData.courseCode);
        txtCourseCode.setTextColor(0xFFE0E7FF);
        txtCourseCode.setTextSize(14);
        LinearLayout.LayoutParams codeParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        codeParams.setMargins(0, dpToPx(4), 0, 0);
        txtCourseCode.setLayoutParams(codeParams);
        courseInfo.addView(txtCourseCode);

        topSection.addView(courseInfo);

        // Arrow
        TextView arrow = new TextView(this);
        arrow.setText("›");
        arrow.setTextColor(Color.WHITE);
        arrow.setTextSize(40);
        arrow.setGravity(Gravity.CENTER_VERTICAL);
        topSection.addView(arrow);

        card.addView(topSection);

        // Instructor label
        TextView lblInstructor = new TextView(this);
        lblInstructor.setText("INSTRUCTOR");
        lblInstructor.setTextColor(0xFFB3D1FF);
        lblInstructor.setTextSize(10);
        LinearLayout.LayoutParams lblParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        lblParams.setMargins(0, dpToPx(20), 0, 0);
        lblInstructor.setLayoutParams(lblParams);
        card.addView(lblInstructor);

        // Instructor name
        TextView txtInstructor = new TextView(this);
        txtInstructor.setText(courseData.instructor);
        txtInstructor.setTextColor(Color.WHITE);
        txtInstructor.setTextSize(16);
        LinearLayout.LayoutParams instrParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        instrParams.setMargins(0, dpToPx(4), 0, 0);
        txtInstructor.setLayoutParams(instrParams);
        card.addView(txtInstructor);

        // Progress label
        TextView lblProgress = new TextView(this);
        lblProgress.setText("Progress");
        lblProgress.setTextColor(0xFFB3D1FF);
        lblProgress.setTextSize(10);
        LinearLayout.LayoutParams progLblParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        progLblParams.setMargins(0, dpToPx(12), 0, 0);
        lblProgress.setLayoutParams(progLblParams);
        card.addView(lblProgress);

        // Progress section
        LinearLayout progressSection = new LinearLayout(this);
        progressSection.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams progSectionParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        progSectionParams.setMargins(0, dpToPx(8), 0, 0);
        progressSection.setLayoutParams(progSectionParams);

        // Progress bar
        ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(
                0,
                dpToPx(8)
        );
        barParams.weight = 1;
        barParams.gravity = Gravity.CENTER_VERTICAL;
        progressBar.setLayoutParams(barParams);
        progressBar.setProgress(courseData.progress);
        progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
        progressSection.addView(progressBar);

        // Progress percentage
        TextView txtProgress = new TextView(this);
        txtProgress.setText(courseData.progress + "%");
        txtProgress.setTextColor(Color.WHITE);
        txtProgress.setTextSize(16);
        txtProgress.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams progTextParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        progTextParams.setMargins(dpToPx(12), 0, 0, 0);
        txtProgress.setLayoutParams(progTextParams);
        progressSection.addView(txtProgress);

        card.addView(progressSection);

        // Set click listener to open subject page with tabs
        card.setOnClickListener(view -> {
            Intent intent = new Intent(course.this, SubjectPage.class);
            intent.putExtra("courseId", courseData.courseId);
            intent.putExtra("courseName", courseData.courseName);
            intent.putExtra("courseCode", courseData.courseCode);
            intent.putExtra("instructor", courseData.instructor);
            startActivity(intent);
        });

        // Add card to container
        coursesContainer.addView(card);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    /**
     * Inner class to store course data
     */
    private class CourseData {
        int courseId;
        String courseCode;
        String courseName;
        String instructor;
        int progress;
        String semester;
        String year;

        CourseData(int courseId, String courseCode, String courseName, String instructor,
                   int progress, String semester, String year) {
            this.courseId = courseId;
            this.courseCode = courseCode;
            this.courseName = courseName;
            this.instructor = instructor;
            this.progress = progress;
            this.semester = semester;
            this.year = year;
        }
    }
}