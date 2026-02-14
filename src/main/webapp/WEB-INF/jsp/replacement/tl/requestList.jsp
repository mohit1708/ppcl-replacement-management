<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="pageTitle" value="Service TL Reviews" scope="request"/>
<%@ include file="../common/header.jsp" %>
<%@ include file="../common/sidebar.jsp" %>

<style>
        .page-header .breadcrumb {
            background: transparent;
            padding: 0;
            margin-bottom: 10px;
        }

        .filter-bar {
            display: flex;
            flex-wrap: wrap;
            gap: 15px;
            align-items: flex-end;
        }

        .filter-bar .form-group {
            margin-bottom: 0;
            min-width: 150px;
        }

        .badge-count {
            font-size: 0.9rem;
            padding: 0.5em 0.8em;
        }

        .printer-link {
            color: #007bff;
            cursor: pointer;
        }

        .printer-link:hover {
            text-decoration: underline;
        }

        .commercial-grid {
            display: grid;
            grid-template-columns: repeat(6, 1fr);
            gap: 10px;
            text-align: center;
        }

        .month-box {
            padding: 15px;
            background: #f8f9fa;
            border-radius: 8px;
            border: 1px solid #dee2e6;
        }

        .printer-card {
            border: 1px solid #dee2e6;
            border-radius: 8px;
            margin-bottom: 15px;
        }

        .printer-card .card-header {
            background: #f8f9fa;
            padding: 12px 15px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .history-item {
            padding: 12px;
            border-left: 3px solid #007bff;
            background: #f8f9fa;
            margin-bottom: 8px;
            border-radius: 0 8px 8px 0;
        }

        .modal-xl {
            max-width: 1100px;
        }

        .dataTable:not(.collapsed) thead > tr > th:first-child {
            display: table-cell;
        }
</style>

<div class="main-content-inner">
    <div class="page-content">
    <!-- Page Header -->
        <div class="page-header mb-4">
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb">
                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/views/replacement/dashboard">Home</a>
                    </li>
                    <li class="breadcrumb-item">Service TL</li>
                    <li class="breadcrumb-item active">Pending Reviews</li>
                </ol>
            </nav>
            <h4 class="text-primary-d2">
                <i class="fas fa-clipboard-check text-dark-m3 mr-2"></i>
                Replacement Request Reviews
                <small class="page-info text-secondary-d2">
                    <i class="fa fa-angle-double-right text-80"></i>
                    Service TL Reviews
                </small>
            </h4>
        </div>

        <!-- Error Banner -->
        <c:if test="${hasError}">
            <div class="alert alert-danger bgc-danger-l4 border-none border-l-4 brc-danger-m1 radius-0 mb-4">
                <div class="d-flex">
                    <i class="fas fa-exclamation-triangle mr-3 text-150 text-danger-m1"></i>
                    <div>
                        <h5 class="alert-heading text-danger-d1 font-600">System Error</h5>
                        <p class="mb-0 text-danger-d2">${errorMessage}</p>
                    </div>
                </div>
                <button type="button" class="close" data-dismiss="alert">&times;</button>
            </div>
        </c:if>

        <!-- Filters Card -->
        <div class="card mb-4">
            <div class="card-body">
                <div class="filter-bar">
                    <div class="form-group mb-0">
                        <label class="small font-weight-bold mb-1">From Date</label>
                        <input type="date" class="form-control form-control-sm" id="filterDateFrom">
                    </div>
                    <div class="form-group mb-0">
                        <label class="small font-weight-bold mb-1">To Date</label>
                        <input type="date" class="form-control form-control-sm" id="filterDateTo">
                    </div>
                    <div class="form-group mb-0">
                        <label class="small font-weight-bold mb-1">Requester</label>
                        <select class="form-control form-control-sm" id="filterRequester">
                            <option value="">All Requesters</option>
                        </select>
                    </div>
                    <div class="form-group mb-0">
                        <label class="small font-weight-bold mb-1">Status</label>
                        <select class="form-control form-control-sm" id="filterStatus">
                            <option value="">All Status</option>
                            <option value="OPEN">Pending (Open)</option>
                            <option value="REJECTED">Rejected</option>
                            <option value="CANCELLED">Cancelled</option>
                            <option value="CLOSED">Closed</option>
                        </select>
                    </div>
                    <div class="form-group mb-0">
                        <label class="small font-weight-bold mb-1">Table</label>
                        <select class="form-control form-control-sm" id="filterTable">
                            <option value="">All Tables</option>
                            <option value="pending">Pending Requests</option>
                            <option value="completed">Completed Reviews</option>
                        </select>
                    </div>
                    <div class="form-group d-flex align-items-end mb-0">
                        <button class="btn btn-primary btn-sm px-3 mr-2" id="btnApplyFilters">
                            <i class="fas fa-filter mr-1"></i> Apply
                        </button>
                        <button class="btn btn-outline-secondary btn-sm px-3" id="btnClearFilters">
                            <i class="fas fa-times mr-1"></i> Clear
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <!-- Requests Table Card -->
        <div class="card bcard shadow-sm">
            <div class="card-header bgc-white py-3 pl-3 d-flex justify-content-between align-items-center">
                <h5 class="card-title text-120 text-primary-d2 mb-0">
                    <i class="fas fa-clock text-warning-d1 mr-1"></i> Pending Replacement Requests
                </h5>
                <div>
                    <button class="btn btn-success btn-sm px-3 mr-2" onclick="exportToCSV()">
                        <i class="fas fa-file-csv mr-1"></i> Export CSV
                    </button>
                    <button class="btn btn-danger btn-sm px-3 mr-2" onclick="exportToPDF()">
                        <i class="fas fa-file-pdf mr-1"></i> Export PDF
                    </button>
                    <span class="badge badge-lg bgc-warning-l2 text-warning-d2 border-1 brc-warning-m3 radius-1 px-3">
                        <i class="fas fa-info-circle mr-1"></i> ${pendingCount} pending reviews
                    </span>
                </div>
            </div>
            <div class="card-body p-0">
                <c:choose>
                    <c:when test="${hasError}">
                        <div class="text-center py-5">
                            <i class="fas fa-database fa-4x text-muted mb-3"></i>
                            <h5 class="text-muted">Unable to load data</h5>
                            <button class="btn btn-primary" onclick="location.reload()">
                                <i class="fas fa-sync-alt"></i> Retry
                            </button>
                        </div>
                    </c:when>
                    <c:when test="${empty requests}">
                        <div class="text-center py-5">
                            <i class="fas fa-inbox fa-4x text-muted mb-3"></i>
                            <h5 class="text-muted">No pending requests</h5>
                            <p class="text-muted">All caught up! Check back later.</p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="table-responsive border-1 brc-grey-l2 radius-1">
                            <table class="table table-hover table-striped mb-0" id="requestsTable">
                                <thead class="bgc-grey-l4 text-grey-d2">
                                <tr>
                                    <th class="border-0">Req ID</th>
                                    <th class="border-0">Client Name</th>
                                    <th class="border-0">Requester</th>
                                    <th class="border-0">Printer History</th>
                                    <th class="border-0">Requested Date</th>
                                    <th class="border-0">Actions</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach items="${requests}" var="req">
                                    <tr data-requester="${req.requesterName}" data-status="${req.status}"
                                        data-date="<fmt:formatDate value="${req.createdAt}" pattern="yyyy-MM-dd" />">
                                        <td><strong>REQ-${req.id}</strong></td>
                                        <td>${fn:escapeXml(req.clientName)}</td>
                                        <td>${fn:escapeXml(req.requesterName)}</td>
                                        <td>
                                            <a href="javascript:void(0)"
                                               class="printer-link"
                                               onclick="showPrintersModal(${req.id}, '${fn:escapeXml(req.clientName)}')">
                                                <i class="fas fa-print"></i>
                                                    ${req.printerCount} Printer${req.printerCount > 1 ? 's' : ''}
                                                <i class="fas fa-eye ml-1"></i>
                                            </a>
                                        </td>
                                        <td>
                                            <fmt:formatDate value="${req.createdAt}" pattern="dd-MMM-yyyy"/><br>
                                            <small class="text-muted">
                                                <fmt:formatDate value="${req.createdAt}" pattern="HH:mm"/>
                                            </small>
                                        </td>
                                        <td class="text-nowrap">
                                            <button class="btn btn-outline-primary btn-sm px-2" style="min-width:90px;"
                                                    onclick="openViewModal(${req.id})" title="View Details">
                                                <i class="fas fa-eye"></i> View
                                            </button>
                                            <button class="btn btn-primary btn-sm px-2 ml-1" style="min-width:90px;"
                                                    onclick="openActionModal(${req.id}, '${fn:escapeXml(req.clientName)}')">
                                                <i class="fas fa-edit"></i> Recommend
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

        <!-- ============================================================ -->
        <!-- COMPLETED REQUESTS TABLE (TL has provided recommendation) -->
        <div class="card mt-4 bcard shadow-sm">
            <div class="card-header bgc-white py-3 pl-3 d-flex justify-content-between align-items-center">
                <h5 class="card-title text-120 text-primary-d2 mb-0">
                    <i class="fas fa-check-circle text-success-m1 mr-1"></i> Completed Reviews
                </h5>
                <span class="badge badge-lg bgc-success-l2 text-success-d2 border-1 brc-success-m3 radius-1 px-3">
                    <i class="fas fa-check-circle mr-1"></i> ${completedCount} reviewed
                </span>
            </div>
            <div class="card-body p-0">
                <c:choose>
                    <c:when test="${empty completedRequests}">
                        <div class="text-center py-4">
                            <i class="fas fa-clipboard-check fa-3x text-muted mb-3"></i>
                            <h6 class="text-muted">No completed reviews yet</h6>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="table-responsive border-1 brc-grey-l2 radius-1">
                            <table class="table table-hover table-striped mb-0" id="completedRequestsTable">
                                <thead class="bgc-grey-l4 text-grey-d2">
                                <tr>
                                    <th class="border-0">Req ID</th>
                                    <th class="border-0">Client Name</th>
                                    <th class="border-0">Requester</th>
                                    <th class="border-0">Current Stage</th>
                                    <th class="border-0">Stage Owner</th>
                                    <th class="border-0">Printers</th>
                                    <th class="border-0">Actions</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach items="${completedRequests}" var="req">
                                    <tr data-requester="${req.requesterName}" data-status="${req.status}"
                                        data-date="<fmt:formatDate value="${req.createdAt}" pattern="yyyy-MM-dd" />">
                                        <td><strong>REQ-${req.id}</strong></td>
                                        <td>${fn:escapeXml(req.clientName)}</td>
                                        <td>${fn:escapeXml(req.requesterName)}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${req.status == 'REJECTED' || req.status == 'CANCELLED'}">
                                                    <span class="badge badge-danger">${fn:escapeXml(req.status)}</span>
                                                </c:when>
                                                <c:when test="${req.status == 'CLOSED'}">
                                                    <span class="badge badge-secondary">${fn:escapeXml(req.status)}</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge badge-info">${fn:escapeXml(req.currentStageName)}</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <i class="fas fa-user-tie text-primary"></i>
                                                ${fn:escapeXml(req.stageOwnerName)}
                                        </td>
                                        <td>
                                            <a href="javascript:void(0)"
                                               class="printer-link"
                                               onclick="showPrintersModal(${req.id}, '${fn:escapeXml(req.clientName)}')">
                                                <i class="fas fa-print"></i>
                                                    ${req.printerCount} Printer${req.printerCount > 1 ? 's' : ''}
                                            </a>
                                        </td>
                                        <td class="text-nowrap">
                                            <button class="btn btn-outline-primary btn-sm px-2"
                                                    onclick="openViewModal(${req.id})"
                                                    title="View Request">
                                                <i class="fas fa-eye"></i> View
                                            </button>
                                            <c:if test="${req.currentStage == 'STG3_AM_MANAGER_REVIEW' && req.status != 'CLOSED' && req.status != 'CANCELLED' && req.status != 'REJECTED'}">
                                                <button class="btn btn-outline-warning btn-sm px-2 ml-1"
                                                        onclick="sendReminder(${req.id}, '${fn:escapeXml(req.stageOwnerName)}', '${fn:escapeXml(req.currentStageName)}')"
                                                        title="Send Reminder">
                                                    <i class="fas fa-bell"></i> Remind
                                                </button>
                                            </c:if>
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

    </div><%-- /.page-content --%>
</div><%-- /.main-content-inner --%>

<%@ include file="../common/footer.jsp" %>

<!-- ============================================================ -->
<!-- VIEW REQUEST MODAL (Read-Only) -->
<!-- ============================================================ -->
<div class="modal fade" id="viewRequestModal" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <div class="modal-content border-0 radius-1 shadow">
            <div class="modal-header py-2 bgc-primary-d1 border-0 radius-t-1">
                <h5 class="modal-title text-white text-110 pl-2 font-bolder">
                    <i class="fas fa-eye mr-1"></i> View Request - <span id="viewModalReqId"></span>
                </h5>
                <button type="button" class="position-tr btn btn-xs btn-outline-white btn-h-yellow btn-a-yellow mt-1px mr-1px btn-brc-tp" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true" class="text-150">&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <div id="viewModalLoading" class="text-center py-4">
                    <i class="fas fa-spinner fa-spin fa-2x"></i>
                    <p>Loading request details...</p>
                </div>
                <div id="viewRequestContent" style="display:none;"></div>

                <!-- Comment History Section -->
                <div id="commentHistoryWrapper" style="display:none;" class="mt-3">
                    <button type="button" class="btn btn-info btn-sm" onclick="toggleCommentHistory()">
                        <i class="fas fa-history"></i> View Comment History
                    </button>
                    <div id="commentHistorySection" style="display:none;" class="mt-3">
                        <h6 class="font-weight-bold"><i class="fas fa-comments"></i> Comment History</h6>
                        <div id="commentHistoryLoading" class="text-center py-2" style="display:none;">
                            <i class="fas fa-spinner fa-spin"></i> Loading...
                        </div>
                        <div class="table-responsive">
                            <table class="table table-sm table-bordered" id="commentHistoryTable" style="display:none;">
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
            <div class="modal-footer bgc-grey-l4">
                <button type="button" class="btn btn-secondary px-4 radius-1" data-dismiss="modal">Close</button>
            </div>
        </div>
    </div>
</div>
<input type="hidden" id="viewReqIdHidden" value="">

<!-- ============================================================ -->
<!-- REMINDER MODAL -->
<!-- ============================================================ -->
<div class="modal fade" id="reminderModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content border-0 radius-1 shadow">
            <div class="modal-header py-2 bgc-primary-d1 border-0 radius-t-1">
                <h5 class="modal-title text-white text-110 pl-2 font-bolder">
                    <i class="fas fa-bell mr-1"></i> Send Reminder
                </h5>
                <button type="button" class="position-tr btn btn-xs btn-outline-white btn-h-yellow btn-a-yellow mt-1px mr-1px btn-brc-tp" data-dismiss="modal" aria-label="Close">
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
            <div class="modal-footer bgc-grey-l4">
                <button type="button" class="btn btn-secondary px-4 radius-1" data-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-warning px-4 radius-1" onclick="confirmSendReminder()">
                    <i class="fas fa-bell"></i> Send Reminder
                </button>
            </div>
        </div>
    </div>
</div>

<!-- ============================================================ -->
<!-- PRINTERS DETAIL MODAL -->
<!-- ============================================================ -->
<div class="modal fade" id="printersDetailModal" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <div class="modal-content border-0 radius-1 shadow">
            <div class="modal-header py-2 bgc-primary-d1 border-0 radius-t-1">
                <h5 class="modal-title text-white text-110 pl-2 font-bolder">
                    <i class="fas fa-print mr-1"></i> Printers Detail
                </h5>
                <button type="button" class="position-tr btn btn-xs btn-outline-white btn-h-yellow btn-a-yellow mt-1px mr-1px btn-brc-tp" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true" class="text-150">&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <div id="printersDetailContent">
                    <div class="text-center py-4">
                        <i class="fas fa-spinner fa-spin fa-2x"></i>
                        <p>Loading printer details...</p>
                    </div>
                </div>
            </div>
            <div class="modal-footer bgc-grey-l4">
                <button type="button" class="btn btn-secondary px-4 radius-1" data-dismiss="modal">Close</button>
            </div>
        </div>
    </div>
</div>

<!-- ============================================================ -->
<!-- PRINTER HISTORY MODAL -->
<!-- ============================================================ -->
<div class="modal fade" id="printerHistoryModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content border-0 radius-1 shadow">
            <div class="modal-header py-2 bgc-primary-d1 border-0 radius-t-1">
                <h5 class="modal-title text-white text-110 pl-2 font-bolder">
                    <i class="fas fa-history"></i> Printer History - <span id="historyPrinterName"></span>
                </h5>
                <button type="button" class="position-tr btn btn-xs btn-outline-white btn-h-yellow btn-a-yellow mt-1px mr-1px btn-brc-tp" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true" class="text-150">&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <div id="printerHistoryContent">
                    <div class="text-center py-4">
                        <i class="fas fa-spinner fa-spin fa-2x"></i>
                        <p>Loading printer history...</p>
                    </div>
                </div>
            </div>
            <div class="modal-footer bgc-grey-l4">
                <button type="button" class="btn btn-outline-secondary btn-sm radius-1" onclick="exportServiceCalls()">
                    <i class="fas fa-download"></i> Export Data
                </button>
                <button type="button" class="btn btn-secondary px-4 radius-1" data-dismiss="modal">Close</button>
            </div>
        </div>
    </div>
</div>

<!-- ============================================================ -->
<!-- TL ACTION MODAL (Provide Recommendation) -->
<!-- ============================================================ -->
<div class="modal fade" id="actionModal" tabindex="-1">
    <div class="modal-dialog modal-xl">
        <div class="modal-content border-0 radius-1 shadow">
            <div class="modal-header py-2 bgc-primary-tp1 border-0  radius-t-1">
                <h5 class="modal-title text-white-tp1 text-110 pl-2 font-bolder">
                    <i class="fas fa-edit"></i> Provide Recommendation - <span id="actionModalReqId"></span>
                </h5>
                <button type="button" class="position-tr btn btn-xs btn-outline-white btn-h-yellow btn-a-yellow mt-1px mr-1px btn-brc-tp" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true" class="text-150">&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <input type="hidden" id="actionReqId">

                <div id="actionModalLoading" class="text-center py-4">
                    <i class="fas fa-spinner fa-spin fa-2x"></i>
                    <p>Loading request details...</p>
                </div>

                <div id="actionModalContent" style="display:none;">
                    <!-- Info Alert -->
                    <div class="alert alert-info">
                        <i class="fas fa-info-circle"></i>
                        Provide printer model recommendations for each printer in this request.
                        You can recommend either new or refurbished printers.
                    </div>

                    <!-- Request Info -->
                    <div class="row mb-4">
                        <div class="col-md-4">
                            <strong>Requester:</strong> <span id="actionRequester">-</span>
                        </div>
                        <div class="col-md-4">
                            <strong>Account Manager:</strong> <span id="actionAM">-</span>
                        </div>
                        <div class="col-md-4">
                            <strong>Reason:</strong> <span id="actionReason">-</span>
                        </div>
                    </div>

                    <!-- Printers Section -->
                    <div id="actionPrintersSection"></div>

                    <!-- Overall Comments -->
                    <div class="form-group mt-4">
                        <label class="font-weight-bold">Overall Comments (Optional)</label>
                        <textarea class="form-control" id="actionComments" rows="3"
                                  placeholder="Add any overall comments for this replacement request..."></textarea>
                    </div>
                </div>
            </div>
            <div class="modal-footer d-flex justify-content-between">
                <div>
                    <button type="button" class="btn btn-danger" onclick="openRejectModal()">
                        <i class="fas fa-times-circle"></i> Reject
                    </button>
                    <button type="button" class="btn btn-warning" onclick="openForwardModal()">
                        <i class="fas fa-share"></i> Forward to Higher Authority
                    </button>
                </div>
                <div>
                    <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                    <button type="button" class="btn btn-success" onclick="approveAndForward()">
                        <i class="fas fa-check"></i> Approve & Forward to AM Manager
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- ============================================================ -->
<!-- REJECT MODAL -->
<!-- ============================================================ -->
<div class="modal fade" id="rejectModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header py-2 bgc-danger-tp1 border-0  radius-t-1">
                <h5 class="modal-title text-white-tp1 text-110 pl-2 font-bolder"><i class="fas fa-times-circle"></i> Reject Replacement Request</h5>
                <button type="button" class="position-tr btn btn-xs btn-outline-white btn-h-yellow btn-a-yellow mt-1px mr-1px btn-brc-tp" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true" class="text-150">&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <div class="alert alert-warning">
                    <i class="fas fa-exclamation-triangle"></i>
                    <strong>Warning:</strong> Rejecting this request will notify the Account Manager and Requester.
                </div>
                <div class="form-group">
                    <label class="font-weight-bold">Rejection Reason <span class="text-danger">*</span></label>
                    <select class="form-control mb-2" id="rejectReasonSelect">
                        <option value="">Select Reason</option>
                        <option value="insufficient">Insufficient justification</option>
                        <option value="noteligible">Printer not eligible for replacement</option>
                        <option value="duplicate">Duplicate request</option>
                        <option value="other">Other</option>
                    </select>
                </div>
                <div class="form-group">
                    <label class="font-weight-bold">Comments <span class="text-danger">*</span></label>
                    <textarea class="form-control" id="rejectComments" rows="3" required
                              placeholder="Provide detailed rejection reason..."></textarea>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-danger" onclick="confirmReject()">
                    <i class="fas fa-times-circle"></i> Confirm Rejection
                </button>
            </div>
        </div>
    </div>
</div>

<!-- ============================================================ -->
<!-- FORWARD MODAL -->
<!-- ============================================================ -->
<div class="modal fade" id="forwardModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header py-2 bgc-warning-tp1 border-0  radius-t-1">
                <h5 class="modal-title text-white-tp1 text-110 pl-2 font-bolder"><i class="fas fa-share"></i> Forward to Higher Authority for Approval</h5>
                <button type="button" class="position-tr btn btn-xs btn-outline-white btn-h-yellow btn-a-yellow mt-1px mr-1px btn-brc-tp" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true" class="text-150">&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <div class="form-group">
                    <label class="font-weight-bold">Select Authority <span class="text-danger">*</span></label>
                    <select class="form-control" id="forwardTo" required>
                        <option value="">Select Person</option>
                        <c:forEach items="${reportingHierarchy}" var="manager">
                            <option value="${manager.id}">${fn:escapeXml(manager.name)}
                                (${fn:escapeXml(manager.role)})
                            </option>
                        </c:forEach>
                        <c:if test="${empty reportingHierarchy}">
                            <option value="" disabled>No managers found in hierarchy</option>
                        </c:if>
                    </select>
                </div>
                <div class="form-group">
                    <label class="font-weight-bold">Comments <span class="text-danger">*</span></label>
                    <textarea class="form-control" id="forwardComments" rows="3" required maxlength="128"
                              placeholder="Explain why you're forwarding this request..."></textarea>
                    <small class="form-text text-muted">Max 128 characters</small>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-warning" onclick="confirmForward()">
                    <i class="fas fa-share"></i> Forward Request
                </button>
            </div>
        </div>
    </div>
</div>

<script>
    var contextPath = '${pageContext.request.contextPath}';
    var currentAgrProdId = null;
    var allPrinterModels = []; // Global array to store printer models

    $(document).ready(function () {
        <c:if test="${!hasError && !empty requests}">
        $('#requestsTable').DataTable({
            "order": [[4, "desc"]],
            "pageLength": 25,
            "columnDefs": [
                {"orderable": false, "targets": [3, 5]}
            ]
        });

        $('#completedRequestsTable').DataTable({
            "order": [[0, "desc"]],
            "pageLength": 10,
            "columnDefs": [
                {"orderable": false, "targets": [5, 6]}
            ]
        });
        </c:if>

        // Load filter data
        loadFilterData();
    });

    function loadFilterData() {
        // Populate Requesters dropdown from existing table rows
        var requesters = {};
        $('#requestsTable tbody tr').each(function () {
            var reqName = $(this).data('requester');
            if (reqName && !requesters[reqName]) {
                requesters[reqName] = true;
            }
        });
        var reqSelect = $('#filterRequester');
        Object.keys(requesters).sort().forEach(function (name) {
            reqSelect.append('<option value="' + escapeHtml(name) + '">' + escapeHtml(name) + '</option>');
        });

        // Load printer models for Action Modal dropdowns
        $.get(contextPath + '/views/replacement/tl/getFilterData', function (resp) {
            if (resp.success && resp.data) {
                allPrinterModels = resp.data.printerModels || [];
            }
        }, 'json');
    }

    // Build printer model dropdown options HTML
    function buildPrinterModelOptions(selectedModelId, selectedModelText) {
        var options = '<option value="">-- Select Model --</option>';
        allPrinterModels.forEach(function (m) {
            var selected = (m.id == selectedModelId) ? ' selected' : '';
            options += '<option value="' + m.id + '"' + selected + '>' + escapeHtml(m.modelName) + '</option>';
        });
        return options;
    }

    // ============================================================
    // PRINTERS DETAIL MODAL
    // ============================================================
    function showPrintersModal(reqId, clientName) {
        $('#printersModalReqId').text('REQ-' + reqId + ' (' + clientName + ')');
        $('#printersDetailContent').html('<div class="text-center py-4"><i class="fas fa-spinner fa-spin fa-2x"></i><p>Loading...</p></div>');
        $('#printersDetailModal').modal('show');

        $.get(contextPath + '/views/replacement/tl/getPrinterDetails', {reqId: reqId}, function (resp) {
            if (resp.success && resp.data) {
                var printers = resp.data.printers || [];
                var html = '<table class="table table-bordered table-sm">';
                html += '<thead class="thead-light"><tr>';
                html += '<th>Existing Printer</th><th>Serial No.</th><th>New Printer Model</th>';
                html += '<th>City / Location</th><th>Comments</th><th>Printer History</th>';
                html += '</tr></thead><tbody>';

                printers.forEach(function (p) {
                    html += '<tr>';
                    html += '<td><strong>' + escapeHtml(p.existingModel || '-') + '</strong></td>';
                    html += '<td><code>' + escapeHtml(p.serial || '-') + '</code></td>';
                    html += '<td>' + escapeHtml(p.newModelName || p.newModelText || '-') + '</td>';
                    html += '<td>' + escapeHtml(p.location || '') + ', ' + escapeHtml(p.city || '') + '</td>';
                    html += '<td>' + escapeHtml(p.comments || '-') + '</td>';
                    html += '<td><button class="btn btn-outline-secondary btn-sm" onclick="showPrinterHistory(\'' + escapeHtml(p.serial || '') + '\', \'' + escapeHtml(p.existingModel || '') + '\')">';
                    html += '<i class="fas fa-history"></i> View History</button></td>';
                    html += '</tr>';
                });

                html += '</tbody></table>';
                $('#printersDetailContent').html(html);
            } else {
                $('#printersDetailContent').html('<div class="alert alert-danger">Failed to load printer details</div>');
            }
        }, 'json');
    }

    // ============================================================
    // PRINTER HISTORY MODAL
    // ============================================================
    var currentHistoryData = [];
    var currentHistorySerial = '';

    function showPrinterHistory(serial, modelName) {
        currentHistorySerial = serial;
        currentHistoryData = [];
        $('#historyPrinterName').text(modelName + ' (' + serial + ')');
        $('#printerHistoryContent').html('<div class="text-center py-4"><i class="fas fa-spinner fa-spin fa-2x"></i><p>Loading history...</p></div>');
        $('#printerHistoryModal').css('z-index', 1060).modal('show');
        $('.modal-backdrop').last().css('z-index', 1055);

        $.get(contextPath + '/views/replacement/tl/getPrinterHistory', {serial: serial}, function (resp) {
            if (resp.success && resp.data) {
                var data = resp.data;
                var serviceCalls = data.serviceCalls || [];
                currentHistoryData = serviceCalls;

                var html = '';

                // Service Calls
                html += '<div>';
                html += '<h6><i class="fas fa-wrench text-primary"></i> Service Calls (Last 6 Months)</h6>';
                if (serviceCalls.length > 0) {
                    html += '<table class="table table-sm table-bordered">';
                    html += '<thead class="thead-light"><tr><th>Call ID</th><th>Date</th><th>Serial</th><th>Details</th><th>Status</th></tr></thead>';
                    html += '<tbody>';
                    serviceCalls.forEach(function (sc) {
                        html += '<tr>';
                        html += '<td>' + escapeHtml(sc.callId || '-') + '</td>';
                        html += '<td>' + escapeHtml(sc.callDate || '-') + '</td>';
                        html += '<td><code>' + escapeHtml(sc.serial || '-') + '</code></td>';
                        html += '<td>' + escapeHtml(sc.callDetails || '-') + '</td>';
                        html += '<td><span class="badge badge-info">' + escapeHtml(sc.callStatusName || '-') + '</span></td>';
                        html += '</tr>';
                    });
                    html += '</tbody></table>';
                } else {
                    html += '<p class="text-muted">No service calls in the last 6 months</p>';
                }
                html += '<div class="small text-muted">Total: ' + (data.serviceCallsCount || 0) + ' service calls in last 6 months</div>';
                html += '</div>';

                $('#printerHistoryContent').html(html);
            } else {
                $('#printerHistoryContent').html('<div class="alert alert-danger">Failed to load printer history</div>');
            }
        }, 'json');
    }

    // ============================================================
    // ACTION MODAL (Provide Recommendation)
    // ============================================================
    function openActionModal(reqId, clientName) {
        $('#actionReqId').val(reqId);
        $('#actionModalReqId').text('REQ-' + reqId + ' (' + clientName + ')');
        $('#actionModalLoading').show();
        $('#actionModalContent').hide();
        $('#actionModal').modal('show');

        $.get(contextPath + '/views/replacement/tl/getPrinterDetails', {reqId: reqId}, function (resp) {
            if (resp.success && resp.data) {
                var req = resp.data.request || {};
                var printers = resp.data.printers || [];

                $('#actionRequester').text(req.requester || '-');
                $('#actionAM').text(req.accountManager || '-');
                $('#actionReason').text(req.reasonName || '-');

                // Build printers section
                var html = '';
                printers.forEach(function (p, idx) {
                    html += '<div class="printer-card">';
                    html += '<div class="card-header">';
                    html += '<span><span class="badge badge-primary mr-2">Printer ' + (idx + 1) + '</span>';
                    html += escapeHtml(p.existingModel || '') + ' → <span class="text-primary">' + escapeHtml(p.newModelName || p.newModelText || 'TBD') + '</span></span>';
                    html += '<div>';
                    // Add "Copy to All" button only on first printer if there are multiple printers
                    if (idx === 0 && printers.length > 1) {
                        html += '<button class="btn btn-outline-primary btn-sm mr-2" onclick="copyFirstToAll()">';
                        html += '<i class="fas fa-copy"></i> Copy to All</button>';
                    }
                    html += '<button class="btn btn-outline-secondary btn-sm mr-2" onclick="showPrinterHistory(\'' + escapeHtml(p.serial || '') + '\', \'' + escapeHtml(p.existingModel || '') + '\')">';
                    html += '<i class="fas fa-history"></i> Printer History</button>';
                    html += '<span class="text-muted">' + escapeHtml(p.location || '') + '</span>';
                    html += '</div></div>';
                    html += '<div class="card-body p-3">';
                    html += '<div class="row">';
                    html += '<div class="col-md-4">';
                    html += '<div class="form-group mb-0"><label class="small">Recommended Printer Model <span class="text-danger">*</span></label>';
                    html += '<select class="form-control form-control-sm printer-rec-model" data-printer-id="' + p.id + '" data-new-model-id="' + (p.newModelId || '') + '">';
                    html += buildPrinterModelOptions(p.newModelId, p.newModelText);
                    html += '</select>';
                    html += '</div></div>';
                    html += '<div class="col-md-4">';
                    html += '<div class="form-group mb-0"><label class="small">Printer Type <span class="text-danger">*</span></label>';
                    html += '<select class="form-control form-control-sm printer-type" data-printer-id="' + p.id + '">';
                    html += '<option value="NEW">New</option><option value="REFURB">Refurbished</option>';
                    html += '</select></div></div>';
                    html += '<div class="col-md-4">';
                    html += '<div class="form-group mb-0"><label class="small">Comments</label>';
                    html += '<input type="text" class="form-control form-control-sm printer-comments" data-printer-id="' + p.id + '" ';
                    html += 'value="' + escapeHtml(p.comments || '') + '" placeholder="Add comments...">';
                    html += '</div></div>';
                    html += '</div></div></div>';
                });

                $('#actionPrintersSection').html(html);
                $('#actionModalLoading').hide();
                $('#actionModalContent').show();
            } else {
                alert('Failed to load request details');
                $('#actionModal').modal('hide');
            }
        }, 'json');
    }

    function copyFirstToAll() {
        var $allModels = $('#actionPrintersSection .printer-rec-model');
        var $allTypes = $('#actionPrintersSection .printer-type');
        var $allComments = $('#actionPrintersSection .printer-comments');

        if ($allModels.length === 0) {
            alert('No printers to copy from');
            return;
        }

        if ($allModels.length === 1) {
            alert('Only one printer exists, nothing to copy to');
            return;
        }

        var modelVal = $allModels.first().val();
        var typeVal = $allTypes.first().val();
        var commentsVal = $allComments.first().val();

        if (!modelVal) {
            alert('Please select a recommended printer model for the first printer before copying');
            return;
        }

        // Copy to all other printers (skip the first one)
        $allModels.each(function (index) {
            if (index > 0) {
                $(this).val(modelVal);
            }
        });
        $allTypes.each(function (index) {
            if (index > 0) {
                $(this).val(typeVal);
            }
        });
        $allComments.each(function (index) {
            if (index > 0) {
                $(this).val(commentsVal);
            }
        });

        alert('✅ Values from first printer copied to all ' + ($allModels.length - 1) + ' other printer(s)');
    }

    function approveAndForward() {
        // First save all printer recommendations
        var reqId = $('#actionReqId').val();
        var printerUpdates = [];
        var hasError = false;

        // Collect all printer recommendations
        $('.printer-rec-model').each(function () {
            var printerId = $(this).data('printer-id');
            var newModelId = $(this).val();
            var printerType = $('.printer-type[data-printer-id="' + printerId + '"]').val();
            var comments = $('.printer-comments[data-printer-id="' + printerId + '"]').val();

            if (!newModelId) {
                hasError = true;
                alert('Please select a recommended printer model for all printers');
                return false;
            }

            printerUpdates.push({
                printerId: printerId,
                newModelId: newModelId,
                printerType: printerType,
                comments: comments
            });
        });

        if (hasError) return;

        if (!confirm('Save recommendations and forward to AM Manager for commercial review?')) {
            return;
        }

        // Save each printer recommendation
        var savePromises = printerUpdates.map(function (p) {
            return $.post(contextPath + '/views/replacement/tl/updateRecommendation', {
                printerId: p.printerId,
                reqId: reqId,
                newModelId: p.newModelId,
                printerType: p.printerType,
                comments: p.comments
            });
        });

        // Wait for all updates, then approve
        $.when.apply($, savePromises).done(function () {
            var actionComments = $('#actionComments').val();

            $.post(contextPath + '/views/replacement/tl/action', {
                reqId: reqId,
                actionType: 'APPROVE',
                comments: actionComments
            }, function (resp) {
                if (resp.success) {
                    alert('✅ Recommendations saved and ' + resp.message);
                    $('#actionModal').modal('hide');
                    location.reload();
                } else {
                    alert('❌ ' + (resp.message || 'Failed'));
                }
            }, 'json');
        }).fail(function () {
            alert('❌ Failed to save recommendations');
        });
    }

    // ============================================================
    // REJECT MODAL
    // ============================================================
    function openRejectModal() {
        $('#rejectModal').modal('show');
    }

    function confirmReject() {
        var reason = $('#rejectReasonSelect').val();
        var comments = $('#rejectComments').val();

        if (!comments) {
            alert('Please provide rejection comments');
            return;
        }

        var reqId = $('#actionReqId').val();
        var fullReason = reason ? reason + ': ' + comments : comments;

        $.post(contextPath + '/views/replacement/tl/action', {
            reqId: reqId,
            actionType: 'REJECT',
            comments: fullReason
        }, function (resp) {
            if (resp.success) {
                alert('✅ ' + resp.message);
                $('#rejectModal').modal('hide');
                $('#actionModal').modal('hide');
                location.reload();
            } else {
                alert('❌ ' + (resp.message || 'Failed'));
            }
        }, 'json');
    }

    // ============================================================
    // FORWARD MODAL
    // ============================================================
    function openForwardModal() {
        $('#forwardModal').modal('show');
    }

    function confirmForward() {
        var forwardTo = $('#forwardTo').val();
        var comments = $('#forwardComments').val();

        if (!forwardTo || !comments) {
            alert('Please fill all required fields');
            return;
        }

        var reqId = $('#actionReqId').val();

        var printerUpdates = [];
        var hasError = false;

        $('.printer-rec-model').each(function () {
            var printerId = $(this).data('printer-id');
            var newModelId = $(this).val();
            var printerType = $('.printer-type[data-printer-id="' + printerId + '"]').val();
            var printerComments = $('.printer-comments[data-printer-id="' + printerId + '"]').val();

            if (!newModelId) {
                hasError = true;
                alert('Please select a recommended printer model for all printers');
                return false;
            }

            printerUpdates.push({
                printerId: printerId,
                newModelId: newModelId,
                printerType: printerType,
                comments: printerComments
            });
        });

        if (hasError) return;

        var savePromises = printerUpdates.map(function (p) {
            return $.post(contextPath + '/views/replacement/tl/updateRecommendation', {
                printerId: p.printerId,
                reqId: reqId,
                newModelId: p.newModelId,
                printerType: p.printerType,
                comments: p.comments
            });
        });

        $.when.apply($, savePromises).done(function () {
            $.post(contextPath + '/views/replacement/tl/action', {
                reqId: reqId,
                actionType: 'FORWARD',
                forwardTo: forwardTo,
                comments: comments
            }, function (resp) {
                if (resp.success) {
                    alert('✅ ' + resp.message);
                    $('#forwardModal').modal('hide');
                    $('#actionModal').modal('hide');
                    location.reload();
                } else {
                    alert('❌ ' + (resp.message || 'Failed'));
                }
            }, 'json');
        }).fail(function () {
            alert('❌ Failed to save printer recommendations');
        });
    }

    function exportServiceCalls() {
        if (currentHistoryData.length === 0) {
            alert('No service calls data to export');
            return;
        }

        var csv = [];
        var headers = ['Call ID', 'Date', 'Serial', 'Details', 'Status'];
        csv.push(headers.join(','));

        currentHistoryData.forEach(function (sc) {
            var rowData = [
                sc.callId || '',
                sc.callDate || '',
                sc.serial || '',
                sc.callDetails || '',
                sc.callStatusName || ''
            ];
            csv.push(rowData.map(function (val) {
                return '"' + (val || '').replace(/"/g, '""') + '"';
            }).join(','));
        });

        var csvContent = csv.join('\n');
        var blob = new Blob([csvContent], {type: 'text/csv;charset=utf-8;'});
        var link = document.createElement('a');
        var url = URL.createObjectURL(blob);
        link.setAttribute('href', url);
        link.setAttribute('download', 'service_calls_' + currentHistorySerial + '_' + new Date().toISOString().slice(0, 10) + '.csv');
        link.style.visibility = 'hidden';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    }

    // ============================================================
    // VIEW MODAL (for completed requests)
    // ============================================================
    var commentHistoryLoaded = false;

    function openViewModal(reqId) {
        $('#viewModalLoading').show();
        $('#viewRequestContent').hide();
        $('#commentHistoryWrapper').hide();
        $('#viewModalReqId').text('REQ-' + reqId);
        $('#viewReqIdHidden').val(reqId);
        $('#viewRequestModal').modal('show');

        // Reset comment history
        commentHistoryLoaded = false;
        $('#commentHistorySection').hide();
        $('#commentHistoryBody').empty();

        // Fetch request details using existing getPrinterDetails endpoint
        $.get(contextPath + '/views/replacement/tl/getPrinterDetails', {
            reqId: reqId
        }, function (data) {
            if (data.success) {
                populateViewModal(data.data);
            } else {
                $('#viewRequestContent').html('<div class="alert alert-danger">Failed to load request details</div>');
            }
            $('#viewModalLoading').hide();
            $('#viewRequestContent').show();
            $('#commentHistoryWrapper').show();
        }, 'json').fail(function () {
            $('#viewRequestContent').html('<div class="alert alert-danger">Network error. Please try again.</div>');
            $('#viewModalLoading').hide();
            $('#viewRequestContent').show();
        });
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

    function populateViewModal(data) {
        var req = data.request || {};
        var printers = data.printers || [];

        var html = '<div class="row mb-3">';
        html += '<div class="col-md-6"><strong>Client:</strong> ' + escapeHtml(req.clientName) + ' (' + escapeHtml(req.clientId) + ')</div>';
        html += '<div class="col-md-6"><strong>Replacement Type:</strong> ' + escapeHtml(req.replacementType) + '</div>';
        html += '</div>';
        html += '<div class="row mb-3">';
        html += '<div class="col-md-6"><strong>Requester:</strong> ' + escapeHtml(req.requester) + '</div>';
        html += '<div class="col-md-6"><strong>Current Stage:</strong> <span class="badge badge-info">' + escapeHtml(req.stageName) + '</span></div>';
        html += '</div>';
        html += '<div class="row mb-3">';
        html += '<div class="col-md-6"><strong>Reason:</strong> ' + escapeHtml(req.reasonName) + '</div>';
        html += '<div class="col-md-6"><strong>Status:</strong> ' + escapeHtml(req.status) + '</div>';
        html += '</div>';

        // Printers table
        html += '<h6 class="mt-3"><i class="fas fa-print"></i> Printers</h6>';
        html += '<div class="table-responsive"><table class="table table-sm table-bordered">';
        html += '<thead class="thead-light"><tr><th>Serial</th><th>Existing Model</th><th>Recommended Model</th><th>Location</th></tr></thead>';
        html += '<tbody>';
        printers.forEach(function (p) {
            var newModel = p.newModelName || p.newModelText || '-';
            html += '<tr>';
            html += '<td><code>' + escapeHtml(p.serial) + '</code></td>';
            html += '<td>' + escapeHtml(p.existingModel) + '</td>';
            html += '<td>' + escapeHtml(newModel) + '</td>';
            html += '<td>' + escapeHtml(p.location) + (p.city ? ', ' + escapeHtml(p.city) : '') + '</td>';
            html += '</tr>';
        });
        html += '</tbody></table></div>';

        $('#viewRequestContent').html(html);
    }

    // ============================================================
    // REMINDER FUNCTIONS (for completed requests)
    // ============================================================
    function sendReminder(reqId, ownerName, stageName) {
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
                    alert("✅ Reminder sent successfully!");
                } else {
                    alert("❌ Failed: " + (resp.message || "Unknown error"));
                }
            },
            'json'
        ).fail(function () {
            alert("❌ Network error. Please try again.");
        });
    }

    // ============================================================
    // UTILITY FUNCTIONS
    // ============================================================
    function escapeHtml(text) {
        if (!text) return '';
        return text.toString()
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

    function formatNumber(num) {
        if (!num) return '0';
        return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
    }

    // Filter functionality
    $('#btnApplyFilters').click(function () {
        var dateFrom = $('#filterDateFrom').val();
        var dateTo = $('#filterDateTo').val();
        var requester = $('#filterRequester').val();
        var status = $('#filterStatus').val();
        var tableFilter = $('#filterTable').val();

        // Function to apply filters to a table
        function applyFiltersToTable(tableSelector) {
            $(tableSelector + ' tbody tr').each(function () {
                var row = $(this);
                var showRow = true;
                var rowDate = row.data('date');
                var rowStatus = row.data('status');

                if (dateFrom && rowDate && rowDate < dateFrom) showRow = false;
                if (dateTo && rowDate && rowDate > dateTo) showRow = false;
                if (requester && row.data('requester') !== requester) showRow = false;
                if (status && rowStatus !== status) showRow = false;

                row.toggle(showRow);
            });
        }

        // Handle table-specific filtering
        if (tableFilter === 'pending') {
            // Show pending table, hide completed table
            $('#requestsTable').closest('.card').show();
            $('#completedRequestsTable').closest('.card').hide();
            applyFiltersToTable('#requestsTable');
        } else if (tableFilter === 'completed') {
            // Show completed table, hide pending table
            $('#requestsTable').closest('.card').hide();
            $('#completedRequestsTable').closest('.card').show();
            applyFiltersToTable('#completedRequestsTable');
        } else {
            // Show both tables and apply filters to both
            $('#requestsTable').closest('.card').show();
            $('#completedRequestsTable').closest('.card').show();
            applyFiltersToTable('#requestsTable');
            applyFiltersToTable('#completedRequestsTable');
        }
    });

    $('#btnClearFilters').click(function () {
        $('#filterDateFrom').val('');
        $('#filterDateTo').val('');
        $('#filterRequester').val('');
        $('#filterStatus').val('');
        $('#filterTable').val('');
        // Show all rows in both tables
        $('#requestsTable tbody tr').show();
        $('#completedRequestsTable tbody tr').show();
        // Show both tables
        $('#requestsTable').closest('.card').show();
        $('#completedRequestsTable').closest('.card').show();
    });

    // Export to CSV functionality
    function exportToCSV() {
        var csv = [];
        var headers = ['Req ID', 'Client Name', 'Client ID', 'Requester', 'Account Manager', 'Printer Count', 'Requested Date', 'Status'];
        csv.push(headers.join(','));

        $('#requestsTable tbody tr:visible').each(function () {
            var row = $(this);
            var cols = row.find('td');
            var rowData = [
                $(cols[0]).text().trim(),
                $(cols[1]).find('strong').text().trim(),
                $(cols[1]).find('small').text().trim(),
                $(cols[2]).text().trim(),
                $(cols[3]).text().trim(),
                $(cols[4]).text().trim().replace(/[^\d]/g, ''),
                $(cols[5]).text().trim().replace(/\s+/g, ' '),
                row.data('status') || ''
            ];
            csv.push(rowData.map(function (val) {
                return '"' + (val || '').replace(/"/g, '""') + '"';
            }).join(','));
        });

        var csvContent = csv.join('\n');
        var blob = new Blob([csvContent], {type: 'text/csv;charset=utf-8;'});
        var link = document.createElement('a');
        var url = URL.createObjectURL(blob);
        link.setAttribute('href', url);
        link.setAttribute('download', 'replacement_requests_' + new Date().toISOString().slice(0, 10) + '.csv');
        link.style.visibility = 'hidden';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    }

    // Export to PDF functionality
    function exportToPDF() {
        var table = document.getElementById('requestsTable');
        if (!table) {
            alert('No data to export');
            return;
        }

        var printWindow = window.open('', '_blank');
        if (!printWindow) {
            alert('Please allow popups for this site to export PDF');
            return;
        }

        // Clone table and remove Actions column (last column)
        var tableClone = table.cloneNode(true);
        var headerCells = tableClone.querySelectorAll('thead th');
        var bodyRows = tableClone.querySelectorAll('tbody tr');

        // Remove last column (Actions) from header
        if (headerCells.length > 0) {
            headerCells[headerCells.length - 1].remove();
        }

        // Remove last column from each body row
        bodyRows.forEach(function(row) {
            var cells = row.querySelectorAll('td');
            if (cells.length > 0) {
                cells[cells.length - 1].remove();
            }
        });

        printWindow.document.write('<html><head><title>Pending Replacement Requests</title>');
        printWindow.document.write('<style>');
        printWindow.document.write('body { font-family: Arial, sans-serif; padding: 20px; }');
        printWindow.document.write('h1 { font-size: 18px; margin-bottom: 20px; }');
        printWindow.document.write('table { width: 100%; border-collapse: collapse; font-size: 11px; }');
        printWindow.document.write('th, td { border: 1px solid #333; padding: 6px; text-align: left; }');
        printWindow.document.write('th { background-color: #333; color: white; }');
        printWindow.document.write('@media print { body { -webkit-print-color-adjust: exact; print-color-adjust: exact; } }');
        printWindow.document.write('</style></head><body>');
        printWindow.document.write('<h1>Pending Replacement Requests - ' + new Date().toLocaleDateString() + '</h1>');
        printWindow.document.write(tableClone.outerHTML);
        printWindow.document.write('</body></html>');
        printWindow.document.close();

        setTimeout(function() {
            printWindow.print();
        }, 500);
    }
</script>
