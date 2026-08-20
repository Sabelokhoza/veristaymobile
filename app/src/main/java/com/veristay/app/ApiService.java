package com.veristay.app;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    // ── Auth ─────────────────────────────────────────────────────────

    @POST("Auth/login")
    Call<Models.ApiResponse<Models.LoginResponse>> login(
            @Body Models.LoginRequest body);
    @GET("RentPayment/get-receipt")
    Call<Models.ApiResponse<String>> getReceipt(
            @Query("id") int paymentId);
    // Get all notifications for user
    @GET("Notifications/user/{userId}")
    Call<Models.ApiResponse<List<Models.AppNotification>>> getUserNotifications(
            @Path("userId") String userId);
    // Get announcements by landlord's property for the student
    @GET("Announcement/student/{studentId}")
    Call<Models.ApiResponse<List<Models.Announcement>>> getStudentAnnouncements(
            @Path("studentId") String studentId);
    // Get unread count
    @GET("Notifications/unread-count/{userId}")
    Call<Models.ApiResponse<Integer>> getUnreadCount(
            @Path("userId") String userId);

    // Mark single as read
    @PATCH("Notifications/{id}/read")
    Call<Models.ApiResponse<Boolean>> markAsRead(
            @Path("id") int notificationId);

    // Mark all as read
    @PATCH("Notifications/user/{userId}/read-all")
    Call<Models.ApiResponse<Boolean>> markAllRead(
            @Path("userId") String userId);

    // Delete single
    @DELETE("Notifications/{id}")
    Call<Models.ApiResponse<Boolean>> deleteNotification(
            @Path("id") int notificationId);

    // Delete all
    @DELETE("Notifications/user/{userId}/all")
    Call<Models.ApiResponse<Boolean>> deleteAllNotifications(
            @Path("userId") String userId);
    // ── Student Dashboard ─────────────────────────────────────────────

    @GET("User/get-student-dashboarddata")
    Call<Models.ApiResponse<Models.StudentDashboard>> getStudentDashboard(
            @Query("user_id") String userId);

    // ── Payments ──────────────────────────────────────────────────────

    @GET("RentPayment/get-student-summary")
   Call<Models.ApiResponse<Models.StudentPaymentSummary>> getStudentPaymentSummary(
            @Query("studentId") String studentId);

    @PATCH("RentPayment/mark-paid")
    Call<Models.ApiResponse<Models.RentPayment>> markRentPaid(
            @Body int body);

    class MarkRentPaidDto {
        public int    rentPaymentId;
        public String receiptUrl;

        public MarkRentPaidDto(int rentPaymentId) {
            this.rentPaymentId = rentPaymentId;
            this.receiptUrl    = "";
        }
    }

    @POST("User/update-fcm-token")
    Call<Models.ApiResponse<Boolean>> updateFcmToken(
            @Body UpdateFcmTokenDto body);

    class UpdateFcmTokenDto {
        public String userId;
        public String fcmToken;

        public UpdateFcmTokenDto(String userId, String fcmToken) {
            this.userId   = userId;
            this.fcmToken = fcmToken;
        }
    }

    // ── Maintenance ───────────────────────────────────────────────────

    @POST("api/MaintenanceRequest/{studentId}")
    Call<Models.ApiResponse<Models.MaintenanceRequest>> addMaintenanceRequest(
            @Path("studentId") String studentId,
            @Body Models.AddMaintenanceRequest body
    );

    @GET("MaintenanceRequest/student/{studentId}")
    Call<Models.ApiResponse<java.util.List<Models.MaintenanceRequest>>> getStudentMaintenanceRequests(
            @Path("studentId") String studentId);
}