<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html>
<head>
    <title>AM - Replacement Request Review</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@4.6.2/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">
</head>
<body>

<div class="container-fluid mt-4">
    <div class="row">
        <div class="col-12">
            <div class="card">
                <div class="card-header bg-info text-white">
                    <h4><i class="fas fa-check-circle"></i> Replacement Requests - AM Approval</h4>
                </div>

                <div class="card-body bg-light">
                    <form method="get" action="${pageContext.request.contextPath}/views/replacement/am/requestList">
                        <div class="form-row">
                            <div class="col-md-2">
                                <label>Date From</label>
                                <input type="date" class="form-control" name="dateFrom" value="${param.dateFrom}">
                            </div>
                            <div class="col-md-2">
                                <label>Date To</label>
                                <input type="date" class="form-control" name="dateTo" value="${param.dateTo}">
                            </div>
                            <div class="col-md-2">
                                <label>Requester</label>
                                <input type="text" class="form-control" name="requester" placeholder="User ID" value="${param.requester}">
                            </div>
                            <div class="col-md-4">
                                <label>&nbsp;</label><br/>
                                <button type="submit" class="btn btn-primary">
                                    <i class="fas fa-filter"></i> Filter
                                </button>
                                <button type="button" class="btn btn-secondary" onclick="clearFilters()">
                                    <i class="fas fa-times"></i> Clear
                                </button>
                            </div>
                        </div>
                    </form>
                </div>

                <div class="card-body">
                    <div class="alert alert-warning">
                        <i class="fas fa-info-circle"></i>
                        <strong>AM Role:</strong> Commercial details were added by AM Manager.
                        Review and approve/reject the replacement request.
                    </div>

                    <c:if test="${empty requests}">
                        <div class="alert alert-info">No pending requests.</div>
                    </c:if>

                    <c:if test="${not empty requests}">
                        <div class="table-responsive">
                            <table class="table table-bordered table-hover">
                                <thead class="thead-dark">
                                <tr>
                                    <th>Req ID</th>
                                    <th>Client</th>
                                    <th>Requester</th>
                                    <th class="text-center">Printers</th>
                                    <th>Date</th>
                                    <th>Status</th>
                                    <th>Actions</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach items="${requests}" var="req">
                                    <tr>
                                        <td><strong>#${req.id}</strong></td>
                                        <td>${req.clientName}<br/><small>${req.clientId}</small></td>
                                        <td>${req.requesterName}</td>
                                        <td class="text-center"><span class="badge badge-info">${req.printerCount}</span></td>
                                        <td><fmt:formatDate value="${req.createdAt}" pattern="dd-MMM-yyyy"/></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${req.firstActionTaken}">
                                                    <span class="badge badge-success">In Progress</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge badge-warning">New</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <button class="btn btn-sm btn-primary" onclick="viewFullRequest(${req.id})">
                                                <i class="fas fa-eye"></i> View
                                            </button>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:if>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="fullRequestModal.jsp" />

<script src="../../../../js/replacement/replacement_am_requestList.js"></script>
<script src="../../../../js/replacement/replacement_am_requestList.js"></script>

<script src="../../../../js/replacement/replacement_am_requestList.js"></script>

</body>
</html>
