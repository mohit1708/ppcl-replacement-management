<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Dashboard" scope="request"/>
<%@ include file="../common/header.jsp" %>
<%@ include file="../common/sidebar.jsp" %>


<!-- Select2 CSS (Bootstrap 4 compatible) -->
<link href="https://cdn.jsdelivr.net/npm/select2@4.1.0-rc.0/dist/css/select2.min.css" rel="stylesheet" />
<link href="https://cdn.jsdelivr.net/npm/@ttskch/select2-bootstrap4-theme@1.5.2/dist/select2-bootstrap4.min.css" rel="stylesheet" />

<div class="main-content-inner">
<div class="page-content">

    <!-- Page Header -->
    <div class="d-flex justify-content-between align-items-center mb-3">
        <div>
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb mb-1 bg-transparent p-0">
                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/">Home</a></li>
                    <li class="breadcrumb-item active">Dashboard</li>
                </ol>
            </nav>
            <h1 class="h3 mb-0">Dashboard Overview</h1>
        </div>
        <div>
            <a href="javascript:void(0)" onclick="openDetailedView()" class="btn btn-outline-primary mr-2">
                <i class="fas fa-chart-bar mr-1"></i> Detailed View
            </a>
            <button class="btn btn-outline-secondary" onclick="refreshDashboard()">
                <i class="fas fa-sync-alt"></i> Refresh
            </button>
        </div>
    </div>

    <!-- Error Message -->
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            <i class="fas fa-exclamation-triangle"></i> ${errorMessage}
            <button type="button" class="close" data-dismiss="alert">&times;</button>
        </div>
    </c:if>

    <!-- Filters Card -->
    <form id="filterForm" method="get" action="${pageContext.request.contextPath}/views/replacement/dashboard">
        <div class="card mb-4 shadow-sm">
            <div class="card-header bg-light">
                <h6 class="mb-0"><i class="fas fa-filter mr-2"></i>Dashboard Filters</h6>
            </div>
            <div class="card-body">
                <!-- Row 1: Date Range -->
                <div class="form-row mb-3">
                    <div class="col-md-6">
                        <label class="font-weight-bold small text-uppercase text-muted">Date Range</label>
                        <div class="input-group">
                            <div class="input-group-prepend">
                                <span class="input-group-text"><i class="fas fa-calendar"></i></span>
                            </div>
                            <input type="date" class="form-control" name="fromDate" id="fromDate" value="${filters.fromDate}" placeholder="From Date">
                            <div class="input-group-prepend input-group-append">
                                <span class="input-group-text">to</span>
                            </div>
                            <input type="date" class="form-control" name="toDate" id="toDate" value="${filters.toDate}" placeholder="To Date">
                        </div>
                    </div>
                    <div class="col-md-3">
                        <label class="font-weight-bold small text-uppercase text-muted">Status</label>
                        <select class="form-control" name="status" id="status">
                            <option value="">All Status</option>
                            <c:forEach items="${statusOptions}" var="opt">
                                <option value="${opt.value}" ${filters.status == opt.value ? 'selected' : ''}>${opt.label}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="col-md-3">
                        <label class="font-weight-bold small text-uppercase text-muted">Stage</label>
                        <select class="form-control" name="stageId" id="stageId">
                            <option value="">All Stages</option>
                            <c:forEach items="${stages}" var="stage">
                                <option value="${stage.id}" ${filters.stageId == stage.id ? 'selected' : ''}>${stage.description}</option>
                            </c:forEach>
                        </select>
                    </div>
                </div>

                <!-- Row 2: People Filters -->
                <div class="form-row mb-3">
                    <div class="col-md-4">
                        <label class="font-weight-bold small text-uppercase text-muted">Requester</label>
                        <select class="form-control select2-ajax" name="requesterId" id="requesterId" data-placeholder="Search Requester..." data-url="${pageContext.request.contextPath}/views/replacement/dashboard/filters?type=requesters">
                            <option value="">All Requesters</option>
                            <c:if test="${not empty filters.requesterId}">
                                <c:forEach items="${requesters}" var="user">
                                    <c:if test="${user.id == filters.requesterId}">
                                        <option value="${user.id}" selected>${user.name}</option>
                                    </c:if>
                                </c:forEach>
                            </c:if>
                        </select>
                    </div>
                    <div class="col-md-4">
                        <label class="font-weight-bold small text-uppercase text-muted">Account Manager</label>
                        <select class="form-control select2-ajax" name="accountManagerId" id="accountManagerId" data-placeholder="Search Account Manager..." data-url="${pageContext.request.contextPath}/views/replacement/dashboard/filters?type=accountManagers">
                            <option value="">All Account Managers</option>
                            <c:if test="${not empty filters.accountManagerId}">
                                <c:forEach items="${accountManagers}" var="user">
                                    <c:if test="${user.id == filters.accountManagerId}">
                                        <option value="${user.id}" selected>${user.name}</option>
                                    </c:if>
                                </c:forEach>
                            </c:if>
                        </select>
                    </div>
                    <div class="col-md-4">
                        <label class="font-weight-bold small text-uppercase text-muted">Current Owner</label>
                        <select class="form-control select2-ajax" name="ownerId" id="ownerId" data-placeholder="Search Owner..." data-url="${pageContext.request.contextPath}/views/replacement/dashboard/filters?type=owners">
                            <option value="">All Owners</option>
                            <c:if test="${not empty filters.currentOwnerId}">
                                <c:forEach items="${owners}" var="user">
                                    <c:if test="${user.id == filters.currentOwnerId}">
                                        <option value="${user.id}" selected>${user.name}</option>
                                    </c:if>
                                </c:forEach>
                            </c:if>
                        </select>
                    </div>
                </div>

                <!-- Hidden fields -->
                <input type="hidden" name="page" value="1">
                <input type="hidden" name="view" id="viewFilter" value="${filters.view}">
                <input type="hidden" name="tatFilter" id="tatFilterInput" value="${filters.tatFilter}">
                <input type="hidden" name="categoryId" id="categoryIdInput" value="${filters.categoryId}">
                <input type="hidden" name="departmentId" id="departmentIdInput" value="${filters.departmentId}">

                <!-- Active Filters + Reset -->
                <div class="d-flex justify-content-between align-items-center pt-3 border-top">
                    <!-- Active Filters Badges -->
                    <div class="flex-grow-1">
                        <c:if test="${not empty filters.view || not empty filters.tatFilter || not empty filters.categoryId || not empty filters.departmentId || not empty filters.status || not empty filters.stageId || not empty filters.requesterId || not empty filters.accountManagerId || not empty filters.currentOwnerId}">
                            <span class="small text-muted mr-2"><i class="fas fa-filter mr-1"></i>Active:</span>
                            <c:if test="${not empty filters.view}">
                                <span class="badge badge-info mr-1">
                                    <c:choose>
                                        <c:when test="${filters.view == 'total'}">All Requests</c:when>
                                        <c:when test="${filters.view == 'pending'}">Pending</c:when>
                                        <c:when test="${filters.view == 'tat-breach'}">TAT Breach</c:when>
                                        <c:when test="${filters.view == 'closed'}">Closed</c:when>
                                    </c:choose>
                                    <i class="fas fa-times ml-1" style="cursor:pointer" onclick="removeFilter('view')"></i>
                                </span>
                            </c:if>
                            <c:if test="${not empty filters.tatFilter}">
                                <span class="badge ${filters.tatFilter == 'within' ? 'badge-success' : 'badge-danger'} mr-1">
                                    ${filters.tatFilter == 'within' ? 'Within TAT' : 'Beyond TAT'}
                                    <i class="fas fa-times ml-1" style="cursor:pointer" onclick="removeFilter('tatFilter')"></i>
                                </span>
                            </c:if>
                            <c:if test="${not empty filters.categoryId}">
                                <span class="badge badge-info mr-1">Category: ${filters.categoryName} <i class="fas fa-times ml-1" style="cursor:pointer" onclick="removeFilter('categoryId')"></i></span>
                            </c:if>
                            <c:if test="${not empty filters.departmentId}">
                                <span class="badge badge-info mr-1">Dept: ${filters.departmentName} <i class="fas fa-times ml-1" style="cursor:pointer" onclick="removeFilter('departmentId')"></i></span>
                            </c:if>
                        </c:if>
                    </div>
                    <!-- Reset Button -->
                    <div>
                        <button type="button" class="btn btn-outline-danger" onclick="clearFilters()">
                            <i class="fas fa-undo mr-1"></i> Reset All
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </form>

    <!-- KPI Cards -->
    <div class="row mb-4">
        <div class="col-md-3">
            <div class="card border-left-primary shadow h-100 ${filters.view == 'total' ? 'border-primary' : ''}" style="cursor:pointer;border-left:4px solid #4e73df!important" onclick="viewDetails('total')">
                <div class="card-body">
                    <div class="row no-gutters align-items-center">
                        <div class="col mr-2">
                            <div class="text-xs font-weight-bold text-primary text-uppercase mb-1">Total Requests</div>
                            <div class="h5 mb-0 font-weight-bold text-gray-800"><fmt:formatNumber value="${summary.totalRequests}" /></div>
                        </div>
                        <div class="col-auto">
                            <i class="fas fa-file-alt fa-2x text-gray-300"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card border-left-warning shadow h-100 ${filters.view == 'pending' ? 'border-warning' : ''}" style="cursor:pointer;border-left:4px solid #f6c23e!important" onclick="viewDetails('pending')">
                <div class="card-body">
                    <div class="row no-gutters align-items-center">
                        <div class="col mr-2">
                            <div class="text-xs font-weight-bold text-warning text-uppercase mb-1">Pending Approvals</div>
                            <div class="h5 mb-0 font-weight-bold text-gray-800"><fmt:formatNumber value="${summary.pendingRequests}" /></div>
                        </div>
                        <div class="col-auto">
                            <i class="fas fa-clock fa-2x text-gray-300"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card border-left-danger shadow h-100 ${filters.view == 'tat-breach' ? 'border-danger' : ''}" style="cursor:pointer;border-left:4px solid #e74a3b!important" onclick="viewDetails('tat-breach')">
                <div class="card-body">
                    <div class="row no-gutters align-items-center">
                        <div class="col mr-2">
                            <div class="text-xs font-weight-bold text-danger text-uppercase mb-1">TAT Breaches</div>
                            <div class="h5 mb-0 font-weight-bold text-gray-800"><fmt:formatNumber value="${summary.tatBreachCount}" /></div>
                        </div>
                        <div class="col-auto">
                            <i class="fas fa-exclamation-triangle fa-2x text-gray-300"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card border-left-success shadow h-100 ${filters.view == 'closed' ? 'border-success' : ''}" style="cursor:pointer;border-left:4px solid #1cc88a!important" onclick="viewDetails('closed')">
                <div class="card-body">
                    <div class="row no-gutters align-items-center">
                        <div class="col mr-2">
                            <div class="text-xs font-weight-bold text-success text-uppercase mb-1">Closed</div>
                            <div class="h5 mb-0 font-weight-bold text-gray-800"><fmt:formatNumber value="${summary.closedRequests}" /></div>
                        </div>
                        <div class="col-auto">
                            <i class="fas fa-check-circle fa-2x text-gray-300"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Replacement Requests Table -->
    <div class="card shadow mb-4">
        <div class="card-header py-3 d-flex justify-content-between align-items-center">
            <h6 class="m-0 font-weight-bold text-primary">
                <i class="fas fa-list mr-2"></i>Replacement Requests
                <c:if test="${not empty filters.view}">
                    <span class="badge badge-secondary ml-2">
                        <c:choose>
                            <c:when test="${filters.view == 'pending'}">Pending</c:when>
                            <c:when test="${filters.view == 'tat-breach'}">TAT Breach</c:when>
                            <c:when test="${filters.view == 'closed'}">Closed</c:when>
                            <c:otherwise>All</c:otherwise>
                        </c:choose>
                    </span>
                </c:if>
            </h6>
            <div class="dropdown">
                <button class="btn btn-sm btn-outline-secondary dropdown-toggle" data-toggle="dropdown">
                    <i class="fas fa-download mr-1"></i> Export
                </button>
                <div class="dropdown-menu dropdown-menu-right">
                    <a class="dropdown-item" href="javascript:void(0)" onclick="exportData('csv')"><i class="fas fa-file-csv mr-2"></i>CSV</a>
                    <a class="dropdown-item" href="javascript:void(0)" onclick="exportData('excel')"><i class="fas fa-file-excel mr-2"></i>Excel</a>
                </div>
            </div>
        </div>
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-hover mb-0">
                    <thead class="thead-light">
                    <tr>
                        <th>Request ID</th>
                        <th>Client Name</th>
                        <th>Requester</th>
                        <th>Account Manager</th>
                        <th>Status</th>
                        <th>Current Owner</th>
                        <th>Stage</th>
                        <th>Request Date</th>
                        <th>TAT Status</th>
                        <th class="text-center">Printers</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${requests}" var="req">
                        <tr>
                            <td><span class="font-weight-bold text-primary">${req.id}</span></td>
                            <td>
                                <strong>${req.clientName}</strong>
                                <c:if test="${not empty req.clientCity}"><br><small class="text-muted">${req.clientCity}</small></c:if>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${not empty req.requesterUserIdStr}">${req.requesterUserIdStr}</c:when>
                                    <c:otherwise><span class="text-muted">N/A</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${not empty req.accountManagerUserId}">${req.accountManagerUserId}</c:when>
                                    <c:otherwise><span class="text-muted">N/A</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td><span class="badge ${req.statusBadgeClass}">${req.status}</span></td>
                            <td>
                                <c:choose>
                                    <c:when test="${not empty req.currentOwnerUserId}">${req.currentOwnerUserId}</c:when>
                                    <c:otherwise><span class="text-muted">N/A</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td><small class="text-muted">${req.currentStageDescription}</small></td>
                            <td>
                                <fmt:formatDate value="${req.creationDateTime}" pattern="dd MMM yyyy"/>
                                <br><small class="text-muted"><fmt:formatDate value="${req.creationDateTime}" pattern="HH:mm"/></small>
                            </td>
                            <td>
                                <span class="badge ${req.tatBadgeClass}">${req.tatStatus}</span>
                                <c:if test="${req.tatPercentage != null}">
                                    <div class="progress mt-1" style="height:5px;width:60px">
                                        <div class="progress-bar ${req.withinTat ? 'bg-success' : 'bg-danger'}" style="width:${req.tatPercentage > 100 ? 100 : req.tatPercentage}%"></div>
                                    </div>
                                    <small class="text-muted"><fmt:formatNumber value="${req.tatPercentage}" maxFractionDigits="0"/>%</small>
                                </c:if>
                            </td>
                            <td class="text-center">
                                <button class="btn btn-sm btn-outline-info" onclick="showPrintersModal(${req.id}, ${req.printerCount})" title="View Printers">
                                    <i class="fas fa-print"></i> ${req.printerCount}
                                </button>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty requests}">
                        <tr>
                            <td colspan="10" class="text-center py-5 text-muted">
                                <i class="fas fa-inbox fa-3x mb-3 d-block"></i>
                                <p class="mb-0">No replacement requests found</p>
                            </td>
                        </tr>
                    </c:if>
                    </tbody>
                </table>
            </div>
        </div>
        <c:if test="${totalCount > 0}">
            <div class="card-footer d-flex justify-content-between align-items-center bg-light">
                <div class="small text-muted">
                    Showing <strong>${startRecord}</strong> - <strong>${endRecord}</strong> of <strong><fmt:formatNumber value="${totalCount}"/></strong> entries
                </div>
                <c:if test="${totalPages > 1}">
                    <nav>
                        <ul class="pagination pagination-sm mb-0">
                            <li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
                                <a class="page-link" href="javascript:void(0)" onclick="goToPage(${currentPage - 1})"><i class="fas fa-chevron-left"></i></a>
                            </li>
                            <c:forEach begin="1" end="${totalPages > 5 ? 5 : totalPages}" var="i">
                                <c:set var="pageNum" value="${currentPage <= 3 ? i : (currentPage + i - 3)}"/>
                                <c:if test="${pageNum <= totalPages}">
                                    <li class="page-item ${currentPage == pageNum ? 'active' : ''}">
                                        <a class="page-link" href="javascript:void(0)" onclick="goToPage(${pageNum})">${pageNum}</a>
                                    </li>
                                </c:if>
                            </c:forEach>
                            <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
                                <a class="page-link" href="javascript:void(0)" onclick="goToPage(${currentPage + 1})"><i class="fas fa-chevron-right"></i></a>
                            </li>
                        </ul>
                    </nav>
                </c:if>
            </div>
        </c:if>
    </div>

    <!-- Current Stage Wise + Replacement Category (side by side) -->
    <div class="row mb-4">
        <!-- Current Stage Wise -->
        <div class="col-lg-6 mb-4">
            <div class="card shadow h-100">
                <div class="card-header py-3">
                    <h6 class="m-0 font-weight-bold text-primary"><i class="fas fa-layer-group mr-2"></i>Current Stage Wise</h6>
                </div>
                <div class="card-body p-0" style="max-height:340px;overflow-y:auto">
                    <table class="table table-sm table-hover mb-0">
                        <thead class="thead-light">
                        <tr>
                            <th>Stage</th>
                            <th class="text-center text-success">Within</th>
                            <th class="text-center text-danger">Beyond</th>
                            <th class="text-center">Total</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${summary.stageSummary}" var="stage">
                            <tr class="${filters.stageId == stage.stageId ? 'table-active' : ''}">
                                <td><a href="javascript:void(0)" onclick="filterByStage(${stage.stageId})" class="text-decoration-none">${stage.stageDescription}</a></td>
                                <td class="text-center"><span class="badge badge-success" style="cursor:pointer;min-width:35px" onclick="filterByStageAndTat(${stage.stageId}, 'within')">${stage.withinTatCount}</span></td>
                                <td class="text-center"><span class="badge badge-danger" style="cursor:pointer;min-width:35px" onclick="filterByStageAndTat(${stage.stageId}, 'beyond')">${stage.beyondTatCount}</span></td>
                                <td class="text-center"><strong>${stage.totalCount}</strong></td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty summary.stageSummary}"><tr><td colspan="4" class="text-center text-muted py-3">No data</td></tr></c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <!-- Replacement Category Pie Chart -->
        <div class="col-lg-6 mb-4">
            <div class="card shadow h-100">
                <div class="card-header py-3">
                    <h6 class="m-0 font-weight-bold text-info"><i class="fas fa-chart-pie mr-2"></i>Replacement Category</h6>
                </div>
                <div class="card-body d-flex align-items-center justify-content-center">
                    <c:if test="${not empty summary.categorySummary}">
                        <div style="position:relative;width:100%;max-width:360px">
                            <canvas id="categoryPieChart"></canvas>
                        </div>
                    </c:if>
                    <c:if test="${empty summary.categorySummary}">
                        <div class="text-center text-muted py-4">
                            <i class="fas fa-chart-pie fa-3x mb-3 d-block text-gray-300"></i>
                            <p>No category data available</p>
                        </div>
                    </c:if>
                </div>
            </div>
        </div>
    </div>
</div><%-- /.page-content --%>
</div><%-- /.main-content-inner --%>

<!-- Printers Modal -->
<div class="modal fade" id="printersModal" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title"><i class="fas fa-print mr-2"></i>Printer Details</h5>
                <button type="button" class="close" data-dismiss="modal">&times;</button>
            </div>
            <div class="modal-body" id="printersModalContent">
                <div class="text-center py-4"><div class="spinner-border text-primary"></div></div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
            </div>
        </div>
    </div>
</div>

<!-- Chart.js + Select2 JS -->
<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/select2@4.1.0-rc.0/dist/js/select2.min.js"></script>

<script>
    var contextPath = '${pageContext.request.contextPath}';

    /**
     * IIFE: Captures jQuery reference (with Select2 plugin) as local '$'.
     * This protects against footer.jsp re-loading jQuery which would
     * overwrite the global $ and lose the Select2 plugin.
     * Executes immediately since all DOM elements above this script are already parsed.
     */
        // Flag to prevent auto-submit during reset/programmatic changes
    var _skipAutoSubmit = false;

    (function($) {
        // Set default dates (last 10 days)
        if (!$('#fromDate').val()) {
            var today = new Date();
            var tenDaysAgo = new Date(today);
            tenDaysAgo.setDate(today.getDate() - 10);
            _skipAutoSubmit = true;
            $('#fromDate').val(formatDate(tenDaysAgo));
            $('#toDate').val(formatDate(today));
            _skipAutoSubmit = false;
        }

        // Helper: auto-submit the form (skips during programmatic resets)
        function autoSubmit() {
            if (_skipAutoSubmit) return;
            document.getElementById('filterForm').submit();
        }

        // Initialize Select2 with AJAX for large datasets
        $('.select2-ajax').each(function() {
            var $el = $(this);
            var placeholder = $el.data('placeholder') || 'Search...';
            var url = $el.data('url');

            $el.select2({
                theme: 'bootstrap4',
                placeholder: placeholder,
                allowClear: true,
                width: '100%',
                minimumInputLength: 0,
                ajax: {
                    url: url,
                    dataType: 'json',
                    delay: 250,
                    data: function(params) {
                        return {
                            search: params.term || '',
                            page: params.page || 1,
                            limit: 10,
                            fromDate: $('#fromDate').val() || '',
                            toDate: $('#toDate').val() || '',
                            status: $('#status').val() || '',
                            stageId: $('#stageId').val() || ''
                        };
                    },
                    processResults: function(data, params) {
                        params.page = params.page || 1;
                        var results = [];
                        if (data.success && data.data) {
                            results = data.data.map(function(item) {
                                return { id: item.id, text: item.name || item.userId };
                            });
                        }
                        return {
                            results: results,
                            pagination: { more: results.length >= 10 }
                        };
                    },
                    cache: true
                },
                language: {
                    inputTooShort: function() { return 'Type to search...'; },
                    noResults: function() { return 'No results found'; },
                    searching: function() { return 'Searching...'; }
                }
            });

            // Auto-submit when Select2 AJAX dropdown value changes
            $el.on('change', function() { autoSubmit(); });
        });

        // Regular dropdowns (also need captured $ for Select2)
        $('#status, #stageId').select2({
            theme: 'bootstrap4',
            width: '100%',
            minimumResultsForSearch: 10
        });

        // Auto-submit when Status or Stage changes
        $('#status, #stageId').on('change', function() { autoSubmit(); });

        // Auto-submit when date range changes
        $('#fromDate, #toDate').on('change', function() { autoSubmit(); });
    })(jQuery);

    // =====================================================
    // Category Pie Chart
    // =====================================================
    (function() {
        var canvas = document.getElementById('categoryPieChart');
        if (!canvas) return;

        var labels = [];
        var dataValues = [];
        var categoryIds = [];
        <c:forEach items="${summary.categorySummary}" var="cat">
        labels.push('${cat.categoryName}');
        dataValues.push(${cat.count});
        categoryIds.push(${cat.categoryId});
        </c:forEach>

        if (labels.length === 0) return;

        var colors = [
            '#4e73df', '#1cc88a', '#36b9cc', '#f6c23e', '#e74a3b',
            '#858796', '#5a5c69', '#2e59d9', '#17a673', '#2c9faf',
            '#dda20a', '#be2617', '#6610f2', '#e83e8c', '#fd7e14'
        ];

        var chart = new Chart(canvas, {
            type: 'pie',
            data: {
                labels: labels,
                datasets: [{
                    data: dataValues,
                    backgroundColor: colors.slice(0, labels.length),
                    borderColor: '#fff',
                    borderWidth: 2,
                    hoverBorderWidth: 3
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            padding: 15,
                            usePointStyle: true,
                            pointStyle: 'circle',
                            font: { size: 12 }
                        }
                    },
                    tooltip: {
                        callbacks: {
                            label: function(ctx) {
                                var total = ctx.dataset.data.reduce(function(a, b) { return a + b; }, 0);
                                var pct = total > 0 ? ((ctx.parsed / total) * 100).toFixed(1) : 0;
                                return ctx.label + ': ' + ctx.parsed + ' (' + pct + '%)';
                            }
                        }
                    }
                },
                onClick: function(evt, elements) {
                    if (elements.length > 0) {
                        var idx = elements[0].index;
                        drillDownCategory(categoryIds[idx]);
                    }
                }
            }
        });
    })();

    // =====================================================
    // Global functions (called from HTML onclick attributes)
    // These use the global $ which is fine - they don't need Select2
    // =====================================================

    function formatDate(date) {
        return date.toISOString().split('T')[0];
    }

    function clearFilters() {
        _skipAutoSubmit = true; // Prevent auto-submit while resetting

        var today = new Date();
        var tenDaysAgo = new Date(today);
        tenDaysAgo.setDate(today.getDate() - 10);

        $('#fromDate').val(formatDate(tenDaysAgo));
        $('#toDate').val(formatDate(today));
        $('#requesterId, #accountManagerId, #ownerId').val(null).trigger('change');
        $('#status, #stageId').val('').trigger('change');
        $('#viewFilter, #tatFilterInput, #categoryIdInput, #departmentIdInput').val('');

        _skipAutoSubmit = false;
        $('#filterForm').submit(); // Single submit after all resets
    }

    function removeFilter(name) {
        _skipAutoSubmit = true;
        if (name === 'view') $('#viewFilter').val('');
        else if (name === 'tatFilter') $('#tatFilterInput').val('');
        else if (name === 'categoryId') $('#categoryIdInput').val('');
        else if (name === 'departmentId') $('#departmentIdInput').val('');
        _skipAutoSubmit = false;
        $('#filterForm').submit();
    }

    function refreshDashboard() { location.reload(); }

    function openDetailedView() {
        var params = buildFilterParams();
        window.location.href = contextPath + '/views/replacement/dashboard/events' + (params ? '?' + params : '');
    }

    function viewDetails(type) {
        _skipAutoSubmit = true;
        $('#viewFilter').val(type);
        _skipAutoSubmit = false;
        $('#filterForm').submit();
    }

    function showPrintersModal(reqId, count) {
        $('#printersModal .modal-title').html('<i class="fas fa-print mr-2"></i>Printer Details - Request #' + reqId + ' (' + count + ' printer' + (count > 1 ? 's' : '') + ')');
        $('#printersModalContent').html('<div class="text-center py-4"><div class="spinner-border text-primary"></div></div>');
        $('#printersModal').modal('show');

        $.ajax({
            url: contextPath + '/views/replacement/dashboard/printers',
            data: { reqId: reqId },
            success: function(r) {
                if (r.success && r.data) {
                    var html = '<table class="table table-bordered table-sm"><thead class="thead-light"><tr><th>#</th><th>Existing Printer</th><th>Serial No.</th><th>New Printer Model</th><th>City / Location</th><th>Comments</th></tr></thead><tbody>';
                    if (r.data.length === 0) {
                        html += '<tr><td colspan="6" class="text-center text-muted">No printers found</td></tr>';
                    } else {
                        r.data.forEach(function(p, i) {
                            html += '<tr>';
                            html += '<td class="text-center">' + (i+1) + '</td>';
                            html += '<td><strong>' + (p.existingModelName || 'N/A') + '</strong></td>';
                            html += '<td><code>' + (p.existingSerial || 'N/A') + '</code></td>';
                            html += '<td>' + (p.newModelSelectedText || p.newModelSelectedName || 'TBD') + '</td>';
                            html += '<td>' + (p.clientCity || '') + (p.clientBranch ? ' / ' + p.clientBranch : '') + '</td>';
                            html += '<td>' + (p.recommendedComments || '-') + '</td>';
                            html += '</tr>';
                        });
                    }
                    html += '</tbody></table>';
                    $('#printersModalContent').html(html);
                } else {
                    $('#printersModalContent').html('<div class="alert alert-danger">' + (r.message || 'Error loading data') + '</div>');
                }
            },
            error: function() {
                $('#printersModalContent').html('<div class="alert alert-danger">Failed to load printer details</div>');
            }
        });
    }

    /**
     * Build query string from all current filter values
     */
    function buildFilterParams() {
        var params = [];
        var fromDate = document.getElementById('fromDate').value;
        var toDate = document.getElementById('toDate').value;
        var status = document.getElementById('status').value;
        var stageId = document.getElementById('stageId').value;
        var requesterId = document.getElementById('requesterId').value;
        var accountManagerId = document.getElementById('accountManagerId').value;
        var ownerId = document.getElementById('ownerId').value;
        var view = document.getElementById('viewFilter').value;
        var tatFilter = document.getElementById('tatFilterInput').value;
        var categoryId = document.getElementById('categoryIdInput').value;
        var departmentId = document.getElementById('departmentIdInput').value;

        if (fromDate) params.push('fromDate=' + encodeURIComponent(fromDate));
        if (toDate) params.push('toDate=' + encodeURIComponent(toDate));
        if (status) params.push('status=' + encodeURIComponent(status));
        if (stageId) params.push('stageId=' + encodeURIComponent(stageId));
        if (requesterId) params.push('requesterId=' + encodeURIComponent(requesterId));
        if (accountManagerId) params.push('accountManagerId=' + encodeURIComponent(accountManagerId));
        if (ownerId) params.push('ownerId=' + encodeURIComponent(ownerId));
        if (view) params.push('view=' + encodeURIComponent(view));
        if (tatFilter) params.push('tatFilter=' + encodeURIComponent(tatFilter));
        if (categoryId) params.push('categoryId=' + encodeURIComponent(categoryId));
        if (departmentId) params.push('departmentId=' + encodeURIComponent(departmentId));

        return params.join('&');
    }

    function exportData(format) {
        var params = buildFilterParams();
        window.location.href = contextPath + '/views/replacement/dashboard/export?format=' + format + (params ? '&' + params : '');
    }

    function goToPage(page) {
        var form = document.getElementById('filterForm');
        // Update the hidden page input
        var pageInput = form.querySelector('input[name="page"]');
        pageInput.value = page;
        form.submit();
    }

    function filterByStage(stageId) {
        // Apply stage filter to dashboard by setting the stageId dropdown and submitting
        document.getElementById('stageId').value = stageId;
        document.getElementById('filterForm').submit();
    }

    function filterByStageAndTat(stageId, tatFilter) {
        // Apply stage + TAT filter to dashboard
        document.getElementById('stageId').value = stageId;
        document.getElementById('tatFilterInput').value = tatFilter;
        document.getElementById('filterForm').submit();
    }

    function drillDownCategory(categoryId) {
        var params = buildFilterParams();
        window.location.href = contextPath + '/views/replacement/dashboard?categoryId=' + categoryId + (params ? '&' + params : '');
    }
</script>

<%@ include file="../common/footer.jsp" %>
