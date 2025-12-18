package com.matibag.presentlast.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.matibag.presentlast.AttendanceOverview;
import com.matibag.presentlast.GradesOverView;
import com.matibag.presentlast.R;
import com.matibag.presentlast.SubmissionPage;
import com.matibag.presentlast.api.ApiClient;
import com.matibag.presentlast.api.AuthManager;
import com.matibag.presentlast.api.QRAttendanceHelper;
import com.matibag.presentlast.api.models.QRValidateResponse;
import com.matibag.presentlast.api.models.StudentInboxResponse;
import com.matibag.presentlast.setting;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";

    private TextView txtWelcomeMessage;
    private ImageView imgLogo;
    private TextView tabRecent, tabToday, tabUpcoming;
    private LinearLayout updatesContainer;
    private ProgressBar progressBar;
    private View btnHome, btnCourse, btnGrades, btnAttendance, btnScanQR;

    private String currentTab = "Recent";
    private AuthManager authManager;
    private StudentInboxResponse inboxResponse;

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
        setContentView(R.layout.activity_home);

        authManager = AuthManager.getInstance(this);

        if (!authManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        initViews();
        setupUserInfo();
        setupNavigation();
        setupTabs();
        loadInboxData();
    }

    private void initViews() {
        txtWelcomeMessage = findViewById(R.id.txtWelcomeMessage);
        imgLogo = findViewById(R.id.imgLogo);
        tabRecent = findViewById(R.id.tabRecent);
        tabToday = findViewById(R.id.tabToday);
        tabUpcoming = findViewById(R.id.tabUpcoming);
        updatesContainer = findViewById(R.id.updatesContainer);
        progressBar = findViewById(R.id.progress_bar);
        btnHome = findViewById(R.id.home);
        btnCourse = findViewById(R.id.course);
        btnGrades = findViewById(R.id.Grades);
        btnAttendance = findViewById(R.id.attendance);
        btnScanQR = findViewById(R.id.scanButton);
    }

    private void setupUserInfo() {
        String fullName = authManager.getCurrentFullName();
        if (txtWelcomeMessage != null) {
            txtWelcomeMessage.setText("Welcome back, " + (fullName != null ? fullName : "Student") + "!");
        }
        if (imgLogo != null) {
            imgLogo.setOnClickListener(v -> startActivity(new Intent(this, setting.class)));
        }
    }

    private void setupNavigation() {
        if (btnHome != null) btnHome.setOnClickListener(v -> loadInboxData());
        if (btnCourse != null) btnCourse.setOnClickListener(v -> startActivity(new Intent(this, CourseActivity.class)));
        if (btnGrades != null) btnGrades.setOnClickListener(v -> startActivity(new Intent(this, GradesOverView.class)));
        if (btnAttendance != null) btnAttendance.setOnClickListener(v -> startActivity(new Intent(this, AttendanceOverview.class)));
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
    // ATTENDANCE LOGIC
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
                        "You will be marked as: " + statusText)
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

                    new androidx.appcompat.app.AlertDialog.Builder(HomeActivity.this)
                            .setTitle(title)
                            .setMessage(icon + " " + message)
                            .setPositiveButton("OK", null)
                            .show();

                    loadInboxData(); // Refresh the home feed
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
    // UI & DATA LOADING (TABS, INBOX, UTILS)
    // ============================================================

    private void setupTabs() {
        if (tabRecent != null) tabRecent.setOnClickListener(v -> handleTabClick(tabRecent, "Recent"));
        if (tabToday != null) tabToday.setOnClickListener(v -> handleTabClick(tabToday, "Today"));
        if (tabUpcoming != null) tabUpcoming.setOnClickListener(v -> handleTabClick(tabUpcoming, "Upcoming"));
        setActiveTabUI(tabRecent);
    }

    private void handleTabClick(TextView tab, String name) {
        currentTab = name;
        setActiveTabUI(tab);
        displayUpdates(name);
    }

    private void setActiveTabUI(TextView activeTab) {
        if (activeTab == null) return;
        resetTabUI(tabRecent);
        resetTabUI(tabToday);
        resetTabUI(tabUpcoming);
        activeTab.setTextColor(Color.WHITE);
        activeTab.setTypeface(null, Typeface.BOLD);
        activeTab.setBackgroundResource(R.drawable.bg_nav_rounded);
        if (activeTab.getBackground() != null) activeTab.getBackground().setTint(0xFF2563EB);
    }

    private void resetTabUI(TextView tab) {
        if (tab == null) return;
        tab.setTextColor(0xFF94A3B8);
        tab.setTypeface(null, Typeface.NORMAL);
        tab.setBackground(null);
    }

    private void loadInboxData() {
        int studentId = authManager.getCurrentUserId();
        if (studentId == -1) { navigateToLogin(); return; }

        setLoading(true);
        ApiClient.getApiService().getStudentInbox(studentId, 30).enqueue(new Callback<StudentInboxResponse>() {
            @Override
            public void onResponse(@NonNull Call<StudentInboxResponse> call, @NonNull Response<StudentInboxResponse> response) {
                runOnUiThread(() -> {
                    setLoading(false);
                    if (response.isSuccessful() && response.body() != null) {
                        StudentInboxResponse res = response.body();
                        if (res.isSuccess()) {
                            inboxResponse = res;
                            displayUpdates(currentTab);
                        } else {
                            showError(res.getError());
                            displayNoUpdates();
                        }
                    } else {
                        displayNoUpdates();
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Call<StudentInboxResponse> call, @NonNull Throwable t) {
                runOnUiThread(() -> {
                    setLoading(false);
                    displayNoUpdates();
                });
            }
        });
    }

    private void displayUpdates(String category) {
        if (updatesContainer == null) return;
        updatesContainer.removeAllViews();
        if (inboxResponse == null) { displayNoUpdates(); return; }

        boolean hasContent = false;
        List<StudentInboxResponse.TaskItem> tasks = inboxResponse.getTasks();
        if (tasks != null) {
            for (StudentInboxResponse.TaskItem task : tasks) {
                if (shouldShowTask(task, category)) {
                    addTaskCard(task);
                    hasContent = true;
                }
            }
        }

        if ("Recent".equals(category)) {
            if (inboxResponse.getGrades() != null) {
                for (StudentInboxResponse.GradeItem grade : inboxResponse.getGrades()) {
                    addGradeCard(grade);
                    hasContent = true;
                }
            }
            if (inboxResponse.getAttendance() != null) {
                for (StudentInboxResponse.AttendanceItem att : inboxResponse.getAttendance()) {
                    addAttendanceCard(att);
                    hasContent = true;
                }
            }
        }
        if (!hasContent) displayNoUpdates();
    }

    private boolean shouldShowTask(StudentInboxResponse.TaskItem task, String category) {
        switch (category) {
            case "Recent": return true;
            case "Today": return isToday(task.getDueDate());
            case "Upcoming": return !task.isSubmitted() && !task.isOverdue();
            default: return true;
        }
    }

    private void addTaskCard(StudentInboxResponse.TaskItem task) {
        LinearLayout card = createCardBase();
        LinearLayout row = createHeaderRow();

        TextView icon = new TextView(this);
        icon.setText(task.isGraded() ? "✅" : (task.isOverdue() ? "⚠️" : "📋"));
        icon.setTextSize(20);
        row.addView(icon);

        TextView lbl = new TextView(this);
        lbl.setText("  " + (task.isGraded() ? "GRADED" : (task.isSubmitted() ? "SUBMITTED" : (task.isOverdue() ? "OVERDUE" : "NEW TASK"))));
        lbl.setTextColor(task.isGraded() ? 0xFF10B981 : (task.isSubmitted() ? 0xFF3B82F6 : (task.isOverdue() ? 0xFFEF4444 : 0xFF6366F1)));
        lbl.setTextSize(12);
        lbl.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2); lp.weight = 1; lbl.setLayoutParams(lp);
        row.addView(lbl);

        TextView time = new TextView(this);
        time.setText(getTimeAgo(task.getCreatedAt()));
        time.setTextColor(0xFF94A3B8);
        time.setTextSize(12);
        row.addView(time);

        card.addView(row);

        TextView sub = createSubjectText((task.getSubjectCode() != null ? task.getSubjectCode() : "") + " - " + task.getSubjectName());
        card.addView(sub);

        TextView title = new TextView(this);
        title.setText(task.getTaskName());
        title.setTextColor(Color.WHITE);
        title.setTextSize(18);
        title.setTypeface(null, Typeface.BOLD);
        card.addView(title);

        card.setOnClickListener(v -> {
            Intent i = new Intent(this, SubmissionPage.class);
            i.putExtra("taskId", task.getId());
            startActivity(i);
        });
        updatesContainer.addView(card);
    }

    private void addGradeCard(StudentInboxResponse.GradeItem grade) {
        LinearLayout card = createCardBase();
        TextView title = new TextView(this);
        title.setText("📊  NEW GRADE");
        title.setTextColor(0xFF10B981);
        title.setTypeface(null, Typeface.BOLD);
        card.addView(title);

        TextView body = new TextView(this);
        body.setText(grade.getTaskName() + ": " + grade.getGrade() + "/100");
        body.setTextColor(Color.WHITE);
        card.addView(body);
        updatesContainer.addView(card);
    }

    private void addAttendanceCard(StudentInboxResponse.AttendanceItem att) {
        LinearLayout card = createCardBase();
        TextView title = new TextView(this);
        title.setText("📅  " + att.getSubjectName() + ": " + att.getStatus().toUpperCase());
        title.setTextColor(0xFF3B82F6);
        card.addView(title);
        updatesContainer.addView(card);
    }

    private LinearLayout createCardBase() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(0xFF1E293B);
        int p = dpToPx(16); card.setPadding(p, p, p, p);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, dpToPx(12)); card.setLayoutParams(lp);
        card.setFocusable(true); card.setClickable(true);
        return card;
    }

    private LinearLayout createHeaderRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        return row;
    }

    private TextView createSubjectText(String text) {
        TextView tv = new TextView(this); tv.setText(text);
        tv.setTextColor(0xFF94A3B8); tv.setTextSize(14);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        lp.setMargins(0, dpToPx(8), 0, dpToPx(4)); tv.setLayoutParams(lp);
        return tv;
    }

    private void displayNoUpdates() {
        if (updatesContainer == null) return;
        updatesContainer.removeAllViews();
        TextView tv = new TextView(this);
        tv.setText("No " + currentTab.toLowerCase() + " updates");
        tv.setTextColor(0xFF64748B); tv.setGravity(Gravity.CENTER);
        tv.setBackgroundColor(0xFF1E293B);
        updatesContainer.addView(tv, new LinearLayout.LayoutParams(-1, dpToPx(100)));
    }

    private void setLoading(boolean load) {
        if (progressBar != null) progressBar.setVisibility(load ? View.VISIBLE : View.GONE);
    }

    private void showError(String msg) {
        if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void navigateToLogin() {
        Intent i = new Intent(this, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i); finish();
    }

    private boolean isToday(String ds) {
        if (ds == null) return false;
        return ds.equals(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
    }

    private String getTimeAgo(String dts) {
        if (dts == null) return "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(dts.contains("T") ? "yyyy-MM-dd'T'HH:mm:ss" : "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date d = sdf.parse(dts); if (d == null) return dts;
            long diff = System.currentTimeMillis() - d.getTime();
            long m = TimeUnit.MILLISECONDS.toMinutes(diff);
            long h = TimeUnit.MILLISECONDS.toHours(diff);
            if (m < 60) return m + "m ago";
            if (h < 24) return h + "h ago";
            return new SimpleDateFormat("MMM d", Locale.getDefault()).format(d);
        } catch (ParseException e) { return dts; }
    }

    private int dpToPx(int dp) { return Math.round(dp * getResources().getDisplayMetrics().density); }

    @Override
    protected void onResume() {
        super.onResume();
        if (authManager.isLoggedIn()) loadInboxData();
    }
}