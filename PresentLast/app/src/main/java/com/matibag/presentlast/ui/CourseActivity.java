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
import com.matibag.presentlast.api.models.StudentSubjectsResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourseActivity extends AppCompatActivity {

    private static final String TAG = "CourseActivity";

    // Header views (from included layout)
    private TextView txtHeaderTitle, txtWelcomeMessage;
    private ImageView imgLogo;

    // Navigation views (from included layout)
    private View btnHome, btnCourse, btnGrades, btnAttendance, btnScanQR;

    // Course-specific views
    private LinearLayout coursesContainer;
    private Spinner spinnerSemester, spinnerYear;
    private ProgressBar progressBar;

    // Data
    private AuthManager authManager;
    private StudentSubjectsResponse subjectsResponse;
    private String currentSemester = "";
    private String currentYear = "";

    // Card colors
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
        setContentView(R.layout.activity_courses);

        authManager = AuthManager.getInstance(this);

        if (!authManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        initViews();
        setupHeader();
        setupNavigation();
        setupSpinners();
        loadSubjectsData();
    }

    // ============================================================
    // VIEW INITIALIZATION
    // ============================================================

    private void initViews() {
        // Header views (from included layout_part_header)
        View headerLayout = findViewById(R.id.layoutHeader);
        if (headerLayout != null) {
            txtHeaderTitle = headerLayout.findViewById(R.id.txtHeaderTitle);
            txtWelcomeMessage = headerLayout.findViewById(R.id.txtWelcomeMessage);
            imgLogo = headerLayout.findViewById(R.id.imgLogo);
        }

        // Navigation views (from included layout_part_nav)
        View navLayout = findViewById(R.id.layoutNav);
        if (navLayout != null) {
            btnHome = navLayout.findViewById(R.id.home);
            btnCourse = navLayout.findViewById(R.id.course);
            btnGrades = navLayout.findViewById(R.id.Grades);
            btnAttendance = navLayout.findViewById(R.id.attendance);
            btnScanQR = navLayout.findViewById(R.id.scanButton);
        }

        // Course-specific views
        coursesContainer = findViewById(R.id.coursesContainer);
        spinnerSemester = findViewById(R.id.spinnerSemester);
        spinnerYear = findViewById(R.id.spinnerYear);

        // Create a progress bar programmatically if not in XML
        progressBar = new ProgressBar(this);
        progressBar.setVisibility(View.GONE);
    }

    private void setupHeader() {
        // Set header title for Courses page
        if (txtHeaderTitle != null) {
            txtHeaderTitle.setText("Courses");
        }

        // Set welcome message with user's name
        String fullName = authManager.getCurrentFullName();
        if (txtWelcomeMessage != null) {
            txtWelcomeMessage.setText("Track your subjects" + (fullName != null ? ", " + fullName : ""));
        }
    }

    // ============================================================
    // NAVIGATION SETUP
    // ============================================================

    private void setupNavigation() {
        // Highlight Course tab as active
        if (btnCourse != null) {
            ViewCompat.setBackgroundTintList(btnCourse,
                    android.content.res.ColorStateList.valueOf(0xFF2563EB));
        }

        if (btnHome != null) {
            btnHome.setOnClickListener(v -> {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            });
        }

        if (btnCourse != null) {
            btnCourse.setOnClickListener(v -> loadSubjectsData()); // Refresh
        }

        if (btnGrades != null) {
            btnGrades.setOnClickListener(v -> {
                startActivity(new Intent(this, GradesOverviewActivity.class));
                finish();
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

    // ============================================================
    // SPINNER SETUP
    // ============================================================

    private void setupSpinners() {
        // Initial placeholder data - will be replaced by API response
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

        // Listeners for filtering
        spinnerSemester.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentSemester = parent.getItemAtPosition(position).toString();
                displayCourses();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentYear = parent.getItemAtPosition(position).toString();
                displayCourses();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateSpinnersFromResponse() {
        if (subjectsResponse == null) return;

        // Update semester spinner
        List<String> semesters = subjectsResponse.getSemesters();
        if (semesters != null && !semesters.isEmpty()) {
            List<String> semesterList = new ArrayList<>();
            semesterList.add("All Semesters");
            semesterList.addAll(semesters);

            ArrayAdapter<String> semesterAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, semesterList);
            semesterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerSemester.setAdapter(semesterAdapter);
        }

        // Update year spinner
        List<String> years = subjectsResponse.getYears();
        if (years != null && !years.isEmpty()) {
            List<String> yearList = new ArrayList<>();
            yearList.add("All Years");
            yearList.addAll(years);

            ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, yearList);
            yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerYear.setAdapter(yearAdapter);
        }
    }

    // ============================================================
    // API DATA LOADING
    // ============================================================

    private void loadSubjectsData() {
        int studentId = authManager.getCurrentUserId();
        if (studentId == -1) {
            navigateToLogin();
            return;
        }

        setLoading(true);

        // Pass empty strings to get all subjects initially
        String semesterFilter = currentSemester.equals("All Semesters") ? "" : currentSemester;
        String yearFilter = currentYear.equals("All Years") ? "" : currentYear;

        ApiClient.getApiService().getStudentSubjects(studentId, semesterFilter, yearFilter)
                .enqueue(new Callback<StudentSubjectsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<StudentSubjectsResponse> call,
                                           @NonNull Response<StudentSubjectsResponse> response) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            if (response.isSuccessful() && response.body() != null) {
                                StudentSubjectsResponse res = response.body();
                                if (res.isSuccess()) {
                                    subjectsResponse = res;
                                    updateSpinnersFromResponse();
                                    displayCourses();
                                } else {
                                    showError(res.getError());
                                    displayNoCoursesMessage();
                                }
                            } else {
                                showError("Failed to load courses");
                                displayNoCoursesMessage();
                            }
                        });
                    }

                    @Override
                    public void onFailure(@NonNull Call<StudentSubjectsResponse> call, @NonNull Throwable t) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            showError("Connection error: " + t.getMessage());
                            displayNoCoursesMessage();
                        });
                    }
                });
    }

    // ============================================================
    // DISPLAY COURSES
    // ============================================================

    private void displayCourses() {
        if (coursesContainer == null) return;
        coursesContainer.removeAllViews();

        if (subjectsResponse == null || subjectsResponse.getSubjects() == null) {
            displayNoCoursesMessage();
            return;
        }

        List<StudentSubjectsResponse.Subject> subjects = subjectsResponse.getSubjects();
        int colorIndex = 0;
        int displayedCount = 0;

        for (StudentSubjectsResponse.Subject subject : subjects) {
            // Filter by semester and year
            boolean semesterMatch = currentSemester.equals("All Semesters") ||
                    currentSemester.isEmpty() ||
                    (subject.getSemesterName() != null && subject.getSemesterName().equals(currentSemester));

            boolean yearMatch = currentYear.equals("All Years") ||
                    currentYear.isEmpty() ||
                    (subject.getSchoolYear() != null && subject.getSchoolYear().equals(currentYear));

            if (semesterMatch && yearMatch) {
                int cardColor = cardColors[colorIndex % cardColors.length];
                addCourseCard(subject, cardColor);
                colorIndex++;
                displayedCount++;
            }
        }

        if (displayedCount == 0) {
            displayNoCoursesMessage();
        }
    }

    private void addCourseCard(StudentSubjectsResponse.Subject subject, int backgroundColor) {
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

        // Course info
        LinearLayout courseInfo = new LinearLayout(this);
        courseInfo.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
        infoParams.weight = 1;
        courseInfo.setLayoutParams(infoParams);

        // Course name
        TextView txtCourseName = new TextView(this);
        txtCourseName.setText(subject.getName());
        txtCourseName.setTextColor(Color.WHITE);
        txtCourseName.setTextSize(22);
        txtCourseName.setTypeface(null, Typeface.BOLD);
        courseInfo.addView(txtCourseName);

        // Course code
        TextView txtCourseCode = new TextView(this);
        txtCourseCode.setText(subject.getCode() != null ? subject.getCode() : "");
        txtCourseCode.setTextColor(0xFFE0E7FF);
        txtCourseCode.setTextSize(14);
        LinearLayout.LayoutParams codeParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
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
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lblParams.setMargins(0, dpToPx(20), 0, 0);
        lblInstructor.setLayoutParams(lblParams);
        card.addView(lblInstructor);

        // Instructor name
        TextView txtInstructor = new TextView(this);
        txtInstructor.setText(subject.getInstructors() != null ? subject.getInstructors() : "TBA");
        txtInstructor.setTextColor(Color.WHITE);
        txtInstructor.setTextSize(16);
        LinearLayout.LayoutParams instrParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        instrParams.setMargins(0, dpToPx(4), 0, 0);
        txtInstructor.setLayoutParams(instrParams);
        card.addView(txtInstructor);

        // Progress label
        TextView lblProgress = new TextView(this);
        lblProgress.setText("Progress");
        lblProgress.setTextColor(0xFFB3D1FF);
        lblProgress.setTextSize(10);
        LinearLayout.LayoutParams progLblParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        progLblParams.setMargins(0, dpToPx(12), 0, 0);
        lblProgress.setLayoutParams(progLblParams);
        card.addView(lblProgress);

        // Progress section
        LinearLayout progressSection = new LinearLayout(this);
        progressSection.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams progSectionParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        progSectionParams.setMargins(0, dpToPx(8), 0, 0);
        progressSection.setLayoutParams(progSectionParams);

        // Progress bar
        ProgressBar progressBarItem = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(0, dpToPx(8));
        barParams.weight = 1;
        barParams.gravity = Gravity.CENTER_VERTICAL;
        progressBarItem.setLayoutParams(barParams);
        progressBarItem.setProgress(subject.getProgress());
        progressBarItem.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
        progressSection.addView(progressBarItem);

        // Progress percentage
        TextView txtProgress = new TextView(this);
        txtProgress.setText(subject.getProgress() + "%");
        txtProgress.setTextColor(Color.WHITE);
        txtProgress.setTextSize(16);
        txtProgress.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams progTextParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        progTextParams.setMargins(dpToPx(12), 0, 0, 0);
        txtProgress.setLayoutParams(progTextParams);
        progressSection.addView(txtProgress);

        card.addView(progressSection);

        // Click listener
        card.setOnClickListener(view -> {
            Intent intent = new Intent(CourseActivity.this, SubjectPageActivity.class);
            intent.putExtra("courseId", subject.getId());
            intent.putExtra("courseName", subject.getName());
            intent.putExtra("courseCode", subject.getCode());
            intent.putExtra("instructor", subject.getInstructors());
            startActivity(intent);
        });

        coursesContainer.addView(card);
    }

    private void displayNoCoursesMessage() {
        if (coursesContainer == null) return;
        coursesContainer.removeAllViews();

        TextView emptyText = new TextView(this);
        emptyText.setText("No courses found for selected filters");
        emptyText.setTextColor(0xFF94A3B8);
        emptyText.setTextSize(16);
        emptyText.setGravity(Gravity.CENTER);
        emptyText.setBackgroundColor(0xFF1E293B);
        int padding = dpToPx(24);
        emptyText.setPadding(padding, padding, padding, padding);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dpToPx(16), 0, 0);
        emptyText.setLayoutParams(params);

        coursesContainer.addView(emptyText);
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

                    new androidx.appcompat.app.AlertDialog.Builder(CourseActivity.this)
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
                    showError("You are not enrolled in this activity_subject");
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

    private void setLoading(boolean loading) {
        if (loading && coursesContainer != null) {
            coursesContainer.removeAllViews();

            LinearLayout loadingContainer = new LinearLayout(this);
            loadingContainer.setGravity(Gravity.CENTER);
            loadingContainer.setPadding(0, dpToPx(48), 0, dpToPx(48));

            ProgressBar pb = new ProgressBar(this);
            pb.setIndeterminateTintList(android.content.res.ColorStateList.valueOf(0xFF2563EB));
            loadingContainer.addView(pb);

            coursesContainer.addView(loadingContainer);
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
            loadSubjectsData();
        }
    }
}