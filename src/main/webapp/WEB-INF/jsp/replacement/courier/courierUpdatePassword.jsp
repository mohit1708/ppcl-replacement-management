<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Update Password - Courier Login</title>
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
                    <i class="fas fa-user-shield fa-3x text-warning d-block mb-2"></i>
                    <h3 class="font-weight-bold text-dark">Update Password</h3>
                    <p class="text-secondary text-95">Please set a new password for your account</p>
                </div>

                <div class="alert alert-warning mb-3">
                    <i class="fas fa-info-circle mr-1"></i>
                    <strong>First Login:</strong> You must set a new password before continuing.
                </div>

                <c:if test="${not empty error}">
                    <div class="alert alert-danger alert-dismissible fade show" role="alert">
                        <i class="fas fa-exclamation-circle mr-1"></i> ${error}
                        <button type="button" class="close" data-dismiss="alert"><span>&times;</span></button>
                    </div>
                </c:if>

                <form action="<%= request.getContextPath() %>/CourierUpdatePassword.do" method="post" id="updateForm">
                    <div class="form-group">
                        <label class="font-weight-bold text-dark-m1">New Password</label>
                        <div class="input-group">
                            <div class="input-group-prepend">
                                <span class="input-group-text bg-transparent"><i class="fas fa-lock text-muted"></i></span>
                            </div>
                            <input type="password" name="newPassword" id="newPassword" class="form-control"
                                   placeholder="Enter new password" required minlength="4">
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="font-weight-bold text-dark-m1">Confirm Password</label>
                        <div class="input-group">
                            <div class="input-group-prepend">
                                <span class="input-group-text bg-transparent"><i class="fas fa-lock text-muted"></i></span>
                            </div>
                            <input type="password" name="confirmPassword" id="confirmPassword" class="form-control"
                                   placeholder="Confirm new password" required minlength="4">
                        </div>
                        <small id="passwordMatch" class="form-text"></small>
                    </div>

                    <button type="submit" class="btn btn-warning btn-block mt-4 py-2 font-weight-bold text-white" id="updateBtn" disabled>
                        <i class="fas fa-save mr-1"></i> Update Password & Continue
                    </button>
                </form>
            </div>
        </div>
    </div>

    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@4.6.2/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        $(document).ready(function() {
            $('#confirmPassword, #newPassword').on('keyup', function() {
                var pw = $('#newPassword').val();
                var cpw = $('#confirmPassword').val();
                if (cpw.length === 0) {
                    $('#passwordMatch').text('').removeClass('text-success text-danger');
                    $('#updateBtn').prop('disabled', true);
                } else if (pw === cpw) {
                    $('#passwordMatch').html('<i class="fas fa-check-circle"></i> Passwords match').removeClass('text-danger').addClass('text-success');
                    $('#updateBtn').prop('disabled', false);
                } else {
                    $('#passwordMatch').html('<i class="fas fa-times-circle"></i> Passwords do not match').removeClass('text-success').addClass('text-danger');
                    $('#updateBtn').prop('disabled', true);
                }
            });
        });
    </script>
</body>
</html>
