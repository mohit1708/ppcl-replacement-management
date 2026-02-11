<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="Commercial Approvals - AM Manager" scope="request"/>
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

        /* TAT Styling */
        .tat-display {
            display: flex;
            flex-direction: column;
            gap: 4px;
        }
        .tat-value {
            font-weight: 600;
            font-size: 0.875rem;
        }
        .tat-progress {
            width: 60px;
            height: 4px;
            background: #e9ecef;
            border-radius: 2px;
            overflow: hidden;
        }
        .tat-progress__bar {
            height: 100%;
            border-radius: 2px;
            transition: width 0.3s ease;
        }
        .tat--within .tat-value { color: #28a745; }
        .tat--within .tat-progress__bar { background: #28a745; }
        .tat--warning .tat-value { color: #ffc107; }
        .tat--warning .tat-progress__bar { background: #ffc107; }
        .tat--breach .tat-value { color: #dc3545; }
        .tat--breach .tat-progress__bar { background: #dc3545; }
        /* Last Action Styling */
        .last-action {
            display: flex;
            flex-direction: column;
            gap: 2px;
        }
        .last-action__date {
            font-weight: 500;
            font-size: 0.875rem;
            color: #343a40;
        }
        .last-action__time {
            font-size: 0.75rem;
            color: #6c757d;
        }
        .last-action__by {
            font-size: 0.75rem;
            color: #007bff;
        }
        /* Status TAT Badge */
        .status-tat {
            display: inline-flex;
            align-items: center;
            gap: 4px;
            padding: 2px 8px;
            border-radius: 4px;
            font-size: 0.75rem;
            font-weight: 500;
        }
        .status-tat--good {
            background: #d4edda;
            color: #155724;
        }
        .status-tat--warning {
            background: #fff3cd;
            color: #856404;
        }
        .status-tat--breach {
            background: #f8d7da;
            color: #721c24;
        }
        /* Fix modal stacking z-index */
        .modal-backdrop.show + .modal-backdrop.show {
            z-index: 1060;
        }
        #forwardModal {
            z-index: 1070;
        }
        #forwardModal + .modal-backdrop {
            z-index: 1065;
        }
        /* Printer Commercial modal should appear on top of commercialActionModal */
        #printerCommercialModal {
            z-index: 1080;
        }
        #printerCommercialModal ~ .modal-backdrop {
            z-index: 1075;
        }
        .commercial-link {
            color: #007bff;
            cursor: pointer;
            text-decoration: none;
        }
        .commercial-link:hover {
            text-decoration: underline;
        }

        .dataTable:not(.collapsed) thead > tr > th:first-child {
            display: table-cell;
        }
</style>


<div class="main-content-inner">
    <div class="page-content">
        <!-- Page Header -->
        <div class="page-header mb-4">
            <h1 class="page-title text-primary-d2">
                <i class="fas fa-dollar-sign text-dark-m3 mr-1"></i>
                Commercial Approval Requests
                <small class="page-info text-secondary-d2">
                    <i class="fa fa-angle-double-right text-80"></i>
                    AM Manager Approvals
                </small>
            </h1>
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
                        <input type="date" class="form-control form-control-sm" name="dateFrom" id="filterDateFrom" value="${param.dateFrom}">
                    </div>
                    <div class="form-group mb-0">
                        <label class="small font-weight-bold mb-1">To Date</label>
                        <input type="date" class="form-control form-control-sm" name="dateTo" id="filterDateTo" value="${param.dateTo}">
                    </div>
                    <div class="form-group mb-0">
                        <label class="small font-weight-bold mb-1">Requester</label>
                        <select class="form-control form-control-sm" name="requester" id="filterRequester">
                            <option value="">All Requesters</option>
                            <c:forEach items="${requesters}" var="r">
                                <option value="${r.userId}" ${param.requester == r.userId ? 'selected' : ''}>${r.name}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="form-group mb-0">
                        <label class="small font-weight-bold mb-1">Status</label>
                        <select class="form-control form-control-sm" name="status" id="filterStatus">
                            <option value="">All Status</option>
                            <option value="PENDING" ${param.status == 'PENDING' ? 'selected' : ''}>Pending Review</option>
                            <option value="APPROVED" ${param.status == 'APPROVED' ? 'selected' : ''}>Approved</option>
                        </select>
                    </div>
                    <div class="form-group mb-0">
                        <label class="small font-weight-bold mb-1">Table</label>
                        <select class="form-control form-control-sm" id="filterTable">
                            <option value="">All Tables</option>
                            <option value="pending">Pending Requests</option>
                            <option value="completed">Approved Requests</option>
                        </select>
                    </div>
                    <div class="form-group d-flex align-items-end mb-0">
                        <button type="button" class="btn btn-primary btn-sm px-3 mr-2" id="btnApplyFilters">
                            <i class="fas fa-filter mr-1"></i> Apply
                        </button>
                        <button type="button" class="btn btn-outline-secondary btn-sm px-3" id="btnClearFilters">
                            <i class="fas fa-times mr-1"></i> Clear
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <!-- Requests Table Card -->
        <div class="card bcard shadow-sm">
            <div class="card-header bgc-white py-3 pl-3 d-flex justify-content-between align-items-center">
                <h5 class="card-title text-120 text-dark-m3 mb-0">
                    <i class="fas fa-clock text-warning-d1 mr-1"></i> Pending Commercial Approvals
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
                        <div class="text-center py-5 text-secondary-m1">
                            <i class="fas fa-database fa-4x mb-3 text-grey-l1"></i>
                            <h5 class="text-grey-d1">Unable to load data</h5>
                            <button class="btn btn-primary btn-bold radius-1 px-4 mt-2" onclick="location.reload()">
                                <i class="fas fa-sync-alt mr-1"></i> Retry
                            </button>
                        </div>
                    </c:when>
                    <c:when test="${empty requests}">
                        <div class="text-center py-5 text-secondary-m1">
                            <i class="fas fa-inbox fa-4x mb-3 text-grey-l1"></i>
                            <h5 class="text-grey-d1">No pending requests</h5>
                            <p>All caught up! Check back later.</p>
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
                                    <th class="border-0 text-center">Commercial Details</th>
                                    <th class="border-0">Last Action</th>
                                    <th class="border-0">Status TAT</th>
                                    <th class="border-0">Overall TAT</th>
                                    <th class="border-0">Actions</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach items="${requests}" var="req">
                                    <tr data-req-id="${req.id}" data-requester="${req.requesterName}" data-am="${req.accountManager}" data-status="${req.status}"
                                        data-date="<fmt:formatDate value="${req.lastActionDate}" pattern="yyyy-MM-dd" />">
                                        <td><strong>REQ-${req.id}</strong></td>
                                        <td>
                                            <strong>${req.clientName}</strong><br/>
                                            <small class="text-muted">${req.clientId}</small>
                                        </td>
                                        <td>${req.requesterName}</td>
                                        <td class="text-center">
                                            <button class="btn btn-link commercial-link p-0" onclick="viewCommercialDetail(${req.id}, '${req.clientName}')">
                                                    ${req.printerCount} Printers
                                                <i class="fas fa-dollar-sign ml-1"></i>
                                            </button>
                                        </td>
                                        <td>
                                            <div class="last-action">
                                                    <span class="last-action__date">
                                                        <fmt:formatDate value="${req.lastActionDate}" pattern="dd-MMM-yyyy"/>
                                                    </span>
                                                <span class="last-action__time">
                                                        <fmt:formatDate value="${req.lastActionDate}" pattern="HH:mm"/> IST
                                                    </span>
                                                <span class="last-action__by">by ${req.lastActionBy}</span>
                                            </div>
                                        </td>
                                        <td>
                                            <c:set var="stageTatClass" value="${req.stageTatDays <= 2 ? 'good' : (req.stageTatDays <= 4 ? 'warning' : 'breach')}"/>
                                            <span class="status-tat status-tat--${stageTatClass}">
                                                    <i class="far fa-clock"></i>
                                                    ${req.stageTatDays} day<c:if test="${req.stageTatDays != 1}">s</c:if> at this stage
                                                </span>
                                        </td>
                                        <td>
                                            <c:set var="tatPercentage" value="${req.overallTatDays * 100 / req.targetTatDays}"/>
                                            <c:set var="tatClass" value="${tatPercentage <= 50 ? 'within' : (tatPercentage <= 75 ? 'warning' : 'breach')}"/>
                                            <div class="tat-display tat--${tatClass}">
                                                <span class="tat-value">${req.overallTatDays} days</span>
                                                <div class="tat-progress">
                                                    <div class="tat-progress__bar" style="width: ${tatPercentage > 100 ? 100 : tatPercentage}%;"></div>
                                                </div>
                                                <small class="text-muted">Target: ${req.targetTatDays} days</small>
                                            </div>
                                        </td>
                                        <td>
                                            <button class="btn btn-info btn-xs py-0 px-2 mr-1" onclick="openViewModal(${req.id})" title="View Details">
                                                <i class="fas fa-eye mr-1"></i> View
                                            </button>
                                            <button class="btn btn-primary btn-xs py-0 px-2" onclick="openCommercialAction(${req.id}, '${req.clientName}')">
                                                <i class="fas fa-check-square"></i> Submit Commercial
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
        <!-- APPROVED COMMERCIAL REQUESTS SECTION -->
        <!-- ============================================================ -->
        <div class="card mt-4 bcard shadow-sm">
            <div class="card-header bgc-white py-3 pl-3 d-flex justify-content-between align-items-center">
                <h5 class="card-title text-120 text-dark-m3 mb-0">
                    <i class="fas fa-check-circle text-success-m1 mr-1"></i> Approved Commercial Requests
                </h5>
                <span class="badge badge-lg bgc-success-l2 text-success-d2 border-1 brc-success-m3 radius-1 px-3">
                    <i class="fas fa-check-circle mr-1"></i> ${completedCount} approved
                </span>
            </div>
            <div class="card-body p-0">
                <c:choose>
                    <c:when test="${empty completedRequests}">
                        <div class="text-center py-5 text-secondary-m1">
                            <i class="fas fa-clipboard-check fa-4x mb-3 text-grey-l1"></i>
                            <h5 class="text-grey-d1">No approved commercial requests yet</h5>
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
                                        <td>
                                            <strong>${fn:escapeXml(req.clientName)}</strong><br>
                                            <small class="text-muted">${fn:escapeXml(req.clientId)}</small>
                                        </td>
                                        <td>
                                            <i class="fas fa-user text-secondary"></i>
                                                ${fn:escapeXml(req.requesterName)}
                                        </td>
                                        <td>
                                            <span class="badge badge-info">${fn:escapeXml(req.currentStageName)}</span>
                                        </td>
                                        <td>
                                            <i class="fas fa-user-tie text-primary"></i>
                                                ${fn:escapeXml(req.stageOwnerName)}
                                        </td>
                                        <td>
                                            <button class="btn btn-link commercial-link p-0" onclick="viewCommercialDetail(${req.id}, '${fn:escapeXml(req.clientName)}')">
                                                <i class="fas fa-print"></i>
                                                    ${req.printerCount} Printer${req.printerCount > 1 ? 's' : ''}
                                            </button>
                                        </td>
                                        <td class="text-nowrap">
                                            <button class="btn btn-outline-primary btn-sm"
                                                    onclick="openViewModal(${req.id})"
                                                    title="View Request">
                                                <i class="fas fa-eye"></i> View
                                            </button>
                                            <c:if test="${req.currentStageName == 'Quotation Pending'}">
                                            <button class="btn btn-outline-warning btn-sm ml-1"
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


<%@ include file="../common/footer.jsp" %>

<!-- ============================================================ -->
<!-- VIEW REQUEST MODAL (Read-Only) -->
<!-- ============================================================ -->
<div class="modal fade modal-lg" id="viewRequestModal" tabindex="-1">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 radius-1 shadow">
            <div class="modal-header bgc-primary-d1 text-white py-2 pl-3">
                <h5 class="modal-title text-110">
                    <i class="fas fa-eye mr-1"></i> View Request - <span id="viewModalReqId"></span>
                </h5>
                <button type="button" class="close text-white" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body px-4">
                <div id="viewModalLoading" class="text-center py-4">
                    <i class="fas fa-spinner fa-spin fa-2x text-primary-m1"></i>
                    <p class="mt-2">Loading request details...</p>
                </div>
                <div id="viewRequestContent" style="display:none;"></div>
                
                <!-- Comment History Section -->
                <div id="viewCommentHistoryWrapper" style="display:none;" class="mt-3">
                    <button type="button" class="btn btn-info btn-bold px-4 btn-sm" onclick="toggleViewCommentHistory()">
                        <i class="fas fa-history mr-1"></i> View Comment History
                    </button>
                    <div id="viewCommentHistorySection" style="display:none;" class="mt-3">
                        <h6 class="font-weight-bold text-primary-d1"><i class="fas fa-comments mr-1"></i> Comment History</h6>
                        <div id="viewCommentHistoryLoading" class="text-center py-2" style="display:none;">
                            <i class="fas fa-spinner fa-spin text-info"></i> Loading...
                        </div>
                        <div class="table-responsive border-1 brc-grey-l2 radius-1">
                            <table class="table table-sm table-striped mb-0" id="viewCommentHistoryTable" style="display:none;">
                                <thead class="bgc-grey-l4 text-grey-d2">
                                    <tr>
                                        <th class="border-0">User</th>
                                        <th class="border-0">Stage</th>
                                        <th class="border-0">Comments</th>
                                        <th class="border-0">Date/Time</th>
                                    </tr>
                                </thead>
                                <tbody id="viewCommentHistoryBody">
                                </tbody>
                            </table>
                        </div>
                        <div id="viewNoCommentsMsg" class="alert alert-info py-2 px-3 mt-2" style="display:none;">
                            <i class="fas fa-info-circle mr-1"></i> No comments found for this request.
                        </div>
                    </div>
                </div>
                <input type="hidden" id="viewReqIdHidden" value="">
            </div>
            <div class="modal-footer bgc-grey-l4 border-t-1 brc-grey-l2">
                <button type="button" class="btn btn-secondary btn-bold px-4" data-dismiss="modal">
                    <i class="fas fa-times mr-1"></i> Close
                </button>
            </div>
        </div>
    </div>
</div>

<!-- ============================================================ -->
<!-- REMINDER MODAL -->
<!-- ============================================================ -->
<div class="modal fade" id="reminderModal" tabindex="-1">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 radius-1 shadow">
            <div class="modal-header bgc-warning-d1 text-white py-2 pl-3">
                <h5 class="modal-title text-110">
                    <i class="fas fa-bell mr-1"></i> Send Reminder - <span id="reminderReqId"></span>
                </h5>
                <button type="button" class="close text-white" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body px-4">
                <div class="alert alert-warning bgc-warning-l4 border-none border-l-4 brc-warning-m1 radius-0">
                    <p class="mb-0 text-warning-d2">Send a reminder to <strong id="reminderOwnerName"></strong> for this request.</p>
                </div>
                <div class="form-group mb-3">
                    <label class="font-weight-bold text-grey-d1">Current Owner</label>
                    <div class="form-control-plaintext text-dark-m2 font-600 ml-1" id="reminderOwner"></div>
                </div>
                <div class="form-group mb-3">
                    <label class="font-weight-bold text-grey-d1">Additional Message (Optional)</label>
                    <textarea class="form-control" id="reminderMessage" rows="3" placeholder="Enter any additional message..."></textarea>
                </div>
                <input type="hidden" id="reminderReqIdHidden" value="">
            </div>
            <div class="modal-footer bgc-grey-l4 border-t-1 brc-grey-l2">
                <button type="button" class="btn btn-secondary btn-bold px-4" data-dismiss="modal">
                    <i class="fas fa-times mr-1"></i> Cancel
                </button>
                <button type="button" class="btn btn-warning btn-bold px-4" onclick="confirmSendReminder()">
                    <i class="fas fa-bell mr-1"></i> Send Reminder
                </button>
            </div>
        </div>
    </div>
</div>

<!-- Include All Modals -->
<%@ include file="modals.jsp" %>

<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.1/dist/umd/popper.min.js"></script>
<script src="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/js/bootstrap.min.js"></script>
<script src="https://cdn.datatables.net/1.10.24/js/jquery.dataTables.min.js"></script>
<script src="https://cdn.datatables.net/1.10.24/js/dataTables.bootstrap4.min.js"></script>

<script>
    const contextPath = '${pageContext.request.contextPath}';
    let currentReqId = null;
    let currentPrinters = [];
    let hierarchyUsers = [];
    let allPrinterModels = [];

    $(document).ready(function() {
        if ($('#requestsTable tbody tr').length > 0 && !$('#requestsTable').hasClass('dataTable')) {
            $('#requestsTable').DataTable({
                order: [[4, 'desc']],
                pageLength: 10,
                language: {
                    emptyTable: "No pending requests"
                }
            });
        }
        
        // Initialize DataTable for completed requests
        if ($('#completedRequestsTable tbody tr').length > 0 && !$('#completedRequestsTable').hasClass('dataTable')) {
            $('#completedRequestsTable').DataTable({
                order: [[0, 'desc']],
                pageLength: 10,
                language: {
                    emptyTable: "No approved requests"
                }
            });
        }
        
        // Fix Bootstrap modal stacking issue - handle multiple backdrops
        $(document).on('show.bs.modal', '.modal', function () {
            var zIndex = 1040 + (10 * $('.modal:visible').length);
            $(this).css('z-index', zIndex);
            setTimeout(function() {
                $('.modal-backdrop').not('.modal-stack').css('z-index', zIndex - 1).addClass('modal-stack');
            }, 0);
        });
        
        // Fix body scroll when closing stacked modal
        $(document).on('hidden.bs.modal', '.modal', function () {
            if ($('.modal:visible').length > 0) {
                $('body').addClass('modal-open');
            }
        });
        
        console.log("✅ AM Manager requestList.jsp loaded successfully!");
    });

    // ==================== FILTER FUNCTIONALITY ====================
    $('#btnApplyFilters').click(function () {
        var dateFrom = $('#filterDateFrom').val();
        var dateTo = $('#filterDateTo').val();
        var requester = $('#filterRequester').val();
        var am = $('#filterAM').val();
        var status = $('#filterStatus').val();
        var tableFilter = $('#filterTable').val();

        function applyFiltersToTable(tableSelector) {
            $(tableSelector + ' tbody tr').each(function () {
                var row = $(this);
                var showRow = true;
                var rowDate = row.data('date');
                var rowRequester = row.data('requester');
                var rowAm = row.data('am');
                var rowStatus = row.data('status');

                if (dateFrom && rowDate && rowDate < dateFrom) showRow = false;
                if (dateTo && rowDate && rowDate > dateTo) showRow = false;
                if (requester && rowRequester !== requester) showRow = false;
                if (am && rowAm !== am) showRow = false;
                if (status && rowStatus !== status) showRow = false;

                row.toggle(showRow);
            });
        }

        if (tableFilter === 'pending') {
            $('#requestsTable').closest('.card').show();
            $('#completedRequestsTable').closest('.card').hide();
            applyFiltersToTable('#requestsTable');
        } else if (tableFilter === 'completed') {
            $('#requestsTable').closest('.card').hide();
            $('#completedRequestsTable').closest('.card').show();
            applyFiltersToTable('#completedRequestsTable');
        } else {
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
        $('#filterAM').val('');
        $('#filterStatus').val('');
        $('#filterTable').val('');
        $('#requestsTable tbody tr').show();
        $('#completedRequestsTable tbody tr').show();
        $('#requestsTable').closest('.card').show();
        $('#completedRequestsTable').closest('.card').show();
    });

    // ==================== EXPORT FUNCTIONS ====================
    function exportToCSV() {
        var csv = [];
        var headers = ['Req ID', 'Client Name', 'Requester', 'Printers', 'Last Action', 'Status'];
        csv.push(headers.join(','));

        $('#requestsTable tbody tr:visible').each(function () {
            var row = $(this);
            var cols = row.find('td');
            var rowData = [
                $(cols[0]).text().trim(),
                $(cols[1]).find('strong').text().trim(),
                $(cols[2]).text().trim(),
                $(cols[3]).text().trim().replace(/[^\d]/g, ''),
                $(cols[4]).text().trim().replace(/\s+/g, ' '),
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
        link.setAttribute('download', 'ammanager_requests_' + new Date().toISOString().slice(0, 10) + '.csv');
        link.style.visibility = 'hidden';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    }

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

        var tableClone = table.cloneNode(true);
        var headerCells = tableClone.querySelectorAll('thead th');
        var bodyRows = tableClone.querySelectorAll('tbody tr');

        if (headerCells.length > 0) {
            headerCells[headerCells.length - 1].remove();
        }
        bodyRows.forEach(function (row) {
            var cells = row.querySelectorAll('td');
            if (cells.length > 0) {
                cells[cells.length - 1].remove();
            }
        });

        printWindow.document.write('<html><head><title>Commercial Approval Requests</title>');
        printWindow.document.write('<style>');
        printWindow.document.write('body { font-family: Arial, sans-serif; margin: 20px; }');
        printWindow.document.write('h2 { color: #333; }');
        printWindow.document.write('table { width: 100%; border-collapse: collapse; margin-top: 15px; }');
        printWindow.document.write('th, td { border: 1px solid #ddd; padding: 8px; text-align: left; font-size: 12px; }');
        printWindow.document.write('th { background-color: #f8f9fa; font-weight: bold; }');
        printWindow.document.write('.text-muted { color: #6c757d; }');
        printWindow.document.write('</style></head><body>');
        printWindow.document.write('<h2>Commercial Approval Requests - AM Manager</h2>');
        printWindow.document.write('<p>Generated: ' + new Date().toLocaleString() + '</p>');
        printWindow.document.write(tableClone.outerHTML);
        printWindow.document.write('</body></html>');
        printWindow.document.close();
        printWindow.print();
    }

    // ==================== VIEW COMMERCIAL DETAIL ====================
    function viewCommercialDetail(reqId, clientName) {
        console.log("📋 viewCommercialDetail called with reqId: " + reqId);
        currentReqId = reqId;
        $('#commercialDetailReqId').text('REQ-' + reqId);
        $('#commercialDetailModal').modal('show');
        $('#commercialDetailTableBody').html('<tr><td colspan="6" class="text-center"><i class="fas fa-spinner fa-spin"></i> Loading...</td></tr>');

        // Use the new endpoint to get commercial details with agreement data
        $.ajax({
            url: contextPath + '/views/replacement/ammanager/getCommercialByRequest',
            method: 'GET',
            data: { reqId: reqId },
            success: function(response) {
                console.log("✅ getCommercialByRequest success:", response);
                if (response.success) {
                    currentCommercials = response.data || [];
                    displayCommercialDetailTable(response.data);
                } else {
                    $('#commercialDetailTableBody').html('<tr><td colspan="6" class="text-center text-danger">Failed to load data</td></tr>');
                }
            },
            error: function(xhr, status, error) {
                console.error("❌ getCommercialByRequest error:", error);
                $('#commercialDetailTableBody').html('<tr><td colspan="6" class="text-center text-danger">Error loading commercial details</td></tr>');
            }
        });
    }

    var currentCommercials = [];

    function displayCommercialDetailTable(commercials) {
        let html = '';
        if (!commercials || commercials.length === 0) {
            html = '<tr><td colspan="6" class="text-center text-muted">No printers found</td></tr>';
        } else {
            commercials.forEach(function(c, idx) {
                html += '<tr>';
                html += '<td><strong>' + escapeHtml(c.agrNo || '-') + '</strong></td>';
                html += '<td><code>' + escapeHtml(c.serial || '-') + '</code></td>';
                html += '<td class="text-right">₹' + formatNumber(c.rent || 0) + '</td>';
                html += '<td class="text-right">' + formatNumber(c.freePrints || 0) + '</td>';
                html += '<td class="text-right">₹' + formatDecimal(c.a4Rate || 0) + '</td>';
                html += '<td><button class="btn btn-outline-info btn-sm" onclick="viewPrinterCommercialDetail(' + idx + ')">';
                html += '<i class="fas fa-eye"></i> Details</button></td>';
                html += '</tr>';
            });
        }
        $('#commercialDetailTableBody').html(html);
    }

    // ==================== OPEN COMMERCIAL ACTION ====================
    function openCommercialAction(reqId, clientName) {
        console.log("💰 openCommercialAction called with reqId: " + reqId);
        currentReqId = reqId;
        // Reset comment history when opening a new request
        commentHistoryLoaded = false;
        $('#commentHistorySection').hide();
        $('#commentHistoryBody').empty();
        
        $('#commercialActionReqId').text('REQ-' + reqId + ' (' + clientName + ')');
        $('#commercialActionModal').modal('show');

        // Fetch both printer details and commercial data in parallel
        $.ajax({
            url: contextPath + '/views/replacement/ammanager/getPrinterDetails',
            method: 'GET',
            data: { reqId: reqId },
            success: function(response) {
                console.log("✅ getPrinterDetails success:", response);
                if (response.success) {
                    currentPrinters = response.data.printers || [];
                    displayCommercialPrinterCards(response.data.printers);
                }
            },
            error: function(xhr, status, error) {
                console.error("❌ getPrinterDetails error:", error);
                alert('Error loading printer details: ' + error);
            }
        });

        // Also fetch commercial data for the View Commercial button
        $.ajax({
            url: contextPath + '/views/replacement/ammanager/getCommercialByRequest',
            method: 'GET',
            data: { reqId: reqId },
            success: function(response) {
                console.log("✅ getCommercialByRequest (for action modal) success:", response);
                if (response.success) {
                    currentCommercials = response.data || [];
                }
            },
            error: function(xhr, status, error) {
                console.error("❌ getCommercialByRequest error:", error);
            }
        });
    }

    function displayCommercialPrinterCards(printers) {
        let html = '';
        if (!printers || printers.length === 0) {
            html = '<div class="alert alert-warning">No printers found for this request.</div>';
            $('#applyToAllContainer').addClass('d-none');
        } else {
            // Show "Apply to All" only if more than 1 printer
            if (printers.length > 1) {
                $('#applyToAllContainer').removeClass('d-none');
            } else {
                $('#applyToAllContainer').addClass('d-none');
            }
            
            var template = $('#commercialPrinterCardTemplate').html();
            printers.forEach(function(p, idx) {
                var card = template
                    .replace(/\{\{printerId\}\}/g, p.id)
                    .replace(/\{\{printerIndex\}\}/g, (idx + 1))
                    .replace(/\{\{existingModel\}\}/g, escapeHtml(p.existingModel || ''))
                    .replace(/\{\{newModel\}\}/g, escapeHtml(p.newModelName || p.newModelText || 'TBD'))
                    .replace(/\{\{newModelBadgeClass\}\}/g, p.printerType === 'NEW' ? 'success' : 'warning')
                    .replace(/\{\{serialNumber\}\}/g, escapeHtml(p.serial || ''))
                    .replace(/\{\{agreementNumber\}\}/g, escapeHtml(p.agreementNo || 'Active'))
                    .replace(/\{\{primaryRecommendation\}\}/g, escapeHtml(p.newModelName || p.newModelText || 'TBD'))
                    .replace(/\{\{alternateRecommendation\}\}/g, p.alternateModel ? 'Alternate: ' + escapeHtml(p.alternateModel) : 'No alternate provided')
                    .replace(/\{\{editDisabled\}\}/g, p.alternateModel ? '' : 'disabled title="No alternate available"')
                    .replace(/\{\{printerOptions\}\}/g, buildPrinterOptions(p));
                html += card;
            });
        }
        $('#commercialPrintersContainer').html(html);
    }
    
    /**
     * Apply commercial decision and comments from first printer to all others
     */
    function applyCommercialToAll() {
        var firstCard = $('.commercial-printer-card').first();
        if (!firstCard.length) return;
        
        var firstPrinterId = firstCard.data('printer-id');
        var continueExisting = $('#commercialDecision_' + firstPrinterId).is(':checked');
        var comments = $('#printerComments_' + firstPrinterId).val();
        
        var appliedCount = 0;
        $('.commercial-printer-card').each(function(index) {
            if (index === 0) return; // Skip first printer
            
            var $card = $(this);
            var printerId = $card.data('printer-id');
            
            // Apply "Continue with Existing Commercial" checkbox
            $('#commercialDecision_' + printerId).prop('checked', continueExisting);
            toggleCommercialComments(printerId, continueExisting);
            
            // Apply comments
            $('#printerComments_' + printerId).val(comments);
            
            // Visual feedback
            $card.css('box-shadow', '0 0 0 2px #17a2b8');
            setTimeout(function() { $card.css('box-shadow', ''); }, 1500);
            
            appliedCount++;
        });
        
        if (appliedCount > 0) {
            alert('Settings from Printer 1 applied to ' + appliedCount + ' other printer(s)!');
        }
    }

    function buildPrinterOptions(printer) {
        var options = '<option value="' + (printer.newModelId || '') + '" selected>' + escapeHtml(printer.newModelName || printer.newModelText || 'TBD') + ' (Current)</option>';
        if (printer.alternateModel) {
            options += '<option value="' + (printer.alternateModelId || '') + '">' + escapeHtml(printer.alternateModel) + ' (TL Alternate)</option>';
        }
        return options;
    }

    function editPrinterRecommendation(printerId) {
        var selectGroup = $('#printerSelectGroup_' + printerId);

        if (selectGroup.hasClass('d-none')) {
            if (allPrinterModels.length === 0) {
                loadAllPrinterModels(function() {
                    populatePrinterModelDropdown(printerId);
                    selectGroup.removeClass('d-none');
                });
            } else {
                populatePrinterModelDropdown(printerId);
                selectGroup.removeClass('d-none');
            }
        } else {
            selectGroup.addClass('d-none');
        }
    }

    function loadAllPrinterModels(callback) {
        $.ajax({
            url: contextPath + '/views/replacement/ammanager/getPrinterModels',
            method: 'GET',
            success: function(response) {
                if (response.success) {
                    allPrinterModels = response.data || [];
                    console.log("✅ Loaded " + allPrinterModels.length + " printer models");
                    if (callback) callback();
                }
            },
            error: function(xhr, status, error) {
                console.error("❌ getPrinterModels error:", error);
                alert('Error loading printer models: ' + error);
            }
        });
    }

    function populatePrinterModelDropdown(printerId) {
        var printer = currentPrinters.find(function(p) { return p.id == printerId; });
        var select = $('#printerSelect_' + printerId);

        var html = '<option value="">-- Select Printer Model --</option>';

        if (printer && printer.newModelId) {
            html += '<option value="' + printer.newModelId + '" selected>' + escapeHtml(printer.newModelName || printer.newModelText || 'Current') + ' (Current)</option>';
        }

        allPrinterModels.forEach(function(model) {
            if (!printer || model.id != printer.newModelId) {
                var colorLabel = model.color == 1 ? ' (Color)' : ' (Mono)';
                html += '<option value="' + model.id + '">' + escapeHtml(model.modelName) + colorLabel + '</option>';
            }
        });

        select.html(html);
    }

    function updateSelectedPrinter(printerId, newModelId) {
        console.log('Printer ' + printerId + ' changed to model ID: ' + newModelId);

        if (!newModelId) return;

        var selectedModel = allPrinterModels.find(function(m) { return m.id == newModelId; });
        var modelName = selectedModel ? selectedModel.modelName : '';

        // Update local printer data (will be saved on Approve)
        var printer = currentPrinters.find(function(p) { return p.id == printerId; });
        if (printer) {
            printer.newModelId = newModelId;
            printer.newModelName = modelName;
            printer.modelChanged = true;
        }

        // Update UI to reflect the change
        var card = $('.commercial-printer-card[data-printer-id="' + printerId + '"]');
        card.find('.badge-success, .badge-warning').first().text(modelName);
        card.find('.font-weight-bold:contains("Primary:")').text('Primary: ' + modelName);

        console.log("📝 Printer " + printerId + " selection updated locally to: " + modelName + " (will save on submit)");
    }

    function toggleCommercialComments(printerId, isChecked) {
        var required = $('#commentsRequired_' + printerId);
        if (isChecked) {
            // Checkbox is checked = Continue with existing commercial, comments optional
            required.addClass('d-none');
        } else {
            // Checkbox is unchecked = New terms required, comments mandatory
            required.removeClass('d-none');
        }
    }

    // ==================== VIEW PRINTER COMMERCIAL ====================
    function viewPrinterCommercial(agrProdId, modelName, serial) {
        console.log("📊 viewPrinterCommercial called with agrProdId: " + agrProdId);
        
        // Reset to loading state
        resetPrinterCommercialModal(modelName, serial);
        
        $('#printerCommercialModal').modal('show');

        $.ajax({
            url: contextPath + '/views/replacement/ammanager/getPrinterHistory',
            method: 'GET',
            data: { agrProdId: agrProdId },
            success: function(response) {
                console.log("✅ getPrinterHistory success:", response);
                if (response.success && response.data) {
                    displayPrinterCommercial(response.data, modelName, serial);
                } else {
                    showPrinterCommercialError('No data available');
                }
            },
            error: function(xhr, status, error) {
                console.error("❌ getPrinterHistory error:", error);
                showPrinterCommercialError('Error loading data: ' + error);
            }
        });
    }
    
    function resetPrinterCommercialModal(modelName, serial) {
        $('#commercialProductHeader').text('1] Product: ' + (modelName || '--') + ' (' + (serial || '--') + ')');
        $('#commercialLocation').text('Loading...');
        $('#commercialType').text('Loading...');
        $('#commercialDetailsTableBody').html('<tr><td colspan="4" class="text-center text-muted">Loading...</td></tr>');
    }
    
    function showPrinterCommercialError(message) {
        $('#commercialLocation').text('--');
        $('#commercialType').html('<span class="text-danger">' + escapeHtml(message) + '</span>');
    }

    function displayPrinterCommercial(data, modelName, serial) {
        var c = data.agreement || data;
        var productName = c.productName || modelName || '--';
        var serialNo = c.serialNumber || serial || '--';
        var location = c.location || c.locationName || '--';
        
        $('#commercialProductHeader').text('1] Product: ' + productName + ' (' + serialNo + ')');
        $('#commercialLocation').text(location);
        
        var commercialType = determineCommercialType(c);
        $('#commercialType').text(commercialType);
        
        buildDynamicCommercialTable(c);
    }

    function viewPrinterCommercialDetail(idxOrPrinterId, modelName, serial) {
        var c = null;
        var printerIndex = 1;
        
        if (typeof idxOrPrinterId === 'number' && currentCommercials.length > 0) {
            c = currentCommercials[idxOrPrinterId];
            printerIndex = idxOrPrinterId + 1;
        } else if (idxOrPrinterId && currentCommercials.length > 0) {
            c = currentCommercials.find(function(item, idx) {
                if (item.printerId == idxOrPrinterId || item.agrProdId == idxOrPrinterId) {
                    printerIndex = idx + 1;
                    return true;
                }
                return false;
            });
        }
        
        if (!c && serial && currentPrinters && currentPrinters.length > 0) {
            var printer = currentPrinters.find(function(p) { return p.serial === serial; });
            if (printer && printer.agrProdId) {
                viewPrinterCommercial(printer.agrProdId, modelName || printer.existingModel, serial);
                return;
            }
        }
        
        if (!c) {
            if (idxOrPrinterId && modelName && serial) {
                viewPrinterCommercial(idxOrPrinterId, modelName, serial);
                return;
            }
            alert('Commercial details not found. Please reload and try again.');
            return;
        }
        
        var productName = c.productName || c.existingModel || modelName || '--';
        var serialNo = c.serial || serial || '--';
        var location = c.location || c.locationName || '--';
        var city = c.city || '';
        if (city) location = location + ', ' + city;
        
        var commercialType = determineCommercialType(c);
        
        $('#commercialProductHeader').text(printerIndex + '] Product: ' + productName + ' (' + serialNo + ')');
        $('#commercialLocation').text(location);
        $('#commercialType').text(commercialType);
        
        buildDynamicCommercialTable(c);
        
        $('#printerCommercialModal').modal('show');
    }

    function buildDynamicCommercialTable(c) {
        var fields = [
            {label: 'Rent', value: c.rent},
            {label: 'Free Prints', value: c.freePrints},
            {label: 'A3 Rate Post', value: c.a3RatePost},
            {label: 'A4 Rate Post', value: c.a4RatePost},
            {label: 'A3 Rate', value: c.a3Rate},
            {label: 'A4 Rate', value: c.a4Rate},
            {label: 'Free Scan', value: c.freeScan},
            {label: 'Scan Rate', value: c.scanRate},
            {label: 'Scan Rate Post', value: c.scanRatePost},
            {label: 'Page Committed', value: c.pageCommited},
            {label: 'Black Cart Rate', value: c.blackCartRate},
            {label: 'Color Cart Rate', value: c.clrCartRate},
            {label: 'Cart Committed', value: c.cartCommited},
            {label: 'Billing Committed', value: c.billingCommited},
            {label: 'Commitment Period', value: c.commitmentPeriod},
            {label: 'Drum Unit Free', value: c.drumUnitFree},
            {label: 'Drum Unit Charge', value: c.drumUnitCharge},
            {label: 'Free A3 Black', value: c.freeA3Black},
            {label: 'Free A4 Color', value: c.freeA4Color},
            {label: 'Agr Commerce Type', value: c.agrCommerceType},
            {label: 'Free A3 Color', value: c.freeA3Color},
            {label: 'A3 Rate Post Color', value: c.a3RatePostColor}
        ];
        
        var nonZero = fields.filter(function(f) {
            return f.value !== null && f.value !== undefined && f.value !== 0 && f.value !== '0' && f.value !== 0.0;
        });
        
        var html = '';
        for (var i = 0; i < nonZero.length; i += 2) {
            html += '<tr>';
            html += '<td class="bg-light font-weight-bold" style="width:25%">' + nonZero[i].label + '</td>';
            html += '<td style="width:25%">' + formatCommercialValue(nonZero[i]) + '</td>';
            if (i + 1 < nonZero.length) {
                html += '<td class="bg-light font-weight-bold" style="width:25%">' + nonZero[i+1].label + '</td>';
                html += '<td style="width:25%">' + formatCommercialValue(nonZero[i+1]) + '</td>';
            } else {
                html += '<td class="bg-light font-weight-bold" style="width:25%"></td>';
                html += '<td style="width:25%"></td>';
            }
            html += '</tr>';
        }
        
        if (nonZero.length === 0) {
            html = '<tr><td colspan="4" class="text-center text-muted">No commercial data available</td></tr>';
        }
        
        $('#commercialDetailsTableBody').html(html);
    }

    function formatCommercialValue(field) {
        var rateFields = ['Rent', 'A3 Rate Post', 'A4 Rate Post', 'A3 Rate', 'A4 Rate', 
                          'Scan Rate', 'Scan Rate Post', 'Black Cart Rate', 'Color Cart Rate',
                          'Drum Unit Charge', 'A3 Rate Post Color'];
        if (rateFields.indexOf(field.label) >= 0) {
            return 'Rs ' + formatDecimal(field.value);
        }
        return formatNumber(field.value);
    }
    
    // Helper function to determine commercial type
    function determineCommercialType(c) {
        var hasRent = (c.rent && c.rent > 0);
        var hasPerPrint = (c.a4Rate && c.a4Rate > 0) || (c.freePrints && c.freePrints > 0);
        var hasAmc = (c.amc && c.amc > 0);
        
        if (c.commercialType) {
            return c.commercialType;
        }
        
        var parts = [];
        if (hasRent) parts.push('RENTAL');
        if (hasPerPrint) parts.push('PER PRINT');
        if (hasAmc && parts.length === 0) parts.push('AMC');
        
        return parts.length > 0 ? parts.join(' + ') : 'STANDARD';
    }

    // ==================== APPROVE AND NOTIFY ====================
    function approveAndNotifyAM() {
        console.log("✅ approveAndNotifyAM called");

        var printerDecisions = [];
        var printerRecommendations = [];
        var hasError = false;

        $('.commercial-printer-card').each(function() {
            var printerId = $(this).data('printer-id');
            var isChecked = $('#commercialDecision_' + printerId).is(':checked');
            var decision = isChecked ? 'yes' : 'no';
            var comments = $('#printerComments_' + printerId).val();

            if (!isChecked && !comments.trim()) {
                hasError = true;
                alert('Comments are required when not continuing with existing commercial terms');
                return false;
            }

            printerDecisions.push({
                printerId: printerId,
                continueExisting: decision,
                comments: comments
            });

            // Check if model was changed for this printer
            var printer = currentPrinters.find(function(p) { return p.id == printerId; });
            if (printer && printer.modelChanged) {
                printerRecommendations.push({
                    printerId: printerId,
                    newModelId: printer.newModelId,
                    newModelText: printer.newModelName,
                    comments: 'Updated by AM Manager'
                });
            }
        });

        if (hasError) return;

        if (!confirm('Approve commercial terms and notify Account Manager?')) return;

        var overallComments = $('#commercialOverallComments').val();

        $.ajax({
            url: contextPath + '/views/replacement/ammanager/approveRequest',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({
                reqId: currentReqId,
                printerDecisions: printerDecisions,
                printerRecommendations: printerRecommendations,
                overallComments: overallComments
            }),
            success: function(response) {
                console.log("✅ approveRequest success:", response);
                if (response.success) {
                    alert('✅ Commercial terms approved. Account Manager has been notified.');
                    $('#commercialActionModal').modal('hide');
                    location.reload();
                } else {
                    alert('❌ ' + (response.message || 'Failed to approve'));
                }
            },
            error: function(xhr, status, error) {
                console.error("❌ approveRequest error:", error);
                alert('Error approving request: ' + error);
            }
        });
    }

    // ==================== REJECT FROM COMMERCIAL ====================
    function openRejectFromCommercial() {
        $('#rejectModal').modal('show');
    }

    function submitReject() {
        console.log("📤 submitReject called");
        var rejectionReason = $('#rejectionReason').val();
        var rejectComments = $('#rejectComments').val().trim();

        if (!rejectionReason) {
            alert('Please select rejection reason');
            return;
        }
        if (!rejectComments) {
            alert('Please enter rejection comments');
            return;
        }

        if (!confirm('Are you sure you want to REJECT this request?')) return;

        $.ajax({
            url: contextPath + '/views/replacement/ammanager/rejectRequest',
            method: 'POST',
            data: {
                reqId: currentReqId,
                rejectionReason: rejectionReason,
                comments: rejectComments
            },
            success: function(response) {
                console.log("✅ rejectRequest success:", response);
                if (response.success) {
                    alert('✅ ' + response.message);
                    $('#rejectModal').modal('hide');
                    $('#commercialActionModal').modal('hide');
                    location.reload();
                }
            },
            error: function(xhr, status, error) {
                console.error("❌ rejectRequest error:", error);
                alert('Error rejecting request: ' + error);
            }
        });
    }

    // ==================== FORWARD FROM COMMERCIAL ====================
    function forwardFromCommercial() {
        // Hide the parent modal first, then show forwardModal
        $('#commercialActionModal').modal('hide');
        setTimeout(function() {
            $('#forwardModal').modal('show');
        }, 300);
    }

    function updateForwardRole() {
        var selected = $('#forwardTargetUser option:selected');
        var role = selected.data('role');
        $('#forwardRoleDisplay').text('Role: ' + (role || ''));
    }

    function submitForward() {
        var targetUserId = $('#forwardTargetUser').val();
        var targetRole = $('#forwardTargetUser option:selected').data('role');
        var forwardComments = $('#forwardComments').val().trim();

        if (!targetUserId) {
            alert('Please select target user');
            return;
        }

        $.ajax({
            url: contextPath + '/views/replacement/ammanager/forwardRequest',
            method: 'POST',
            data: {
                reqId: currentReqId,
                targetUserId: targetUserId,
                targetRole: targetRole,
                comments: forwardComments
            },
            success: function(response) {
                if (response.success) {
                    alert('✅ ' + response.message);
                    $('#forwardModal').modal('hide');
                    $('#commercialActionModal').modal('hide');
                    location.reload();
                }
            },
            error: function(xhr, status, error) {
                alert('Error forwarding request: ' + error);
            }
        });
    }

    // ==================== COMMENT HISTORY ====================
    var commentHistoryLoaded = false;
    
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
        var reqId = currentReqId;
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

    function resetCommentHistory() {
        commentHistoryLoaded = false;
        $('#commentHistorySection').hide();
        $('#commentHistoryBody').empty();
    }

    // ============================================================
    // VIEW MODAL (for completed requests)
    // ============================================================
    var viewCommentHistoryLoaded = false;
    
    function openViewModal(reqId) {
        $('#viewModalLoading').show();
        $('#viewRequestContent').hide();
        $('#viewCommentHistoryWrapper').hide();
        $('#viewModalReqId').text('REQ-' + reqId);
        $('#viewReqIdHidden').val(reqId);
        $('#viewRequestModal').modal('show');
        
        // Reset comment history
        viewCommentHistoryLoaded = false;
        $('#viewCommentHistorySection').hide();
        $('#viewCommentHistoryBody').empty();

        // Fetch request details using existing getPrinterDetails endpoint
        $.get(contextPath + '/views/replacement/ammanager/getPrinterDetails', {
            reqId: reqId
        }, function (data) {
            if (data.success) {
                populateViewModal(data.data, reqId);
            } else {
                $('#viewRequestContent').html('<div class="alert alert-danger">Failed to load request details</div>');
            }
            $('#viewModalLoading').hide();
            $('#viewRequestContent').show();
            $('#viewCommentHistoryWrapper').show();
        }, 'json').fail(function () {
            $('#viewRequestContent').html('<div class="alert alert-danger">Network error. Please try again.</div>');
            $('#viewModalLoading').hide();
            $('#viewRequestContent').show();
        });
    }
    
    function toggleViewCommentHistory() {
        var section = $('#viewCommentHistorySection');
        if (section.is(':visible')) {
            section.hide();
        } else {
            section.show();
            if (!viewCommentHistoryLoaded) {
                loadViewCommentHistory();
            }
        }
    }

    function loadViewCommentHistory() {
        var reqId = $('#viewReqIdHidden').val();
        if (!reqId) return;

        $('#viewCommentHistoryLoading').show();
        $('#viewCommentHistoryTable').hide();
        $('#viewNoCommentsMsg').hide();

        $.get(contextPath + '/views/replacement/request', {
            action: 'getCommentHistory',
            id: reqId
        }, function (data) {
            $('#viewCommentHistoryLoading').hide();
            
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
                $('#viewCommentHistoryBody').html(html);
                $('#viewCommentHistoryTable').show();
                viewCommentHistoryLoaded = true;
            } else {
                $('#viewNoCommentsMsg').show();
            }
        }, 'json').fail(function () {
            $('#viewCommentHistoryLoading').hide();
            $('#viewNoCommentsMsg').text('Failed to load comment history').show();
        });
    }

    function populateViewModal(data, reqId) {
        var printers = data.printers || [];

        // Get request info via getFullRequest for more details
        $.get(contextPath + '/views/replacement/ammanager/getFullRequest', {
            reqId: reqId
        }, function(reqData) {
            var req = (reqData.data && reqData.data.request) ? reqData.data.request : {};
            
            var html = '<div class="row mb-3">';
            html += '<div class="col-md-6"><strong>Client:</strong> ' + escapeHtml(req.clientName) + ' (' + escapeHtml(req.clientId) + ')</div>';
            html += '<div class="col-md-6"><strong>City:</strong> ' + escapeHtml(req.city || '-') + '</div>';
            html += '</div>';
            html += '<div class="row mb-3">';
            html += '<div class="col-md-6"><strong>Requester:</strong> ' + escapeHtml(req.requesterName) + '</div>';
            html += '<div class="col-md-6"><strong>Current Stage:</strong> <span class="badge badge-info">' + escapeHtml(req.stageName) + '</span></div>';
            html += '</div>';
            html += '<div class="row mb-3">';
            html += '<div class="col-md-6"><strong>Reason:</strong> ' + escapeHtml(req.reasonName || '-') + '</div>';
            html += '<div class="col-md-6"><strong>TL Comments:</strong> ' + escapeHtml(req.tlComments || '-') + '</div>';
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
        }, 'json').fail(function() {
            // Fallback - just show printers if request info fails
            var html = '<h6 class="mt-3"><i class="fas fa-print"></i> Printers</h6>';
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
        });
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

    // ==================== UTILITY FUNCTIONS ====================
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

    function formatDecimal(num) {
        if (num === null || num === undefined) return '0.00';
        return parseFloat(num).toFixed(2);
    }
</script>

<%@ include file="../common/footer.jsp" %>
