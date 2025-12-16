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

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;

public class AttendanceOverview extends Activity {
    Button HOME, COURSE, GRADES, ATTENDANCE,btnScanQR;
    ImageView PROFILE;
    LinearLayout subjectsContainer;
    TextView txtOverallAttendance, txtPresentDays, txtAbsentDays, txtLateDays, txtTotalDays, txtOverallGrade;
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
        setContentView(R.layout.attendance_overview);

        // Initialize views
        HOME = findViewById(R.id.home);
        COURSE = findViewById(R.id.course);
        GRADES = findViewById(R.id.Grades);
        ATTENDANCE = findViewById(R.id.attendance);
        btnScanQR = findViewById(R.id.scanButton);
        PROFILE = findViewById(R.id.imgLogo);
        subjectsContainer = findViewById(R.id.subjectsContainer);
        txtOverallAttendance = findViewById(R.id.txtOverallAttendance);
        txtPresentDays = findViewById(R.id.txtPresentDays);
        txtAbsentDays = findViewById(R.id.txtAbsentDays);
        txtLateDays = findViewById(R.id.txtLateDays);
        txtTotalDays = findViewById(R.id.txtTotalDays);
        txtOverallGrade = findViewById(R.id.txtOverallGrade);
        btnScanQR.setOnClickListener(view -> {
            ScanOptions options = new ScanOptions();
            options.setPrompt("Scan QR Code to Mark Attendance");
            options.setBeepEnabled(true);
            options.setBarcodeImageEnabled(true);
            barcodeLauncher.launch(options);
        });
        // Navigation listeners
        PROFILE.setOnClickListener(view -> {
            Intent intent = new Intent(AttendanceOverview.this, setting.class);
            startActivity(intent);
        });

        HOME.setOnClickListener(view -> {
            Intent intent = new Intent(AttendanceOverview.this, home.class);
            startActivity(intent);
            finish();
        });

        COURSE.setOnClickListener(view -> {
            Intent intent = new Intent(AttendanceOverview.this, course.class);
            startActivity(intent);
            finish();
        });

        GRADES.setOnClickListener(view -> {
            Intent intent = new Intent(AttendanceOverview.this, GradesOverView.class);
            startActivity(intent);
            finish();
        });

        ATTENDANCE.setOnClickListener(view -> {
            Intent intent = new Intent(AttendanceOverview.this, AttendanceOverview.class);
            startActivity(intent);
            finish();
        });

        // Calculate and display overall attendance
        calculateOverallAttendance();

        // Load subjects with attendance
        loadSubjectsAttendance();
    }

    private void calculateOverallAttendance() {
        // TODO: Query from database
        // Sample calculation with Late attendance
        int totalPresent = 85;
        int totalLate = 7;
        int totalAbsent = 8;
        int totalDays = totalPresent + totalLate + totalAbsent;

        // Calculate attendance percentage (Present + Late)
        double attendancePercentage = ((totalPresent + totalLate) * 100.0) / totalDays;

        // Calculate attendance grade (Present = 100%, Late = 50%, Absent = 0%)
        double attendanceGrade = ((totalPresent * 100.0) + (totalLate * 50.0)) / totalDays;

        txtOverallAttendance.setText(String.format("%.0f%%", attendancePercentage));
        txtPresentDays.setText("Present: " + totalPresent + " days (100%)");
        txtLateDays.setText("Late: " + totalLate + " days (50%)");
        txtAbsentDays.setText("Absent: " + totalAbsent + " days (0%)");
        txtTotalDays.setText("Total: " + totalDays + " days");

        // Display attendance grade
        if (txtOverallGrade != null) {
            txtOverallGrade.setText(String.format("Overall Grade: %.1f%%", attendanceGrade));
            txtOverallGrade.setTextColor(getGradeColor(attendanceGrade));
        }

        // Set color based on attendance grade
        if (attendanceGrade >= 90) {
            txtOverallAttendance.setTextColor(0xFF10B981); // Green
        } else if (attendanceGrade >= 75) {
            txtOverallAttendance.setTextColor(0xFFFFA500); // Orange
        } else {
            txtOverallAttendance.setTextColor(0xFFEF4444); // Red
        }
    }

    private void loadSubjectsAttendance() {
        // Sample data with Late attendance - TODO: Load from database
        String[][] subjectsData = {
                {"1", "CS101", "Introduction to Programming", "Dr. Smith", "17", "1", "2", "20"},
                {"2", "MATH201", "Calculus II", "Prof. Johnson", "15", "2", "3", "20"},
                {"3", "ENG102", "English Composition", "Dr. Williams", "18", "1", "1", "20"},
                {"4", "PHY301", "Physics III", "Dr. Brown", "14", "1", "5", "20"},
                {"5", "CHEM101", "General Chemistry", "Prof. Davis", "19", "0", "1", "20"}
        };

        for (String[] subject : subjectsData) {
            int courseId = Integer.parseInt(subject[0]);
            String courseCode = subject[1];
            String courseName = subject[2];
            String instructor = subject[3];
            int present = Integer.parseInt(subject[4]);
            int late = Integer.parseInt(subject[5]);
            int absent = Integer.parseInt(subject[6]);
            int total = Integer.parseInt(subject[7]);

            // Calculate attendance percentage (Present + Late)
            int percentage = ((present + late) * 100) / total;

            // Calculate attendance grade (Present = 100%, Late = 50%, Absent = 0%)
            double attendanceGrade = ((present * 100.0) + (late * 50.0)) / total;

            addSubjectAttendanceCard(courseId, courseCode, courseName, instructor,
                    present, late, absent, total, percentage, attendanceGrade);
        }
    }

    private void addSubjectAttendanceCard(int courseId, String courseCode, String courseName,
                                          String instructor, int present, int late, int absent,
                                          int total, int percentage, double attendanceGrade) {
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

        // Top row: Course info and percentage
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

        // Course code and instructor
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

        // Right: Attendance percentage and grade
        LinearLayout rightSection = new LinearLayout(this);
        rightSection.setOrientation(LinearLayout.VERTICAL);
        rightSection.setGravity(Gravity.END);

        TextView txtPercentage = new TextView(this);
        txtPercentage.setText(percentage + "%");
        txtPercentage.setTextSize(40);
        txtPercentage.setTypeface(null, android.graphics.Typeface.BOLD);

        // Set color based on attendance grade
        txtPercentage.setTextColor(getGradeColor(attendanceGrade));

        rightSection.addView(txtPercentage);

        // Grade display
        TextView txtGrade = new TextView(this);
        txtGrade.setText(String.format("Grade: %.1f%%", attendanceGrade));
        txtGrade.setTextColor(getGradeColor(attendanceGrade));
        txtGrade.setTextSize(14);
        txtGrade.setTypeface(null, android.graphics.Typeface.BOLD);
        rightSection.addView(txtGrade);

        topRow.addView(rightSection);
        card.addView(topRow);

        // Attendance details row
        LinearLayout detailsRow = new LinearLayout(this);
        detailsRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams detailsParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        detailsParams.setMargins(0, dpToPx(12), 0, 0);
        detailsRow.setLayoutParams(detailsParams);

        // Present count
        TextView txtPresent = new TextView(this);
        txtPresent.setText("Present: " + present + " (100%)");
        txtPresent.setTextColor(0xFF10B981);
        txtPresent.setTextSize(13);
        detailsRow.addView(txtPresent);

        // Late count
        TextView txtLate = new TextView(this);
        txtLate.setText(" • Late: " + late + " (50%)");
        txtLate.setTextColor(0xFFFFA500);
        txtLate.setTextSize(13);
        detailsRow.addView(txtLate);

        // Absent count
        TextView txtAbsent = new TextView(this);
        txtAbsent.setText(" • Absent: " + absent + " (0%)");
        txtAbsent.setTextColor(0xFFEF4444);
        txtAbsent.setTextSize(13);
        detailsRow.addView(txtAbsent);

        // Total count
        TextView txtTotal = new TextView(this);
        txtTotal.setText(" • Total: " + total);
        txtTotal.setTextColor(0xFF94A3B8);
        txtTotal.setTextSize(13);
        detailsRow.addView(txtTotal);

        card.addView(detailsRow);

        // Progress bar
        ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(8)
        );
        barParams.setMargins(0, dpToPx(12), 0, 0);
        progressBar.setLayoutParams(barParams);
        progressBar.setProgress((int) attendanceGrade);
        progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(getGradeColor(attendanceGrade)));
        card.addView(progressBar);

        // Click listener to open detailed attendance page for this subject
        card.setOnClickListener(view -> {
            Intent intent = new Intent(AttendanceOverview.this, attendance.class);
            intent.putExtra("courseId", courseId);
            intent.putExtra("courseName", courseName);
            intent.putExtra("courseCode", courseCode);
            intent.putExtra("instructor", instructor);
            intent.putExtra("present", present);
            intent.putExtra("late", late);
            intent.putExtra("absent", absent);
            intent.putExtra("total", total);
            intent.putExtra("percentage", percentage);
            intent.putExtra("attendanceGrade", attendanceGrade);
            startActivity(intent);
        });

        subjectsContainer.addView(card);
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
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}