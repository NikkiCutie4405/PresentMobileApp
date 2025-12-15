package com.matibag.presentlast;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.matibag.presentlast.api.ApiClient;
import com.matibag.presentlast.api.ApiService;
import com.matibag.presentlast.api.LoginRequest;
import com.matibag.presentlast.api.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login extends Activity {

    private static final String TAG = "LoginActivity";

    EditText emailinp, passinp;
    Button login;
    TextView forgotPassword;
    ApiService apiService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        Log.d(TAG, "onCreate: Login activity started");

        // Initialize API service
        apiService = ApiClient.getApiService();
        Log.d(TAG, "onCreate: API service initialized");

        emailinp = findViewById(R.id.edtEmail);
        passinp = findViewById(R.id.edtPassword);
        login = findViewById(R.id.btnLogin);
        forgotPassword = findViewById(R.id.txtForgotPassword);

        // Login button click
        login.setOnClickListener(view -> {
            String email = emailinp.getText().toString().trim();
            String password = passinp.getText().toString().trim();

            Log.d(TAG, "Login button clicked");
            Log.d(TAG, "Email: " + email);
            Log.d(TAG, "Password length: " + password.length());

            // Validate empty fields
            if (email.isEmpty() || password.isEmpty()) {
                Log.w(TAG, "Validation failed: empty fields");
                if (email. isEmpty()) {
                    emailinp.setError("Email is required");
                }
                if (password.isEmpty()) {
                    passinp.setError("Password is required");
                }
                return;
            }

            // Disable button while loading
            login.setEnabled(false);
            login.setText("Signing in.. .");

            Log.d(TAG, "Starting login API call.. .");

            // Call login API
            performLogin(email, password);
        });

        // Forgot password click
        forgotPassword.setOnClickListener(view -> {
            showForgotPasswordDialog();
        });
    }

    private void performLogin(String email, String password) {
        Log.d(TAG, "performLogin: Creating login request");

        LoginRequest request = new LoginRequest(email, password);

        Log.d(TAG, "performLogin: Sending request to server");

        apiService.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                Log.d(TAG, "onResponse:  Received response");
                Log.d(TAG, "onResponse: HTTP Code = " + response.code());
                Log.d(TAG, "onResponse: isSuccessful = " + response.isSuccessful());
                Log.d(TAG, "onResponse: URL = " + call.request().url());

                // Re-enable button
                login. setEnabled(true);
                login.setText("Login");

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    Log.d(TAG, "onResponse: Body received, success = " + loginResponse.isSuccess());

                    if (loginResponse.isSuccess()) {
                        Log. d(TAG, "onResponse:  Login successful!");
                        LoginResponse.User user = loginResponse. getUser();
                        Log. d(TAG, "onResponse:  User role = " + user.getRole());
                        Log.d(TAG, "onResponse: Username = " + user.getUsername());

                        Intent intent = new Intent(Login.this, home.class);
                        intent.putExtra("userId", user.getId());
                        intent.putExtra("userEmail", user.getEmail());
                        intent.putExtra("username", user.getUsername());
                        intent.putExtra("userRole", user.getRole());
                        startActivity(intent);
                        finish();
                    } else {
                        Log.w(TAG, "onResponse: Login failed - " + loginResponse.getError());
                        Toast.makeText(Login.this,
                                loginResponse.getError() != null ? loginResponse.getError() : "Invalid credentials",
                                Toast.LENGTH_SHORT).show();
                        passinp.setText("");
                    }
                } else {
                    Log.e(TAG, "onResponse: Response not successful");
                    Log.e(TAG, "onResponse: Error body = " + response.errorBody());
                    try {
                        if (response.errorBody() != null) {
                            Log.e(TAG, "onResponse: Error = " + response.errorBody().string());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "onResponse: Could not read error body", e);
                    }
                    Toast.makeText(Login.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                    passinp.setText("");
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e(TAG, "onFailure:  Request failed!");
                Log.e(TAG, "onFailure: URL = " + call.request().url());
                Log.e(TAG, "onFailure: Error message = " + t.getMessage());
                Log.e(TAG, "onFailure: Error class = " + t.getClass().getSimpleName());
                t.printStackTrace();

                // Re-enable button
                login.setEnabled(true);
                login.setText("Login");

                // Network error
                Toast. makeText(Login.this, "Connection error:  " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Forgot Password");

        final EditText input = new EditText(this);
        input.setHint("Enter your email");
        input.setPadding(50, 30, 50, 30);
        builder.setView(input);

        builder.setPositiveButton("Submit", (dialog, which) -> {
            String email = input.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS. matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
                return;
            }

            new AlertDialog.Builder(this)
                    .setTitle("Request Sent!")
                    .setMessage("Your password reset request has been sent to the administrator.")
                    . setPositiveButton("OK", null)
                    .show();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}