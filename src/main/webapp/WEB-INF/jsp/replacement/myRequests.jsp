<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="isEmbedded" value="${param.embedded == 'true' || embedded == true}"/>

<c:if test="${!isEmbedded}">
    <c:set var="pageTitle" value="My Replacement Requests" scope="request"/>
    <%@ include file="common/header.jsp" %>
    <%@ include file="common/sidebar.jsp" %>
    <div class="main-content-inner">
        <div class="page-content">
</c:if>

<%-- TAT Display Styles (included in both modes) --%>
<style>
    /* Fix DataTables alignment with Ace sidebar */
    #openRequestsTable, #completedRequestsTable, #rejectedRequestsTable, #cancelledRequestsTable {
        width: 100% !important;
        table-layout: auto;
    }
    /* TAT Display Styles */
    .tat-display {
        display: flex;
        flex-direction: column;
        gap: 4px;
        min-width: 120px;
    }

    .tat-display__time {
        font-size: 0.875rem;
        font-weight: 600;
    }

    .tat-display__time--within {
        color: #155724;
    }

    .tat-display__time--warning {
        color: #856404;
    }

    .tat-display__time--breach {
        color: #721c24;
    }

    .tat-display__label {
        font-size: 0.75rem;
        color: #6c757d;
    }

    .tat-display__progress {
        width: 100%;
        height: 6px;
        background: #e9ecef;
        border-radius: 3px;
        overflow: hidden;
        margin-top: 4px;
    }

    .tat-display__progress-bar {
        height: 100%;
        border-radius: 3px;
        transition: width 0.3s ease;
    }

    .tat-display__progress-bar--within {
        background: #28a745;
    }

    .tat-display__progress-bar--warning {
        background: #ffc107;
    }

    .tat-display__progress-bar--breach {
        background: #dc3545;
    }

    /* Badge Styles */
    .badge-stage {
        font-size: 0.8rem;
        padding: 0.4em 0.6em;
    }

    /* TAT Breach Row */
    .row-breach {
        background-color: rgba(220, 53, 69, 0.05) !important;
    }

    /* Fix Ace hiding first th in DataTables */
    #openRequestsTable thead > tr > th:first-child,
    #completedRequestsTable thead > tr > th:first-child,
    #rejectedRequestsTable thead > tr > th:first-child,
    #cancelledRequestsTable thead > tr > th:first-child {
        display: table-cell !important;
    }

    /* Table Improvements */
    #openRequestsTable td, #openRequestsTable th,
    #completedRequestsTable td, #completedRequestsTable th,
    #rejectedRequestsTable td, #rejectedRequestsTable th,
    #cancelledRequestsTable td, #cancelledRequestsTable th {
        vertical-align: middle;
    }

    .client-col {
        max-width: 180px;
    }

    /* Modal Scroll Fix */
    .modal {
        overflow-y: auto !important;
    }

    .modal-open {
        overflow: hidden !important;
        padding-right: 0 !important;
    }

    .modal-dialog {
        margin: 1.75rem auto;
        max-height: calc(100vh - 3.5rem);
    }

    .modal-content {
        max-height: calc(100vh - 3.5rem);
        display: flex;
        flex-direction: column;
    }

    .modal-body {
        overflow-y: auto;
        flex: 1 1 auto;
    }

    .modal-header, .modal-footer {
        flex-shrink: 0;
    }
</style>

<%-- Main Card Content --%>
<div class="card shadow-sm">
    <div class="card-header bg-white d-flex justify-content-between align-items-center py-3">
        <h5 class="mb-0">
            <c:choose>
                <c:when test="${isEmbedded}">
                    <i class="fas fa-clock text-warning mr-2"></i>Requests Pending at Service TL Review
                </c:when>
                <c:otherwise>
                    <i class="fas fa-list-alt text-primary mr-2"></i>My Replacement Requests
                </c:otherwise>
            </c:choose>
        </h5>
        <c:if test="${!isEmbedded}">
            <div>
                <a href="${pageContext.request.contextPath}/views/replacement/request?action=new"
                   class="btn btn-primary btn-sm">
                    <i class="fas fa-plus"></i> New Request
                </a>
            </div>
        </c:if>
    </div>

    <div class="card-body">
        <%-- Success/Error Messages --%>
        <c:if test="${param.success == 'created'}">
            <div class="alert alert-success alert-dismissible fade show">
                <i class="fas fa-check-circle"></i>
                Replacement request created successfully! Request ID: #${param.id}
                <button type="button" class="close" data-dismiss="alert">&times;</button>
            </div>
        </c:if>

        <c:if test="${param.error != null}">
            <div class="alert alert-danger alert-dismissible fade show">
                <i class="fas fa-exclamation-triangle"></i>
                Error: ${fn:escapeXml(param.error)}
                <button type="button" class="close" data-dismiss="alert">&times;</button>
            </div>
        </c:if>

        <%-- Filter Section (hidden in embedded mode) --%>
        <c:if test="${!isEmbedded}">
            <div class="filter-section">
                <div class="row">
                    <div class="col-md-3">
                        <label class="small font-weight-bold mb-1">Status</label>
                        <select id="filterStatus" class="form-control form-control-sm">
                            <option value="">All Status</option>
                            <option value="PENDING">Pending</option>
                            <option value="COMPLETED">Completed</option>
                            <option value="REJECTED">Rejected</option>
                            <option value="OPEN">Open</option>
                        </select>
                    </div>
                    <div class="col-md-3">
                        <label class="small font-weight-bold mb-1">Stage</label>
                        <select id="filterStage" class="form-control form-control-sm">
                            <option value="">All Stages</option>
                            <c:forEach items="${stageList}" var="stage">
                                <option value="${stage.code}">${stage.name}</option>
                            </c:forEach>
                                <%-- Fallback if stageList not provided --%>
                            <c:if test="${empty stageList}">
                                <option value="STG01_TL_REVIEW">TL Review</option>
                                <option value="STG02_AM_REVIEW">AM Review</option>
                                <option value="STG03_QUOTATION">Quotation</option>
                                <option value="STG04_PO_APPROVAL">PO Approval</option>
                                <option value="STG05_DISPATCH">Dispatch</option>
                            </c:if>
                        </select>
                    </div>
                    <div class="col-md-2">
                        <label class="small font-weight-bold mb-1">From Date</label>
                        <input type="date" id="filterDateFrom" class="form-control form-control-sm">
                    </div>
                    <div class="col-md-2">
                        <label class="small font-weight-bold mb-1">To Date</label>
                        <input type="date" id="filterDateTo" class="form-control form-control-sm">
                    </div>
                    <div class="col-md-2 d-flex align-items-end">
                        <button type="button" id="btnApplyFilters" class="btn btn-primary btn-sm mr-2">
                            <i class="fas fa-filter"></i> Apply
                        </button>
                        <button type="button" id="btnClearFilters" class="btn btn-outline-secondary btn-sm">
                            <i class="fas fa-times"></i> Clear
                        </button>
                    </div>
                </div>
            </div>
        </c:if>

        <%-- Segregate requests by status --%>
        <%
            final java.util.List openRequests = new java.util.ArrayList();
            final java.util.List completedRequests = new java.util.ArrayList();
            final java.util.List rejectedRequests = new java.util.ArrayList();
            final java.util.List cancelledRequests = new java.util.ArrayList();
            final java.util.List allRequests = (java.util.List) request.getAttribute("requests");
            if (allRequests != null) {
                for (final Object obj : allRequests) {
                    final com.ppcl.replacement.model.MyRequestRow req = (com.ppcl.replacement.model.MyRequestRow) obj;
                    final String status = req.getStatus();
                    if ("COMPLETED".equals(status)) {
                        completedRequests.add(req);
                    } else if ("REJECTED".equals(status)) {
                        rejectedRequests.add(req);
                    } else if ("CANCELLED".equals(status)) {
                        cancelledRequests.add(req);
                    } else {
                        openRequests.add(req);
                    }
                }
            }
            request.setAttribute("openRequests", openRequests);
            request.setAttribute("completedRequests", completedRequests);
            request.setAttribute("rejectedRequests", rejectedRequests);
            request.setAttribute("cancelledRequests", cancelledRequests);
        %>

        <%-- Check if requests list exists --%>
        <c:choose>
            <c:when test="${empty requests}">
                <div class="text-center py-5">
                    <i class="fas fa-inbox fa-3x text-muted mb-3"></i>
                    <h5 class="text-muted">No Requests Found</h5>
                    <p class="text-muted">You don't have any replacement requests at the moment.</p>
                </div>
            </c:when>
            <c:otherwise>
                <%-- Status Tabs - Only show when not embedded --%>
                <c:if test="${!isEmbedded}">
                    <ul class="nav nav-tabs mb-3" id="requestStatusTabs" role="tablist">
                        <li class="nav-item">
                            <a class="nav-link active" id="open-tab" data-toggle="tab" href="#openRequestsTab"
                               role="tab">
                                <i class="fas fa-folder-open text-primary"></i> Open
                                <span class="badge badge-primary ml-1">${fn:length(openRequests)}</span>
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" id="completed-tab" data-toggle="tab" href="#completedRequestsTab"
                               role="tab">
                                <i class="fas fa-check-circle text-success"></i> Completed
                                <span class="badge badge-success ml-1">${fn:length(completedRequests)}</span>
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" id="rejected-tab" data-toggle="tab" href="#rejectedRequestsTab"
                               role="tab">
                                <i class="fas fa-times-circle text-danger"></i> Rejected
                                <span class="badge badge-danger ml-1">${fn:length(rejectedRequests)}</span>
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" id="cancelled-tab" data-toggle="tab" href="#cancelledRequestsTab"
                               role="tab">
                                <i class="fas fa-ban text-secondary"></i> Cancelled
                                <span class="badge badge-secondary ml-1">${fn:length(cancelledRequests)}</span>
                            </a>
                        </li>
                    </ul>
                </c:if>

                <div class="tab-content" id="requestStatusTabContent">
                        <%-- OPEN Requests Tab / Simple table when embedded --%>
                    <div class="${isEmbedded ? '' : 'tab-pane fade show active'}" id="openRequestsTab" role="tabpanel">
                        <c:choose>
                            <c:when test="${empty openRequests}">
                                <div class="text-center py-4">
                                    <i class="fas fa-check-circle fa-2x text-success mb-2"></i>
                                    <p class="text-muted">No open requests</p>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="table-responsive">
                                    <table class="table table-striped table-hover table-sm" id="openRequestsTable">
                                        <thead class="thead-light">
                                        <tr>
                                            <th>Request ID</th>
                                            <th>Client</th>
                                            <th class="text-center">No. of Printers</th>
                                            <th>Requested Date</th>
                                            <th>Current TAT</th>
                                            <th>Current Stage</th>
                                            <th>Stage Owner</th>
                                            <th>Actions</th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        <c:forEach items="${openRequests}" var="req">
                                            <tr class="${req.tatStatus == 'BREACH' ? 'row-breach' : ''}"
                                                data-status="${req.status}"
                                                data-stage="${req.currentStage}"
                                                data-date="<fmt:formatDate value='${req.createdAt}' pattern='yyyy-MM-dd' />">

                                                    <%-- Request ID --%>
                                                <td>
                                                    <a href="javascript:void(0);"
                                                       onclick="openViewModal(${req.id}, '${req.currentStage}')"
                                                       class="font-weight-bold text-primary">
                                                        REQ-${req.id}
                                                    </a>

                                                </td>

                                                    <%-- Client --%>
                                                <td class="client-col">
                                                    <strong>${fn:escapeXml(req.clientName)}</strong>
                                                    <c:if test="${not empty req.branch}">
                                                        <br><small class="text-muted">${req.branch}</small>
                                                    </c:if>
                                                </td>

                                                    <%-- No. of Printers --%>
                                                <td class="text-center">
                                                    <span class="badge badge-primary badge-pill">${req.printerCount}</span>
                                                </td>

                                                    <%-- Requested Date --%>
                                                <td>
                                                    <fmt:formatDate value="${req.createdAt}" pattern="dd MMM yyyy"/>
                                                    <br><small class="text-muted">
                                                    <fmt:formatDate value="${req.createdAt}" pattern="HH:mm"/>
                                                </small>
                                                </td>

                                                    <%-- Current TAT with Progress Bar --%>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${req.tatDurationMinutes > 0}">
                                                            <div class="tat-display">
                                                                <c:set var="tatClass" value="within"/>
                                                                <c:if test="${req.tatPercentage >= 80 && req.tatPercentage < 100}">
                                                                    <c:set var="tatClass" value="warning"/>
                                                                </c:if>
                                                                <c:if test="${req.tatPercentage >= 100}">
                                                                    <c:set var="tatClass" value="breach"/>
                                                                </c:if>

                                                                <span class="tat-display__time tat-display__time--${tatClass}">
                                                                        ${req.tatActualDisplay}
                                                                </span>
                                                                <span class="tat-display__label">
                                                    <c:choose>
                                                        <c:when test="${req.tatPercentage > 100}">
                                                            of ${req.tatDurationDisplay} (${req.tatBreachDisplay})
                                                        </c:when>
                                                        <c:otherwise>
                                                            of ${req.tatDurationDisplay} (<fmt:formatNumber
                                                                value="${req.tatPercentage}" maxFractionDigits="0"/>%)
                                                        </c:otherwise>
                                                    </c:choose>
                                                </span>
                                                                <div class="tat-display__progress">
                                                                    <c:set var="progressWidth"
                                                                           value="${req.tatPercentage > 100 ? 100 : req.tatPercentage}"/>
                                                                    <div class="tat-display__progress-bar tat-display__progress-bar--${tatClass}"
                                                                         style="width: ${progressWidth}%;"></div>
                                                                </div>
                                                            </div>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="text-muted">N/A</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>

                                                    <%-- Current Stage --%>
                                                <td>
                                                    <c:set var="stageBadgeClass" value="badge-secondary"/>
                                                    <c:if test="${fn:contains(req.currentStage, 'REVIEW')}">
                                                        <c:set var="stageBadgeClass" value="badge-warning"/>
                                                    </c:if>
                                                    <c:if test="${fn:contains(req.currentStage, 'APPROVAL')}">
                                                        <c:set var="stageBadgeClass" value="badge-info"/>
                                                    </c:if>
                                                    <c:if test="${fn:contains(req.currentStage, 'DISPATCH') || fn:contains(req.currentStage, 'CLOSURE')}">
                                                        <c:set var="stageBadgeClass" value="badge-success"/>
                                                    </c:if>
                                                    <span class="badge ${stageBadgeClass} badge-stage">
                                                            ${not empty req.currentStageName ? req.currentStageName : req.currentStage}
                                                    </span>
                                                </td>

                                                    <%-- Stage Owner --%>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${not empty req.stageOwnerName}">
                                                            ${fn:escapeXml(req.stageOwnerName)}
                                                        </c:when>
                                                        <c:otherwise>
                                                            ${fn:escapeXml(req.currentOwnerUserId)}
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>

                                                    <%-- Actions --%>
                                                <td>
                                                    <div class="action-buttons">
                                                            <%-- View button - always available --%>
                                                        <button type="button"
                                                                class="btn btn-outline-primary btn-action"
                                                                onclick="openViewModal(${req.id}, '${req.currentStage}')"
                                                                title="View Details">
                                                            <i class="fas fa-eye"></i>
                                                        </button>

                                                            <%-- Edit button - available at Service TL Review stage --%>
                                                        <c:if test="${req.currentStage == 'STG2_SERVICE_TL_REVIEW' || req.currentStage == 'Service TL Review'}">
                                                            <button type="button"
                                                                    class="btn btn-outline-warning btn-action"
                                                                    onclick="openEditModal(${req.id})"
                                                                    title="Edit Request">
                                                                <i class="fas fa-edit"></i>
                                                            </button>
                                                        </c:if>

                                                        <c:if test="${!isEmbedded}">
                                                            <%-- AM/Accounts Manager Stage Buttons (roleId=4 or roleCode=AM/ACCOUNTS_MANAGER) --%>
                                                            <c:if test="${sessionScope.roleId == 4 || sessionScope.roleId == 2 || sessionScope.roleCode == 'AM' || sessionScope.roleCode == 'ACCOUNTS_MANAGER' }">
                                                                <%-- STG4_AM_COMMERCIAL: Quotation Pending --%>
                                                                <c:if test="${req.currentStage == 'STG4_AM_COMMERCIAL' || req.currentStage == 'Quotation Pending'}">
                                                                    <button type="button"
                                                                            class="btn btn-info btn-action"
                                                                            onclick="openQuotationModal(${req.id}, '${fn:escapeXml(req.clientName)}', ${req.printerCount})"
                                                                            title="Quotation Pending">
                                                                        <i class="fas fa-file-invoice"></i> Quotation
                                                                        Pending
                                                                    </button>
                                                                </c:if>

                                                                <%-- STG5_AM_MANAGER_FINAL: Quotation Sent --%>
                                                                <c:if test="${req.currentStage == 'STG5_AM_MANAGER_FINAL'}">
                                                                    <button type="button"
                                                                            class="btn btn-success btn-action"
                                                                            onclick="openQuotationSentModal(${req.id}, '${fn:escapeXml(req.clientName)}')"
                                                                            title="Quotation Sent">
                                                                        <i class="fas fa-paper-plane"></i> Quotation
                                                                        Sent
                                                                    </button>
                                                                </c:if>

                                                                <%-- STG6_PRINTER_ORDER: Book Printer Order --%>
                                                                <c:if test="${req.currentStage == 'STG6_PRINTER_ORDER'}">
                                                                    <button type="button"
                                                                            class="btn btn-primary btn-action"
                                                                            onclick="openBookOrderModal(${req.id}, '${fn:escapeXml(req.clientName)}')"
                                                                            title="BB">
                                                                        <i class="fas fa-shopping-cart"></i> Book
                                                                        Printer Order
                                                                    </button>
                                                                </c:if>

                                                                <%-- STG11_CREDIT_NOTE: Issue Credit Note --%>
                                                                <c:if test="${req.currentStage == 'STG11_CREDIT_NOTE'}">
                                                                    <button type="button"
                                                                            class="btn btn-warning btn-action"
                                                                            onclick="openCreditNoteModal(${req.id}, '${fn:escapeXml(req.clientName)}')"
                                                                            title="Issue Credit Note">
                                                                        <i class="fas fa-credit-card"></i> Issue Credit
                                                                        Note
                                                                    </button>
                                                                </c:if>

                                                                <%-- STG7_DISPATCH_LETTER: Replacement Letter --%>
                                                                <c:if test="${req.currentStage == 'STG7_DISPATCH_LETTER' && req.printerCount== req.allotedPrinterCount}">
                                                                    <a href="${pageContext.request.contextPath}/am/replacementLetter?requestId=${req.id}"
                                                                       class="btn btn-success btn-action"
                                                                       title="Replacement Letter">
                                                                        <i class="fas fa-file-alt"></i> Replacement
                                                                        Letter
                                                                    </a>
                                                                </c:if>
                                                            </c:if>
                                                        </c:if>

                                                            <%-- Remind button - only at Service TL Review stage --%>
                                                        <c:if test="${req.currentStage == 'STG2_SERVICE_TL_REVIEW' || req.currentStage == 'Service TL Review'}">
                                                            <button type="button"
                                                                    class="btn btn-outline-info btn-action"
                                                                    onclick="sendRemind(${req.id}, '${fn:escapeXml(req.stageOwnerName)}', '${fn:escapeXml(req.currentStageName)}')"
                                                                    title="Send reminder to stage owner">
                                                                <i class="fas fa-bell"></i> Remind
                                                            </button>
                                                        </c:if>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>

                        <%-- COMPLETED Requests Tab - Only show when not embedded --%>
                    <c:if test="${!isEmbedded}">
                        <div class="tab-pane fade" id="completedRequestsTab" role="tabpanel">
                            <c:choose>
                                <c:when test="${empty completedRequests}">
                                    <div class="text-center py-4">
                                        <i class="fas fa-inbox fa-2x text-muted mb-2"></i>
                                        <p class="text-muted">No completed requests</p>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="table-responsive">
                                        <table class="table table-striped table-hover table-sm"
                                               id="completedRequestsTable">
                                            <thead class="thead-light">
                                            <tr>
                                                <th>Request ID</th>
                                                <th>Client</th>
                                                <th class="text-center">No. of Printers</th>
                                                <th>Requested Date</th>
                                                <th>Final Stage</th>
                                                <th>Status</th>
                                                <th>Actions</th>
                                            </tr>
                                            </thead>
                                            <tbody>
                                            <c:forEach items="${completedRequests}" var="req">
                                                <tr>
                                                    <td>
                                                        <a href="javascript:void(0);"
                                                           onclick="openViewModal(${req.id}, '${req.currentStage}')"
                                                           class="font-weight-bold text-success">
                                                            REQ-${req.id}
                                                        </a>
                                                    </td>
                                                    <td>
                                                        <strong>${fn:escapeXml(req.clientName)}</strong>
                                                        <c:if test="${not empty req.branch}">
                                                            <br><small class="text-muted">${req.branch}</small>
                                                        </c:if>
                                                    </td>
                                                    <td class="text-center">
                                                        <span class="badge badge-secondary">${req.printerCount}</span>
                                                    </td>
                                                    <td>
                                                        <fmt:formatDate value="${req.createdAt}" pattern="dd-MMM-yyyy"/>
                                                        <br><small class="text-muted"><fmt:formatDate
                                                            value="${req.createdAt}" pattern="HH:mm"/></small>
                                                    </td>
                                                    <td>${fn:escapeXml(req.currentStageName)}</td>
                                                    <td><span class="badge badge-success">COMPLETED</span></td>
                                                    <td>
                                                        <button type="button" class="btn btn-info btn-action"
                                                                onclick="openViewModal(${req.id}, '${req.currentStage}')"
                                                                title="View">
                                                            <i class="fas fa-eye"></i>
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

                        <%-- REJECTED Requests Tab --%>
                        <div class="tab-pane fade" id="rejectedRequestsTab" role="tabpanel">
                            <c:choose>
                                <c:when test="${empty rejectedRequests}">
                                    <div class="text-center py-4">
                                        <i class="fas fa-inbox fa-2x text-muted mb-2"></i>
                                        <p class="text-muted">No rejected requests</p>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="table-responsive">
                                        <table class="table table-striped table-hover table-sm"
                                               id="rejectedRequestsTable">
                                            <thead class="thead-light">
                                            <tr>
                                                <th>Request ID</th>
                                                <th>Client</th>
                                                <th class="text-center">No. of Printers</th>
                                                <th>Requested Date</th>
                                                <th>Rejected At Stage</th>
                                                <th>Status</th>
                                                <th>Actions</th>
                                            </tr>
                                            </thead>
                                            <tbody>
                                            <c:forEach items="${rejectedRequests}" var="req">
                                                <tr>
                                                    <td>
                                                        <a href="javascript:void(0);"
                                                           onclick="openViewModal(${req.id}, '${req.currentStage}')"
                                                           class="font-weight-bold text-danger">
                                                            REQ-${req.id}
                                                        </a>
                                                    </td>
                                                    <td>
                                                        <strong>${fn:escapeXml(req.clientName)}</strong>
                                                        <c:if test="${not empty req.branch}">
                                                            <br><small class="text-muted">${req.branch}</small>
                                                        </c:if>
                                                    </td>
                                                    <td class="text-center">
                                                        <span class="badge badge-secondary">${req.printerCount}</span>
                                                    </td>
                                                    <td>
                                                        <fmt:formatDate value="${req.createdAt}" pattern="dd-MMM-yyyy"/>
                                                        <br><small class="text-muted"><fmt:formatDate
                                                            value="${req.createdAt}" pattern="HH:mm"/></small>
                                                    </td>
                                                    <td>${fn:escapeXml(req.currentStageName)}</td>
                                                    <td><span class="badge badge-danger">REJECTED</span></td>
                                                    <td>
                                                        <button type="button" class="btn btn-info btn-action"
                                                                onclick="openViewModal(${req.id}, '${req.currentStage}')"
                                                                title="View">
                                                            <i class="fas fa-eye"></i>
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

                        <%-- CANCELLED Requests Tab --%>
                        <div class="tab-pane fade" id="cancelledRequestsTab" role="tabpanel">
                            <c:choose>
                                <c:when test="${empty cancelledRequests}">
                                    <div class="text-center py-4">
                                        <i class="fas fa-inbox fa-2x text-muted mb-2"></i>
                                        <p class="text-muted">No cancelled requests</p>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="table-responsive">
                                        <table class="table table-striped table-hover table-sm"
                                               id="cancelledRequestsTable">
                                            <thead class="thead-light">
                                            <tr>
                                                <th>Request ID</th>
                                                <th>Client</th>
                                                <th class="text-center">No. of Printers</th>
                                                <th>Requested Date</th>
                                                <th>Cancelled At Stage</th>
                                                <th>Status</th>
                                                <th>Actions</th>
                                            </tr>
                                            </thead>
                                            <tbody>
                                            <c:forEach items="${cancelledRequests}" var="req">
                                                <tr>
                                                    <td>
                                                        <a href="javascript:void(0);"
                                                           onclick="openViewModal(${req.id}, '${req.currentStage}')"
                                                           class="font-weight-bold text-secondary">
                                                            REQ-${req.id}
                                                        </a>
                                                    </td>
                                                    <td>
                                                        <strong>${fn:escapeXml(req.clientName)}</strong>
                                                        <c:if test="${not empty req.branch}">
                                                            <br><small class="text-muted">${req.branch}</small>
                                                        </c:if>
                                                    </td>
                                                    <td class="text-center">
                                                        <span class="badge badge-secondary">${req.printerCount}</span>
                                                    </td>
                                                    <td>
                                                        <fmt:formatDate value="${req.createdAt}" pattern="dd-MMM-yyyy"/>
                                                        <br><small class="text-muted"><fmt:formatDate
                                                            value="${req.createdAt}" pattern="HH:mm"/></small>
                                                    </td>
                                                    <td>${fn:escapeXml(req.currentStageName)}</td>
                                                    <td><span class="badge badge-secondary">CANCELLED</span></td>
                                                    <td>
                                                        <button type="button" class="btn btn-info btn-action"
                                                                onclick="openViewModal(${req.id}, '${req.currentStage}')"
                                                                title="View">
                                                            <i class="fas fa-eye"></i>
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
                    </c:if>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<!-- Edit Request Modal -->
<div class="modal fade modal-lg" id="editRequestModal" tabindex="-1" role="dialog" aria-labelledby="editRequestModalLabel"
     aria-hidden="true">
    <div class="modal-dialog" role="document">
        <div class="modal-content border-0 radius-1 shadow">
            <div class="modal-header bgc-primary-d1 text-white py-2 pl-3">
                <h5 class="modal-title text-110" id="editRequestModalLabel">
                    <i class="fas fa-edit mr-1"></i> Edit Replacement Request
                </h5>
                <button type="button" class="close text-white" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <div id="editModalLoading" class="text-center py-4">
                    <i class="fas fa-spinner fa-spin fa-2x"></i>
                    <p class="mt-2">Loading request details...</p>
                </div>
                <form id="editRequestForm" style="display:none;">
                    <input type="hidden" id="editReqId" name="reqId">

                    <!-- TAT Info -->
                    <div class="card mb-3 bg-light">
                        <div class="card-body py-2">
                            <div class="row">
                                <div class="col-md-4">
                                    <small class="text-muted">Stage Start Time</small>
                                    <div id="editStageStartTime" class="font-weight-bold">-</div>
                                </div>
                                <div class="col-md-4">
                                    <small class="text-muted">Current Time</small>
                                    <div id="editCurrentTime" class="font-weight-bold">-</div>
                                </div>
                                <div class="col-md-4">
                                    <small class="text-muted">TAT Duration</small>
                                    <div id="editTatDuration" class="font-weight-bold">-</div>
                                </div>
                            </div>
                            <div class="mt-2">
                                <small class="text-muted">TAT Status</small>
                                <div id="editTatProgress"></div>
                            </div>
                        </div>
                    </div>

                    <!-- Request Info -->
                    <div class="row">
                        <div class="col-md-6">
                            <div class="form-group">
                                <label>Request ID</label>
                                <input type="text" class="form-control" id="editReqIdDisplay" readonly>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="form-group">
                                <label>Client</label>
                                <input type="text" class="form-control" id="editClientName" readonly>
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-6">
                            <div class="form-group">
                                <label>Current Stage</label>
                                <input type="text" class="form-control" id="editCurrentStage" readonly>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="form-group">
                                <label>Replacement Type <span class="text-danger">*</span></label>
                                <select class="form-control" id="editReplacementType" name="replacementType" required>
                                    <option value="DURING_CONTRACT">During Contract</option>
                                    <option value="AFTER_CONTRACT">After Contract</option>
                                </select>
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-6">
                            <div class="form-group">
                                <label>Reason <span class="text-danger">*</span></label>
                                <select class="form-control" id="editReasonId" name="reasonId" required>
                                    <option value="">Select Reason</option>
                                    <c:forEach items="${reasons}" var="reason">
                                        <option value="${reason.id}">${reason.name}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="form-group">
                                <label>Status</label>
                                <input type="text" class="form-control" id="editStatus" readonly>
                            </div>
                        </div>
                    </div>

                    <!-- Sign-In Location -->
                    <div class="row">
                        <div class="col-md-12">
                            <div class="form-group">
                                <label><i class="fas fa-map-marker-alt text-primary mr-1"></i> Sign-In Location <span
                                        class="text-danger">*</span></label>
                                <select class="form-control" id="editSignInLocation" name="signInBranchId" required>
                                    <option value="">Select Sign-In Location</option>
                                </select>
                                <small class="text-muted">Changing the location will update contact details.</small>
                            </div>
                        </div>
                    </div>

                    <!-- Contact Details Section -->
                    <h6 class="mt-3 mb-2"><i class="fas fa-address-card text-primary"></i> Contact Details</h6>
                    <div class="row">
                        <div class="col-md-4">
                            <div class="form-group">
                                <label>Contact Name</label>
                                <input type="text" class="form-control" id="editContactName" name="contactName"
                                       placeholder="Contact person name">
                            </div>
                        </div>
                        <div class="col-md-4">
                            <div class="form-group">
                                <label>Contact Number</label>
                                <input type="text" class="form-control" id="editContactNumber" name="contactNumber"
                                       placeholder="Mobile/Phone number">
                            </div>
                        </div>
                        <div class="col-md-4">
                            <div class="form-group">
                                <label>Contact Email</label>
                                <input type="email" class="form-control" id="editContactEmail" name="contactEmail"
                                       placeholder="Email address">
                            </div>
                        </div>
                    </div>

                    <!-- Printer Details Section -->
                    <h6 class="mt-3 mb-2"><i class="fas fa-print"></i> Printers</h6>
                    <div class="table-responsive">
                        <table class="table table-sm table-bordered" id="editPrintersTable">
                            <thead class="thead-light">
                            <tr>
                                <th>Location</th>
                                <th>Existing Model</th>
                                <th>Serial</th>
                                <th>New Model</th>
                            </tr>
                            </thead>
                            <tbody id="editPrintersBody">
                            </tbody>
                        </table>
                    </div>

                    <!-- Comments -->
                    <div class="form-group">
                        <label>Comments</label>
                        <textarea class="form-control" id="editComments" name="comments" rows="3"
                                  placeholder="Add any additional comments..."></textarea>
                    </div>
                </form>

                <div id="editModalError" class="alert alert-danger" style="display:none;">
                    <i class="fas fa-exclamation-triangle"></i>
                    <span id="editModalErrorMsg"></span>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-danger" id="btnCloseRequest" onclick="closeRequest()">
                    <i class="fas fa-ban"></i> Close Request
                </button>
                <button type="button" class="btn btn-secondary" data-dismiss="modal">
                    <i class="fas fa-times"></i> Cancel
                </button>
                <button type="button" class="btn btn-warning" id="btnSaveEdit" onclick="saveEditRequest()">
                    <i class="fas fa-save"></i> Save Changes
                </button>
            </div>
        </div>
    </div>
</div>

<!-- Close Request Confirmation Modal -->
<div class="modal fade" id="closeRequestModal" tabindex="-1" role="dialog">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header py-2 bgc-danger-tp1 border-0  radius-t-1">
                <h5 class="modal-title text-white-tp1 text-110 pl-2 font-bolder">
                    <i class="fas fa-ban"></i> Close Request
                </h5>
                <button type="button"
                        class="position-tr btn btn-xs btn-outline-white btn-h-yellow btn-a-yellow mt-1px mr-1px btn-brc-tp"
                        data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true" class="text-150">&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <p>Are you sure you want to close this request?</p>
                <p class="text-danger"><strong>This action cannot be undone.</strong></p>
                <input type="hidden" id="closeReqIdHidden">
                <div class="form-group">
                    <label for="closeReasonText">Reason for Closing <span class="text-danger">*</span></label>
                    <textarea class="form-control" id="closeReasonText" rows="3"
                              placeholder="Enter reason for closing this request..." required></textarea>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-danger" id="btnConfirmClose" onclick="confirmCloseRequest()">
                    <i class="fas fa-ban"></i> Confirm Close
                </button>
            </div>
        </div>
    </div>
</div>

<!-- View Request Modal (Read-Only) -->
<div class="modal fade modal-lg" id="viewRequestModal" tabindex="-1" role="dialog" aria-labelledby="viewRequestModalLabel"
     aria-hidden="true">
    <div class="modal-dialog" role="document">
        <div class="modal-content border-0 radius-1 shadow">
            <div class="modal-header bgc-primary-d1 text-white py-2 pl-3">
                <h5 class="modal-title text-110" id="viewRequestModalLabel">
                    <i class="fas fa-eye mr-1"></i> View Replacement Request
                </h5>
                <button type="button" class="close text-white" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <div id="viewModalLoading" class="text-center py-4">
                    <i class="fas fa-spinner fa-spin fa-2x"></i>
                    <p class="mt-2">Loading request details...</p>
                </div>
                <div id="viewRequestContent" style="display:none;">
                    <!-- TAT Info -->
                    <div class="card mb-3 bg-light">
                        <div class="card-body py-2">
                            <div class="row">
                                <div class="col-md-4">
                                    <small class="text-muted">Stage Start Time</small>
                                    <div id="viewStageStartTime" class="font-weight-bold">-</div>
                                </div>
                                <div class="col-md-4">
                                    <small class="text-muted">Current Time</small>
                                    <div id="viewCurrentTime" class="font-weight-bold">-</div>
                                </div>
                                <div class="col-md-4">
                                    <small class="text-muted">TAT Duration</small>
                                    <div id="viewTatDuration" class="font-weight-bold">-</div>
                                </div>
                            </div>
                            <div class="mt-2">
                                <small class="text-muted">TAT Status</small>
                                <div id="viewTatProgress"></div>
                            </div>
                        </div>
                    </div>

                    <!-- Request Info -->
                    <div class="row">
                        <div class="col-md-6">
                            <div class="form-group">
                                <label class="font-weight-bold">Request ID</label>
                                <div id="viewReqIdDisplay"
                                     class="form-control-plaintext border rounded px-2 py-1 bg-white">-
                                </div>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="form-group">
                                <label class="font-weight-bold">Client</label>
                                <div id="viewClientName"
                                     class="form-control-plaintext border rounded px-2 py-1 bg-white">-
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-6">
                            <div class="form-group">
                                <label class="font-weight-bold">Current Stage</label>
                                <div id="viewCurrentStage"
                                     class="form-control-plaintext border rounded px-2 py-1 bg-white">-
                                </div>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="form-group">
                                <label class="font-weight-bold">Replacement Type</label>
                                <div id="viewReplacementType"
                                     class="form-control-plaintext border rounded px-2 py-1 bg-white">-
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-6">
                            <div class="form-group">
                                <label class="font-weight-bold">Reason</label>
                                <div id="viewReasonName"
                                     class="form-control-plaintext border rounded px-2 py-1 bg-white">-
                                </div>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="form-group">
                                <label class="font-weight-bold">Status</label>
                                <div id="viewStatus" class="form-control-plaintext border rounded px-2 py-1 bg-white">
                                    -
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-12">
                            <div class="form-group">
                                <label class="font-weight-bold"><i class="fas fa-user text-primary mr-1"></i> Requester
                                    (Raised By)</label>
                                <div id="viewRequester"
                                     class="form-control-plaintext border rounded px-2 py-1 bg-white">-
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Sign-In Location -->
                    <div class="row">
                        <div class="col-md-12">
                            <div class="form-group">
                                <label class="font-weight-bold"><i class="fas fa-map-marker-alt text-primary mr-1"></i>
                                    Sign-In Location</label>
                                <div id="viewSignInLocation"
                                     class="form-control-plaintext border rounded px-2 py-1 bg-white">-
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Contact Details Section -->
                    <h6 class="mt-3 mb-2"><i class="fas fa-address-card text-primary"></i> Contact Details</h6>
                    <div class="row">
                        <div class="col-md-4">
                            <div class="form-group">
                                <label class="font-weight-bold">Contact Name</label>
                                <div id="viewContactName"
                                     class="form-control-plaintext border rounded px-2 py-1 bg-white">-
                                </div>
                            </div>
                        </div>
                        <div class="col-md-4">
                            <div class="form-group">
                                <label class="font-weight-bold">Contact Number</label>
                                <div id="viewContactNumber"
                                     class="form-control-plaintext border rounded px-2 py-1 bg-white">-
                                </div>
                            </div>
                        </div>
                        <div class="col-md-4">
                            <div class="form-group">
                                <label class="font-weight-bold">Contact Email</label>
                                <div id="viewContactEmail"
                                     class="form-control-plaintext border rounded px-2 py-1 bg-white">-
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Printer Details Section -->
                    <h6 class="mt-3 mb-2"><i class="fas fa-print"></i> Printers</h6>
                    <div class="table-responsive">
                        <table class="table table-sm table-bordered" id="viewPrintersTable">
                            <thead class="thead-light">
                            <tr>
                                <th>Location</th>
                                <th>Existing Model</th>
                                <th>Serial</th>
                                <th>Recommended Model</th>
                            </tr>
                            </thead>
                            <tbody id="viewPrintersBody">
                            </tbody>
                        </table>
                    </div>

                    <!-- Timestamps -->
                    <div class="row mt-3">
                        <div class="col-md-6">
                            <small class="text-muted">Created At: <span id="viewCreatedAt">-</span></small>
                        </div>
                        <div class="col-md-6">
                            <small class="text-muted">Last Updated: <span id="viewUpdatedAt">-</span></small>
                        </div>
                    </div>

                    <!-- Comment History Section -->
                    <div class="mt-3">
                        <button type="button" class="btn btn-info btn-sm" onclick="toggleCommentHistory()">
                            <i class="fas fa-history"></i> View Comment History
                        </button>
                        <div id="commentHistorySection" style="display:none;" class="mt-3">
                            <h6 class="font-weight-bold"><i class="fas fa-comments"></i> Comment History</h6>
                            <div id="commentHistoryLoading" class="text-center py-2" style="display:none;">
                                <i class="fas fa-spinner fa-spin"></i> Loading...
                            </div>
                            <div class="table-responsive">
                                <table class="table table-sm table-bordered" id="commentHistoryTable"
                                       style="display:none;">
                                    <thead class="thead-light">
                                    <tr>
                                        <th>User</th>
                                        <th>Stage</th>
                                        <th>Comments</th>
                                        <th>Date/Time</th>
                                    </tr>
                                    </thead>
                                    <tbody id="commentHistoryBody">
                                    </tbody>
                                </table>
                            </div>
                            <div id="noCommentsMsg" class="text-muted" style="display:none;">
                                No comments found for this request.
                            </div>
                        </div>
                    </div>
                </div>

                <div id="viewModalError" class="alert alert-danger" style="display:none;">
                    <i class="fas fa-exclamation-triangle"></i>
                    <span id="viewModalErrorMsg"></span>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">
                    <i class="fas fa-times"></i> Close
                </button>
                <button type="button" class="btn btn-warning" id="viewModalEditBtn" style="display:none;"
                        onclick="openEditFromView()">
                    <i class="fas fa-edit"></i> Edit Request
                </button>
            </div>
        </div>
    </div>
</div>
<input type="hidden" id="viewReqIdHidden" value="">

<!-- Quotation Pending Modal (STG4_AM_COMMERCIAL) -->
<div class="modal fade" id="quotationModal" tabindex="-1" role="dialog">
    <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
            <div class="modal-header py-2 bgc-info-tp1 border-0  radius-t-1">
                <h5 class="modal-title text-white-tp1 text-110 pl-2 font-bolder">
                    <i class="fas fa-file-invoice"></i> Quotation Management - <span id="quotationReqId">REQ-0000</span>
                </h5>
                <button type="button"
                        class="position-tr btn btn-xs btn-outline-white btn-h-yellow btn-a-yellow mt-1px mr-1px btn-brc-tp"
                        data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true" class="text-150">&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <input type="hidden" id="quotationReqIdHidden">

                <!-- Request Summary -->
                <div class="card mb-3 bg-light">
                    <div class="card-body py-2">
                        <div class="row text-sm">
                            <div class="col-md-3">
                                <small class="text-muted">Client:</small>
                                <div class="font-weight-bold" id="quotationClientName">-</div>
                            </div>
                            <div class="col-md-3">
                                <small class="text-muted">Printers:</small>
                                <div class="font-weight-bold" id="quotationPrinterCount">-</div>
                            </div>
                            <div class="col-md-3">
                                <small class="text-muted">Commercial Type:</small>
                                <div class="font-weight-bold" id="quotationCommercialType">-</div>
                            </div>
                            <div class="col-md-3">
                                <small class="text-muted">Agreement:</small>
                                <div class="font-weight-bold" id="quotationAgreement">-</div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Quotation Status Stepper -->
                <div class="mb-4">
                    <div class="d-flex justify-content-between align-items-center mb-2">
                        <div class="text-center flex-fill">
                            <div class="rounded-circle bg-success text-white d-inline-flex align-items-center justify-content-center"
                                 style="width:32px;height:32px;">
                                <i class="fas fa-check"></i>
                            </div>
                            <div class="small mt-1">Commercial Approved</div>
                        </div>
                        <div class="flex-fill border-top border-secondary" style="height:2px;margin-top:-15px;"></div>
                        <div class="text-center flex-fill">
                            <div class="rounded-circle bg-info text-white d-inline-flex align-items-center justify-content-center"
                                 style="width:32px;height:32px;">
                                <strong>2</strong>
                            </div>
                            <div class="small mt-1 font-weight-bold text-info">Quotation Pending</div>
                        </div>
                        <div class="flex-fill border-top border-secondary" style="height:2px;margin-top:-15px;"></div>
                        <div class="text-center flex-fill">
                            <div class="rounded-circle bg-secondary text-white d-inline-flex align-items-center justify-content-center"
                                 style="width:32px;height:32px;">
                                <strong>3</strong>
                            </div>
                            <div class="small mt-1 text-muted">Approval Received</div>
                        </div>
                        <div class="flex-fill border-top border-secondary" style="height:2px;margin-top:-15px;"></div>
                        <div class="text-center flex-fill">
                            <div class="rounded-circle bg-secondary text-white d-inline-flex align-items-center justify-content-center"
                                 style="width:32px;height:32px;">
                                <strong>4</strong>
                            </div>
                            <div class="small mt-1 text-muted">PO Uploaded</div>
                        </div>
                    </div>
                </div>

                <!-- Actions -->
                <div class="form-group">
                    <button class="btn btn-primary btn-block" id="markQuotationSentBtn" onclick="submitQuotation()">
                        <i class="fas fa-paper-plane"></i> Mark "Quotation Sent"
                    </button>
                </div>

                <!-- File Upload -->
                <div class="form-group">
                    <label>Upload Approved Quotation or Purchase Order</label>
                    <div class="custom-file">
                        <input type="file" class="custom-file-input" id="quotationFile" accept=".pdf,.doc,.docx">
                        <label class="custom-file-label" for="quotationFile">Choose file...</label>
                    </div>
                    <small class="form-text text-muted">PDF, DOC (Max 10MB)</small>
                </div>

                <div class="form-group">
                    <button class="btn btn-success btn-block" id="approvalReceivedBtn" disabled
                            onclick="submitQuotationApprovalFromModal()">
                        <i class="fas fa-check"></i> Mark "Quotation Approval Received"
                    </button>
                    <small class="form-text text-muted text-center">Available after quotation is sent</small>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-outline-primary" onclick="viewAgreement()">
                    <i class="fas fa-eye"></i> View Agreement
                </button>
            </div>
        </div>
    </div>
</div>

<!-- Quotation Sent Modal (STG5_AM_MANAGER_FINAL) -->
<div class="modal fade" id="quotationSentModal" tabindex="-1" role="dialog">
    <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
            <div class="modal-header bg-success text-white">
                <h5 class="modal-title">
                    <i class="fas fa-check-circle"></i> Quotation Approval - <span
                        id="quotationSentReqId">REQ-0000</span>
                </h5>
                <button type="button" class="close text-white" data-dismiss="modal">&times;</button>
            </div>
            <div class="modal-body">
                <input type="hidden" id="quotationSentReqIdHidden">

                <div class="card mb-3 bg-light">
                    <div class="card-body py-2">
                        <div class="row text-sm">
                            <div class="col-md-4">
                                <small class="text-muted">Client:</small>
                                <div class="font-weight-bold" id="quotationSentClientName">-</div>
                            </div>
                            <div class="col-md-4">
                                <small class="text-muted">Stage:</small>
                                <div class="font-weight-bold">Quotation Sent</div>
                            </div>
                            <div class="col-md-4">
                                <small class="text-muted">Status:</small>
                                <div class="font-weight-bold text-info">Awaiting Approval</div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- PO Upload -->
                <div class="form-group">
                    <label>Upload Approved Quotation / Purchase Order</label>
                    <input type="file" class="form-control-file" id="poFile" accept=".pdf,.doc,.docx">
                    <small class="form-text text-muted">Upload PO or approved quotation (PDF, DOC - Max 10MB)</small>
                </div>

                <div class="form-group">
                    <label>Comments</label>
                    <textarea class="form-control" id="quotationSentComments" rows="3"
                              placeholder="Add any comments..."></textarea>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-success" onclick="submitQuotationApproval()">
                    <i class="fas fa-check"></i> Mark Approval Received
                </button>
            </div>
        </div>
    </div>
</div>

<!-- Credit Note Modal (STG11_CREDIT_NOTE) -->
<style>
    .location-group {
        background: #f8f9fa;
        border: 1px solid #dee2e6;
        border-radius: 0.5rem;
        margin-bottom: 1rem;
        overflow: hidden;
    }

    .location-group__header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 1rem;
        background: linear-gradient(135deg, #e9ecef, #f8f9fa);
        border-bottom: 1px solid #dee2e6;
    }

    .location-group__body {
        padding: 1rem;
    }

    .location-group.saved {
        border-color: #28a745;
    }

    .location-group.saved .location-group__header {
        background: linear-gradient(135deg, #d4edda, #f8f9fa);
    }

    .printer-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 0.75rem;
        background: white;
        border: 1px solid #dee2e6;
        border-radius: 0.375rem;
        margin-bottom: 0.5rem;
    }

    .printer-item:last-child {
        margin-bottom: 0;
    }

    .saved-indicator {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        color: #155724;
        font-size: 0.75rem;
        font-weight: 500;
    }
</style>

<div class="modal fade modal-lg" id="creditNoteModal" tabindex="-1" role="dialog">
    <div class="modal-dialog modal-dialog-centered" role="document">
        <div class="modal-content border-0 radius-1 shadow">
            <div class="modal-header bgc-primary-d1 text-white py-2 pl-3">
                <h5 class="modal-title text-110">
                    <i class="fas fa-credit-card mr-1"></i> Issue Credit Note - <span id="creditNoteReqId">REQ-0000</span>
                </h5>
                <button type="button" class="close text-white" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body px-4" style="max-height: 70vh; overflow-y: auto;">
                <input type="hidden" id="creditNoteReqIdHidden">

                <!-- Request Summary -->
                <div class="card mb-3 bg-light">
                    <div class="card-body py-2">
                        <div class="row text-sm">
                            <div class="col-md-4">
                                <small class="text-muted">Client:</small>
                                <div class="font-weight-bold" id="creditNoteClientName">-</div>
                            </div>
                            <div class="col-md-4">
                                <small class="text-muted">Request ID:</small>
                                <div class="font-weight-bold" id="creditNoteReqIdDisplay">-</div>
                            </div>
                            <div class="col-md-4">
                                <small class="text-muted">Agreement:</small>
                                <div class="font-weight-bold" id="creditNoteAgreement">-</div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Loading Indicator -->
                <div id="creditNoteLoading" class="text-center py-4">
                    <i class="fas fa-spinner fa-spin fa-2x"></i>
                    <p class="mt-2">Loading credit note details...</p>
                </div>

                <!-- Credit Note Details - Grouped by Location -->
                <div id="creditNoteDetails" style="display:none;">
                    <h6 class="font-weight-bold mb-3">
                        <i class="fas fa-map-marker-alt text-primary"></i> Printers (Grouped by Location)
                    </h6>

                    <!-- Location Groups Container -->
                    <div id="creditNoteLocationsContainer">
                        <!-- Populated dynamically -->
                    </div>

                    <!-- Total -->
                    <div class="d-flex justify-content-between align-items-center p-3 bg-light rounded mb-3">
                        <span class="font-weight-bold">Total Potential Credit:</span>
                        <span class="h5 mb-0 font-weight-bold text-danger" id="creditNoteTotalAmount">₹0</span>
                    </div>

                    <!-- Forward to Higher Authority Section (hidden by default) -->
                    <div id="creditNoteForwardSection" style="display:none;" class="card mb-3 border-primary">
                        <div class="card-header bgc-primary-l4 text-primary-d2">
                            <h6 class="mb-0 font-weight-bold"><i class="fas fa-share"></i> Forward to Higher Authority</h6>
                        </div>
                        <div class="card-body">
                            <div class="form-group">
                                <label class="font-weight-bold">Select Manager <span
                                        class="text-danger">*</span></label>
                                <select class="form-control" id="creditNoteManagerSelect" required>
                                    <option value="">Select a manager for approval</option>
                                    <option value="priya">Priya Patel (AM Manager)</option>
                                    <option value="sanjay">Sanjay Kumar (Regional Head)</option>
                                    <option value="arun">Arun Mehta (Director)</option>
                                </select>
                            </div>
                            <div class="form-group">
                                <label class="font-weight-bold">Comment <span class="text-danger">*</span></label>
                                <textarea class="form-control" id="creditNoteForwardComment" rows="3" required
                                          placeholder="Enter your comment for the manager..."></textarea>
                            </div>
                            <div>
                                <button type="button" class="btn btn-secondary btn-sm px-3"
                                        onclick="toggleCreditNoteForwardSection()">Cancel
                                </button>
                                <button type="button" class="btn btn-primary btn-sm px-3"
                                        onclick="submitCreditNoteForward()">
                                    <i class="fas fa-share"></i> Forward Request
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="modal-footer bgc-grey-l4 border-t-1 brc-grey-l2 justify-content-between">
                <div>
                    <button type="button" class="btn btn-outline-secondary btn-bold px-4" onclick="submitCreditNoteNoAction()">
                        <i class="fas fa-ban mr-1"></i> No Action
                    </button>
                    <button type="button" class="btn btn-outline-primary btn-bold px-4 ml-2" onclick="toggleCreditNoteForwardSection()">
                        <i class="fas fa-share mr-1"></i> Forward to Higher Authority
                    </button>
                </div>
                <div>
                    <button type="button" class="btn btn-secondary btn-bold px-4" data-dismiss="modal">
                        <i class="fas fa-times mr-1"></i> Cancel
                    </button>
                    <button type="button" class="btn btn-primary btn-bold px-4" onclick="submitCreditNoteToBilling()">
                        <i class="fas fa-check-circle mr-1"></i> Forward to Billing
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Location Credit Note Modal (for entering/saving details per location) -->
<div class="modal fade" id="locationCreditNoteModal" tabindex="-1" role="dialog" data-backdrop="static">
    <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
            <div class="modal-header bg-primary text-white">
                <h5 class="modal-title">
                    <i class="fas fa-edit"></i> Add Credit Note - <span id="locationCreditNoteTitle">Location</span>
                </h5>
                <button type="button" class="close text-white" onclick="closeLocationCreditNoteModal()">&times;</button>
            </div>
            <div class="modal-body">
                <input type="hidden" id="locationCreditNoteLocId">

                <!-- Location Header -->
                <div class="alert alert-info mb-3">
                    <i class="fas fa-map-marker-alt"></i>
                    <strong>Location:</strong> <span id="locationCreditNoteLocation">-</span>
                </div>

                <!-- Printers Table for this Location -->
                <div class="table-responsive mb-3">
                    <table class="table table-bordered table-sm">
                        <thead class="thead-light">
                        <tr>
                            <th>Printer / Item</th>
                            <th>Issue Description</th>
                            <th>Agreement Rate</th>
                            <th style="width:120px;">Adjust Amount</th>
                        </tr>
                        </thead>
                        <tbody id="locationPrintersTable">
                        <!-- Populated dynamically -->
                        </tbody>
                        <tfoot>
                        <tr class="bg-light">
                            <td colspan="3" class="text-right"><strong>Total Credit Amount:</strong></td>
                            <td><strong id="locationTotalAmount">₹0</strong></td>
                        </tr>
                        </tfoot>
                    </table>
                </div>

                <small class="text-muted d-block mb-3">
                    <i class="fas fa-info-circle"></i> Amounts are auto-filled from commercial agreement. You can adjust
                    if needed.
                </small>

                <!-- Comments -->
                <div class="form-group">
                    <label class="font-weight-bold">Comments</label>
                    <textarea class="form-control" id="locationCreditNoteComments" rows="3"
                              placeholder="Enter comments (optional)..."></textarea>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" onclick="closeLocationCreditNoteModal()">Cancel</button>
                <button type="button" class="btn btn-success" onclick="saveLocationCreditNote()">
                    <i class="fas fa-check"></i> Save
                </button>
            </div>
        </div>
    </div>
</div>

<!-- Credit Note No Action Modal -->
<div class="modal fade" id="creditNoteNoActionModal" tabindex="-1" role="dialog" data-backdrop="static">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header bg-secondary text-white">
                <h5 class="modal-title">
                    <i class="fas fa-times-circle"></i> Confirm No Action
                </h5>
                <button type="button" class="close text-white" data-dismiss="modal">&times;</button>
            </div>
            <div class="modal-body">
                <div class="form-group">
                    <label class="font-weight-bold">Reason for No Action <span class="text-danger">*</span></label>
                    <textarea class="form-control" id="creditNoteNoActionReason" rows="3" required
                              placeholder="Explain why no credit note is required..."></textarea>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-danger" onclick="confirmCreditNoteNoAction()">
                    <i class="fas fa-check"></i> Confirm No Action
                </button>
            </div>
        </div>
    </div>
</div>

<%-- Book Printer Order Modal (STG6_PRINTER_ORDER) - Modular Include --%>
<%@ include file="includes/bookPrinterOrderModal.jsp" %>

<!-- View Agreement Modal -->
<div class="modal fade" id="agreementModal" tabindex="-1" role="dialog">
    <div class="modal-dialog modal-xl" role="document">
        <div class="modal-content">
            <div class="modal-header py-2 bgc-info-tp1 border-0  radius-t-1">
                <h5 class="modal-title text-white-tp1 text-110 pl-2 font-bolder">
                    <i class="fas fa-file-contract"></i> Agreement Details - <span id="agreementReqId">REQ-0000</span>
                </h5>
                <button type="button"
                        class="position-tr btn btn-xs btn-outline-white btn-h-yellow btn-a-yellow mt-1px mr-1px btn-brc-tp"
                        data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true" class="text-150">&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <div id="agreementLoading" class="text-center py-4">
                    <i class="fas fa-spinner fa-spin fa-2x"></i>
                    <p class="mt-2">Loading agreement details...</p>
                </div>
                <div id="agreementContent" style="display:none;">
                    <div id="agreementEmpty" class="text-center py-4" style="display:none;">
                        <i class="fas fa-file-alt fa-3x text-muted mb-3"></i>
                        <h5 class="text-muted">No Agreement Details Found</h5>
                    </div>
                    <div id="agreementData">
                        <div class="table-responsive">
                            <table class="table table-bordered table-sm" id="agreementTable">
                                <thead class="thead-light">
                                <tr>
                                    <th>Serial No.</th>
                                    <th>Agreement No.</th>
                                    <th>Rent</th>
                                    <th>Free Prints</th>
                                    <th>A4 Rate</th>
                                    <th>A3 Rate</th>
                                    <th>Color A4 Rate</th>
                                    <th>Color A3 Rate</th>
                                    <th>Scan Rate</th>
                                    <th>AMC</th>
                                </tr>
                                </thead>
                                <tbody id="agreementTableBody">
                                </tbody>
                            </table>
                        </div>

                        <div class="card mt-3 bg-light">
                            <div class="card-header">
                                <h6 class="mb-0"><i class="fas fa-info-circle"></i> Rate Legend</h6>
                            </div>
                            <div class="card-body py-2">
                                <div class="row small">
                                    <div class="col-md-4"><strong>Rent:</strong> Monthly fixed rental</div>
                                    <div class="col-md-4"><strong>Free Prints:</strong> Included prints per month</div>
                                    <div class="col-md-4"><strong>A4/A3 Rate:</strong> Per page rate after free prints
                                    </div>
                                </div>
                                <div class="row small mt-1">
                                    <div class="col-md-4"><strong>Post Rate:</strong> Rate after commitment period</div>
                                    <div class="col-md-4"><strong>Color Rate:</strong> Rate for color pages</div>
                                    <div class="col-md-4"><strong>AMC:</strong> Annual Maintenance Contract</div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
            </div>
        </div>
    </div>
</div>

<!-- Reminder Modal -->
<div class="modal fade" id="reminderModal" tabindex="-1" role="dialog">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header py-2 bgc-warning-tp1 border-0  radius-t-1">
                <h5 class="modal-title text-white-tp1 text-110 pl-2 font-bolder">
                    <i class="fas fa-bell"></i> Send Reminder
                </h5>
                <button type="button"
                        class="position-tr btn btn-xs btn-outline-white btn-h-yellow btn-a-yellow mt-1px mr-1px btn-brc-tp"
                        data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true" class="text-150">&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <input type="hidden" id="reminderReqIdHidden">

                <div class="card mb-3 bg-light">
                    <div class="card-body py-2">
                        <div class="row text-sm">
                            <div class="col-md-6">
                                <small class="text-muted">Request ID:</small>
                                <div class="font-weight-bold" id="reminderReqId">-</div>
                            </div>
                            <div class="col-md-6">
                                <small class="text-muted">Stage Owner:</small>
                                <div class="font-weight-bold" id="reminderOwner">-</div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="alert alert-info">
                    <i class="fas fa-info-circle"></i>
                    A notification will be sent to <strong id="reminderOwnerName">the stage owner</strong> to remind
                    them about the pending action.
                </div>

                <div class="form-group">
                    <label>Additional Message (Optional)</label>
                    <textarea class="form-control" id="reminderMessage" rows="3"
                              placeholder="Add a custom message..."></textarea>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-warning" onclick="confirmSendReminder()">
                    <i class="fas fa-bell"></i> Send Reminder
                </button>
            </div>
        </div>
    </div>
</div>

<script>
    var contextPath = '${pageContext.request.contextPath}';
    var commentHistoryLoaded = false;

    // ============================================================
    // View Modal Functions (Read-Only with conditional Edit)
    // ============================================================
    var currentViewStage = '';

    function openViewModal(reqId, stageCode) {
        // Reset modal state
        $('#viewModalLoading').show();
        $('#viewRequestContent').hide();
        $('#viewModalError').hide();
        $('#viewPrintersBody').empty();
        $('#viewModalEditBtn').hide();
        $('#viewReqIdHidden').val(reqId);
        currentViewStage = stageCode || '';

        // Reset comment history
        commentHistoryLoaded = false;
        $('#commentHistorySection').hide();
        $('#commentHistoryBody').empty();

        // Open modal
        $('#viewRequestModal').modal('show');

        // Fetch request details (reuse same endpoint as edit)
        $.get(contextPath + '/views/replacement/request', {
            action: 'getDetails',
            id: reqId
        }, function (data) {
            if (data.success) {
                populateViewForm(data, stageCode);
            } else {
                showViewError(data.message || 'Failed to load request details');
            }
        }, 'json').fail(function () {
            showViewError('Network error. Please try again.');
        });
    }

    function openEditFromView() {
        var reqId = $('#viewReqIdHidden').val();
        $('#viewRequestModal').modal('hide');
        openEditModal(reqId);
    }

    function populateViewForm(data, stageCode) {
        var req = data.request;
        var tat = data.tat;
        var printers = data.printers || [];
        var contact = data.contact || {};
        var reasons = data.reasons || [];

        // Find reason name
        var reasonName = '-';
        if (req.reasonId && reasons.length > 0) {
            var reason = reasons.find(function (r) {
                return r.id == req.reasonId;
            });
            if (reason) reasonName = reason.name;
        }

        // Show Edit button only for STG2_SERVICE_TL_REVIEW stage and not for REJECTED/COMPLETED/CANCELLED
        var actualStage = stageCode || req.currentStage || '';
        var reqStatus = req.status || '';
        if ((actualStage === 'STG2_SERVICE_TL_REVIEW' || actualStage === 'Service TL Review')
            && reqStatus !== 'REJECTED' && reqStatus !== 'COMPLETED' && reqStatus !== 'CANCELLED') {
            $('#viewModalEditBtn').show();
        } else {
            $('#viewModalEditBtn').hide();
        }

        // Request Info
        $('#viewReqIdDisplay').text('REQ-' + req.id);
        $('#viewClientName').text(req.clientName || '-');
        $('#viewCurrentStage').text(req.currentStageName || req.currentStage || '-');
        $('#viewReplacementType').text(formatReplacementType(req.replacementType) || '-');
        $('#viewReasonName').text(reasonName);
        $('#viewStatus').text(req.status || '-');
        $('#viewRequester').text(req.requesterName || req.createdBy || '-');

        // Sign-In Location
        $('#viewSignInLocation').text(req.signInLocation || '-');

        // Contact Details
        $('#viewContactName').text(contact.contactName || '-');
        $('#viewContactNumber').text(contact.contactNumber || '-');
        $('#viewContactEmail').text(contact.contactEmail || '-');

        // Timestamps
        $('#viewCreatedAt').text(req.createdAt || '-');
        $('#viewUpdatedAt').text(req.updatedAt || '-');

        // TAT Info
        if (tat) {
            $('#viewStageStartTime').text(tat.stageStartTime || '-');
            $('#viewCurrentTime').text(tat.currentTime || '-');

            var tatUnitDisplay = (tat.tatUnit || 'DAYS').toUpperCase();
            $('#viewTatDuration').text(tat.tatDuration + ' ' + tatUnitDisplay);

            var percentage = tat.percentage || 0;
            var statusClass = 'within';
            var statusLabel = 'Within TAT';
            if (percentage >= 100) {
                statusClass = 'breach';
                statusLabel = 'TAT Breached';
            } else if (percentage >= 80) {
                statusClass = 'warning';
                statusLabel = 'Warning';
            }

            var progressHtml = '<div class="tat-display__progress mt-1">' +
                '<div class="tat-display__progress-bar tat-display__progress-bar--' + statusClass +
                '" style="width: ' + Math.min(percentage, 100) + '%;"></div>' +
                '</div>' +
                '<small class="text-muted">' + Math.round(percentage) + '% used (' + statusLabel + ')</small>';
            $('#viewTatProgress').html(progressHtml);
        } else {
            $('#viewStageStartTime').text('-');
            $('#viewCurrentTime').text('-');
            $('#viewTatDuration').text('-');
            $('#viewTatProgress').html('<small class="text-muted">N/A</small>');
        }

        // Printers (read-only table)
        var printersHtml = '';
        if (printers.length > 0) {
            printers.forEach(function (p) {
                var recommendedModel = p.newModelName || p.newModelText || '-';
                printersHtml += '<tr>' +
                    '<td>' + escapeHtml(p.location || '-') + '</td>' +
                    '<td>' + escapeHtml(p.existingModelName || '-') + '</td>' +
                    '<td><code>' + escapeHtml(p.existingSerial || '-') + '</code></td>' +
                    '<td>' + escapeHtml(recommendedModel) + '</td>' +
                    '</tr>';
            });
        } else {
            printersHtml = '<tr><td colspan="4" class="text-center text-muted">No printers found</td></tr>';
        }
        $('#viewPrintersBody').html(printersHtml);

        // Show content
        $('#viewModalLoading').hide();
        $('#viewRequestContent').show();
    }

    function formatReplacementType(type) {
        if (!type) return '-';
        return type.replace(/_/g, ' ').replace(/\b\w/g, function (l) {
            return l.toUpperCase();
        });
    }

    function showViewError(msg) {
        $('#viewModalLoading').hide();
        $('#viewRequestContent').hide();
        $('#viewModalErrorMsg').text(msg);
        $('#viewModalError').show();
    }

    function toggleCommentHistory() {
        var section = $('#commentHistorySection');
        if (section.is(':visible')) {
            section.hide();
        } else {
            section.show();
            if (!commentHistoryLoaded) {
                loadCommentHistory();
            }
        }
    }

    function loadCommentHistory() {
        var reqId = $('#viewReqIdHidden').val();
        if (!reqId) return;

        $('#commentHistoryLoading').show();
        $('#commentHistoryTable').hide();
        $('#noCommentsMsg').hide();

        $.get(contextPath + '/views/replacement/request', {
            action: 'getCommentHistory',
            id: reqId
        }, function (data) {
            $('#commentHistoryLoading').hide();

            if (data.success && data.data && data.data.comments && data.data.comments.length > 0) {
                var html = '';
                data.data.comments.forEach(function (c) {
                    var dateStr = c.createdAt ? new Date(c.createdAt).toLocaleString() : '-';
                    html += '<tr>' +
                        '<td>' + escapeHtml(c.userName || c.userId || '-') + '</td>' +
                        '<td>' + escapeHtml(c.stageName || '-') + '</td>' +
                        '<td>' + escapeHtml(c.comments || '-') + '</td>' +
                        '<td><small>' + dateStr + '</small></td>' +
                        '</tr>';
                });
                $('#commentHistoryBody').html(html);
                $('#commentHistoryTable').show();
                commentHistoryLoaded = true;
            } else {
                $('#noCommentsMsg').show();
            }
        }, 'json').fail(function () {
            $('#commentHistoryLoading').hide();
            $('#noCommentsMsg').text('Failed to load comment history').show();
        });
    }

    // ============================================================
    // Edit Modal Functions
    // ============================================================
    function openEditModal(reqId) {
        // Reset modal state
        $('#editModalLoading').show();
        $('#editRequestForm').hide();
        $('#editModalError').hide();
        $('#editPrintersBody').empty();

        // Open modal
        $('#editRequestModal').modal('show');

        // Fetch request details
        $.get(contextPath + '/views/replacement/request', {
            action: 'getDetails',
            id: reqId
        }, function (data) {
            if (data.success) {
                populateEditForm(data);
            } else {
                showEditError(data.message || 'Failed to load request details');
            }
        }, 'json').fail(function () {
            showEditError('Network error. Please try again.');
        });
    }

    function populateEditForm(data) {
        var req = data.request;
        var tat = data.tat;
        var printers = data.printers || [];
        var contact = data.contact || {};
        var reasons = data.reasons || [];

        // Request Info
        $('#editReqId').val(req.id);
        $('#editReqIdDisplay').val('REQ-' + req.id);
        $('#editClientName').val(req.clientName);
        $('#editCurrentStage').val(req.currentStageName || req.currentStage);
        $('#editReplacementType').val(req.replacementType);
        $('#editStatus').val(req.status);

        // Populate Reason dropdown
        var reasonSelect = $('#editReasonId');
        reasonSelect.empty().append('<option value="">Select Reason</option>');
        reasons.forEach(function (r) {
            var selected = (r.id == req.reasonId) ? 'selected' : '';
            reasonSelect.append('<option value="' + r.id + '" ' + selected + '>' + escapeHtml(r.name) + '</option>');
        });

        // Store signInBranchId for location loading (uses branch ID to find all related branches)
        $('#editRequestForm').data('signInBranchId', req.signInBranchId);

        // Load Sign-In Location dropdown using the current branch ID
        loadEditBranches(req.signInBranchId, req.signInBranchId);

        // Contact Details
        $('#editContactName').val(contact.contactName || '');
        $('#editContactNumber').val(contact.contactNumber || '');
        $('#editContactEmail').val(contact.contactEmail || '');

        // TAT Info
        if (tat) {
            $('#editStageStartTime').text(tat.stageStartTime || '-');
            $('#editCurrentTime').text(tat.currentTime || '-');

            // Display TAT Duration with proper unit
            var tatUnitDisplay = (tat.tatUnit || 'DAYS').toUpperCase();
            $('#editTatDuration').text(tat.tatDuration + ' ' + tatUnitDisplay);

            // TAT Progress
            var percentage = tat.percentage || 0;
            var statusClass = 'within';
            var statusLabel = 'Within TAT';
            if (percentage >= 100) {
                statusClass = 'breach';
                statusLabel = 'TAT Breached';
            } else if (percentage >= 80) {
                statusClass = 'warning';
                statusLabel = 'Warning';
            }

            var progressHtml = '<div class="tat-display__progress mt-1">' +
                '<div class="tat-display__progress-bar tat-display__progress-bar--' + statusClass +
                '" style="width: ' + Math.min(percentage, 100) + '%;"></div>' +
                '</div>' +
                '<small class="text-muted">' + Math.round(percentage) + '% used (' + statusLabel + ')</small>';
            $('#editTatProgress').html(progressHtml);
        }

        // Printers
        var printersHtml = '';
        printers.forEach(function (p) {
            printersHtml += '<tr>' +
                '<td>' + escapeHtml(p.location || '-') + '</td>' +
                '<td>' + escapeHtml(p.existingModelName || '-') + '</td>' +
                '<td><code>' + escapeHtml(p.existingSerial || '-') + '</code></td>' +
                '<td>' +
                '<select class="form-control form-control-sm printer-new-model" ' +
                'data-printer-id="' + p.id + '" name="newModelId_' + p.id + '">' +
                '<option value="">-- Select --</option>' +
                '</select>' +
                '<input type="text" class="form-control form-control-sm mt-1 printer-new-model-text" ' +
                'data-printer-id="' + p.id + '" name="newModelText_' + p.id + '" ' +
                'placeholder="Or type manually" value="' + escapeHtml(p.newModelText || '') + '">' +
                '</td>' +
                '</tr>';
        });
        $('#editPrintersBody').html(printersHtml);

        // Load printer models for dropdowns
        loadPrinterModelsForEdit(printers);

        // Show form
        $('#editModalLoading').hide();
        $('#editRequestForm').show();
    }

    function loadPrinterModelsForEdit(printers) {
        $.get(contextPath + '/views/replacement/request', {action: 'getAllPrinterModels'}, function (response) {
            // Handle wrapped response: {success: true, data: [...]}
            var models = response.data || response;
            if (models && models.length > 0) {
                $('.printer-new-model').each(function () {
                    var select = $(this);
                    var printerId = select.data('printer-id');
                    var printer = printers.find(function (p) {
                        return p.id == printerId;
                    });

                    models.forEach(function (m) {
                        var selected = (printer && printer.newModelId == m.id) ? 'selected' : '';
                        select.append('<option value="' + m.id + '" ' + selected + '>' + escapeHtml(m.modelName) + '</option>');
                    });
                });
            } else {
                console.warn('No printer models loaded:', response);
            }
        }, 'json').fail(function (xhr, status, error) {
            console.error('Failed to load printer models:', error);
        });
    }

    /**
     * Load branches for the Sign-In Location dropdown in edit modal
     * Uses branchId (CLIENT.ID) to find all branches for the same logical client
     */
    function loadEditBranches(branchId, selectedBranchId) {
        var $select = $('#editSignInLocation');
        $select.html('<option value="">Loading...</option>');

        if (!branchId) {
            $select.html('<option value="">No location available</option>');
            return;
        }

        $.ajax({
            url: contextPath + '/views/replacement/request',
            data: {action: 'getClientBranches', clientId: branchId},
            dataType: 'json',
            success: function (response) {
                if (response.success && response.data && response.data.length > 0) {
                    var options = '<option value="">Select Sign-In Location</option>';
                    response.data.forEach(function (b) {
                        var addr = (b.address || '').trim();
                        //var label = addr ? (b.branch + ': ' + addr) : (b.branch || '');
                        var label = (b.branch || '');
                        var selected = (b.id == selectedBranchId) ? 'selected' : '';
                        options += '<option value="' + b.id + '" ' + selected + '>' + escapeHtml(label) + '</option>';
                    });
                    $select.html(options);
                } else {
                    $select.html('<option value="">No locations found</option>');
                }
            },
            error: function () {
                $select.html('<option value="">Error loading locations</option>');
            }
        });
    }

    /**
     * Load contact details when Sign-In Location changes in edit modal
     */
    function loadEditBranchContact(branchId) {
        if (!branchId) return;

        $.ajax({
            url: contextPath + '/views/replacement/request',
            data: {action: 'getBranchDetails', branchId: branchId},
            dataType: 'json',
            success: function (response) {
                if (response.success && response.data) {
                    $('#editContactName').val(response.data.contactPerson || '');
                    $('#editContactNumber').val(response.data.mobileNo || '');
                    $('#editContactEmail').val(response.data.emailId1 || '');
                }
            },
            error: function () {
                console.error('Error loading branch contact details');
            }
        });
    }

    // Bind change handler for edit modal Sign-In Location dropdown
    $(document).ready(function () {
        $('#editSignInLocation').on('change', function () {
            var branchId = $(this).val();
            if (branchId) {
                loadEditBranchContact(branchId);
            }
        });
    });

    function showEditError(msg) {
        $('#editModalLoading').hide();
        $('#editRequestForm').hide();
        $('#editModalErrorMsg').text(msg);
        $('#editModalError').show();
    }

    function closeRequest() {
        var reqId = $('#editReqId').val();
        if (!reqId) {
            showAppAlert('No request selected.', 'warning');
            return;
        }
        $('#closeReqIdHidden').val(reqId);
        $('#closeReasonText').val('');
        $('#editRequestModal').modal('hide');
        $('#closeRequestModal').modal('show');
    }

    function confirmCloseRequest() {
        var reqId = $('#closeReqIdHidden').val();
        var reason = $('#closeReasonText').val().trim();

        if (!reason) {
            showAppAlert('Please enter a reason for closing.', 'warning');
            return;
        }

        $('#btnConfirmClose').prop('disabled', true).html('<i class="fas fa-spinner fa-spin"></i> Closing...');

        $.ajax({
            url: contextPath + '/views/replacement/request',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({
                action: 'closeRequest',
                reqId: reqId,
                reason: reason
            }),
            success: function (resp) {
                if (resp.success) {
                    $('#closeRequestModal').modal('hide');
                    showAppAlert('Request closed successfully!', 'success');
                    setTimeout(function() { location.reload(); }, 10000);
                } else {
                    showAppAlert('Failed: ' + (resp.message || 'Unknown error'), 'danger');
                    $('#btnConfirmClose').prop('disabled', false).html('<i class="fas fa-ban"></i> Confirm Close');
                }
            },
            error: function () {
                showAppAlert('Network error. Please try again.', 'danger');
                $('#btnConfirmClose').prop('disabled', false).html('<i class="fas fa-ban"></i> Confirm Close');
            }
        });
    }

    function saveEditRequest() {
        var formData = {
            action: 'update',
            reqId: $('#editReqId').val(),
            replacementType: $('#editReplacementType').val(),
            reasonId: $('#editReasonId').val(),
            signInBranchId: $('#editSignInLocation').val(),
            comments: $('#editComments').val(),
            contactName: $('#editContactName').val(),
            contactNumber: $('#editContactNumber').val(),
            contactEmail: $('#editContactEmail').val(),
            printers: []
        };

        // Collect printer updates
        $('.printer-new-model').each(function () {
            var printerId = $(this).data('printer-id');
            var newModelId = $(this).val();
            var newModelText = $('input[name="newModelText_' + printerId + '"]').val();

            formData.printers.push({
                id: printerId,
                newModelId: newModelId,
                newModelText: newModelText
            });
        });

        // Disable button
        $('#btnSaveEdit').prop('disabled', true).html('<i class="fas fa-spinner fa-spin"></i> Saving...');

        $.ajax({
            url: contextPath + '/views/replacement/request',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(formData),
            success: function (resp) {
                if (resp.success) {
                    $('#editRequestModal').modal('hide');
                    showAppAlert('Request updated successfully!', 'success');
                    setTimeout(function() { location.reload(); }, 10000);
                } else {
                    showAppAlert('Failed: ' + (resp.message || 'Unknown error'), 'danger');
                    $('#btnSaveEdit').prop('disabled', false).html('<i class="fas fa-save"></i> Save Changes');
                }
            },
            error: function () {
                showAppAlert('Network error. Please try again.', 'danger');
                $('#btnSaveEdit').prop('disabled', false).html('<i class="fas fa-save"></i> Save Changes');
            }
        });
    }

    function escapeHtml(text) {
        if (!text) return '';
        return text.toString()
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

    function sendRemind(reqId, ownerName, stageName) {
        $('#reminderReqIdHidden').val(reqId);
        $('#reminderReqId').text('REQ-' + reqId);
        $('#reminderOwner').text(ownerName || '-');
        $('#reminderOwnerName').text(ownerName || 'the stage owner');
        $('#reminderMessage').val('');
        $('#reminderModal').modal('show');
    }

    function confirmSendReminder() {
        var reqId = $('#reminderReqIdHidden').val();
        var message = $('#reminderMessage').val();

        $.post(
            contextPath + '/views/replacement/request',
            {action: 'remind', reqId: reqId, message: message},
            function (resp) {
                if (resp.success) {
                    $('#reminderModal').modal('hide');
                    showAppAlert("Reminder sent successfully!", "success");
                } else {
                    showAppAlert("Failed: " + (resp.message || "Unknown error"), "danger");
                }
            },
            'json'
        ).fail(function () {
            showAppAlert("Network error. Please try again.", "danger");
        });
    }

    // ============================================================
    // AM/Accounts Manager Stage Modal Functions
    // ============================================================

    // STG4_AM_COMMERCIAL: Quotation Pending
    function openQuotationModal(reqId, clientName, printerCount) {
        $('#quotationReqIdHidden').val(reqId);
        $('#quotationReqId').text('REQ-' + reqId);
        $('#quotationClientName').text(clientName || '-');
        $('#quotationPrinterCount').text(printerCount || '-');
        $('#quotationCommercialType').text('New');
        $('#quotationAgreement').text('AGR-' + reqId);
        $('#quotationFile').val('');
        $('.custom-file-label').text('Choose file...');
        $('#approvalReceivedBtn').prop('disabled', true);
        $('#quotationModal').modal('show');
    }

    function submitQuotation() {
        var reqId = $('#quotationReqIdHidden').val();

        showAppConfirm('Mark quotation as sent to client?', function() {
            $.post(
                contextPath + '/views/replacement/request',
                {action: 'quotationSent', reqId: reqId},
                function (resp) {
                    if (resp.success) {
                        showAppAlert("Quotation marked as sent. You can now upload PO when received.", "success");
                        $('#markQuotationSentBtn').prop('disabled', true);
                        $('#approvalReceivedBtn').prop('disabled', false);
                    } else {
                        showAppAlert("Failed: " + (resp.message || "Unknown error"), "danger");
                    }
                },
                'json'
            ).fail(function () {
                showAppAlert("Network error. Please try again.", "danger");
            });
        });
    }

    function submitQuotationApprovalFromModal() {
        var reqId = $('#quotationReqIdHidden').val();

        showAppConfirm('Mark quotation approval as received?', function() {
            var formData = new FormData();
            formData.append('action', 'approvalReceived');
            formData.append('reqId', reqId);
            var fileInput = $('#quotationFile')[0];
            if (fileInput.files.length > 0) {
                formData.append('quotationFile', fileInput.files[0]);
            }

            $.ajax({
                url: contextPath + '/views/replacement/request',
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                dataType: 'json',
                success: function (resp) {
                    if (resp.success) {
                        $('#quotationModal').modal('hide');
                        showAppAlert("Approval received successfully!", "success");
                        setTimeout(function() { location.reload(); }, 10000);
                    } else {
                        showAppAlert("Failed: " + (resp.message || "Unknown error"), "danger");
                    }
                },
                error: function () {
                    showAppAlert("Network error. Please try again.", "danger");
                }
            });
        });
    }

    function viewAgreement() {
        var reqId = $('#quotationReqIdHidden').val();

        // Reset modal state
        $('#agreementLoading').show();
        $('#agreementContent').hide();
        $('#agreementEmpty').hide();
        $('#agreementData').show();
        $('#agreementTableBody').empty();
        $('#agreementReqId').text('REQ-' + reqId);

        // Open modal
        $('#agreementModal').modal('show');

        // Fetch agreement details
        $.get(contextPath + '/views/replacement/request', {
            action: 'getAgreementDetails',
            id: reqId
        }, function (response) {
            $('#agreementLoading').hide();
            $('#agreementContent').show();

            if (response.success && response.data && response.data.length > 0) {
                var html = '';
                response.data.forEach(function (agr) {
                    html += '<tr>';
                    html += '<td><code>' + escapeHtml(agr.serial || '-') + '</code></td>';
                    html += '<td>' + escapeHtml(agr.agrNo || '-') + '</td>';
                    html += '<td class="text-right">₹' + formatNumber(agr.rent || 0) + '</td>';
                    html += '<td class="text-right">' + formatNumber(agr.freePrints || 0) + '</td>';
                    html += '<td class="text-right">₹' + formatDecimal(agr.a4Rate || 0) + '</td>';
                    html += '<td class="text-right">₹' + formatNumber(agr.a3Rate || 0) + '</td>';
                    html += '<td class="text-right">₹' + formatDecimal(agr.a4RateColor || 0) + '</td>';
                    html += '<td class="text-right">₹' + formatNumber(agr.a3RateColor || 0) + '</td>';
                    html += '<td class="text-right">₹' + formatNumber(agr.scanRate || 0) + '</td>';
                    html += '<td class="text-right">₹' + formatNumber(agr.amc || 0) + '</td>';
                    html += '</tr>';
                });
                $('#agreementTableBody').html(html);
            } else {
                $('#agreementData').hide();
                $('#agreementEmpty').show();
            }
        }, 'json').fail(function () {
            $('#agreementLoading').hide();
            $('#agreementContent').show();
            $('#agreementData').hide();
            $('#agreementEmpty').html('<i class="fas fa-exclamation-triangle fa-3x text-danger mb-3"></i><h5 class="text-danger">Error Loading Agreement Details</h5>').show();
        });
    }

    function formatNumber(num) {
        if (num === null || num === undefined) return '0';
        return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
    }

    function formatDecimal(num) {
        if (num === null || num === undefined) return '0.00';
        return parseFloat(num).toFixed(2);
    }

    // Update file input label
    $(document).on('change', '.custom-file-input', function () {
        var fileName = $(this).val().split('\\').pop();
        $(this).siblings('.custom-file-label').addClass('selected').html(fileName);
    });

    // STG5_AM_MANAGER_FINAL: Quotation Sent
    function openQuotationSentModal(reqId, clientName) {
        $('#quotationSentReqIdHidden').val(reqId);
        $('#quotationSentReqId').text('REQ-' + reqId);
        $('#quotationSentClientName').text(clientName || '-');
        $('#poFile').val('');
        $('#quotationSentComments').val('');
        $('#quotationSentModal').modal('show');
    }

    function submitQuotationApproval() {
        var reqId = $('#quotationSentReqIdHidden').val();
        var comments = $('#quotationSentComments').val();

        showAppConfirm('Mark quotation approval as received?', function() {
            var formData = new FormData();
            formData.append('action', 'approvalReceived');
            formData.append('reqId', reqId);
            formData.append('comments', comments);
            var fileInput = $('#poFile')[0];
            if (fileInput.files.length > 0) {
                formData.append('quotationFile', fileInput.files[0]);
            }

            $.ajax({
                url: contextPath + '/views/replacement/request',
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                dataType: 'json',
                success: function (resp) {
                    if (resp.success) {
                        $('#quotationSentModal').modal('hide');
                        showAppAlert("Approval received marked successfully!", "success");
                        setTimeout(function() { location.reload(); }, 10000);
                    } else {
                        showAppAlert("Failed: " + (resp.message || "Unknown error"), "danger");
                    }
                },
                error: function () {
                    showAppAlert("Network error. Please try again.", "danger");
                }
            });
        });
    }

    // STG6_PRINTER_ORDER: Book Printer Order functions moved to /js/bookPrinterOrder.js
    // The module is loaded via includes/bookPrinterOrderModal.jsp
    // Available functions: openBookOrderModal(reqId, clientName), BookPrinterOrder.applyToAllItems(), BookPrinterOrder.submit()

    // ============================================================
    // STG11_CREDIT_NOTE: Issue Credit Note Functions
    // ============================================================

    function openCreditNoteModal(reqId, clientName) {
        $('#creditNoteReqIdHidden').val(reqId);
        $('#creditNoteReqId').text('REQ-' + reqId);
        $('#creditNoteReqIdDisplay').text('REQ-' + reqId);
        $('#creditNoteClientName').text(clientName || '-');
        $('#creditNoteAgreement').text('AGR-' + reqId);
        $('#creditNoteComments').val('');
        $('#creditNoteLoading').show();
        $('#creditNoteDetails').hide();
        $('#creditNoteModal').modal('show');

        // Load credit note details via AJAX
        $.ajax({
            url: contextPath + '/views/replacement/request',
            data: {action: 'getCreditNoteDetails', id: reqId},
            dataType: 'json',
            success: function (resp) {
                $('#creditNoteLoading').hide();
                if (resp.success) {
                    populateCreditNoteDetails(resp);
                    $('#creditNoteDetails').show();
                } else {
                    showAppAlert('Failed to load details: ' + (resp.message || 'Unknown error'), 'danger');
                    $('#creditNoteModal').modal('hide');
                }
            },
            error: function () {
                $('#creditNoteLoading').hide();
                showAppAlert('Network error. Please try again.', 'danger');
                $('#creditNoteModal').modal('hide');
            }
        });
    }

    // Store credit note data
    var creditNoteData = {
        printers: [],
        locations: {},
        savedLocations: {}
    };
    var currentLocationId = null;

    function populateCreditNoteDetails(data) {
        creditNoteData.printers = data.printers || [];
        creditNoteData.locations = {};
        creditNoteData.savedLocations = {};

        var container = $('#creditNoteLocationsContainer');
        container.empty();

        if (creditNoteData.printers.length === 0) {
            container.html('<div class="alert alert-warning">No printer details found for this request.</div>');
            $('#creditNoteTotalAmount').text('₹0');
            return;
        }

        // Group printers by location
        creditNoteData.printers.forEach(function (p, idx) {
            var locKey = p.location || 'Unknown Location';
            if (!creditNoteData.locations[locKey]) {
                creditNoteData.locations[locKey] = [];
            }
            p._idx = idx;
            creditNoteData.locations[locKey].push(p);
        });

        // Build location groups
        var grandTotal = 0;
        var locIndex = 0;

        for (var locName in creditNoteData.locations) {
            var printers = creditNoteData.locations[locName];
            var locTotal = 0;

            printers.forEach(function (p) {
                locTotal += (p.agreementRate || 0);
            });
            grandTotal += locTotal;

            var html = '<div class="location-group" id="locationGroup_' + locIndex + '" data-location="' + escapeHtml(locName) + '">' +
                '<div class="location-group__header">' +
                '<div class="d-flex align-items-center">' +
                '<i class="fas fa-map-marker-alt text-primary mr-2"></i>' +
                '<strong>' + escapeHtml(locName) + '</strong>' +
                '<span class="badge badge-info ml-2">' + printers.length + ' Printer' + (printers.length > 1 ? 's' : '') + '</span>' +
                '<span class="saved-indicator ml-2" id="savedIndicator_' + locIndex + '" style="display:none;">' +
                '<i class="fas fa-check text-success"></i> Saved' +
                '</span>' +
                '</div>' +
                '<button type="button" class="btn btn-primary btn-sm" onclick="openLocationCreditNote(\'' + locIndex + '\', \'' + escapeHtml(locName).replace(/'/g, "\\'") + '\')">' +
                '<i class="fas fa-edit"></i> Add Credit Note' +
                '</button>' +
                '</div>' +
                '<div class="location-group__body">';

            printers.forEach(function (p) {
                html += '<div class="printer-item">' +
                    '<div>' +
                    '<strong>' + escapeHtml(p.modelName || '-') + '</strong>' +
                    '<div class="text-sm text-muted">SN: ' + escapeHtml(p.serialNo || 'N/A') + ' | Issue: ' + escapeHtml(p.issueDescription || 'Replacement') + '</div>' +
                    '</div>' +
                    '<span class="badge badge-danger">₹' + (p.agreementRate || 0).toLocaleString('en-IN') + '</span>' +
                    '</div>';
            });

            html += '</div></div>';
            container.append(html);
            locIndex++;
        }

        $('#creditNoteTotalAmount').text('₹' + grandTotal.toLocaleString('en-IN'));
    }

    function openLocationCreditNote(locIndex, locName) {
        currentLocationId = locIndex;
        $('#locationCreditNoteLocId').val(locIndex);
        $('#locationCreditNoteTitle').text(locName);
        $('#locationCreditNoteLocation').text(locName);
        $('#locationCreditNoteComments').val(creditNoteData.savedLocations[locIndex]?.comments || '');

        // Populate printers for this location
        var printers = creditNoteData.locations[locName] || [];
        var tbody = $('#locationPrintersTable');
        tbody.empty();

        var total = 0;
        printers.forEach(function (p, idx) {
            var rate = creditNoteData.savedLocations[locIndex]?.amounts?.[idx] ?? (p.agreementRate || 0);
            total += rate;

            var row = '<tr>' +
                '<td><strong>' + escapeHtml(p.modelName || '-') + '</strong>' +
                (p.serialNo ? '<br><small class="text-muted">' + escapeHtml(p.serialNo) + '</small>' : '') + '</td>' +
                '<td>' + (p.issueDescription ? '<span class="badge badge-warning">' + escapeHtml(p.issueDescription) + '</span>' : '-') + '</td>' +
                '<td>₹' + (p.agreementRate || 0).toLocaleString('en-IN') + '</td>' +
                '<td><input type="number" class="form-control form-control-sm location-credit-amount" ' +
                'data-idx="' + idx + '" value="' + rate + '" onchange="updateLocationTotal()"></td>' +
                '</tr>';
            tbody.append(row);
        });

        $('#locationTotalAmount').text('₹' + total.toLocaleString('en-IN'));
        $('#locationCreditNoteModal').modal('show');
    }

    function updateLocationTotal() {
        var total = 0;
        $('.location-credit-amount').each(function () {
            total += parseInt($(this).val()) || 0;
        });
        $('#locationTotalAmount').text('₹' + total.toLocaleString('en-IN'));
    }

    function saveLocationCreditNote() {
        var locId = currentLocationId;
        var comments = $('#locationCreditNoteComments').val();
        var amounts = [];
        var total = 0;

        $('.location-credit-amount').each(function () {
            var amt = parseInt($(this).val()) || 0;
            amounts.push(amt);
            total += amt;
        });

        creditNoteData.savedLocations[locId] = {
            comments: comments,
            amounts: amounts,
            total: total
        };

        // Mark as saved in UI
        $('#locationGroup_' + locId).addClass('saved');
        $('#savedIndicator_' + locId).show();

        // Update grand total
        var grandTotal = 0;
        for (var id in creditNoteData.savedLocations) {
            grandTotal += creditNoteData.savedLocations[id].total;
        }
        // Add unsaved locations
        for (var locName in creditNoteData.locations) {
            var printers = creditNoteData.locations[locName];
            var locIndex = $('#creditNoteLocationsContainer .location-group[data-location="' + locName + '"]').attr('id')?.replace('locationGroup_', '');
            if (locIndex && !creditNoteData.savedLocations[locIndex]) {
                printers.forEach(function (p) {
                    grandTotal += (p.agreementRate || 0);
                });
            }
        }
        $('#creditNoteTotalAmount').text('₹' + grandTotal.toLocaleString('en-IN'));

        $('#locationCreditNoteModal').modal('hide');
        showAppAlert('Credit note details saved for ' + $('#locationCreditNoteLocation').text(), 'success');
    }

    function closeLocationCreditNoteModal() {
        $('#locationCreditNoteModal').modal('hide');
    }

    function toggleCreditNoteForwardSection() {
        var section = $('#creditNoteForwardSection');
        if (section.is(':visible')) {
            section.hide();
        } else {
            section.show();
            section[0].scrollIntoView({behavior: 'smooth'});
        }
    }

    function submitCreditNoteForward() {
        var manager = $('#creditNoteManagerSelect').val();
        var comment = $('#creditNoteForwardComment').val().trim();

        if (!manager) {
            showAppAlert('Please select a manager.', 'warning');
            return;
        }
        if (!comment) {
            showAppAlert('Please enter a comment.', 'warning');
            return;
        }

        var reqId = $('#creditNoteReqIdHidden').val();
        var managerName = $('#creditNoteManagerSelect option:selected').text();

        $.post(
            contextPath + '/views/replacement/request',
            {
                action: 'forwardCreditNote',
                reqId: reqId,
                manager: manager,
                comments: comment
            },
            function (resp) {
                if (resp.success) {
                    $('#creditNoteModal').modal('hide');
                    showAppAlert('Request forwarded to ' + managerName, 'success');
                    setTimeout(function() { location.reload(); }, 10000);
                } else {
                    showAppAlert('Failed: ' + (resp.message || 'Unknown error'), 'danger');
                }
            },
            'json'
        ).fail(function () {
            showAppAlert('Network error. Please try again.', 'danger');
        });
    }

    function submitCreditNoteToBilling() {
        var savedCount = Object.keys(creditNoteData.savedLocations).length;

        if (savedCount === 0) {
            showAppAlert('Please add credit note details for at least one location.', 'warning');
            return;
        }

        showAppConfirm('Forward credit note to Billing team?', function() {
            var reqId = $('#creditNoteReqIdHidden').val();

            $.post(
                contextPath + '/views/replacement/request',
                {
                    action: 'submitCreditNote',
                    reqId: reqId,
                    locationData: JSON.stringify(creditNoteData.savedLocations)
                },
                function (resp) {
                    if (resp.success) {
                        $('#creditNoteModal').modal('hide');
                        showAppAlert('Credit note forwarded to Billing team successfully!', 'success');
                        setTimeout(function() { location.reload(); }, 10000);
                    } else {
                        showAppAlert('Failed: ' + (resp.message || 'Unknown error'), 'danger');
                    }
                },
                'json'
            ).fail(function () {
                showAppAlert('Network error. Please try again.', 'danger');
            });
        });
    }

    function submitCreditNoteNoAction() {
        $('#creditNoteNoActionReason').val('');
        $('#creditNoteNoActionModal').modal('show');
    }

    function confirmCreditNoteNoAction() {
        var reqId = $('#creditNoteReqIdHidden').val();
        var reason = $('#creditNoteNoActionReason').val().trim();

        if (!reason) {
            showAppAlert('Please provide a reason for no action.', 'warning');
            return;
        }

        $.post(
            contextPath + '/views/replacement/request',
            {
                action: 'creditNoteNoAction',
                reqId: reqId,
                reason: reason
            },
            function (resp) {
                if (resp.success) {
                    $('#creditNoteNoActionModal').modal('hide');
                    $('#creditNoteModal').modal('hide');
                    showAppAlert('Request marked as "No Action Required".', 'success');
                    setTimeout(function() { location.reload(); }, 10000);
                } else {
                    showAppAlert('Failed: ' + (resp.message || 'Unknown error'), 'danger');
                }
            },
            'json'
        ).fail(function () {
            showAppAlert('Network error. Please try again.', 'danger');
        });
    }

    $(document).ready(function () {
        var dtOptions = {
            "order": [[3, "desc"]],
            "pageLength": 25,
            "autoWidth": false,
            "scrollX": false,
            "language": {
                "emptyTable": "No requests found",
                "search": "Search:"
            },
            "columnDefs": [
                {"orderable": false, "targets": -1}
            ]
        };

        var tables = {};
        var tableIds = ['openRequestsTable', 'completedRequestsTable', 'rejectedRequestsTable', 'cancelledRequestsTable'];
        tableIds.forEach(function(id) {
            var $tbl = $('#' + id);
            if ($.fn.DataTable && $tbl.length && $tbl.find('tbody tr').length > 0) {
                tables[id] = $tbl.DataTable(dtOptions);
            }
        });

        // Check if viewId parameter is present in URL to auto-open view modal
        var urlParams = new URLSearchParams(window.location.search);
        var viewId = urlParams.get('viewId');
        if (viewId) {
            openViewModal(viewId, '');
        }

        var statusTabMap = {
            'PENDING': 'open-tab',
            'OPEN': 'open-tab',
            'COMPLETED': 'completed-tab',
            'REJECTED': 'rejected-tab',
            'CANCELLED': 'cancelled-tab'
        };

        function applyFilters() {
            var status = $('#filterStatus').val();
            var stage = $('#filterStage').val();
            var dateFrom = $('#filterDateFrom').val();
            var dateTo = $('#filterDateTo').val();

            if (status && statusTabMap[status]) {
                $('#' + statusTabMap[status]).tab('show');
            }

            $.fn.dataTable.ext.search.length = 0;

            if (stage || dateFrom || dateTo) {
                $.fn.dataTable.ext.search.push(function(settings, data, dataIndex) {
                    var $row = $(settings.aoData[dataIndex].nTr);
                    var rowStage = String($row.data('stage') || '');
                    var rowDate = String($row.data('date') || '');

                    if (stage && rowStage !== stage) return false;
                    if (dateFrom && rowDate < dateFrom) return false;
                    if (dateTo && rowDate > dateTo) return false;
                    return true;
                });
            }

            tableIds.forEach(function(id) {
                if (tables[id]) {
                    tables[id].draw();
                }
            });
        }

        $('#btnApplyFilters').on('click', function () {
            applyFilters();
        });

        $('#btnClearFilters').on('click', function () {
            $('#filterStatus').val('');
            $('#filterStage').val('');
            $('#filterDateFrom').val('');
            $('#filterDateTo').val('');
            $.fn.dataTable.ext.search.length = 0;
            tableIds.forEach(function(id) {
                if (tables[id]) {
                    tables[id].search('').columns().search('').draw();
                }
            });
            $('#open-tab').tab('show');
        });

        $('.filter-section input, .filter-section select').on('keypress', function (e) {
            if (e.which === 13) {
                applyFilters();
            }
        });
    });
</script>

<c:if test="${!isEmbedded}">
        </div><%-- /.page-content --%>
    </div><%-- /.main-content-inner --%>
    <%@ include file="common/footer.jsp" %>
</c:if>
