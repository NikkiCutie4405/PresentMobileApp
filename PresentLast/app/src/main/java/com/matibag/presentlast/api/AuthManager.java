package com.matibag.presentlast.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Patterns;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Manages authentication state and login/logout operations.
 * Only allows student accounts to log in on the mobile app.
 * Supports login with either username or email address.
 */
public class AuthManager {

    private static final String TAG = "AuthManager";
    private static final String PREF_NAME = "present_auth";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_ROLE = "role";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private static AuthManager instance;
    private final SharedPreferences prefs;
    private LoginResponse.User currentUser;

    private AuthManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        loadUserFromPrefs();
    }

    public static synchronized AuthManager getInstance(Context context) {
        if (instance == null) {
            instance = new AuthManager(context);
        }
        return instance;
    }

    public interface LoginCallback {
        void onSuccess(LoginResponse.User user);
        void onError(String errorMessage);
        void onRoleNotAllowed(String role);
    }

    // ============================================================
    // VALIDATION METHODS
    // ============================================================

    public static boolean isValidEmail(String text) {
        return text != null && Patterns.EMAIL_ADDRESS.matcher(text).matches();
    }

    @Nullable
    public static String validateLoginInput(String usernameOrEmail) {
        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
            return "Please enter your username or email";
        }

        String trimmed = usernameOrEmail.trim();

        if (trimmed.contains("@")) {
            if (!isValidEmail(trimmed)) {
                return "Please enter a valid email address";
            }
        } else if (trimmed.length() < 3) {
            return "Username must be at least 3 characters";
        }

        return null;
    }

    @Nullable
    public static String validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return "Please enter your password";
        }
        return null;
    }

    // ============================================================
    // LOGIN/LOGOUT METHODS
    // ============================================================

    public void login(String usernameOrEmail, String password, @NonNull LoginCallback callback) {
        String trimmedInput = usernameOrEmail != null ? usernameOrEmail.trim() : "";
        Log.d(TAG, "Attempting login for: " + trimmedInput);

        LoginRequest request = new LoginRequest(trimmedInput, password);

        ApiClient.getApiService().login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    if (loginResponse.isSuccess() && loginResponse.getUser() != null) {
                        LoginResponse.User user = loginResponse.getUser();

                        if (!user.isStudent()) {
                            Log.w(TAG, "Login rejected: Role " + user.getRole() + " not allowed");
                            callback.onRoleNotAllowed(user.getRole());
                            return;
                        }

                        saveUser(user);
                        currentUser = user;
                        callback.onSuccess(user);
                    } else {
                        String error = loginResponse.getError() != null ? loginResponse.getError() : "Invalid credentials";
                        callback.onError(error);
                    }
                } else {
                    handleHttpError(response.code(), callback);
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                String error = "Connection failed";
                if (t instanceof java.net.UnknownHostException) {
                    error = "No internet connection";
                } else if (t instanceof java.net.SocketTimeoutException) {
                    error = "Connection timed out";
                } else if (t instanceof java.net.ConnectException) {
                    error = "Unable to connect to server";
                }
                Log.e(TAG, error, t);
                callback.onError(error);
            }
        });
    }

    private void handleHttpError(int code, LoginCallback callback) {
        String error;
        switch (code) {
            case 401: error = "Invalid username/email or password"; break;
            case 404: error = "Account not found"; break;
            case 500: error = "Server error. Please try again later."; break;
            default: error = "Login failed (Error " + code + ")";
        }
        callback.onError(error);
    }

    public void logout() {
        currentUser = null;
        prefs.edit().clear().apply();
        Log.d(TAG, "User logged out and session cleared");
    }

    // ============================================================
    // SESSION STATE METHODS
    // ============================================================

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    @Nullable
    public LoginResponse.User getCurrentUser() {
        return currentUser;
    }

    public int getCurrentUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    @Nullable
    public String getCurrentUsername() {
        return prefs.getString(KEY_USERNAME, null);
    }

    @Nullable
    public String getCurrentFullName() {
        return prefs.getString(KEY_FULL_NAME, null);
    }

    @Nullable
    public String getCurrentEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    @Nullable
    public String getCurrentRole() {
        return prefs.getString(KEY_ROLE, null);
    }

    // ============================================================
    // PRIVATE HELPER METHODS
    // ============================================================

    private void saveUser(LoginResponse.User user) {
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putInt(KEY_USER_ID, user.getId())
                .putString(KEY_USERNAME, user.getUsername())
                .putString(KEY_EMAIL, user.getEmail())
                .putString(KEY_FULL_NAME, user.getFullName())
                .putString(KEY_ROLE, user.getRole())
                .apply();
    }

    private void loadUserFromPrefs() {
        if (isLoggedIn()) {
            Log.d(TAG, "Persistent session restored for: " + getCurrentUsername());
        }
    }

    public void clearAuthData() {
        prefs.edit().clear().apply();
        currentUser = null;
    }
}