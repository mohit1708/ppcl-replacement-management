<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Detailed Dashboard Overview" scope="request"/>
<%@ include file="../common/header.jsp" %>
<%@ include file="../common/sidebar.jsp" %>

<div class="col-md-10 py-4">

    <!-- Page Header -->
    <div class="d-flex justify-content-between align-items-center mb-3">
        <div>
            <a href="${pageContext.request.contextPath}/views/replacement/dashboard" class="text-muted small"><i class="fas fa-arrow-left mr-1"></i> Back to Dashboard</a>
            <h1 class="h3 mb-0"><i class="fas fa-clipboard-list mr-2"></i>Detailed Dashboard Overview</h1>
        </div>
        <span class="badge badge-secondary p-2"><i class="fas fa-list-alt mr-1"></i> <strong>${totalCount}</strong> Events</span>
    </div>

    <!-- Filters -->
    <div class="card mb-3">
        <div class="card-body">
            <form id="filterForm" method="GET" action="${pageContext.request.contextPath}/views/replacement/dashboard/events">
                <div class="row align-items-end">
                    <div class="col-md-1">
                        <label class="small font-weight-bold">Request ID</label>
                        <input type="number" class="form-control form-control-sm" name="eventRequestId" placeholder="ID" value="${filters.eventRequestId}">
                    </div>
                    <div class="col-md-2">
                        <label class="small font-weight-bold">From Date</label>
                        <input type="date" class="form-control form-control-sm" name="fromDate" value="${filters.fromDate}">
                    </div>
                    <div class="col-md-2">
                        <label class="small font-weight-bold">To Date</label>
                        <input type="date" class="form-control form-control-sm" name="toDate" value="${filters.toDate}">
                    </div>
                    <div class="col-md-2">
                        <label class="small font-weight-bold">Stage</label>
                        <select class="form-control form-control-sm" name="stageId">
                            <option value="">All Stages</option>
                            <c:forEach items="${stages}" var="stage">
                                <option value="${stage.stageId}" ${filters.stageId == stage.stageId ? 'selected' : ''}>${stage.stageDescription}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="col-md-2">
                        <label class="small font-weight-bold">User</label>
                        <select class="form-control form-control-sm" name="ownerId">
                            <option value="">All Users</option>
                            <c:forEach items="${owners}" var="owner">
                                <option value="${owner.ownerId}" ${filters.currentOwnerId == owner.ownerId ? 'selected' : ''}>${owner.ownerName}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="col-md-1">
                        <label class="small font-weight-bold">TAT</label>
                        <select class="form-control form-control-sm" name="tatFilter">
                            <option value="">All</option>
                            <option value="within" ${filters.tatFilter == 'within' ? 'selected' : ''}>Within</option>
                            <option value="beyond" ${filters.tatFilter == 'beyond' ? 'selected' : ''}>Beyond</option>
                        </select>
                    </div>
                    <div class="col-md-2">
                        <button type="submit" class="btn btn-primary btn-sm"><i class="fas fa-search mr-1"></i> Apply</button>
                        <a href="${pageContext.request.contextPath}/views/replacement/dashboard/events" class="btn btn-outline-secondary btn-sm"><i class="fas fa-times mr-1"></i> Clear</a>
                    </div>
                </div>
                <input type="hidden" name="departmentId" id="departmentId" value="${filters.departmentId}">

                <!-- Active Filters -->
                <c:if test="${not empty filters.eventRequestId || not empty filters.stageId || not empty filters.currentOwnerId || not empty filters.departmentId || not empty filters.tatFilter}">
                    <div class="mt-2 pt-2 border-top">
                        <span class="text-muted small mr-2">Active:</span>
                        <c:if test="${not empty filters.eventRequestId}">
                            <span class="badge badge-primary mr-1"><i class="fas fa-hashtag"></i> Request: ${filters.eventRequestId} <i class="fas fa-times ml-1" style="cursor:pointer" onclick="removeFilter('eventRequestId')"></i></span>
                        </c:if>
                        <c:if test="${not empty filters.stageId}">
                            <span class="badge badge-primary mr-1"><i class="fas fa-layer-group"></i> ${stageName} <i class="fas fa-times ml-1" style="cursor:pointer" onclick="removeFilter('stageId')"></i></span>
                        </c:if>
                        <c:if test="${not empty filters.currentOwnerId}">
                            <span class="badge badge-primary mr-1"><i class="fas fa-user"></i> ${ownerName} <i class="fas fa-times ml-1" style="cursor:pointer" onclick="removeFilter('ownerId')"></i></span>
                        </c:if>
                        <c:if test="${not empty filters.departmentId}">
                            <span class="badge badge-info mr-1"><i class="fas fa-building"></i> ${departmentName} <i class="fas fa-times ml-1" style="cursor:pointer" onclick="removeFilter('departmentId')"></i></span>
                        </c:if>
                        <c:if test="${not empty filters.tatFilter}">
                            <span class="badge ${filters.tatFilter == 'within' ? 'badge-success' : 'badge-danger'} mr-1"><i class="fas fa-clock"></i> ${filters.tatFilter == 'within' ? 'Within TAT' : 'Beyond TAT'} <i class="fas fa-times ml-1" style="cursor:pointer" onclick="removeFilter('tatFilter')"></i></span>
                        </c:if>
                    </div>
                </c:if>
            </form>
        </div>
    </div>

    <!-- Results Table -->
    <div class="card">
        <div class="card-header d-flex justify-content-between align-items-center">
            <div><i class="fas fa-table mr-2"></i> Showing <strong>${fn:length(events)}</strong> of <strong>${totalCount}</strong> events</div>
            <a href="${pageContext.request.contextPath}/views/replacement/dashboard" class="btn btn-sm btn-outline-primary"><i class="fas fa-chart-bar mr-1"></i> View Summary</a>
        </div>

        <c:choose>
            <c:when test="${empty events}">
                <div class="card-body text-center py-5 text-muted">
                    <i class="fas fa-inbox fa-3x mb-3"></i>
                    <p>No events found matching your filters.</p>
                    <a href="${pageContext.request.contextPath}/views/replacement/dashboard/events" class="btn btn-outline-primary btn-sm">Clear Filters</a>
                </div>
            </c:when>
            <c:otherwise>
                <div class="table-responsive">
                    <table id="eventsTable" class="table table-hover table-striped mb-0">
                        <thead class="thead-light">
                        <tr>
                            <th>Request ID</th>
                            <th>Client</th>
                            <th>Stage</th>
                            <th>Current Owner</th>
                            <th>Department</th>
                            <th class="text-center">TAT %</th>
                            <th class="text-center">TAT Status</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${events}" var="event">
                            <tr>
                                <td><a href="${pageContext.request.contextPath}/views/replacement/request?action=view&id=${event.replacementRequestId}" class="font-weight-bold text-primary">${event.replacementRequestId}</a></td>
                                <td><div>${event.clientName}</div><small class="text-muted">${event.clientCity}</small></td>
                                <td><span class="badge badge-secondary">${event.stageDescription}</span></td>
                                <td>${event.ownerName}</td>
                                <td>${event.departmentName}</td>
                                <td class="text-center"><span class="${event.withinTat ? 'text-success font-weight-bold' : 'text-danger font-weight-bold'}">${event.formattedTatPercentage}</span></td>
                                <td class="text-center"><span class="badge ${event.tatBadgeClass}">${event.tatStatus}</span></td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- Summary Cards: Stage Wise + Owner Wise + Department Wise -->
    <div class="row mt-3">
        <!-- Stage Wise -->
        <div class="col-lg-4">
            <div class="card shadow h-100">
                <div class="card-header py-2">
                    <h6 class="m-0 font-weight-bold text-primary"><i class="fas fa-layer-group mr-2"></i>Stage Wise</h6>
                </div>
                <div class="card-body p-0" style="max-height:300px;overflow-y:auto">
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
                        <c:forEach items="${stageSummary}" var="stage">
                            <tr>
                                <td><a href="javascript:void(0)" onclick="applyFilter('stageId', ${stage.stageId})" class="text-decoration-none">${stage.stageDescription}</a></td>
                                <td class="text-center"><span class="badge badge-success" style="min-width:30px;cursor:pointer" onclick="applyFilter('stageId', ${stage.stageId}, 'within')">${stage.withinTatCount}</span></td>
                                <td class="text-center"><span class="badge badge-danger" style="min-width:30px;cursor:pointer" onclick="applyFilter('stageId', ${stage.stageId}, 'beyond')">${stage.beyondTatCount}</span></td>
                                <td class="text-center"><strong style="cursor:pointer" onclick="applyFilter('stageId', ${stage.stageId})">${stage.totalCount}</strong></td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty stageSummary}"><tr><td colspan="4" class="text-center text-muted py-3">No data</td></tr></c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <!-- Owner Wise -->
        <div class="col-lg-4">
            <div class="card shadow h-100">
                <div class="card-header py-2">
                    <h6 class="m-0 font-weight-bold text-info"><i class="fas fa-users mr-2"></i>Owner Wise</h6>
                </div>
                <div class="card-body p-0" style="max-height:300px;overflow-y:auto">
                    <table class="table table-sm table-hover mb-0">
                        <thead class="thead-light">
                        <tr>
                            <th>Owner</th>
                            <th class="text-center text-success">Within</th>
                            <th class="text-center text-danger">Beyond</th>
                            <th class="text-center">Total</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${ownerSummary}" var="owner">
                            <tr>
                                <td><a href="javascript:void(0)" onclick="applyFilter('ownerId', ${owner.ownerId})" class="text-decoration-none">${owner.ownerName}</a></td>
                                <td class="text-center"><span class="badge badge-success" style="min-width:30px;cursor:pointer" onclick="applyFilter('ownerId', ${owner.ownerId}, 'within')">${owner.withinTatCount}</span></td>
                                <td class="text-center"><span class="badge badge-danger" style="min-width:30px;cursor:pointer" onclick="applyFilter('ownerId', ${owner.ownerId}, 'beyond')">${owner.beyondTatCount}</span></td>
                                <td class="text-center"><strong style="cursor:pointer" onclick="applyFilter('ownerId', ${owner.ownerId})">${owner.totalCount}</strong></td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty ownerSummary}"><tr><td colspan="4" class="text-center text-muted py-3">No data</td></tr></c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <!-- Department Wise -->
        <div class="col-lg-4">
            <div class="card shadow h-100">
                <div class="card-header py-2">
                    <h6 class="m-0 font-weight-bold text-warning"><i class="fas fa-building mr-2"></i>Department Wise</h6>
                </div>
                <div class="card-body p-0" style="max-height:300px;overflow-y:auto">
                    <table class="table table-sm table-hover mb-0">
                        <thead class="thead-light">
                        <tr>
                            <th>Department</th>
                            <th class="text-center text-success">Within</th>
                            <th class="text-center text-danger">Beyond</th>
                            <th class="text-center">Total</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${departmentSummary}" var="dept">
                            <tr>
                                <td><a href="javascript:void(0)" onclick="applyFilter('departmentId', ${dept.departmentId})" class="text-decoration-none">${dept.departmentName}</a></td>
                                <td class="text-center"><span class="badge badge-success" style="min-width:30px;cursor:pointer" onclick="applyFilter('departmentId', ${dept.departmentId}, 'within')">${dept.withinTatCount}</span></td>
                                <td class="text-center"><span class="badge badge-danger" style="min-width:30px;cursor:pointer" onclick="applyFilter('departmentId', ${dept.departmentId}, 'beyond')">${dept.beyondTatCount}</span></td>
                                <td class="text-center"><strong style="cursor:pointer" onclick="applyFilter('departmentId', ${dept.departmentId})">${dept.totalCount}</strong></td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty departmentSummary}"><tr><td colspan="4" class="text-center text-muted py-3">No data</td></tr></c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>

</div>

<script>
    $(document).ready(function() {
        $('#eventsTable').DataTable({
            paging: true,
            pageLength: 25,
            lengthMenu: [[10, 25, 50, 100], [10, 25, 50, 100]],
            ordering: true,
            order: [[0, 'desc']],
            info: true,
            searching: true,
            language: { search: "_INPUT_", searchPlaceholder: "Search..." },
            columnDefs: [{ orderable: false, targets: [6] }]
        });
    });

    function applyFilter(filterName, filterValue, tatFilter) {
        var form = document.getElementById('filterForm');
        var input = form.querySelector('[name="' + filterName + '"]');
        if (input) {
            if (input.tagName === 'SELECT') input.value = filterValue;
            else input.value = filterValue;
        }
        if (tatFilter) {
            var tatInput = form.querySelector('[name="tatFilter"]');
            if (tatInput) tatInput.value = tatFilter;
        }
        form.submit();
    }

    function removeFilter(filterName) {
        var form = document.getElementById('filterForm');
        var input = form.querySelector('[name="' + filterName + '"]');
        if (input) {
            if (input.tagName === 'SELECT') input.selectedIndex = 0;
            else input.value = '';
        }
        form.submit();
    }
</script>

<%@ include file="../common/footer.jsp" %>
