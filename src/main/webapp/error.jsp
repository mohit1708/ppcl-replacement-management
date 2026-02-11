<%@ page contentType="text/html;charset=UTF-8" isErrorPage="true" %>
<!DOCTYPE html>
<html>
<head>
    <title>Error</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@4.6.2/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
    <div class="container mt-5">
        <div class="alert alert-danger">
            <h4>Error Occurred</h4>
            <p><%= exception != null ? exception.getMessage() : "An unexpected error occurred" %></p>
            <a href="<%= request.getContextPath() %>/views/replacement/dashboard" class="btn btn-primary">Go to Dashboard</a>
        </div>
    </div>
</body>
</html>