package com.matibag.presentlast.api;

import com.matibag.presentlast.api.models.StudentAttendanceResponse;
import com.matibag.presentlast.api.models.StudentGradesResponse;
import com.matibag.presentlast.api.models.StudentInboxResponse;
import com.matibag.presentlast.api.models.StudentProfileResponse;
import com.matibag.presentlast.api.models.StudentSubjectsResponse;
import com.matibag.presentlast.api.models.SubjectAttendanceDetailResponse;
import com.matibag.presentlast.api.models.SubjectDetailResponse;
import com.matibag.presentlast.api.models.SubjectGradesDetailResponse;
import com.matibag.presentlast.api.models.SubmissionRequest;
import com.matibag.presentlast.api.models.SubmissionResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

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
    // STUDENT INBOX (Tasks, Grades, Attendance updates)
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
    // STUDENT SUBMISSIONS
    // ============================================================

    @POST("api/student/submissions")
    Call<SubmissionResponse> submitAssignment(@Body SubmissionRequest request);
}