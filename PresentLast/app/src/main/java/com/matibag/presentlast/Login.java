package com.matibag.presentlast;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class Login extends Activity {

    EditText emailinp, passinp;
    Button login;
    TextView forgotPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        emailinp = findViewById(R.id.edtEmail);
        passinp = findViewById(R.id.edtPassword);
        login = findViewById(R.id.btnLogin);
        forgotPassword = findViewById(R.id.txtForgotPassword);

        // Login button click
        login.setOnClickListener(view -> {
            String email = emailinp.getText().toString().trim();
            String password = passinp.getText().toString().trim();

            // Validate empty fields
            if(email.isEmpty() || password.isEmpty()){
                if(email.isEmpty()) {
                    emailinp.setError("Email is required");
                }
                if(password.isEmpty()) {
                    passinp.setError("Password is required");
                }
                return;
            }

            // Validate email format
            if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailinp.setError("Please enter a valid email");
                return;
            }

            // TODO: Verify credentials with database
            // For now, using hardcoded credentials for testing

            // Check if user exists in database
            boolean isValidUser = verifyUserCredentials(email, password);

            if(isValidUser) {
                // Get user role from database
                String userRole = getUserRole(email);

                // Redirect to dashboard based on role
                Intent intent = new Intent(Login.this, home.class);
                intent.putExtra("userEmail", email);
                intent.putExtra("userRole", userRole);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                passinp.setText("");
            }
        });

        // Forgot password click
        forgotPassword.setOnClickListener(view -> {
            showForgotPasswordDialog();
        });
    }

    /**
     * TODO FOR BACKEND:
     * Verify user credentials against database
     *
     * Query: SELECT * FROM users WHERE email = ? AND password = ?
     * (Note: In production, use hashed passwords)
     */
    private boolean verifyUserCredentials(String email, String password) {
        // Temporary hardcoded validation for testing
        // Replace with actual database query

        if(email.equals("student@school.edu") && password.equals("student123")) {
            return true;
        }
        if(email.equals("teacher@school.edu") && password.equals("teacher123")) {
            return true;
        }

        return false;
    }

    /**
     * TODO FOR BACKEND:
     * Get user role from database
     *
     * Query: SELECT role FROM users WHERE email = ?
     * Returns: "student" or "teacher"
     */
    private String getUserRole(String email) {
        // Temporary role assignment for testing
        // Replace with actual database query

        if(email.equals("student@school.edu")) {
            return "student";
        }
        if(email.equals("teacher@school.edu")) {
            return "teacher";
        }

        return "student"; // Default
    }

    /**
     * Show forgot password dialog
     * Sends password reset request to admin
     */
    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Forgot Password");

        // Create input field for email
        final EditText input = new EditText(this);
        input.setHint("Enter your email");
        input.setPadding(50, 30, 50, 30);
        builder.setView(input);

        builder.setPositiveButton("Submit", (dialog, which) -> {
            String email = input.getText().toString().trim();

            if(email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }

            if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: Send password reset request to admin
            sendPasswordResetRequest(email);

            // Show success message
            new AlertDialog.Builder(this)
                    .setTitle("Request Sent!")
                    .setMessage("Your password reset request has been sent to the administrator. You will receive instructions via inbox once approved.")
                    .setPositiveButton("OK", null)
                    .show();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    /**
     * TODO FOR BACKEND:
     * Send password reset request to admin
     *
     * INSERT INTO password_reset_requests (email, request_date, status)
     * VALUES (?, CURRENT_TIMESTAMP, 'pending')
     *
     * Also create a notification for admin
     */
    private void sendPasswordResetRequest(String email) {
        // TODO: Implement database insertion
        // This should:
        // 1. Insert request into password_reset_requests table
        // 2. Create notification for admin
        // 3. Store user email for later processing
    }
}