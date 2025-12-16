package com.matibag.presentlast;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.List;

public class attendance extends AppCompatActivity {

    TextView btnBack;
    Button btnScanQR;
    TextView txtCourseName, txtCourseInfo, txtAttendanceGrade;
    TextView tabContent, tabAttendance, tabGrades;
    LinearLayout attendanceContainer, overviewSection, overviewContainer;
    Spinner spinnerSemester, spinnerYear;

    int courseId;
    String courseName, courseCode, instructor;
    boolean isOverviewMode = false;

    // Store all attendance data for filtering
    private List<SubjectAttendanceData> allSubjectsAttendance = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendance);

        // Initialize views
        btnBack = findViewById(R.id.btnBack);
        btnScanQR = findViewById(R.id.scanButton);
        attendanceContainer = findViewById(R.id.attendanceContainer);
        overviewSection = findViewById(R.id.overviewSection);
        overviewContainer = findViewById(R.id.overviewContainer);
        txtCourseName = findViewById(R.id.txtCourseName);
        txtCourseInfo = findViewById(R.id.txtCourseInfo);
        txtAttendanceGrade = findViewById(R.id.txtAttendanceGrade);
        tabContent = findViewById(R.id.tabContent);
        tabAttendance = findViewById(R.id.tabAttendance);
        tabGrades = findViewById(R.id.tabGrades);
        spinnerSemester = findViewById(R.id.spinnerSemester);
        spinnerYear = findViewById(R.id.spinnerYear);

        // Get course data
        courseId = getIntent().getIntExtra("courseId", -1);
        courseName = getIntent().getStringExtra("courseName");
        courseCode = getIntent().getStringExtra("courseCode");
        instructor = getIntent().getStringExtra("instructor");
        isOverviewMode = getIntent().getBooleanExtra("overviewMode", false);

        // AFR2 - Overview Mode vs Single Subject Mode
        if (isOverviewMode) {
            setupOverviewMode();
        } else {
            setupSingleSubjectMode();
        }

        setActiveTab(tabAttendance);

        // Tab navigation
        tabContent.setOnClickListener(view -> {
            Intent intent = new Intent(attendance.this, SubjectPage.class);
            intent.putExtra("courseId", courseId);
            intent.putExtra("courseName", courseName);
            intent.putExtra("courseCode", courseCode);
            intent.putExtra("instructor", instructor);
            startActivity(intent);
            finish();
        });

        tabAttendance.setOnClickListener(view -> setActiveTab(tabAttendance));

        tabGrades.setOnClickListener(view -> {
            Intent intent = new Intent(attendance.this, GradesPage.class);
            intent.putExtra("courseId", courseId);
            intent.putExtra("courseName", courseName);
            intent.putExtra("courseCode", courseCode);
            intent.putExtra("instructor", instructor);
            startActivity(intent);
            finish();
        });


        // Back button
        btnBack.setOnClickListener(view -> finish());

    }

    private void setupOverviewMode() {
        txtCourseName.setText("Attendance Overview");
        txtCourseInfo.setText("All Subjects");
        overviewSection.setVisibility(View.VISIBLE);
        attendanceContainer.setVisibility(View.GONE);
        if (txtAttendanceGrade != null) txtAttendanceGrade.setVisibility(View.GONE);

        setupSpinners();
        loadAllSubjectsAttendance();
        displaySubjectsOverview("All Semesters", "All Years");
    }

    private void setupSingleSubjectMode() {
        if (courseName != null) txtCourseName.setText(courseName);
        if (courseCode != null) {
            String instructorName = instructor != null ? instructor : getInstructorName(courseId);
            txtCourseInfo.setText(courseCode + " • " + instructorName);
        }

        overviewSection.setVisibility(View.GONE);
        attendanceContainer.setVisibility(View.VISIBLE);

        loadSampleAttendance();
    }

    private void setupSpinners() {
        String[] semesters = {"All Semesters", "1st Semester", "2nd Semester", "Summer"};
        ArrayAdapter<String> semesterAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, semesters);
        semesterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSemester.setAdapter(semesterAdapter);

        String[] years = {"All Years", "2023", "2024", "2025"};
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);

        spinnerSemester.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterSubjectsOverview();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterSubjectsOverview();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadAllSubjectsAttendance() {
        allSubjectsAttendance.clear();

        allSubjectsAttendance.add(new SubjectAttendanceData(
                1, "CS 201", "Data Structures", "85%", 17, 3, 2, "1st Semester", "2024", 88.5));
        allSubjectsAttendance.add(new SubjectAttendanceData(
                2, "CS 202", "Database Systems", "90%", 18, 2, 1, "1st Semester", "2024", 92.5));
        allSubjectsAttendance.add(new SubjectAttendanceData(
                3, "CS 203", "Web Development", "95%", 19, 1, 0, "2nd Semester", "2024", 97.5));
        allSubjectsAttendance.add(new SubjectAttendanceData(
                4, "MA 301", "Discrete Mathematics", "80%", 16, 4, 3, "1st Semester", "2024", 82.5));
        allSubjectsAttendance.add(new SubjectAttendanceData(
                5, "CS 301", "Software Engineering", "88%", 17, 3, 1, "2nd Semester", "2024", 90.0));
    }

    private void filterSubjectsOverview() {
        String selectedSemester = spinnerSemester.getSelectedItem().toString();
        String selectedYear = spinnerYear.getSelectedItem().toString();
        displaySubjectsOverview(selectedSemester, selectedYear);
    }

    private void displaySubjectsOverview(String semester, String year) {
        overviewContainer.removeAllViews();

        int displayedCount = 0;

        for (SubjectAttendanceData data : allSubjectsAttendance) {
            boolean semesterMatch = semester.equals("All Semesters") || data.semester.equals(semester);
            boolean yearMatch = year.equals("All Years") || data.year.equals(year);

            if (semesterMatch && yearMatch) {
                addSubjectOverviewCard(data);
                displayedCount++;
            }
        }

        if (displayedCount == 0) {
            TextView emptyText = new TextView(this);
            emptyText.setText("No subjects found for selected filters");
            emptyText.setTextColor(0xFF9CA3B8);
            emptyText.setTextSize(16);
            emptyText.setGravity(Gravity.CENTER);
            emptyText.setPadding(16, 64, 16, 16);
            overviewContainer.addView(emptyText);
        }
    }

    private void addSubjectOverviewCard(SubjectAttendanceData data) {
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
        topRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        LinearLayout leftSection = new LinearLayout(this);
        leftSection.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        leftParams.weight = 1;
        leftSection.setLayoutParams(leftParams);

        TextView txtSubjectName = new TextView(this);
        txtSubjectName.setText(data.subjectName);
        txtSubjectName.setTextColor(Color.WHITE);
        txtSubjectName.setTextSize(18);
        txtSubjectName.setTypeface(null, android.graphics.Typeface.BOLD);
        leftSection.addView(txtSubjectName);

        TextView txtSubjectCode = new TextView(this);
        txtSubjectCode.setText(data.subjectCode);
        txtSubjectCode.setTextColor(0xFF94A3B8);
        txtSubjectCode.setTextSize(14);
        LinearLayout.LayoutParams codeParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        codeParams.setMargins(0, dpToPx(4), 0, 0);
        txtSubjectCode.setLayoutParams(codeParams);
        leftSection.addView(txtSubjectCode);

        topRow.addView(leftSection);

        // Right side: Attendance percentage and grade
        LinearLayout rightSection = new LinearLayout(this);
        rightSection.setOrientation(LinearLayout.VERTICAL);
        rightSection.setGravity(Gravity.END);
        rightSection.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        TextView txtOverallAttendance = new TextView(this);
        txtOverallAttendance.setText(data.overallAttendance);
        txtOverallAttendance.setTextColor(0xFF10B981);
        txtOverallAttendance.setTextSize(32);
        txtOverallAttendance.setTypeface(null, android.graphics.Typeface.BOLD);
        rightSection.addView(txtOverallAttendance);

        TextView txtGrade = new TextView(this);
        txtGrade.setText(String.format("Grade: %.1f%%", data.attendanceGrade));
        txtGrade.setTextColor(getGradeColor(data.attendanceGrade));
        txtGrade.setTextSize(14);
        txtGrade.setTypeface(null, android.graphics.Typeface.BOLD);
        rightSection.addView(txtGrade);

        topRow.addView(rightSection);

        card.addView(topRow);

        LinearLayout statsRow = new LinearLayout(this);
        statsRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams statsParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        statsParams.setMargins(0, dpToPx(16), 0, 0);
        statsRow.setLayoutParams(statsParams);

        TextView txtPresent = new TextView(this);
        txtPresent.setText("✓ Present: " + data.presentCount + " (100%)");
        txtPresent.setTextColor(0xFF10B981);
        txtPresent.setTextSize(13);
        LinearLayout.LayoutParams presentParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        presentParams.weight = 1;
        txtPresent.setLayoutParams(presentParams);
        statsRow.addView(txtPresent);

        TextView txtLate = new TextView(this);
        txtLate.setText("⚠ Late: " + data.lateCount + " (50%)");
        txtLate.setTextColor(0xFFFFA500);
        txtLate.setTextSize(13);
        LinearLayout.LayoutParams lateParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        lateParams.weight = 1;
        txtLate.setLayoutParams(lateParams);
        statsRow.addView(txtLate);

        TextView txtAbsent = new TextView(this);
        txtAbsent.setText("✗ Absent: " + data.absentCount + " (0%)");
        txtAbsent.setTextColor(0xFFEF4444);
        txtAbsent.setTextSize(13);
        LinearLayout.LayoutParams absentParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        absentParams.weight = 1;
        txtAbsent.setLayoutParams(absentParams);
        statsRow.addView(txtAbsent);

        card.addView(statsRow);

        card.setOnClickListener(view -> {
            Intent intent = new Intent(attendance.this, attendance.class);
            intent.putExtra("courseId", data.courseId);
            intent.putExtra("courseName", data.subjectName);
            intent.putExtra("courseCode", data.subjectCode);
            intent.putExtra("overviewMode", false);
            startActivity(intent);
        });

        overviewContainer.addView(card);
    }

    private String getInstructorName(int courseId) {
        String[] instructors = {"Dr. Smith", "Prof. Johnson", "Dr. Williams", "Dr. Brown", "Prof. Davis"};
        return (courseId >= 1 && courseId <= instructors.length)
                ? instructors[courseId - 1]
                : "Dr. Unknown";
    }

    private void setActiveTab(TextView activeTab) {
        tabContent.setTextColor(0xFF94A3B8);
        tabAttendance.setTextColor(0xFF94A3B8);
        tabGrades.setTextColor(0xFF94A3B8);

        activeTab.setTextColor(0xFF6366F1);
    }

    private void loadSampleAttendance() {
        String[][] attendanceData = {
                {"2024-12-01", "Present"},
                {"2024-12-03", "Present"},
                {"2024-12-05", "Absent"},
                {"2024-12-08", "Present"},
                {"2024-12-10", "Late"},
                {"2024-12-12", "Present"},
                {"2024-12-15", "Present"}
        };

        int totalPresent = 0, totalLate = 0, totalAbsent = 0;
        double totalGradePoints = 0;

        for (String[] record : attendanceData) {
            addAttendanceRow(record[0], record[1]);

            switch (record[1]) {
                case "Present":
                    totalPresent++;
                    totalGradePoints += 100;
                    break;
                case "Late":
                    totalLate++;
                    totalGradePoints += 50;
                    break;
                case "Absent":
                    totalAbsent++;
                    totalGradePoints += 0;
                    break;
            }
        }

        // Calculate attendance grade
        int totalDays = attendanceData.length;
        double attendanceGrade = totalGradePoints / totalDays;

        // Display attendance grade
        if (txtAttendanceGrade != null) {
            txtAttendanceGrade.setText(String.format("Attendance Grade: %.1f%%", attendanceGrade));
            txtAttendanceGrade.setTextColor(getGradeColor(attendanceGrade));
            txtAttendanceGrade.setTextSize(18);
            txtAttendanceGrade.setTypeface(null, android.graphics.Typeface.BOLD);
            txtAttendanceGrade.setVisibility(View.VISIBLE);
        }
    }

    private void addAttendanceRow(String date, String status) {
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

        TextView txtDate = new TextView(this);
        txtDate.setText(date);
        txtDate.setTextColor(Color.WHITE);
        txtDate.setTextSize(16);
        LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        txtDate.setLayoutParams(dateParams);
        row.addView(txtDate);

        TextView txtStatus = new TextView(this);
        txtStatus.setTextSize(16);
        txtStatus.setTypeface(null, android.graphics.Typeface.BOLD);

        int gradePercentage = 0;
        switch (status) {
            case "Present":
                txtStatus.setTextColor(0xFF10B981);
                gradePercentage = 100;
                break;
            case "Absent":
                txtStatus.setTextColor(0xFFEF4444);
                gradePercentage = 0;
                break;
            case "Late":
                txtStatus.setTextColor(0xFFFFA500);
                gradePercentage = 50;
                break;
            default:
                txtStatus.setTextColor(0xFF94A3B8);
                gradePercentage = 0;
                break;
        }

        txtStatus.setText(status + " (" + gradePercentage + "%)");

        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        txtStatus.setLayoutParams(statusParams);
        row.addView(txtStatus);

        attendanceContainer.addView(row);
    }

    private int getGradeColor(double grade) {
        if (grade >= 90) {
            return 0xFF10B981; // Green
        } else if (grade >= 75) {
            return 0xFFFFA500; // Orange
        } else {
            return 0xFFEF4444; // Red
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void markAttendance(String scannedData) {
        Toast.makeText(this, "✓ Attendance marked successfully!", Toast.LENGTH_SHORT).show();
        attendanceContainer.removeAllViews();
        loadSampleAttendance();
    }

    private class SubjectAttendanceData {
        int courseId;
        String subjectCode;
        String subjectName;
        String overallAttendance;
        int presentCount;
        int absentCount;
        int lateCount;
        String semester;
        String year;
        double attendanceGrade;

        SubjectAttendanceData(int courseId, String subjectCode, String subjectName,
                              String overallAttendance, int presentCount, int absentCount,
                              int lateCount, String semester, String year, double attendanceGrade) {
            this.courseId = courseId;
            this.subjectCode = subjectCode;
            this.subjectName = subjectName;
            this.overallAttendance = overallAttendance;
            this.presentCount = presentCount;
            this.absentCount = absentCount;
            this.lateCount = lateCount;
            this.semester = semester;
            this.year = year;
            this.attendanceGrade = attendanceGrade;
        }
    }
}