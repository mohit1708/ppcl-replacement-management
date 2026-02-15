<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Courier Login - PPCL PRM</title>
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
                    <i class="fas fa-truck fa-3x text-primary d-block mb-2"></i>
                    <h3 class="font-weight-bold text-dark">Courier Login</h3>
                    <p class="text-secondary text-95">PPCL Printer Replacement Management</p>
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

                <form action="<%= request.getContextPath() %>/CourierLoginOtp.do" method="post">
                    <input type="hidden" name="encryptedCourierId" value="${encryptedCourierId}">

                    <div class="form-group">
                        <label class="font-weight-bold text-dark-m1">Mobile Number</label>
                        <div class="input-group">
                            <div class="input-group-prepend">
                                <span class="input-group-text bg-transparent"><i class="fas fa-mobile-alt text-muted"></i></span>
                            </div>
                            <input type="text" name="mobileNumber" class="form-control"
                                   placeholder="Enter 10-digit mobile number"
                                   value="${mobileNumber}" required maxlength="10" pattern="[0-9]{10}"
                                   title="Please enter a valid 10-digit mobile number">
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="font-weight-bold text-dark-m1">Password</label>
                        <div class="input-group">
                            <div class="input-group-prepend">
                                <span class="input-group-text bg-transparent"><i class="fas fa-lock text-muted"></i></span>
                            </div>
                            <input type="password" name="password" class="form-control"
                                   placeholder="Enter your password" required>
                        </div>
                    </div>

                    <div class="form-group d-flex justify-content-between align-items-center">
                        <div class="custom-control custom-checkbox">
                            <input type="checkbox" class="custom-control-input" id="rememberMe" name="rememberMe" value="on">
                            <label class="custom-control-label text-95" for="rememberMe">Remember Me</label>
                        </div>
                        <a href="<%= request.getContextPath() %>/CourierForgotPassword.do" class="text-primary text-95">
                            <i class="fas fa-key mr-1"></i> Forgot Password?
                        </a>
                    </div>

                    <button type="submit" class="btn btn-primary btn-block mt-2 py-2 font-weight-bold">
                        <i class="fas fa-sign-in-alt mr-1"></i> Login
                    </button>
                </form>
            </div>
        </div>
    </div>

    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@4.6.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
