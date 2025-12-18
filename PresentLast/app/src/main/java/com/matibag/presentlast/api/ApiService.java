package com.matibag.presentlast.api;

import com.matibag.presentlast.api.models.*;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    // ============================================================
    // AUTHENTICATION
    // ============================================================

    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    // ============================================================
    // STUDENT PROFILE
    // ============================================================

    @GET("api/student/profile")
    Call<StudentProfileResponse> getStudentProfile(@Query("userId") int userId);

    // ============================================================
    // STUDENT SUBJECTS
    // ============================================================

    @GET("api/student/subjects")
    Call<StudentSubjectsResponse> getStudentSubjects(
            @Query("studentId") int studentId,
            @Query("semester") String semester,
            @Query("year") String year
    );

    @GET("api/student/subjects/{id}")
    Call<SubjectDetailResponse> getSubjectDetail(
            @Path("id") int subjectId,
            @Query("studentId") int studentId
    );

    // ============================================================
    // STUDENT INBOX
    // ============================================================

    @GET("api/student/inbox")
    Call<StudentInboxResponse> getStudentInbox(
            @Query("studentId") int studentId,
            @Query("limit") int limit
    );

    // ============================================================
    // STUDENT GRADES
    // ============================================================

    @GET("api/student/grades")
    Call<StudentGradesResponse> getStudentGrades(
            @Query("studentId") int studentId,
            @Query("semester") String semester,
            @Query("year") String year
    );

    @GET("api/student/grades/{subjectId}")
    Call<SubjectGradesDetailResponse> getSubjectGradesDetail(
            @Path("subjectId") int subjectId,
            @Query("studentId") int studentId
    );

    // ============================================================
    // STUDENT ATTENDANCE
    // ============================================================

    @GET("api/student/attendance")
    Call<StudentAttendanceResponse> getStudentAttendance(
            @Query("studentId") int studentId,
            @Query("semester") String semester,
            @Query("year") String year
    );

    @GET("api/student/attendance/{subjectId}")
    Call<SubjectAttendanceDetailResponse> getSubjectAttendanceDetail(
            @Path("subjectId") int subjectId,
            @Query("studentId") int studentId
    );

    // ============================================================
    // QR ATTENDANCE
    // ============================================================

    @GET("api/attendance/qr")
    Call<QRValidateResponse> validateQRToken(@Query("token") String token);

    @PUT("api/attendance/qr")
    Call<QRMarkAttendanceResponse> markQRAttendance(@Body QRMarkAttendanceRequest request);

    // ============================================================
    // FILE UPLOAD (R2 STORAGE)
    // ============================================================

    @GET("api/upload")
    Call<UploadStatusResponse> getUploadStatus();

    @Multipart
    @POST("api/upload")
    Call<FileUploadResponse> uploadFile(
            @Part MultipartBody.Part file,
            @Part("folder") RequestBody folder,
            @Part("studentId") RequestBody studentId,
            @Part("submissionId") RequestBody submissionId
    );

    // ============================================================
    // STUDENT SUBMISSIONS
    // ============================================================

    @POST("api/student/submissions")
    Call<SubmissionResponse> submitAssignment(@Body SubmissionRequest request);

    @GET("api/student/submissions")
    Call<StudentSubmissionResponse> getStudentSubmission(
            @Query("submissionId") int submissionId,
            @Query("studentId") int studentId
    );
}