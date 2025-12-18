package com.matibag.presentlast.api;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.matibag.presentlast.api.models.FileUploadResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FileUploadHelper {

    private static final String TAG = "FileUploadHelper";
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB limit

    public interface UploadCallback {
        void onSuccess(FileUploadResponse.FileInfo fileInfo);
        void onError(String errorMessage);
        void onProgress(int percentage);
    }

    /**
     * Uploads a file from a Uri to the server by creating a temporary local cache file.
     */
    public static void uploadFile(
            Context context,
            Uri fileUri,
            int studentId,
            int submissionId,
            @NonNull UploadCallback callback
    ) {
        try {
            ContentResolver contentResolver = context.getContentResolver();

            // 1. Get File Metadata
            String fileName = getFileName(context, fileUri);
            String mimeType = contentResolver.getType(fileUri);
            if (mimeType == null) {
                mimeType = getMimeTypeFromExtension(fileName);
            }

            // 2. Validate File Size
            long fileSize = getFileSize(context, fileUri);
            if (fileSize > MAX_FILE_SIZE) {
                callback.onError("File size exceeds 10MB limit");
                return;
            }

            // 3. Create Temporary Cache File (Required for Scoped Storage)
            File tempFile = createTempFile(context, fileUri, fileName);
            if (tempFile == null) {
                callback.onError("Failed to process file for upload");
                return;
            }

            // 4. Build Multipart Request
            RequestBody requestBody = RequestBody.create(
                    MediaType.parse(mimeType != null ? mimeType : "application/octet-stream"),
                    tempFile
            );

            MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", fileName, requestBody);
            RequestBody folderPart = RequestBody.create(MediaType.parse("text/plain"), "student-submissions");
            RequestBody studentIdPart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(studentId));
            RequestBody submissionIdPart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(submissionId));

            // 5. Execute API Call
            ApiClient.getApiService().uploadFile(
                    filePart,
                    folderPart,
                    studentIdPart,
                    submissionIdPart
            ).enqueue(new Callback<FileUploadResponse>() {
                @Override
                public void onResponse(@NonNull Call<FileUploadResponse> call, @NonNull Response<FileUploadResponse> response) {
                    // Clean up: delete temp file regardless of result
                    if (tempFile.exists()) tempFile.delete();

                    if (response.isSuccessful() && response.body() != null) {
                        FileUploadResponse uploadResponse = response.body();
                        if (uploadResponse.isSuccess() && uploadResponse.getFile() != null) {
                            callback.onSuccess(uploadResponse.getFile());
                        } else {
                            callback.onError(uploadResponse.getError() != null ? uploadResponse.getError() : "Upload failed");
                        }
                    } else {
                        callback.onError("Server Error: " + response.code());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<FileUploadResponse> call, @NonNull Throwable t) {
                    if (tempFile.exists()) tempFile.delete();
                    Log.e(TAG, "Upload failure", t);
                    callback.onError("Network error: " + t.getMessage());
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Unexpected upload error", e);
            callback.onError("Critical Error: " + e.getMessage());
        }
    }

    @Nullable
    public static String getFileName(Context context, Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) result = cursor.getString(index);
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result != null ? result.lastIndexOf('/') : -1;
            if (cut != -1) result = result.substring(cut + 1);
        }
        return result != null ? result : "file_" + System.currentTimeMillis();
    }

    public static long getFileSize(Context context, Uri uri) {
        long size = 0;
        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                    size = cursor.getLong(sizeIndex);
                }
            }
        }
        return size;
    }

    @Nullable
    private static String getMimeTypeFromExtension(String fileName) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(fileName);
        if (extension != null) {
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
        }
        return null;
    }

    @Nullable
    private static File createTempFile(Context context, Uri uri, String fileName) {
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            if (inputStream == null) return null;
            File tempFile = new File(context.getCacheDir(), fileName);
            try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            return tempFile;
        } catch (IOException e) {
            Log.e(TAG, "File cache creation failed", e);
            return null;
        }
    }
}