package com.veristay.app;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Models {

    // =============================================
    // API Response wrapper
    // =============================================

    public static class ApiResponse<T> {
        @SerializedName("data")    public T data;
        @SerializedName("success") public boolean success;
        @SerializedName("message") public String message;
        @SerializedName("details") public Object details;
    }

    // =============================================
    // Auth
    // =============================================

    public static class LoginRequest {
        @SerializedName("email")    public String email;
        @SerializedName("password") public String password;

        public LoginRequest(String email, String password) {
            this.email    = email;
            this.password = password;
        }
    }

    public static class LoginResponse {
        @SerializedName("id")       public String userId;
        @SerializedName("userName") public String userName;
        @SerializedName("email")    public String email;
        @SerializedName("token")    public String token;
        // role and fullName not in AuthResponse — we'll get them from dashboard
    }

    // =============================================
    // Student Dashboard
    // =============================================

    public static class StudentDashboard {
        @SerializedName("student")           public Profile student;
        @SerializedName("applicationsCount") public int applicationsCount;
        @SerializedName("approvedCount")     public int approvedCount;
        @SerializedName("paymentsCount")     public int paymentsCount;
        @SerializedName("requestsCount")     public int requestsCount;
        @SerializedName("activeTenancy")     public Tenancy activeTenancy;
        @SerializedName("applications")      public List<Application> applications;
        @SerializedName("waitingList")       public List<Application> waitingList;
        @SerializedName("announcementDtos")  public List<Announcement> announcements;
    }

    // =============================================
    // ProfileDto
    // =============================================

    public static class Profile {
        @SerializedName("id")            public String id;
        @SerializedName("fullName")      public String fullName;
        @SerializedName("email")         public String email;
        @SerializedName("studentNumber") public String studentNumber;  // ✅ added
        @SerializedName("university")    public String university;     // ✅ added
        @SerializedName("phoneNumber")   public String phoneNumber;
        @SerializedName("budget")        public double budget;
    }

    // =============================================
    // ApplicationDto
    // =============================================

    public static class Application {
        @SerializedName("id")                    public int id;
        @SerializedName("studentId")             public String studentId;
        @SerializedName("studentName")           public String studentName;
        @SerializedName("propertyId")            public int propertyId;
        @SerializedName("propertyTitle")         public String propertyTitle;
        @SerializedName("propertyDescription")   public String propertyDescription;  // ✅ added
        @SerializedName("propertyLocation")      public String propertyLocation;
        @SerializedName("price")                 public double price;
        @SerializedName("status")                public int status; // 0=Pending,1=Approved,2=Rejected
        @SerializedName("supportingDocumentUrl") public String supportingDocumentUrl; // ✅ added
        @SerializedName("landlordNotes")         public String landlordNotes;         // ✅ added
        @SerializedName("landlordName")          public String landlordName;          // ✅ added
        @SerializedName("fromDate")              public String fromDate;              // ✅ added
        @SerializedName("toDate")                public String toDate;                // ✅ added
        @SerializedName("appliedAt")             public String appliedAt;
        @SerializedName("reviewedAt")            public String reviewedAt;
    }

    // =============================================
    // TenancyDto
    // =============================================

    public static class Tenancy {
        @SerializedName("id")             public int id;
        @SerializedName("studentId")      public String studentId;
        @SerializedName("studentName")    public String studentName;
        @SerializedName("landlordName")   public String landlordName;
        @SerializedName("location")       public String location;
        @SerializedName("leaseDocument")  public String leaseDocument;
        @SerializedName("propertyId")     public int propertyId;
        @SerializedName("propertyTitle")  public String propertyTitle;
        @SerializedName("leaseStartDate") public String leaseStartDate;
        @SerializedName("leaseEndDate")   public String leaseEndDate;
        @SerializedName("monthlyRent")    public double monthlyRent;
        @SerializedName("status")         public int status;
    }

    // =============================================
    // RentPayment
    // =============================================

    public static class RentPayment {
        @SerializedName("id")               public int id;
        @SerializedName("tenancyId")        public int tenancyId;
        @SerializedName("studentName")      public String studentName;
        @SerializedName("propertyTitle")    public String propertyTitle;
        @SerializedName("propertyLocation") public String propertyLocation;
        @SerializedName("amount")           public double amount;
        @SerializedName("dueDate")          public String dueDate;
        @SerializedName("paidAt")           public String paidAt;
        @SerializedName("status")           public int status; // 0=Pending,1=Paid,2=Overdue
        @SerializedName("receiptUrl")       public String receiptUrl;
    }

    public static class StudentPaymentSummary {
        @SerializedName("tenancyId")        public int tenancyId;
        @SerializedName("propertyTitle")    public String propertyTitle;
        @SerializedName("propertyLocation") public String propertyLocation;
        @SerializedName("monthlyRent")      public double monthlyRent;
        @SerializedName("totalPayments")    public int totalPayments;
        @SerializedName("paidCount")        public int paidCount;
        @SerializedName("pendingCount")     public int pendingCount;
        @SerializedName("overdueCount")     public int overdueCount;
        @SerializedName("totalPaid")        public double totalPaid;
        @SerializedName("totalOwed")        public double totalOwed;
        @SerializedName("payments")         public List<RentPayment> payments;
    }

    // =============================================
    // MaintenanceRequest
    // =============================================

    public static class MaintenanceRequest {
        @SerializedName("id")               public int id;
        @SerializedName("studentId")        public String studentId;
        @SerializedName("propertyId")       public int propertyId;
        @SerializedName("propertyTitle")    public String propertyTitle;
        @SerializedName("title")            public String title;
        @SerializedName("description")      public String description;
        @SerializedName("priority")         public int priority; // 0=Low,1=Medium,2=High,3=Emergency
        @SerializedName("status")           public int status;   // 0=Open,1=InProgress,2=Resolved
        @SerializedName("landlordResponse") public String landlordResponse;
        @SerializedName("submittedAt")      public String submittedAt;
    }

    public static class AddMaintenanceRequest {

        @SerializedName("propertyId")
        public int propertyId;

        @SerializedName("title")
        public String title;

        @SerializedName("description")
        public String description;

        @SerializedName("priority")
        public int priority;

        public AddMaintenanceRequest(
                int propertyId,
                String title,
                String description,
                int priority) {

            this.propertyId = propertyId;
            this.title = title;
            this.description = description;
            this.priority = priority;
        }
    }

    public static class Announcement {
        @SerializedName("id")            public int    id;
        @SerializedName("landlordId")    public String landlordId;
        @SerializedName("landlordName")  public String landlordName;
        @SerializedName("propertyId")    public int    propertyId;
        @SerializedName("propertyTitle") public String propertyTitle;
        @SerializedName("message")       public String message;
        @SerializedName("postedAt")      public String postedAt;
    }

    public static class AppNotification {
        @SerializedName("id")        public int     id;
        @SerializedName("userId")    public String  userId;
        @SerializedName("title")     public String  title;
        @SerializedName("message")   public String  message;
        @SerializedName("type")      public String  type;
        @SerializedName("isRead")    public boolean isRead;
        @SerializedName("createdAt") public String  createdAt;
    }
}