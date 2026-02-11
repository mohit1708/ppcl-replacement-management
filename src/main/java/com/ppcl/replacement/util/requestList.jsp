<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <title>Replacement Requests - Service TL View</title>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">
    <link rel="stylesheet" href="https://cdn.datatables.net/1.10.24/css/jquery.dataTables.min.css">
    <style>
        .sidebar {
            position: fixed;
            top: 0;
            bottom: 0;
            left: 0;
            padding: 0;
            box-shadow: 2px 0 5px rgba(0, 0, 0, 0.1);
        }

        .sidebar .nav-link {
            padding: 10px 15px;
            margin: 5px 10px;
            border-radius: 5px;
        }

        .sidebar .nav-link.active {
            background-color: #007bff !important;
        }

        .printer-card {
            transition: all 0.3s ease;
        }

        .printer-card:hover {
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
        }
    </style>
</head>
<body>

<div class="container-fluid">
    <div class="row">
        <!-- Sidebar -->
        <nav class="col-md-2 bg-dark sidebar vh-100">
            <div class="sidebar-sticky pt-3">
                <h5 class="text-white text-center mb-4">
                    <i class="fas fa-sync-alt"></i><br>
                    Replacement Management
                </h5>
                <ul class="nav flex-column">
                    <li class="nav-item">
                        <a class="nav-link text-white" href="${pageContext.request.contextPath}/views/replacement/dashboard">
                            <i class="fas fa-home"></i> Dashboard
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link active text-white bg-primary" href="#">
                            <i class="fas fa-clipboard-list"></i> Pending Requests
                        </a>
                    </li>
                </ul>
            </div>
        </nav>

        <!-- Main Content -->
        <main role="main" class="col-md-10 ml-sm-auto px-4">
            <div class="py-4">
                <h2 class="mb-4">
                    <i class="fas fa-clipboard-list"></i>
                    Replacement Requests - Service TL View
                </h2>

                <!-- Error Banner -->
                <c:if test="${hasError}">
                    <div class="alert alert-danger alert-dismissible fade show" role="alert">
                        <h5 class="alert-heading">
                            <i class="fas fa-exclamation-triangle"></i> System Error
                        </h5>
                        <p class="mb-0">${errorMessage}</p>
                        <hr>
                        <p class="mb-0 small">
                            <strong>What you can do:</strong>
                        <ul class="mb-0">
                            <li>Check your network connection</li>
                            <li>Wait a few moments and <a href="#" onclick="location.reload(); return false;"
                                                          class="alert-link">refresh the page</a></li>
                            <li>Contact IT support if the problem persists</li>
                        </ul>
                        </p>
                        <button type="button" class="close" data-dismiss="alert">
                            <span>&times;</span>
                        </button>
                    </div>
                </c:if>

                <c:if test="${!hasError}">
                    <div class="alert alert-info">
                        <i class="fas fa-info-circle"></i>
                        <strong>Your Role:</strong> Service TL |
                        <strong>Pending Actions:</strong> <span class="badge badge-warning">${requests.size()}</span>
                    </div>
                </c:if>

                <!-- Requests Table -->
                <div class="card">
                    <div class="card-body">
                        <c:choose>
                            <c:when test="${hasError}">
                                <div class="text-center py-5">
                                    <i class="fas fa-database fa-5x text-muted mb-3"></i>
                                    <h4 class="text-muted">Unable to load data</h4>
                                    <p class="text-muted">Please check the error message above</p>
                                    <button class="btn btn-primary" onclick="location.reload()">
                                        <i class="fas fa-sync-alt"></i> Retry
                                    </button>
                                </div>
                            </c:when>
                            <c:when test="${empty requests}">
                                <div class="text-center py-5">
                                    <i class="fas fa-inbox fa-5x text-muted mb-3"></i>
                                    <h4 class="text-muted">No pending requests</h4>
                                    <p class="text-muted">All caught up! Check back later.</p>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="table-responsive">
                                    <table class="table table-hover table-sm" id="requestsTable">
                                        <thead class="thead-dark">
                                        <tr>
                                            <th>Req ID</th>
                                            <th>Client Name</th>
                                            <th>Requester</th>
                                            <th>Account Manager</th>
                                            <th>Number Of Printers</th>
                                            <th>Requested Date</th>
                                            <th>Current Stage</th>
                                            <th>Status</th>
                                            <th>Actions</th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        <c:forEach items="${requests}" var="req">
                                            <tr class="${req.firstActionTaken ? '' : 'table-warning'}">
                                                <td><strong>#${req.id}</strong></td>
                                                <td>
                                                    <strong>${req.clientName}</strong><br>
                                                    <small class="text-muted">${req.clientId}</small>
                                                </td>
                                                <td>
                                                    <i class="fas fa-user"></i> ${req.requesterName}<br>
                                                    <small class="text-muted">${req.requesterRole}</small>
                                                </td>
                                                <td>
                                                    <i class="fas fa-user-tie text-primary"></i> ${req.accountManager}
                                                </td>
                                                <td class="text-center">
                                                    <button type="button"
                                                            class="btn btn-info btn-sm"
                                                            onclick="showPrintersPopup(${req.id})">
                                                        <i class="fas fa-print"></i>
                                                            ${req.printerCount}
                                                        Printer${req.printerCount > 1 ? 's' : ''}
                                                    </button>
                                                </td>
                                                <td>
                                                    <fmt:formatDate value="${req.createdAt}" pattern="dd-MMM-yyyy"/><br>
                                                    <small class="text-muted">
                                                        <fmt:formatDate value="${req.createdAt}" pattern="HH:mm"/>
                                                    </small>
                                                </td>
                                                <td>
                                                    <span class="badge badge-warning">${req.currentStage}</span>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${req.firstActionTaken}">
                                                            <span class="badge badge-secondary">In Progress</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge badge-danger">⚠️ Pending</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>
                                                    <button class="btn btn-success btn-sm"
                                                            onclick="showPrintersPopup(${req.id})">
                                                        <i class="fas fa-eye"></i> View
                                                    </button>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>

<!-- Include Modals -->
<%@ include file="modals.jsp" %>

<!-- Scripts -->
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script src="https://cdn.datatables.net/1.10.24/js/jquery.dataTables.min.js"></script>
<script src="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/chart.js@3.9.1/dist/chart.min.js"></script>
<script>
    // Context path for AJAX calls
    const contextPath = '${pageContext.request.contextPath}';

    $(document).ready(function () {
        <c:if test="${!hasError && !empty requests}">
        $('#requestsTable').DataTable({
            "order": [[7, "desc"], [5, "desc"]],
            "pageLength": 25
        });
        </c:if>
    });
</script>
<script src="${pageContext.request.contextPath}/js/printerPopups.js"></script>

</body>
</html>
