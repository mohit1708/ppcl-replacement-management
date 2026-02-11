<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="View Request" scope="request"/>
<%@ include file="common/header.jsp" %>
<%@ include file="common/sidebar.jsp" %>
<div class="main-content-inner">
    <div class="page-content">


    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2 class="mb-0">
            <i class="fas fa-eye text-primary"></i> Replacement Request #${reqData.id}
        </h2>
        <a href="${pageContext.request.contextPath}/views/replacement/request?action=myList" class="btn btn-secondary">
            <i class="fas fa-arrow-left"></i> Back to My Requests
        </a>
    </div>

    <c:if test="${empty reqData}">
        <div class="alert alert-warning">
            <i class="fas fa-exclamation-triangle"></i> Request not found or could not be loaded.
            <a href="${pageContext.request.contextPath}/views/replacement/request?action=myList">Go back to My Requests</a>
        </div>
    </c:if>

    <c:if test="${not empty reqData}">
        <div class="card mb-3">
            <div class="card-header bg-primary text-white">
                <h5 class="mb-0"><i class="fas fa-info-circle"></i> Request Information</h5>
            </div>
            <div class="card-body">
                <div class="row">
                    <div class="col-md-4">
                        <p><strong>Client:</strong> ${reqData.clientName}</p>
                    </div>
                    <div class="col-md-4">
                        <p><strong>Type:</strong> ${reqData.replacementType}</p>
                    </div>
                    <div class="col-md-4">
                        <p><strong>Reason:</strong> ${reqData.reasonName}</p>
                    </div>
                    <div class="col-md-4">
                        <p><strong>Source:</strong> ${reqData.source}</p>
                    </div>
                    <div class="col-md-4">
                        <p><strong>Current Stage:</strong> 
                            <span class="badge badge-warning">${reqData.currentStage}</span>
                        </p>
                    </div>
                    <div class="col-md-4">
                        <p><strong>Current Owner:</strong> 
                            <span class="badge badge-secondary">${reqData.currentOwnerRole}</span>
                        </p>
                    </div>
                    <div class="col-md-4">
                        <p><strong>Requester:</strong> ${reqData.requesterName}</p>
                    </div>
                    <div class="col-md-4">
                        <p><strong>Created:</strong> 
                            <fmt:formatDate value="${reqData.createdAt}" pattern="dd-MMM-yyyy HH:mm"/>
                        </p>
                    </div>
                    <div class="col-md-4">
                        <p><strong>Last Updated:</strong> 
                            <fmt:formatDate value="${reqData.updatedAt}" pattern="dd-MMM-yyyy HH:mm"/>
                        </p>
                    </div>
                </div>
            </div>
        </div>

        <div class="card mb-3">
            <div class="card-header bg-info text-white">
                <h5 class="mb-0"><i class="fas fa-print"></i> Printer Details</h5>
            </div>
            <div class="card-body">
                <c:choose>
                    <c:when test="${not empty reqData.printers}">
                        <c:forEach items="${reqData.printers}" var="printer" varStatus="status">
                            <div class="border rounded p-3 mb-3">
                                <h6 class="text-primary">
                                    <i class="fas fa-map-marker-alt"></i> Location ${status.count}: ${printer.location}, ${printer.city}
                                </h6>
                                <div class="row">
                                    <div class="col-md-6">
                                        <p><strong>Existing Model:</strong> ${printer.existingModelName}</p>
                                        <p><strong>Existing Serial:</strong> <code>${printer.existingSerial}</code></p>
                                    </div>
                                    <div class="col-md-6">
                                        <p><strong>Recommended Model:</strong> ${printer.recommendedModelName}</p>
                                        <p><strong>Comments:</strong> ${printer.recommendedModelText}</p>
                                    </div>
                                    <div class="col-md-12">
                                        <p><strong>Contact:</strong> ${printer.contactPerson} - ${printer.contactNumber}</p>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <p class="text-muted text-center">No printer details available.</p>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <div class="mt-3">
            <a href="${pageContext.request.contextPath}/views/replacement/request?action=myList" class="btn btn-secondary">
                <i class="fas fa-arrow-left"></i> Back to My Requests
            </a>
        </div>
    </c:if>


    </div><%-- /.page-content --%>
</div><%-- /.main-content-inner --%>
<%@ include file="common/footer.jsp" %>
