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
import com.matibag.presentlast.api.models.StudentGradesResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GradesOverviewActivity extends AppCompatActivity {

    private static final String TAG = "GradesOverviewActivity";

    // Header views
    private ImageView imgLogo;
    private TextView txtSubtitle;

    // Stats views
    private TextView txtAverageGrade, txtSubjectsCount, txtGradedCount;

    // Filter views
    private Spinner spinnerSemester, spinnerYear;

    // Content views
    private LinearLayout subjectsContainer;
    private ProgressBar progressBar;

    // Navigation views
    private View btnHome, btnCourse, btnGrades, btnAttendance, btnScanQR;

    // Data
    private AuthManager authManager;
    private StudentGradesResponse gradesResponse;
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
        setContentView(R.layout.activity_grades_overview);

        authManager = AuthManager.getInstance(this);

        if (!authManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        initViews();
        setupHeader();
        setupNavigation();
        setupSpinners();
        loadGradesData();
    }

    // ============================================================
    // VIEW INITIALIZATION
    // ============================================================

    private void initViews() {
        // Header
        imgLogo = findViewById(R.id.imgLogo);
        txtSubtitle = findViewById(R.id.txtSubtitle);

        // Stats
        txtAverageGrade = findViewById(R.id.txtAverageGrade);
        txtSubjectsCount = findViewById(R.id.txtSubjectsCount);
        txtGradedCount = findViewById(R.id.txtGradedCount);

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
        if (imgLogo != null) {
            imgLogo.setOnClickListener(v -> {
                startActivity(new Intent(this, setting.class));
            });
        }

        // Update subtitle with user name
        String fullName = authManager.getCurrentFullName();
        if (txtSubtitle != null && fullName != null) {
            txtSubtitle.setText("Track your grades, " + fullName);
        }
    }

    private void setupNavigation() {
        // Highlight Grades tab as active
        if (btnGrades != null) {
            btnGrades.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF2563EB));
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
                // Already on grades - refresh
                loadGradesData();
            });
        }

        if (btnAttendance != null) {
            btnAttendance.setOnClickListener(v -> {
                startActivity(new Intent(this, AttendanceOverviewActivity.class));
                finish();
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
        // Default values
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
                    loadGradesData();
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
                    loadGradesData();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // ============================================================
    // API DATA LOADING
    // ============================================================

    private void loadGradesData() {
        int studentId = authManager.getCurrentUserId();
        if (studentId == -1) {
            navigateToLogin();
            return;
        }

        setLoading(true);

        ApiClient.getApiService().getStudentGrades(studentId, currentSemester, currentYear)
                .enqueue(new Callback<StudentGradesResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<StudentGradesResponse> call,
                                           @NonNull Response<StudentGradesResponse> response) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            if (response.isSuccessful() && response.body() != null) {
                                StudentGradesResponse res = response.body();
                                if (res.isSuccess()) {
                                    gradesResponse = res;
                                    updateSpinnersFromResponse();
                                    updateStatsDisplay();
                                    displaySubjects();
                                } else {
                                    showError(res.getError());
                                    displayNoSubjects();
                                }
                            } else {
                                showError("Failed to load grades");
                                displayNoSubjects();
                            }
                        });
                    }

                    @Override
                    public void onFailure(@NonNull Call<StudentGradesResponse> call, @NonNull Throwable t) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            showError("Connection error: " + t.getMessage());
                            displayNoSubjects();
                        });
                    }
                });
    }

    private void updateSpinnersFromResponse() {
        if (gradesResponse == null || gradesResponse.getFilters() == null) return;

        StudentGradesResponse.Filters filters = gradesResponse.getFilters();

        // Update semester spinner
        if (filters.getSemesters() != null && !filters.getSemesters().isEmpty()) {
            List<String> semesters = new ArrayList<>();
            semesters.add("All Semesters");
            semesters.addAll(filters.getSemesters());

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, semesters);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerSemester.setAdapter(adapter);
        }

        // Update year spinner
        if (filters.getYears() != null && !filters.getYears().isEmpty()) {
            List<String> years = new ArrayList<>();
            years.add("All Years");
            years.addAll(filters.getYears());

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, years);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerYear.setAdapter(adapter);
        }
    }

    private void updateStatsDisplay() {
        if (gradesResponse == null) return;

        // Calculate overall average
        List<StudentGradesResponse.SubjectGrade> subjects = gradesResponse.getSubjects();
        if (subjects != null && !subjects.isEmpty()) {
            double totalGrade = 0;
            int gradedCount = 0;
            int totalGradedTasks = 0;

            for (StudentGradesResponse.SubjectGrade subject : subjects) {
                if (subject.getAverageGrade() != null) {
                    totalGrade += subject.getAverageGrade();
                    gradedCount++;
                }
                totalGradedTasks += subject.getGradedCount();
            }

            // Average grade
            if (gradedCount > 0) {
                double average = totalGrade / gradedCount;
                txtAverageGrade.setText(String.format("%.0f", average));
                txtAverageGrade.setTextColor(getGradeColor((int) average));
            } else {
                txtAverageGrade.setText("--");
                txtAverageGrade.setTextColor(0xFF94A3B8);
            }

            // Subjects count
            txtSubjectsCount.setText(String.valueOf(subjects.size()));

            // Graded tasks count
            txtGradedCount.setText(String.valueOf(totalGradedTasks));
        } else {
            txtAverageGrade.setText("--");
            txtSubjectsCount.setText("0");
            txtGradedCount.setText("0");
        }
    }

    // ============================================================
    // DISPLAY SUBJECTS
    // ============================================================

    private void displaySubjects() {
        if (subjectsContainer == null) return;
        subjectsContainer.removeAllViews();

        if (gradesResponse == null || gradesResponse.getSubjects() == null ||
                gradesResponse.getSubjects().isEmpty()) {
            displayNoSubjects();
            return;
        }

        for (StudentGradesResponse.SubjectGrade subject : gradesResponse.getSubjects()) {
            addSubjectGradeCard(subject);
        }
    }

    private void addSubjectGradeCard(StudentGradesResponse.SubjectGrade subject) {
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
        topRow.setGravity(Gravity.CENTER_VERTICAL);
        topRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Left: Course info
        LinearLayout courseInfo = new LinearLayout(this);
        courseInfo.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        courseInfo.setLayoutParams(infoParams);

        TextView txtCourseName = new TextView(this);
        txtCourseName.setText(subject.getName());
        txtCourseName.setTextColor(Color.WHITE);
        txtCourseName.setTextSize(18);
        txtCourseName.setTypeface(null, Typeface.BOLD);
        courseInfo.addView(txtCourseName);

        TextView txtCourseInfoText = new TextView(this);
        String info = (subject.getCode() != null ? subject.getCode() : "") +
                (subject.getInstructors() != null ? " • " + subject.getInstructors() : "");
        txtCourseInfoText.setText(info);
        txtCourseInfoText.setTextColor(0xFF94A3B8);
        txtCourseInfoText.setTextSize(14);
        LinearLayout.LayoutParams infoTextParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        infoTextParams.setMargins(0, dpToPx(4), 0, 0);
        txtCourseInfoText.setLayoutParams(infoTextParams);
        courseInfo.addView(txtCourseInfoText);

        topRow.addView(courseInfo);

        // Right: Average grade
        TextView txtGrade = new TextView(this);
        if (subject.getAverageGrade() != null) {
            int avg = subject.getAverageGrade().intValue();
            txtGrade.setText(String.valueOf(avg));
            txtGrade.setTextColor(getGradeColor(avg));
        } else {
            txtGrade.setText("--");
            txtGrade.setTextColor(0xFF6B7280);
        }
        txtGrade.setTextSize(40);
        txtGrade.setTypeface(null, Typeface.BOLD);
        topRow.addView(txtGrade);

        card.addView(topRow);

        // Stats row
        LinearLayout statsRow = new LinearLayout(this);
        statsRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams statsParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        statsParams.setMargins(0, dpToPx(12), 0, 0);
        statsRow.setLayoutParams(statsParams);

        // Tasks info
        TextView txtTasks = new TextView(this);
        txtTasks.setText("📋 " + subject.getGradedCount() + "/" + subject.getTotalTasks() + " graded");
        txtTasks.setTextColor(0xFF94A3B8);
        txtTasks.setTextSize(13);
        statsRow.addView(txtTasks);

        // Semester info
        if (subject.getSemesterName() != null) {
            TextView txtSemester = new TextView(this);
            txtSemester.setText(" • " + subject.getSemesterName());
            txtSemester.setTextColor(0xFF64748B);
            txtSemester.setTextSize(13);
            statsRow.addView(txtSemester);
        }

        card.addView(statsRow);

        // Progress bar
        if (subject.getAverageGrade() != null) {
            ProgressBar progressBarItem = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(6));
            barParams.setMargins(0, dpToPx(12), 0, 0);
            progressBarItem.setLayoutParams(barParams);
            progressBarItem.setProgress(subject.getAverageGrade().intValue());
            progressBarItem.setProgressTintList(android.content.res.ColorStateList.valueOf(
                    getGradeColor(subject.getAverageGrade().intValue())));
            card.addView(progressBarItem);
        }

        // Click listener
        card.setOnClickListener(v -> {
            Intent intent = new Intent(this, SubjectGradesActivity.class);
            intent.putExtra("courseId", subject.getId());
            intent.putExtra("courseName", subject.getName());
            intent.putExtra("courseCode", subject.getCode());
            intent.putExtra("instructor", subject.getInstructors());
            startActivity(intent);
        });

        subjectsContainer.addView(card);
    }

    private void displayNoSubjects() {
        if (subjectsContainer == null) return;
        subjectsContainer.removeAllViews();

        TextView emptyText = new TextView(this);
        emptyText.setText("No grades available");
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

                    new androidx.appcompat.app.AlertDialog.Builder(GradesOverviewActivity.this)
                            .setTitle(title)
                            .setMessage(icon + " " + message)
                            .setPositiveButton("OK", null)
                            .show();
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

    // ============================================================
    // UTILITY METHODS
    // ============================================================

    private int getGradeColor(int grade) {
        if (grade >= 90) return 0xFF10B981; // Green
        if (grade >= 80) return 0xFF3B82F6; // Blue
        if (grade >= 70) return 0xFFFBBF24; // Yellow
        return 0xFFEF4444; // Red
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
            loadGradesData();
        }
    }
}