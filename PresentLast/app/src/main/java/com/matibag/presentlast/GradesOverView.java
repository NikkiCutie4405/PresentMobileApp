package com.matibag.presentlast;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import androidx.activity.result.ActivityResultCallerLauncher;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;

public class GradesOverView extends Activity {
    Button HOME, COURSE, GRADES, ATTENDANCE,btnScanQR;
    ImageView PROFILE;
    LinearLayout subjectsContainer;
    TextView txtOverallGPA, txtGradeStats;
    private ActivityResultLauncher<ScanOptions> barcodeLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.overview);

        // Initialize views
        btnScanQR = findViewById(R.id.scanButton);
        subjectsContainer = findViewById(R.id.subjectsContainer);
        txtOverallGPA = findViewById(R.id.txtOverallGPA);
        txtGradeStats = findViewById(R.id.txtGradeStats);
        COURSE = findViewById(R.id.course );
        HOME = findViewById(R.id.home);
        COURSE = findViewById(R.id.course); // Initialize COURSE button
        GRADES = findViewById(R.id.Grades);
        ATTENDANCE = findViewById(R.id.attendance);
        PROFILE = findViewById(R.id.imgLogo);

        PROFILE.setOnClickListener(view -> {
            Intent callMainT = new Intent(GradesOverView.this, setting.class);
            startActivity(callMainT);
        });

        HOME.setOnClickListener(view -> {
            Intent callMain = new Intent(GradesOverView.this, home.class);
            startActivity(callMain);
            finish();
        });
        btnScanQR.setOnClickListener(view -> {
            ScanOptions options = new ScanOptions();
            options.setPrompt("Scan QR Code to Mark Attendance");
            options.setBeepEnabled(true);
            options.setBarcodeImageEnabled(true);
            barcodeLauncher.launch(options);
        });
        COURSE.setOnClickListener(view -> {
            Intent callMain = new Intent(GradesOverView.this, course.class);
            startActivity(callMain);
            finish();
        });

        ATTENDANCE.setOnClickListener(view -> {
            Intent callMainT = new Intent(GradesOverView.this, AttendanceOverview.class);
            startActivity(callMainT);
            finish();
        });

        GRADES.setOnClickListener(view -> {
            Intent callMainT = new Intent(GradesOverView.this, GradesOverView.class);
            startActivity(callMainT);
            finish();
        });
        // Calculate and display overall GPA
        calculateOverallGPA();

        // Load subjects with grades
        loadSubjectsGrades();
    }

    private void calculateOverallGPA() {
        // TODO: Query from database
        // Sample calculation
        double totalGPA = 17.5; // Sum of all GPAs
        int totalSubjects = 5;

        double overallGPA = totalGPA / totalSubjects;

        txtOverallGPA.setText(String.format("%.1f", overallGPA));
        txtGradeStats.setText("Based on " + totalSubjects + " subjects");

        // Set color based on GPA
        if (overallGPA >= 3.5) {
            txtOverallGPA.setTextColor(0xFF10B981); // Green
        } else if (overallGPA >= 2.5) {
            txtOverallGPA.setTextColor(0xFFFBBF24); // Yellow
        } else {
            txtOverallGPA.setTextColor(0xFFEF4444); // Red
        }
    }

    private void loadSubjectsGrades() {
        // Sample data - TODO: Load from database
        String[][] subjectsData = {
                {"1", "CS101", "Introduction to Programming", "Dr. Smith", "92", "1.25", "A"},
                {"2", "MATH201", "Calculus II", "Prof. Johnson", "88", "1.75", "B+"},
                {"3", "ENG102", "English Composition", "Dr. Williams", "95", "1.0", "A"},
                {"4", "PHY301", "Physics III", "Dr. Brown", "78", "2.75", "B-"},
                {"5", "CHEM101", "General Chemistry", "Prof. Davis", "85", "2.0", "B"}
        };

        for (String[] subject : subjectsData) {
            int courseId = Integer.parseInt(subject[0]);
            String courseCode = subject[1];
            String courseName = subject[2];
            String instructor = subject[3];
            int numericGrade = Integer.parseInt(subject[4]);
            double gpa = Double.parseDouble(subject[5]);
            String letterGrade = subject[6];

            addSubjectGradeCard(courseId, courseCode, courseName, instructor,
                    numericGrade, gpa, letterGrade);
        }
    }

    private void addSubjectGradeCard(int courseId, String courseCode, String courseName,
                                     String instructor, int numericGrade,
                                     double gpa, String letterGrade) {
        // Card container
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

        // Top row: Course info and grade
        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        // Left: Course info
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
        txtCourseName.setText(courseName);
        txtCourseName.setTextColor(Color.WHITE);
        txtCourseName.setTextSize(18);
        txtCourseName.setTypeface(null, android.graphics.Typeface.BOLD);
        courseInfo.addView(txtCourseName);

        // Course code
        TextView txtCourseCode = new TextView(this);
        txtCourseCode.setText(courseCode + " • " + instructor);
        txtCourseCode.setTextColor(0xFF94A3B8);
        txtCourseCode.setTextSize(14);
        LinearLayout.LayoutParams codeParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        codeParams.setMargins(0, dpToPx(4), 0, 0);
        txtCourseCode.setLayoutParams(codeParams);
        courseInfo.addView(txtCourseCode);

        topRow.addView(courseInfo);

        // Right: Letter grade
        TextView txtLetterGrade = new TextView(this);
        txtLetterGrade.setText(letterGrade);
        txtLetterGrade.setTextSize(40);
        txtLetterGrade.setTypeface(null, android.graphics.Typeface.BOLD);

        // Set color based on grade
        if (numericGrade >= 90) {
            txtLetterGrade.setTextColor(0xFF10B981); // Green - A
        } else if (numericGrade >= 80) {
            txtLetterGrade.setTextColor(0xFF6366F1); // Blue - B
        } else if (numericGrade >= 70) {
            txtLetterGrade.setTextColor(0xFFFBBF24); // Yellow - C
        } else {
            txtLetterGrade.setTextColor(0xFFEF4444); // Red - D/F
        }

        topRow.addView(txtLetterGrade);
        card.addView(topRow);

        // Grade details row
        LinearLayout gradeDetailsRow = new LinearLayout(this);
        gradeDetailsRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams detailsParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        detailsParams.setMargins(0, dpToPx(12), 0, 0);
        gradeDetailsRow.setLayoutParams(detailsParams);

        // Numeric grade
        TextView txtNumericGrade = new TextView(this);
        txtNumericGrade.setText("Grade: " + numericGrade + "%");
        txtNumericGrade.setTextColor(0xFF94A3B8);
        txtNumericGrade.setTextSize(14);
        gradeDetailsRow.addView(txtNumericGrade);

        // GPA
        TextView txtGPA = new TextView(this);
        txtGPA.setText(" • GPA: " + String.format("%.2f", gpa));
        txtGPA.setTextColor(0xFF94A3B8);
        txtGPA.setTextSize(14);
        gradeDetailsRow.addView(txtGPA);

        card.addView(gradeDetailsRow);

        // Progress bar
        ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(8)
        );
        barParams.setMargins(0, dpToPx(12), 0, 0);
        progressBar.setLayoutParams(barParams);
        progressBar.setProgress(numericGrade);
        progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(
                numericGrade >= 90 ? 0xFF10B981 : (numericGrade >= 80 ? 0xFF6366F1 : (numericGrade >= 70 ? 0xFFFBBF24 : 0xFFEF4444))
        ));
        card.addView(progressBar);

        // Click listener to open subject grade page
        card.setOnClickListener(view -> {
            Intent intent = new Intent(GradesOverView.this, GradesPage.class);
            intent.putExtra("courseId", courseId);
            intent.putExtra("courseName", courseName);
            intent.putExtra("courseCode", courseCode);
            intent.putExtra("instructor", instructor);
            startActivity(intent);
        });

        subjectsContainer.addView(card);
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