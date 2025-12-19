package com.matibag.presentlast.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.view.ViewCompat;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.matibag.presentlast.R;
import com.matibag.presentlast.api.ApiClient;
import com.matibag.presentlast.api.AuthManager;
import com.matibag.presentlast.api.QRAttendanceHelper;
import com.matibag.presentlast.api.models.QRValidateResponse;
import com.matibag.presentlast.api.models.StudentAttendanceResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendanceOverviewActivity extends AppCompatActivity {

    private static final String TAG = "AttendanceOverviewActivity";

    // Header views
    private ImageView imgLogo;
    private TextView txtSubtitle;

    // Stats views
    private TextView txtOverallAttendance, txtPresentCount, txtLateCount, txtAbsentCount, txtOverallGrade;

    // Filter views
    private Spinner spinnerSemester, spinnerYear;

    // Content views
    private LinearLayout subjectsContainer;
    private ProgressBar progressBar;

    // Navigation views
    private View btnHome, btnCourse, btnGrades, btnAttendance, btnScanQR;

    // Data
    private AuthManager authManager;
    private StudentAttendanceResponse attendanceResponse;
    private String currentSemester = "";
    private String currentYear = "";

    // QR Scanner
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    markAttendance(result.getContents());
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_overview);

        authManager = AuthManager.getInstance(this);

        if (!authManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        initViews();
        setupHeader();
        setupNavigation();
        setupSpinners();
        loadAttendanceData();
    }

    // ============================================================
    // VIEW INITIALIZATION
    // ============================================================

    private void initViews() {
        // Header
        imgLogo = findViewById(R.id.imgLogo);
        txtSubtitle = findViewById(R.id.txtSubtitle);

        // Stats
        txtOverallAttendance = findViewById(R.id.txtOverallAttendance);
        txtPresentCount = findViewById(R.id.txtPresentCount);
        txtLateCount = findViewById(R.id.txtLateCount);
        txtAbsentCount = findViewById(R.id.txtAbsentCount);
        txtOverallGrade = findViewById(R.id.txtOverallGrade);

        // Filters
        spinnerSemester = findViewById(R.id.spinnerSemester);
        spinnerYear = findViewById(R.id.spinnerYear);

        // Content
        subjectsContainer = findViewById(R.id.subjectsContainer);
        progressBar = findViewById(R.id.progressBar);

        // Navigation (from included layout)
        View navLayout = findViewById(R.id.layoutNav);
        if (navLayout != null) {
            btnHome = navLayout.findViewById(R.id.home);
            btnCourse = navLayout.findViewById(R.id.course);
            btnGrades = navLayout.findViewById(R.id.Grades);
            btnAttendance = navLayout.findViewById(R.id.attendance);
            btnScanQR = navLayout.findViewById(R.id.scanButton);
        }
    }

    private void setupHeader() {
        // Update subtitle with user name
        String fullName = authManager.getCurrentFullName();
        if (txtSubtitle != null && fullName != null) {
            txtSubtitle.setText("Track your attendance, " + fullName);
        }
    }

    private void setupNavigation() {
        // Highlight Attendance tab as active
        if (btnAttendance != null) {
            ViewCompat.setBackgroundTintList(btnAttendance,
                    android.content.res.ColorStateList.valueOf(0xFF2563EB));
        }

        if (btnHome != null) {
            btnHome.setOnClickListener(v -> {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            });
        }

        if (btnCourse != null) {
            btnCourse.setOnClickListener(v -> {
                startActivity(new Intent(this, CourseActivity.class));
                finish();
            });
        }

        if (btnGrades != null) {
            btnGrades.setOnClickListener(v -> {
                startActivity(new Intent(this, GradesOverviewActivity.class));
                finish();
            });
        }

        if (btnAttendance != null) {
            btnAttendance.setOnClickListener(v -> {
                // Already on attendance - refresh
                loadAttendanceData();
            });
        }

        if (btnScanQR != null) {
            btnScanQR.setOnClickListener(v -> {
                ScanOptions options = new ScanOptions();
                options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
                options.setPrompt("Scan attendance QR code");
                options.setBeepEnabled(true);
                options.setOrientationLocked(false);
                barcodeLauncher.launch(options);
            });
        }
    }

    private void setupSpinners() {
        String[] defaultSemesters = {"All Semesters"};
        String[] defaultYears = {"All Years"};

        ArrayAdapter<String> semesterAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, defaultSemesters);
        semesterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSemester.setAdapter(semesterAdapter);

        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, defaultYears);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);

        spinnerSemester.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                if (!selected.equals(currentSemester)) {
                    currentSemester = selected.equals("All Semesters") ? "" : selected;
                    loadAttendanceData();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                if (!selected.equals(currentYear)) {
                    currentYear = selected.equals("All Years") ? "" : selected;
                    loadAttendanceData();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // ============================================================
    // API DATA LOADING
    // ============================================================

    private void loadAttendanceData() {
        int studentId = authManager.getCurrentUserId();
        if (studentId == -1) {
            navigateToLogin();
            return;
        }

        setLoading(true);

        ApiClient.getApiService().getStudentAttendance(studentId, currentSemester, currentYear)
                .enqueue(new Callback<StudentAttendanceResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<StudentAttendanceResponse> call,
                                           @NonNull Response<StudentAttendanceResponse> response) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            if (response.isSuccessful() && response.body() != null) {
                                StudentAttendanceResponse res = response.body();
                                if (res.isSuccess()) {
                                    attendanceResponse = res;
                                    updateSpinnersFromResponse();
                                    updateStatsDisplay();
                                    displaySubjects();
                                } else {
                                    showError(res.getError());
                                    displayNoSubjects();
                                }
                            } else {
                                showError("Failed to load attendance");
                                displayNoSubjects();
                            }
                        });
                    }

                    @Override
                    public void onFailure(@NonNull Call<StudentAttendanceResponse> call, @NonNull Throwable t) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            showError("Connection error: " + t.getMessage());
                            displayNoSubjects();
                        });
                    }
                });
    }

    private void updateSpinnersFromResponse() {
        if (attendanceResponse == null) return;

        List<String> semesters = attendanceResponse.getSemesters();
        if (semesters != null && !semesters.isEmpty()) {
            List<String> semesterList = new ArrayList<>();
            semesterList.add("All Semesters");
            semesterList.addAll(semesters);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, semesterList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerSemester.setAdapter(adapter);
        }

        List<String> years = attendanceResponse.getYears();
        if (years != null && !years.isEmpty()) {
            List<String> yearList = new ArrayList<>();
            yearList.add("All Years");
            yearList.addAll(years);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, yearList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerYear.setAdapter(adapter);
        }
    }

    private void updateStatsDisplay() {
        if (attendanceResponse == null) return;

        List<StudentAttendanceResponse.SubjectAttendance> subjects = attendanceResponse.getSubjects();
        if (subjects != null && !subjects.isEmpty()) {
            int totalPresent = 0, totalLate = 0, totalAbsent = 0, totalSessions = 0;

            for (StudentAttendanceResponse.SubjectAttendance subject : subjects) {
                totalPresent += subject.getPresentCount();
                totalLate += subject.getLateCount();
                totalAbsent += subject.getAbsentCount();
                totalSessions += subject.getTotalSessions();
            }

            int attended = totalPresent + totalLate;
            int percentage = totalSessions > 0 ? (attended * 100) / totalSessions : 0;
            txtOverallAttendance.setText(percentage + "%");
            txtOverallAttendance.setTextColor(getGradeColor(percentage));

            txtPresentCount.setText(String.valueOf(totalPresent));
            txtLateCount.setText(String.valueOf(totalLate));
            txtAbsentCount.setText(String.valueOf(totalAbsent));

            double gradePoints = (totalPresent * 100.0) + (totalLate * 50.0);
            double attendanceGrade = totalSessions > 0 ? gradePoints / totalSessions : 0;
            txtOverallGrade.setText(String.format("%.1f%%", attendanceGrade));
            txtOverallGrade.setTextColor(getGradeColor((int) attendanceGrade));
        } else {
            txtOverallAttendance.setText("--");
            txtPresentCount.setText("0");
            txtLateCount.setText("0");
            txtAbsentCount.setText("0");
            txtOverallGrade.setText("--");
        }
    }

    private void displaySubjects() {
        if (subjectsContainer == null) return;
        subjectsContainer.removeAllViews();

        if (attendanceResponse == null || attendanceResponse.getSubjects() == null ||
                attendanceResponse.getSubjects().isEmpty()) {
            displayNoSubjects();
            return;
        }

        for (StudentAttendanceResponse.SubjectAttendance subject : attendanceResponse.getSubjects()) {
            addSubjectAttendanceCard(subject);
        }
    }

    private void addSubjectAttendanceCard(StudentAttendanceResponse.SubjectAttendance subject) {
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

        // Top row:  Course info and percentage
        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);
        topRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout. LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Left:  Course info
        LinearLayout courseInfo = new LinearLayout(this);
        courseInfo.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoParams = new LinearLayout. LayoutParams(0, LinearLayout.LayoutParams. WRAP_CONTENT, 1);
        courseInfo.setLayoutParams(infoParams);

        TextView txtCourseName = new TextView(this);
        txtCourseName.setText(subject.getName());
        txtCourseName.setTextColor(Color.WHITE);
        txtCourseName.setTextSize(18);
        txtCourseName. setTypeface(null, Typeface. BOLD);
        courseInfo.addView(txtCourseName);

        // Course code only (no instructors available in this model)
        if (subject.getCode() != null) {
            TextView txtCourseCode = new TextView(this);
            txtCourseCode.setText(subject.getCode());
            txtCourseCode. setTextColor(0xFF94A3B8);
            txtCourseCode.setTextSize(14);
            LinearLayout.LayoutParams codeParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams. WRAP_CONTENT, LinearLayout.LayoutParams. WRAP_CONTENT);
            codeParams.setMargins(0, dpToPx(4), 0, 0);
            txtCourseCode.setLayoutParams(codeParams);
            courseInfo.addView(txtCourseCode);
        }

        topRow.addView(courseInfo);

        // Right: Attendance percentage and grade
        LinearLayout rightSection = new LinearLayout(this);
        rightSection.setOrientation(LinearLayout.VERTICAL);
        rightSection.setGravity(Gravity.END);

        int percentage = subject.getAttendanceRate();
        TextView txtPercentage = new TextView(this);
        txtPercentage.setText(percentage + "%");
        txtPercentage.setTextSize(36);
        txtPercentage. setTypeface(null, Typeface. BOLD);
        txtPercentage.setTextColor(getGradeColor(percentage));
        rightSection.addView(txtPercentage);

        // Calculate grade
        int present = subject.getPresentCount();
        int late = subject.getLateCount();
        int total = subject.getTotalSessions();
        double attendanceGrade = total > 0 ? ((present * 100.0) + (late * 50.0)) / total : 0;

        TextView txtGrade = new TextView(this);
        txtGrade.setText(String.format("%.0f%%", attendanceGrade));
        txtGrade.setTextColor(getGradeColor((int) attendanceGrade));
        txtGrade.setTextSize(14);
        txtGrade.setTypeface(null, Typeface.BOLD);
        rightSection.addView(txtGrade);

        topRow.addView(rightSection);
        card.addView(topRow);

        // Stats row
        LinearLayout statsRow = new LinearLayout(this);
        statsRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams statsParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams. WRAP_CONTENT);
        statsParams.setMargins(0, dpToPx(12), 0, 0);
        statsRow.setLayoutParams(statsParams);

        // Present
        TextView txtPresent = new TextView(this);
        txtPresent.setText("✓ " + present);
        txtPresent.setTextColor(0xFF10B981);
        txtPresent.setTextSize(13);
        LinearLayout.LayoutParams presentParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        presentParams.setMargins(0, 0, dpToPx(16), 0);
        txtPresent.setLayoutParams(presentParams);
        statsRow.addView(txtPresent);

        // Late
        TextView txtLate = new TextView(this);
        txtLate.setText("⚠ " + late);
        txtLate.setTextColor(0xFFFFA500);
        txtLate.setTextSize(13);
        LinearLayout.LayoutParams lateParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams. WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lateParams.setMargins(0, 0, dpToPx(16), 0);
        txtLate.setLayoutParams(lateParams);
        statsRow.addView(txtLate);

        // Absent
        TextView txtAbsent = new TextView(this);
        txtAbsent.setText("✗ " + subject.getAbsentCount());
        txtAbsent.setTextColor(0xFFEF4444);
        txtAbsent.setTextSize(13);
        LinearLayout.LayoutParams absentParams = new LinearLayout.LayoutParams(
                LinearLayout. LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams. WRAP_CONTENT);
        absentParams.setMargins(0, 0, dpToPx(16), 0);
        txtAbsent.setLayoutParams(absentParams);
        statsRow.addView(txtAbsent);

        // Excused (if any)
        if (subject.getExcusedCount() > 0) {
            TextView txtExcused = new TextView(this);
            txtExcused.setText("📋 " + subject.getExcusedCount());
            txtExcused.setTextColor(0xFF3B82F6);
            txtExcused. setTextSize(13);
            LinearLayout.LayoutParams excusedParams = new LinearLayout. LayoutParams(
                    LinearLayout. LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            excusedParams.setMargins(0, 0, dpToPx(16), 0);
            txtExcused. setLayoutParams(excusedParams);
            statsRow.addView(txtExcused);
        }

        // Total
        TextView txtTotal = new TextView(this);
        txtTotal.setText("Total: " + total);
        txtTotal.setTextColor(0xFF94A3B8);
        txtTotal.setTextSize(13);
        statsRow.addView(txtTotal);

        card.addView(statsRow);

        // Progress bar
        ProgressBar progressBarItem = new ProgressBar(this, null, android.R.attr. progressBarStyleHorizontal);
        LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(
                LinearLayout. LayoutParams.MATCH_PARENT, dpToPx(6));
        barParams.setMargins(0, dpToPx(12), 0, 0);
        progressBarItem.setLayoutParams(barParams);
        progressBarItem. setProgress((int) attendanceGrade);
        progressBarItem.setProgressTintList(android.content.res.ColorStateList.valueOf(getGradeColor((int) attendanceGrade)));
        card.addView(progressBarItem);

        // Click listener - pass only available data
        card.setOnClickListener(v -> {
            Intent intent = new Intent(this, SubjectAttendanceActivity.class);
            intent.putExtra("courseId", subject.getId());
            intent.putExtra("courseName", subject.getName());
            intent.putExtra("courseCode", subject. getCode());
            // instructor not available in this model
            startActivity(intent);
        });

        subjectsContainer.addView(card);
    }

    private void displayNoSubjects() {
        if (subjectsContainer == null) return;
        subjectsContainer.removeAllViews();

        TextView emptyText = new TextView(this);
        emptyText.setText("No attendance records available");
        emptyText.setTextColor(0xFF94A3B8);
        emptyText.setTextSize(16);
        emptyText.setGravity(Gravity.CENTER);
        emptyText.setBackgroundColor(0xFF1E293B);
        int padding = dpToPx(24);
        emptyText.setPadding(padding, padding, padding, padding);

        subjectsContainer.addView(emptyText);
    }

    // ============================================================
    // QR ATTENDANCE
    // ============================================================

    private void markAttendance(String qrContent) {
        int studentId = authManager.getCurrentUserId();
        if (studentId == -1) {
            showError("Session expired");
            return;
        }

        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(this);
        progressDialog.setMessage("Validating QR code...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        QRAttendanceHelper.validateQRToken(qrContent, new QRAttendanceHelper.ValidateCallback() {
            @Override
            public void onValid(QRValidateResponse.SessionInfo session, String willBeMarkedAs) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    showAttendanceConfirmDialog(qrContent, session, willBeMarkedAs, studentId);
                });
            }

            @Override
            public void onExpired() {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    showError("This QR code has expired");
                });
            }

            @Override
            public void onInvalid(String errorMessage) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    showError(errorMessage);
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    showError(errorMessage);
                });
            }
        });
    }

    private void showAttendanceConfirmDialog(String qrContent, QRValidateResponse.SessionInfo session,
                                             String willBeMarkedAs, int studentId) {
        String subjectInfo = (session.getSubjectCode() != null)
                ? session.getSubjectCode() + " - " + session.getSubjectName()
                : session.getSubjectName();

        String statusText = "present".equals(willBeMarkedAs) ? "Present ✓" : "Late ⏰";

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Mark Attendance")
                .setMessage("Subject: " + subjectInfo + "\n" +
                        "Date: " + session.getDate() + "\n" +
                        "Time: " + session.getTime() + "\n\n" +
                        "You will be marked as:  " + statusText)
                .setPositiveButton("Confirm", (dialog, which) -> confirmAttendance(qrContent, studentId))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmAttendance(String qrContent, int studentId) {
        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(this);
        progressDialog.setMessage("Processing...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        QRAttendanceHelper.markAttendance(qrContent, studentId, new QRAttendanceHelper.MarkCallback() {
            @Override
            public void onSuccess(String status, String message, boolean alreadyMarked) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    String title = alreadyMarked ? "Already Marked" : "Attendance Marked";
                    String icon = "present".equals(status) ? "✓" : "⏰";

                    new androidx.appcompat.app.AlertDialog.Builder(AttendanceOverviewActivity.this)
                            .setTitle(title)
                            .setMessage(icon + " " + message)
                            .setPositiveButton("OK", null)
                            .show();

                    loadAttendanceData();
                });
            }

            @Override
            public void onNotEnrolled() {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    showError("You are not enrolled in this subject");
                });
            }

            @Override
            public void onExpired() {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    showError("This QR code has expired");
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    showError(errorMessage);
                });
            }
        });
    }

    private int getGradeColor(int grade) {
        if (grade >= 90) return 0xFF10B981;
        if (grade >= 75) return 0xFFFFA500;
        return 0xFFEF4444;
    }

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

    @Override
    protected void onResume() {
        super.onResume();
        if (authManager.isLoggedIn()) {
            loadAttendanceData();
        }
    }
}