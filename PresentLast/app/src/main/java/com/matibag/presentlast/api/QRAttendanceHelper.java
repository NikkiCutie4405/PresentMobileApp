package com.matibag.presentlast.api;

import android.util.Log;
import androidx.annotation.NonNull;

import com.matibag.presentlast.api.models.QRMarkAttendanceRequest;
import com.matibag.presentlast.api.models.QRMarkAttendanceResponse;
import com.matibag.presentlast.api.models.QRValidateResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QRAttendanceHelper {

    private static final String TAG = "QRAttendanceHelper";

    public interface ValidateCallback {
        void onValid(QRValidateResponse.SessionInfo session, String willBeMarkedAs);
        void onExpired();
        void onInvalid(String errorMessage);
        void onError(String errorMessage);
    }

    public interface MarkCallback {
        void onSuccess(String status, String message, boolean alreadyMarked);
        void onNotEnrolled();
        void onExpired();
        void onError(String errorMessage);
    }

    /**
     * Extracts token from QR content.
     * Handles formats like: https://domain.com/scan?token=abc123 or raw tokens.
     */
    public static String extractTokenFromQR(String qrContent) {
        if (qrContent == null || qrContent.isEmpty()) return null;

        if (qrContent.contains("token=")) {
            int tokenIndex = qrContent.indexOf("token=") + 6;
            String token = qrContent.substring(tokenIndex);
            int ampIndex = token.indexOf('&');
            if (ampIndex > 0) token = token.substring(0, ampIndex);
            return token;
        }

        // Return if it looks like a direct hash/token
        if (qrContent.length() >= 32) return qrContent;

        return null;
    }

    public static void validateQRToken(String qrContent, @NonNull ValidateCallback callback) {
        String token = extractTokenFromQR(qrContent);
        if (token == null) {
            callback.onInvalid("Invalid QR code format");
            return;
        }

        ApiClient.getApiService().validateQRToken(token).enqueue(new Callback<QRValidateResponse>() {
            @Override
            public void onResponse(@NonNull Call<QRValidateResponse> call, @NonNull Response<QRValidateResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    QRValidateResponse res = response.body();
                    if (res.isSuccess() && res.getSession() != null) {
                        callback.onValid(res.getSession(), res.getWillBeMarkedAs());
                    } else {
                        String err = res.getError();
                        if (err != null && err.toLowerCase().contains("expired")) {
                            callback.onExpired();
                        } else {
                            callback.onInvalid(err != null ? err : "Invalid QR code");
                        }
                    }
                } else {
                    handleErrorResponse(response.code(), callback);
                }
            }

            @Override
            public void onFailure(@NonNull Call<QRValidateResponse> call, @NonNull Throwable t) {
                callback.onError("Connection error: " + t.getMessage());
            }
        });
    }

    private static void handleErrorResponse(int code, ValidateCallback callback) {
        if (code == 410) callback.onExpired();
        else if (code == 404) callback.onInvalid("Invalid QR code");
        else callback.onError("Server Error (" + code + ")");
    }

    public static void markAttendance(String qrContent, int studentId, @NonNull MarkCallback callback) {
        String token = extractTokenFromQR(qrContent);
        if (token == null) {
            callback.onError("Invalid QR code format");
            return;
        }

        QRMarkAttendanceRequest request = new QRMarkAttendanceRequest(token, studentId);
        ApiClient.getApiService().markQRAttendance(request).enqueue(new Callback<QRMarkAttendanceResponse>() {
            @Override
            public void onResponse(@NonNull Call<QRMarkAttendanceResponse> call, @NonNull Response<QRMarkAttendanceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    QRMarkAttendanceResponse res = response.body();
                    if (res.isSuccess()) {
                        callback.onSuccess(res.getStatus(), res.getMessage(), res.isAlreadyMarked());
                    } else {
                        handleMarkError(res.getError(), callback);
                    }
                } else {
                    handleMarkHttpError(response.code(), callback);
                }
            }

            @Override
            public void onFailure(@NonNull Call<QRMarkAttendanceResponse> call, @NonNull Throwable t) {
                callback.onError("Connection error: " + t.getMessage());
            }
        });
    }

    private static void handleMarkError(String error, MarkCallback callback) {
        if (error == null) {
            callback.onError("Failed to mark attendance");
            return;
        }
        String lowErr = error.toLowerCase();
        if (lowErr.contains("expired")) callback.onExpired();
        else if (lowErr.contains("not enrolled")) callback.onNotEnrolled();
        else callback.onError(error);
    }

    private static void handleMarkHttpError(int code, MarkCallback callback) {
        if (code == 410) callback.onExpired();
        else if (code == 403) callback.onNotEnrolled();
        else callback.onError("Server Error (" + code + ")");
    }
}