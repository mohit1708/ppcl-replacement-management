<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Verify OTP - Courier Login</title>
    <link rel="stylesheet" type="text/css" href="https://cdn.jsdelivr.net/npm/bootstrap@4.6.2/dist/css/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/ace-v3.1.1/dist/css/ace-font.css">
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/ace-v3.1.1/dist/css/ace.css">
    <style>
        body { background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%); min-height: 100vh; display: flex; align-items: center; }
        .otp-input { text-align: center; font-size: 1.5rem; letter-spacing: 8px; font-weight: 600; }
    </style>
</head>
<body>
    <div class="container">
        <div class="card shadow-lg border-0 mx-auto" style="max-width:420px; border-radius:12px;">
            <div class="card-body p-5">
                <div class="text-center mb-4">
                    <i class="fas fa-shield-alt fa-3x text-success d-block mb-2"></i>
                    <h3 class="font-weight-bold text-dark">Verify OTP</h3>
                    <p class="text-secondary text-95">Enter the 4-digit OTP sent to your WhatsApp</p>
                </div>

                <c:if test="${not empty error}">
                    <div class="alert alert-danger alert-dismissible fade show" role="alert">
                        <i class="fas fa-exclamation-circle mr-1"></i> ${error}
                        <button type="button" class="close" data-dismiss="alert"><span>&times;</span></button>
                    </div>
                </c:if>

                <c:if test="${not empty success}">
                    <div class="alert alert-success alert-dismissible fade show" role="alert">
                        <i class="fas fa-check-circle mr-1"></i> ${success}
                        <button type="button" class="close" data-dismiss="alert"><span>&times;</span></button>
                    </div>
                </c:if>

                <form action="<%= request.getContextPath() %>/CourierVerifyOtp.do" method="post">
                    <input type="hidden" name="courierId" value="${courierId}">
                    <input type="hidden" name="mobileNumber" value="${mobileNumber}">

                    <div class="form-group">
                        <label class="font-weight-bold text-dark-m1">OTP</label>
                        <input type="text" name="otp" class="form-control otp-input"
                               placeholder="_ _ _ _"
                               required maxlength="4" pattern="[0-9]{4}"
                               title="Please enter a 4-digit OTP" autofocus>
                        <small class="form-text text-muted">
                            <i class="fas fa-clock mr-1"></i> OTP is valid for 10 minutes. Max 3 attempts.
                        </small>
                    </div>

                    <button type="submit" class="btn btn-success btn-block mt-4 py-2 font-weight-bold">
                        <i class="fas fa-check-circle mr-1"></i> Verify OTP
                    </button>

                    <div class="text-center mt-3">
                        <a href="<%= request.getContextPath() %>/CourierForgotPassword.do" class="text-primary text-95">
                            <i class="fas fa-redo mr-1"></i> Resend OTP
                        </a>
                        <span class="mx-2 text-muted">|</span>
                        <a href="<%= request.getContextPath() %>/CourierLoginOtp.do" class="text-primary text-95">
                            <i class="fas fa-arrow-left mr-1"></i> Back to Login
                        </a>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@4.6.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
