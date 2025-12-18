package com.matibag.presentlast.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.matibag.presentlast.R;
import com.matibag.presentlast.api.AuthManager;
import com.matibag.presentlast.api.LoginResponse;
import com.matibag.presentlast.ui.HomeActivity;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout usernameLayout;
    private TextInputEditText usernameInput;
    private TextInputLayout passwordLayout;
    private TextInputEditText passwordInput;
    private Button loginButton;
    private ProgressBar progressBar;
    private TextView forgotPasswordLink;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authManager = AuthManager.getInstance(this);

        // Check for existing session
        if (authManager.isLoggedIn()) {
            navigateToHome();
            return;
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        // Corrected IDs to match your Material XML exactly
        usernameLayout = findViewById(R.id.username_layout);
        usernameInput = findViewById(R.id.username_input);
        passwordLayout = findViewById(R.id.password_layout);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);
        progressBar = findViewById(R.id.progress_bar);
        forgotPasswordLink = findViewById(R.id.forgot_password_link);
    }

    private void setupListeners() {
        // Clear layout errors as the user types
        usernameInput.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                usernameLayout.setError(null);
            }
        });

        passwordInput.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                passwordLayout.setError(null);
            }
        });

        // Trigger login when user presses "Done" on the soft keyboard
        passwordInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin();
                return true;
            }
            return false;
        });

        loginButton.setOnClickListener(v -> attemptLogin());

        if (forgotPasswordLink != null) {
            forgotPasswordLink.setOnClickListener(v -> showForgotPasswordDialog());
        }
    }

    private void attemptLogin() {
        String userVal = usernameInput.getText() != null ? usernameInput.getText().toString().trim() : "";
        String passVal = passwordInput.getText() != null ? passwordInput.getText().toString() : "";

        // 1. Validate Username/Email via AuthManager
        String usernameError = AuthManager.validateLoginInput(userVal);
        if (usernameError != null) {
            usernameLayout.setError(usernameError);
            usernameInput.requestFocus();
            return;
        }

        // 2. Validate Password via AuthManager
        String passwordError = AuthManager.validatePassword(passVal);
        if (passwordError != null) {
            passwordLayout.setError(passwordError);
            passwordInput.requestFocus();
            return;
        }

        // Show loading state and disable UI
        setLoading(true);

        // Perform login
        authManager.login(userVal, passVal, new AuthManager.LoginCallback() {
            @Override
            public void onSuccess(LoginResponse.User user) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(LoginActivity.this, "Welcome, " + user.getFullName(), Toast.LENGTH_SHORT).show();
                    navigateToHome();
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    setLoading(false);
                    handleLoginError(errorMessage);
                });
            }

            @Override
            public void onRoleNotAllowed(String role) {
                runOnUiThread(() -> {
                    setLoading(false);
                    showRoleNotAllowedDialog(role);
                });
            }
        });
    }

    private void handleLoginError(String message) {
        String lower = message.toLowerCase();
        if (lower.contains("password")) {
            passwordLayout.setError(message);
        } else if (lower.contains("username") || lower.contains("email") || lower.contains("account")) {
            usernameLayout.setError(message);
        } else {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    private void showForgotPasswordDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Forgot Password")
                .setMessage("Please contact your administrator to reset your credentials.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showRoleNotAllowedDialog(String role) {
        String roleDisplay = role.substring(0, 1).toUpperCase() + role.substring(1).toLowerCase();
        new AlertDialog.Builder(this)
                .setTitle("Student App Only")
                .setMessage(roleDisplay + " accounts cannot log in here. Please use the Web Portal instead.")
                .setPositiveButton("OK", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!isLoading);
        loginButton.setText(isLoading ? "" : "Sign In");
        usernameInput.setEnabled(!isLoading);
        passwordInput.setEnabled(!isLoading);
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Helper abstract class to clean up TextWatcher boilerplate
    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void afterTextChanged(Editable s) {}
    }
}