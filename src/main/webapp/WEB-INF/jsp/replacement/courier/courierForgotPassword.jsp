<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Forgot Password - Courier Login</title>
    <link rel="stylesheet" type="text/css" href="https://cdn.jsdelivr.net/npm/bootstrap@4.6.2/dist/css/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/ace-v3.1.1/dist/css/ace-font.css">
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/ace-v3.1.1/dist/css/ace.css">
    <style>
        body { background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%); min-height: 100vh; display: flex; align-items: center; }
    </style>
</head>
<body>
    <div class="container">
        <div class="card shadow-lg border-0 mx-auto" style="max-width:420px; border-radius:12px;">
            <div class="card-body p-5">
                <div class="text-center mb-4">
                    <i class="fas fa-key fa-3x text-warning d-block mb-2"></i>
                    <h3 class="font-weight-bold text-dark">Forgot Password</h3>
                    <p class="text-secondary text-95">Enter your registered mobile number to receive an OTP</p>
                </div>

                <c:if test="${not empty error}">
                    <div class="alert alert-danger alert-dismissible fade show" role="alert">
                        <i class="fas fa-exclamation-circle mr-1"></i> ${error}
                        <button type="button" class="close" data-dismiss="alert"><span>&times;</span></button>
                    </div>
                </c:if>

                <form action="<%= request.getContextPath() %>/CourierForgotPassword.do" method="post">
                    <div class="form-group">
                        <label class="font-weight-bold text-dark-m1">Mobile Number</label>
                        <div class="input-group">
                            <div class="input-group-prepend">
                                <span class="input-group-text bg-transparent"><i class="fas fa-mobile-alt text-muted"></i></span>
                            </div>
                            <input type="text" name="mobileNumber" class="form-control"
                                   placeholder="Enter 10-digit mobile number"
                                   required maxlength="10" pattern="[0-9]{10}"
                                   title="Please enter a valid 10-digit mobile number">
                        </div>
                        <small class="form-text text-muted">An OTP will be sent to this number via WhatsApp.</small>
                    </div>

                    <button type="submit" class="btn btn-warning btn-block mt-4 py-2 font-weight-bold text-white">
                        <i class="fas fa-paper-plane mr-1"></i> Send OTP
                    </button>

                    <div class="text-center mt-3">
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
