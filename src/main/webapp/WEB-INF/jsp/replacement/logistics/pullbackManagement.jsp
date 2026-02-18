<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="Pullback Management - Logistics" scope="request"/>
<%@ include file="../common/header.jsp" %>
<%@ include file="../common/sidebar.jsp" %>

<div class="main-content-inner">
    <div class="page-content">

<div class="py-4" style="max-width: 100%; overflow-x: hidden;">
    <h2 class="mb-4">
        <i class="fas fa-truck-loading"></i> Printer Pullback Management
    </h2>

    <!-- Alerts -->
    <c:if test="${not empty error}">
        <div class="alert alert-danger alert-dismissible fade show">
            <i class="fas fa-exclamation-triangle"></i>
            <strong>Error:</strong> ${error}
            <button type="button" class="close" data-dismiss="alert">&times;</button>
        </div>
    </c:if>
    <c:if test="${not empty success}">
        <div class="alert alert-success alert-dismissible fade show">
            <i class="fas fa-check-circle"></i> ${success}
            <button type="button" class="close" data-dismiss="alert">&times;</button>
        </div>
    </c:if>

    <!-- Progress Indicator -->
    <div class="card mb-4 bg-light">
        <div class="card-body p-3">
            <div class="row text-center">
                <div class="col step-indicator active" id="step1Indicator">
                    <div class="step-number">1</div>
                    <small>Filter</small>
                </div>
                <div class="col step-indicator active" id="step2Indicator">
                    <div class="step-number">2</div>
                    <small>Review</small>
                </div>
                <div class="col step-indicator" id="step3Indicator">
                    <div class="step-number">3</div>
                    <small>Receive</small>
                </div>
                <div class="col step-indicator" id="step4Indicator">
                    <div class="step-number">4</div>
                    <small>Verify</small>
                </div>
                <div class="col step-indicator" id="step5Indicator">
                    <div class="step-number">5</div>
                    <small>Credit Note</small>
                </div>
            </div>
        </div>
    </div>

    <!-- ============================================================ -->
    <!-- SECTION 1: FILTER -->
    <!-- ============================================================ -->
    <div class="card mb-4" id="filterCard">
        <div class="card-header bg-primary text-white">
            <h5 class="mb-0">
                <i class="fas fa-filter"></i>
                Step 1: Filter Pullback Records
            </h5>
        </div>
        <div class="card-body">
            <div class="alert alert-info">
                <i class="fas fa-info-circle"></i>
                Use the filters below to narrow down pullback records by request ID, status, or date range.
            </div>
            <form method="get" id="filterForm">
                <div class="row">
                    <div class="col-md-2">
                        <label class="small font-weight-bold mb-1" for="filterReqId">
                            Replacement ID
                        </label>
                        <input type="number" class="form-control form-control-sm" name="reqId" id="filterReqId"
                               value="${param.reqId}" placeholder="Enter Request ID">
                    </div>
                    <div class="col-md-2">
                        <label class="small font-weight-bold mb-1" for="filterStatus">
                            Status
                        </label>
                        <select class="form-control form-control-sm" name="status" id="filterStatus">
                            <option value="">All Statuses</option>
                            <option value="1" ${param.status == '1' ? 'selected' : ''}>To Be Picked</option>
                            <option value="2" ${param.status == '2' ? 'selected' : ''}>To Be Dispatched</option>
                            <option value="3" ${param.status == '3' ? 'selected' : ''}>In Transit</option>
                            <option value="4" ${param.status == '4' ? 'selected' : ''}>Pending Submission To Inventory</option>
                            <option value="5" ${param.status == '5' ? 'selected' : ''}>QC Pending</option>
                            <option value="6" ${param.status == '6' ? 'selected' : ''}>QC Done</option>
                        </select>
                    </div>
                    <div class="col-md-3">
                        <label class="small font-weight-bold mb-1" for="filterDateFrom">
                            Date From
                        </label>
                        <input type="date" class="form-control form-control-sm" name="dateFrom" id="filterDateFrom"
                               value="${param.dateFrom}">
                    </div>
                    <div class="col-md-3">
                        <label class="small font-weight-bold mb-1" for="filterDateTo">
                            Date To
                        </label>
                        <input type="date" class="form-control form-control-sm" name="dateTo" id="filterDateTo"
                               value="${param.dateTo}">
                    </div>
                    <div class="col-md-2 d-flex align-items-end">
                        <button type="button" class="btn btn-outline-secondary btn-sm mr-2" onclick="clearFilters()">
                            <i class="fas fa-times"></i> Clear
                        </button>
                        <button type="submit" class="btn btn-primary btn-sm">
                            <i class="fas fa-search"></i> Apply
                        </button>
                    </div>
                </div>
            </form>
        </div>
    </div>

    <!-- ============================================================ -->
    <!-- SECTION 2: PULLBACK RECORDS -->
    <!-- ============================================================ -->
    <div class="card mb-4" id="recordsCard">
        <div class="card-header bg-info text-white d-flex justify-content-between align-items-center">
            <h5 class="mb-0">
                <i class="fas fa-clipboard-list"></i>
                Step 2: Pullback Records
            </h5>
            <span class="badge badge-light px-3 py-2" id="recordCount">0 records</span>
        </div>
        <div class="card-body">
            <!-- Status Tabs -->
            <ul class="nav nav-tabs mb-3" id="pullbackStatusTabs" role="tablist">
                <li class="nav-item">
                    <a class="nav-link active" id="pullback-all-tab" data-toggle="tab" href="#pullback-all" role="tab">
                        <i class="fas fa-folder-open text-primary"></i> All
                        <span class="badge badge-primary ml-1" id="pullbackAllCount">0</span>
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" data-toggle="tab" href="#pullback-tobepicked" role="tab">
                        <i class="fas fa-hand-paper text-secondary"></i> To Be Picked
                        <span class="badge badge-secondary ml-1" id="pullbackToBePickedCount">0</span>
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" data-toggle="tab" href="#pullback-tobedispatched" role="tab">
                        <i class="fas fa-truck text-warning"></i> To Be Dispatched
                        <span class="badge badge-warning ml-1" id="pullbackToBeDispatchedCount">0</span>
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" data-toggle="tab" href="#pullback-intransit" role="tab">
                        <i class="fas fa-shipping-fast text-info"></i> In Transit
                        <span class="badge badge-info ml-1" id="pullbackInTransitCount">0</span>
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" data-toggle="tab" href="#pullback-pendinginventory" role="tab">
                        <i class="fas fa-warehouse text-danger"></i> Pending Submission To Inventory
                        <span class="badge badge-danger ml-1" id="pullbackPendingInventoryCount">0</span>
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" data-toggle="tab" href="#pullback-qcpending" role="tab">
                        <i class="fas fa-clipboard-check text-primary"></i> QC Pending
                        <span class="badge badge-primary ml-1" id="pullbackQcPendingCount">0</span>
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" data-toggle="tab" href="#pullback-qcdone" role="tab">
                        <i class="fas fa-check-circle text-success"></i> QC Done
                        <span class="badge badge-success ml-1" id="pullbackQcDoneCount">0</span>
                    </a>
                </li>
            </ul>

            <div class="tab-content" id="pullbackTabContent">
                <div class="tab-pane fade show active" id="pullback-all" role="tabpanel">
                    <div class="table-responsive">
                        <table class="table table-hover table-bordered" id="pullbackTableAll">
                            <thead class="thead-light">
                                <tr>
                                    <th>Request ID</th>
                                    <th>Client</th>
                                    <th>Serial</th>
                                    <th>Model</th>
                                    <th>Pickup By</th>
                                    <th>Pickup Status</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody id="pullbackTableBodyAll"></tbody>
                        </table>
                    </div>
                </div>
                <div class="tab-pane fade" id="pullback-tobepicked" role="tabpanel">
                    <div class="table-responsive">
                        <table class="table table-hover table-bordered" id="pullbackTableToBePicked">
                            <thead class="thead-light">
                                <tr><th>Request ID</th><th>Client</th><th>Serial</th><th>Model</th><th>Pickup By</th><th>Pickup Status</th><th>Actions</th></tr>
                            </thead>
                            <tbody id="pullbackTableBodyToBePicked"></tbody>
                        </table>
                    </div>
                </div>
                <div class="tab-pane fade" id="pullback-tobedispatched" role="tabpanel">
                    <div class="table-responsive">
                        <table class="table table-hover table-bordered" id="pullbackTableToBeDispatched">
                            <thead class="thead-light">
                                <tr><th>Request ID</th><th>Client</th><th>Serial</th><th>Model</th><th>Pickup By</th><th>Pickup Status</th><th>Actions</th></tr>
                            </thead>
                            <tbody id="pullbackTableBodyToBeDispatched"></tbody>
                        </table>
                    </div>
                </div>
                <div class="tab-pane fade" id="pullback-intransit" role="tabpanel">
                    <div class="table-responsive">
                        <table class="table table-hover table-bordered" id="pullbackTableInTransit">
                            <thead class="thead-light">
                                <tr><th>Request ID</th><th>Client</th><th>Serial</th><th>Model</th><th>Pickup By</th><th>Pickup Status</th><th>Actions</th></tr>
                            </thead>
                            <tbody id="pullbackTableBodyInTransit"></tbody>
                        </table>
                    </div>
                </div>
                <div class="tab-pane fade" id="pullback-pendinginventory" role="tabpanel">
                    <div class="table-responsive">
                        <table class="table table-hover table-bordered" id="pullbackTablePendingInventory">
                            <thead class="thead-light">
                                <tr><th>Request ID</th><th>Client</th><th>Serial</th><th>Model</th><th>Pickup By</th><th>Pickup Status</th><th>Actions</th></tr>
                            </thead>
                            <tbody id="pullbackTableBodyPendingInventory"></tbody>
                        </table>
                    </div>
                </div>
                <div class="tab-pane fade" id="pullback-qcpending" role="tabpanel">
                    <div class="table-responsive">
                        <table class="table table-hover table-bordered" id="pullbackTableQcPending">
                            <thead class="thead-light">
                                <tr><th>Request ID</th><th>Client</th><th>Serial</th><th>Model</th><th>Pickup By</th><th>Pickup Status</th><th>Actions</th></tr>
                            </thead>
                            <tbody id="pullbackTableBodyQcPending"></tbody>
                        </table>
                    </div>
                </div>
                <div class="tab-pane fade" id="pullback-qcdone" role="tabpanel">
                    <div class="table-responsive">
                        <table class="table table-hover table-bordered" id="pullbackTableQcDone">
                            <thead class="thead-light">
                                <tr><th>Request ID</th><th>Client</th><th>Serial</th><th>Model</th><th>Pickup By</th><th>Pickup Status</th><th>Actions</th></tr>
                            </thead>
                            <tbody id="pullbackTableBodyQcDone"></tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>

</div>

    </div>
</div>


<%@ include file="../common/footer.jsp" %>

<!-- Pickup Checklist Modal -->
    <div class="modal fade" id="pickupChecklistModal" tabindex="-1">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header bg-primary text-white">
                    <h5 class="modal-title"><i class="fas fa-clipboard-list mr-2"></i>Pickup Checklist</h5>
                    <button type="button" class="close text-white" data-dismiss="modal">&times;</button>
                </div>
                <form id="pickupChecklistForm">
                    <input type="hidden" id="pickupReqId">
                    <input type="hidden" id="pickupSerialNo">
                    <div class="modal-body">
                        <div class="row mb-3">
                            <div class="col-6">
                                <strong>Replacement ID:</strong> <span id="pickupReqIdDisplay">-</span>
                            </div>
                            <div class="col-6">
                                <strong>Serial No:</strong> <span id="pickupSerialNoDisplay">-</span>
                            </div>
                        </div>
                        <h6 class="border-bottom pb-2 mb-3">Items Checklist</h6>
                        <div class="form-group">
                            <div class="custom-control custom-checkbox">
                                <input type="checkbox" class="custom-control-input" id="pickupPowerCable">
                                <label class="custom-control-label" for="pickupPowerCable">Power Cable</label>
                            </div>
                        </div>
                        <div class="form-group">
                            <div class="custom-control custom-checkbox">
                                <input type="checkbox" class="custom-control-input" id="pickupPrinter">
                                <label class="custom-control-label" for="pickupPrinter">Printer</label>
                            </div>
                        </div>
                        <div class="form-group">
                            <div class="custom-control custom-checkbox">
                                <input type="checkbox" class="custom-control-input" id="pickupLanCable">
                                <label class="custom-control-label" for="pickupLanCable">LAN Cable</label>
                            </div>
                        </div>
                        <div class="form-group">
                            <div class="custom-control custom-checkbox">
                                <input type="checkbox" class="custom-control-input" id="pickupTray">
                                <label class="custom-control-label" for="pickupTray">Tray</label>
                            </div>
                        </div>
                        <h6 class="border-bottom pb-2 mb-3 mt-4">Cartridge Details</h6>
                        <div class="form-group">
                            <label class="font-weight-bold">Empty Cartridges</label>
                            <input type="number" class="form-control" id="pickupEmptyCartridges" min="0" placeholder="Enter count" required>
                        </div>
                        <div class="form-group">
                            <label class="font-weight-bold">Unused Cartridges</label>
                            <input type="number" class="form-control" id="pickupUnusedCartridge" min="0" placeholder="Enter count" required>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                        <button type="submit" class="btn btn-primary">
                            <i class="fas fa-paper-plane mr-1"></i> Submit Checklist
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- View Cartridges Modal -->
    <div class="modal fade" id="cartridgesModal" tabindex="-1">
        <div class="modal-dialog modal-dialog-centered modal-md">
            <div class="modal-content">
                <div class="modal-header bg-info text-white">
                    <h5 class="modal-title"><i class="fas fa-boxes mr-2"></i>Cartridge Pickup Details</h5>
                    <button type="button" class="close text-white" data-dismiss="modal">&times;</button>
                </div>
                <div class="modal-body">
                    <div class="cart-meta-grid">
                        <div class="cart-meta-item">
                            <small class="text-muted d-block">Replacement ID</small>
                            <strong id="cartReqId">-</strong>
                        </div>
                        <div class="cart-meta-item">
                            <small class="text-muted d-block">Serial No</small>
                            <strong id="cartSerialNo">-</strong>
                        </div>
                    </div>

                    <div class="cart-count-grid mt-3">
                        <div class="cart-count-card empty">
                            <div class="cart-count-head">
                                <i class="fas fa-fill-drip mr-2"></i>
                                <span>Empty Cartridges</span>
                            </div>
                            <div class="cart-count-value" id="emptyCartridges">0</div>
                        </div>
                        <div class="cart-count-card unused">
                            <div class="cart-count-head">
                                <i class="fas fa-fill mr-2"></i>
                                <span>Unused Cartridges</span>
                            </div>
                            <div class="cart-count-value" id="unusedCartridges">0</div>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
                </div>
            </div>
        </div>
    </div>

    <!-- Mark Received Modal -->
    <div class="modal fade" id="receivedModal" tabindex="-1">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header bg-success text-white">
                    <h5 class="modal-title"><i class="fas fa-check-circle mr-2"></i>Mark Printer Received</h5>
                    <button type="button" class="close text-white" data-dismiss="modal">&times;</button>
                </div>
                <form id="receivedForm">
                    <input type="hidden" name="pullbackId" id="receivedPullbackId">
                    <input type="hidden" name="action" value="markReceived">
                    <div class="modal-body">
                        <div class="alert alert-info">
                            <i class="fas fa-info-circle mr-2"></i>
                            Confirm that the printer has been received at the warehouse.
                        </div>
                        <div class="mb-3">
                            <strong>Replacement ID:</strong> <span id="recReqId">-</span><br>
                            <strong>Serial No:</strong> <span id="recSerialNo">-</span>
                        </div>
                        <div class="form-group">
                            <label><strong>Received By</strong> <span class="text-danger">*</span></label>
                            <input type="text" class="form-control" name="receivedBy" id="receivedBy" 
                                   value="${sessionScope.userName}" required>
                        </div>
                        <div class="form-group">
                            <label><strong>Comments</strong></label>
                            <textarea class="form-control" name="comments" rows="2" 
                                      placeholder="Any remarks..."></textarea>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                        <button type="submit" class="btn btn-success">
                            <i class="fas fa-check mr-1"></i> Confirm Received
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- Verify Cartridge Modal -->
    <div class="modal fade" id="verifyModal" tabindex="-1">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header bg-primary text-white">
                    <h5 class="modal-title"><i class="fas fa-clipboard-check mr-2"></i>Verify Cartridge Quantity & Condition</h5>
                    <button type="button" class="close text-white" data-dismiss="modal">&times;</button>
                </div>
                <form id="verifyForm">
                    <input type="hidden" name="pullbackId" id="verifyPullbackId">
                    <input type="hidden" name="action" value="verifyCartridge">
                    <div class="modal-body">
                        <div class="row mb-3">
                            <div class="col-6">
                                <strong>Replacement ID:</strong> <span id="verReqId">-</span>
                            </div>
                            <div class="col-6">
                                <strong>Serial No:</strong> <span id="verSerialNo">-</span>
                            </div>
                        </div>

                        <h6 class="border-bottom pb-2 mb-3">Checklist Verification</h6>
                        <div class="row">
                            <div class="col-md-6">
                                <div class="form-group">
                                    <div class="custom-control custom-checkbox">
                                        <input type="checkbox" class="custom-control-input" id="chkPrinter" name="printerReceived" value="1">
                                        <label class="custom-control-label" for="chkPrinter">Printer Received</label>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <div class="custom-control custom-checkbox">
                                        <input type="checkbox" class="custom-control-input" id="chkPowerCable" name="powerCableReceived" value="1">
                                        <label class="custom-control-label" for="chkPowerCable">Power Cable Received</label>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="form-group">
                                    <div class="custom-control custom-checkbox">
                                        <input type="checkbox" class="custom-control-input" id="chkLanCable" name="lanCableReceived" value="1">
                                        <label class="custom-control-label" for="chkLanCable">LAN Cable Received</label>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <div class="custom-control custom-checkbox">
                                        <input type="checkbox" class="custom-control-input" id="chkTray" name="trayReceived" value="1">
                                        <label class="custom-control-label" for="chkTray">Tray Received</label>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <h6 class="border-bottom pb-2 mb-3 mt-4">Cartridge Verification</h6>
                        <div class="row">
                            <div class="col-md-6">
                                <div class="form-group">
                                    <label>Expected Empty Cartridges</label>
                                    <input type="number" class="form-control" id="expectedEmpty" readonly>
                                </div>
                                <div class="form-group">
                                    <label>Actual Empty Cartridges Received</label>
                                    <input type="number" class="form-control" name="actualEmptyCartridges" id="actualEmpty" min="0" required>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="form-group">
                                    <label>Expected Unused Cartridges</label>
                                    <input type="number" class="form-control" id="expectedUnused" readonly>
                                </div>
                                <div class="form-group">
                                    <label>Actual Unused Cartridges Received</label>
                                    <input type="number" class="form-control" name="actualUnusedCartridges" id="actualUnused" min="0" required>
                                </div>
                            </div>
                        </div>

                        <h6 class="border-bottom pb-2 mb-3 mt-4">Condition Assessment</h6>
                        <div class="form-group">
                            <label><strong>Overall Condition</strong> <span class="text-danger">*</span></label>
                            <select class="form-control" name="condition" id="conditionSelect" required>
                                <option value="">Select Condition</option>
                                <option value="GOOD">Good - All items in acceptable condition</option>
                                <option value="MINOR_DAMAGE">Minor Damage - Some wear but functional</option>
                                <option value="DAMAGED">Damaged - Requires credit note</option>
                            </select>
                        </div>
                        <div class="form-group" id="damageDetailsGroup" style="display: none;">
                            <label><strong>Damage Details</strong> <span class="text-danger">*</span></label>
                            <textarea class="form-control" name="damageDetails" id="damageDetails" rows="3" 
                                      placeholder="Describe the damage in detail..."></textarea>
                        </div>
                        <div class="form-group">
                            <label><strong>Verification Comments</strong></label>
                            <textarea class="form-control" name="verificationComments" rows="2" 
                                      placeholder="Additional comments..."></textarea>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                        <button type="submit" class="btn btn-primary">
                            <i class="fas fa-save mr-1"></i> Save Verification
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- Vendor Dispatch Form Modal -->
    <div class="modal fade" id="vendorDispatchModal" tabindex="-1">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header bg-warning text-dark">
                    <h5 class="modal-title"><i class="fas fa-truck mr-2"></i>Dispatch Details</h5>
                    <button type="button" class="close" data-dismiss="modal">&times;</button>
                </div>
                <form id="vendorDispatchForm">
                    <input type="hidden" id="vdReqId">
                    <input type="hidden" id="vdSerialNo">
                    <div class="modal-body">
                        <div class="row mb-3">
                            <div class="col-6">
                                <strong>Replacement ID:</strong> <span id="vdReqIdDisplay">-</span>
                            </div>
                            <div class="col-6">
                                <strong>Serial No:</strong> <span id="vdSerialNoDisplay">-</span>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="font-weight-bold">Dispatch Mode <span class="text-danger">*</span></label>
                            <select class="form-control" id="vdMode" required>
                                <option value="COURIER">Courier</option>
                                <option value="TRANSPORT">Transport</option>
                            </select>
                        </div>
                        <div id="vdCourierSection">
                            <div class="form-group">
                                <label class="font-weight-bold">Courier Name <span class="text-danger">*</span></label>
                                <input type="text" class="form-control" id="vdCourierName" placeholder="e.g. BlueDart">
                            </div>
                            <div class="form-group">
                                <label class="font-weight-bold">Consignment No <span class="text-danger">*</span></label>
                                <input type="text" class="form-control" id="vdConsignmentNo" placeholder="e.g. BD123456">
                            </div>
                            <div class="form-group">
                                <label class="font-weight-bold">Destination Branch <span class="text-danger">*</span></label>
                                <input type="text" class="form-control" id="vdDestinationBranch" placeholder="e.g. MUMBAI">
                            </div>
                        </div>
                        <div id="vdTransportSection" style="display: none;">
                            <div class="form-group">
                                <label class="font-weight-bold">Transport Type <span class="text-danger">*</span></label>
                                <select class="form-control" id="vdTransportMode">
                                    <option value="">Select transport</option>
                                    <option value="BUS">Bus</option>
                                    <option value="TRAIN">Train</option>
                                    <option value="TRANSPORT">Transport</option>
                                </select>
                            </div>
                            <div class="form-group">
                                <label class="font-weight-bold">Contact Person <span class="text-danger">*</span></label>
                                <input type="text" class="form-control" id="vdContactPerson" placeholder="e.g. Ramesh">
                            </div>
                            <div class="form-group">
                                <label class="font-weight-bold">Contact Number <span class="text-danger">*</span></label>
                                <input type="text" class="form-control" id="vdContactNumber" placeholder="e.g. 9876543210">
                            </div>
                            <div class="form-group">
                                <label class="font-weight-bold">Comments <span class="text-danger">*</span></label>
                                <textarea class="form-control" id="vdComments" rows="2" placeholder="Transport details"></textarea>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-md-6">
                                <div class="form-group">
                                    <label class="font-weight-bold">Dispatch Date <span class="text-danger">*</span></label>
                                    <input type="date" class="form-control" id="vdDispatchDate" required>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="form-group">
                                    <label class="font-weight-bold">Expected Arrival Date <span class="text-danger">*</span></label>
                                    <input type="date" class="form-control" id="vdArrivalDate" required>
                                </div>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="font-weight-bold">Receipt <span class="text-danger">*</span></label>
                            <input type="file" class="form-control" id="vdReceipt" accept=".pdf,image/*" required>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                        <button type="submit" class="btn btn-warning">
                            <i class="fas fa-paper-plane mr-1"></i> Submit Dispatch
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- Dispatch Details Modal -->
    <div class="modal fade" id="dispatchModal" tabindex="-1">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header bg-warning text-dark">
                    <h5 class="modal-title"><i class="fas fa-truck mr-2"></i>Dispatch Details</h5>
                    <button type="button" class="close" data-dismiss="modal">&times;</button>
                </div>
                <div class="modal-body">
                    <div class="row mb-3">
                        <div class="col-6">
                            <strong>Replacement ID:</strong> <span id="dispReqId">-</span>
                        </div>
                        <div class="col-6">
                            <strong>Serial No:</strong> <span id="dispSerialNo">-</span>
                        </div>
                    </div>
                    <table class="table table-sm table-bordered">
                        <tr>
                            <th width="40%">Pullback Mode</th>
                            <td id="dispPullbackMode">-</td>
                        </tr>
                        <tr>
                            <th>Courier Name</th>
                            <td id="dispCourierName">-</td>
                        </tr>
                        <tr>
                            <th>Consignment No</th>
                            <td id="dispConsignmentNo">-</td>
                        </tr>
                        <tr>
                            <th>Transport Mode</th>
                            <td id="dispTransportMode">-</td>
                        </tr>
                        <tr>
                            <th>Dispatch Date</th>
                            <td id="dispDispatchDate">-</td>
                        </tr>
                        <tr>
                            <th>Expected Arrival</th>
                            <td id="dispArrivalDate">-</td>
                        </tr>
                        <tr>
                            <th>Destination Branch</th>
                            <td id="dispDestinationBranch">-</td>
                        </tr>
                        <tr>
                            <th>Contact Person</th>
                            <td id="dispContactPerson">-</td>
                        </tr>
                        <tr>
                            <th>Contact Number</th>
                            <td id="dispContactNumber">-</td>
                        </tr>
                        <tr>
                            <th>Receipt</th>
                            <td id="dispReceipt">-</td>
                        </tr>
                    </table>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
                </div>
            </div>
        </div>
    </div>

    <!-- Update QC Modal -->
    <div class="modal fade" id="qcModal" tabindex="-1">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header bg-warning text-dark">
                    <h5 class="modal-title"><i class="fas fa-clipboard-check mr-2"></i>Update QC</h5>
                    <button type="button" class="close" data-dismiss="modal">&times;</button>
                </div>
                <form id="qcForm">
                    <input type="hidden" name="pullbackId" id="qcPullbackId">
                    <input type="hidden" name="action" value="updateQc">
                    <div class="modal-body">
                        <div class="row">
                            <div class="col-md-6">
                                <h6 class="border-bottom pb-2 mb-3"><i class="fas fa-box-open mr-1"></i> Pickup Checklist</h6>
                                <table class="table table-sm table-bordered">
                                    <tr><th>Printer</th><td id="qcPrinter">-</td></tr>
                                    <tr><th>Power Cable</th><td id="qcPowerCable">-</td></tr>
                                    <tr><th>LAN Cable</th><td id="qcLanCable">-</td></tr>
                                    <tr><th>Tray</th><td id="qcTray">-</td></tr>
                                    <tr><th>Empty Cartridge</th><td id="qcEmptyCartridge">-</td></tr>
                                    <tr><th>Unused Cartridge</th><td id="qcUnusedCartridge">-</td></tr>
                                </table>
                            </div>
                            <div class="col-md-6">
                                <h6 class="border-bottom pb-2 mb-3"><i class="fas fa-clipboard-check mr-1"></i> QC Verification</h6>
                                <div class="form-group">
                                    <div class="custom-control custom-checkbox">
                                        <input type="checkbox" class="custom-control-input" id="qcPrinterQc" name="printerQc" value="1">
                                        <label class="custom-control-label" for="qcPrinterQc">Printer QC</label>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <div class="custom-control custom-checkbox">
                                        <input type="checkbox" class="custom-control-input" id="qcPowerCableQc" name="powerCableQc" value="1">
                                        <label class="custom-control-label" for="qcPowerCableQc">Power Cable QC</label>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <div class="custom-control custom-checkbox">
                                        <input type="checkbox" class="custom-control-input" id="qcLanCableQc" name="lanCableQc" value="1">
                                        <label class="custom-control-label" for="qcLanCableQc">LAN Cable QC</label>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <div class="custom-control custom-checkbox">
                                        <input type="checkbox" class="custom-control-input" id="qcTrayQc" name="trayQc" value="1">
                                        <label class="custom-control-label" for="qcTrayQc">Tray QC</label>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label class="small font-weight-bold">Empty Cartridge QC</label>
                                    <input type="number" class="form-control form-control-sm" name="emptyCartridgeQc" id="qcEmptyCartridgeQc" min="0" value="0">
                                </div>
                                <div class="form-group">
                                    <label class="small font-weight-bold">Unused Cartridge QC</label>
                                    <input type="number" class="form-control form-control-sm" name="unusedCartridgeQc" id="qcUnusedCartridgeQc" min="0" value="0">
                                </div>
                            </div>
                        </div>
                        <h6 class="border-bottom pb-2 mb-3 mt-4"><i class="fas fa-heartbeat mr-1"></i> Condition Assessment</h6>
                        <div class="form-group">
                            <label><strong>Overall Condition</strong> <span class="text-danger">*</span></label>
                            <select class="form-control" name="qcCondition" id="qcConditionSelect" required>
                                <option value="">Select Condition</option>
                                <option value="GOOD">Good - All items in acceptable condition</option>
                                <option value="MINOR_DAMAGE">Minor Damage - Some wear but functional</option>
                                <option value="DAMAGED">Damaged - Requires credit note</option>
                            </select>
                        </div>
                        <div class="form-group" id="qcDamageDetailsGroup" style="display: none;">
                            <label><strong>Damage Details</strong> <span class="text-danger">*</span></label>
                            <textarea class="form-control" name="qcDamageDetails" id="qcDamageDetails" rows="3"
                                      placeholder="Describe the damage in detail..."></textarea>
                        </div>
                        <div class="form-group">
                            <label><strong>QC Comments</strong></label>
                            <textarea class="form-control" name="qcComments" id="qcComments" rows="2"
                                      placeholder="Additional comments..."></textarea>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                        <button type="submit" class="btn btn-warning">
                            <i class="fas fa-save mr-1"></i> Save QC
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- Credit Note Modal -->
    <div class="modal fade" id="creditNoteModal" tabindex="-1">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header bg-danger text-white">
                    <h5 class="modal-title"><i class="fas fa-file-invoice-dollar mr-2"></i>Trigger Credit Note</h5>
                    <button type="button" class="close text-white" data-dismiss="modal">&times;</button>
                </div>
                <form id="creditNoteForm">
                    <input type="hidden" name="pullbackId" id="cnPullbackId">
                    <input type="hidden" name="action" value="triggerCreditNote">
                    <div class="modal-body">
                        <div class="alert alert-warning">
                            <i class="fas fa-exclamation-triangle mr-2"></i>
                            This will initiate a credit note workflow for damaged items.
                        </div>
                        <div class="mb-3">
                            <strong>Replacement ID:</strong> <span id="cnReqId">-</span><br>
                            <strong>Serial No:</strong> <span id="cnSerialNo">-</span>
                        </div>
                        <div class="form-group">
                            <label><strong>Damage Type</strong> <span class="text-danger">*</span></label>
                            <select class="form-control" name="damageType" required>
                                <option value="">Select Type</option>
                                <option value="PRINTER_DAMAGED">Printer Damaged</option>
                                <option value="MISSING_PARTS">Missing Parts/Accessories</option>
                                <option value="CARTRIDGE_MISSING">Cartridge Missing</option>
                                <option value="CARTRIDGE_DAMAGED">Cartridge Damaged</option>
                                <option value="MULTIPLE_ISSUES">Multiple Issues</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label><strong>Estimated Credit Amount (&#8377;)</strong></label>
                            <input type="number" class="form-control" name="creditAmount" min="0" step="0.01" 
                                   placeholder="Enter amount">
                        </div>
                        <div class="form-group">
                            <label><strong>Reason for Credit Note</strong> <span class="text-danger">*</span></label>
                            <textarea class="form-control" name="creditNoteReason" rows="3" 
                                      placeholder="Provide detailed reason..." required></textarea>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                        <button type="submit" class="btn btn-danger">
                            <i class="fas fa-paper-plane mr-1"></i> Submit for Credit Note
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <script>
        var contextPath = '${pageContext.request.contextPath}';

        $(document).ready(function() {
            loadPullbackData();

            $('#conditionSelect').on('change', function() {
                if ($(this).val() === 'DAMAGED') {
                    $('#damageDetailsGroup').show();
                    $('#damageDetails').prop('required', true);
                } else {
                    $('#damageDetailsGroup').hide();
                    $('#damageDetails').prop('required', false);
                }
            });

            $('#qcConditionSelect').on('change', function() {
                if ($(this).val() === 'DAMAGED') {
                    $('#qcDamageDetailsGroup').show();
                    $('#qcDamageDetails').prop('required', true);
                } else {
                    $('#qcDamageDetailsGroup').hide();
                    $('#qcDamageDetails').prop('required', false);
                }
            });

            // DataTables initialized in hidden tabs can miscalculate header widths.
            // Re-adjust when a tab becomes visible.
            $('a[data-toggle="tab"]').on('shown.bs.tab', function() {
                Object.keys(dataTables).forEach(function(key) {
                    if (dataTables[key]) {
                        dataTables[key].columns.adjust().draw(false);
                    }
                });
            });
        });

        function loadPullbackData() {
            var params = new URLSearchParams(window.location.search);
            
            $.ajax({
                url: contextPath + '/api/pullback/list',
                type: 'GET',
                data: {
                    reqId: params.get('reqId') || '',
                    status: params.get('status') || '',
                    dateFrom: params.get('dateFrom') || '',
                    dateTo: params.get('dateTo') || ''
                },
                success: function(response) {
                    if (response.status === 'SUCCESS') {
                        renderTable(response.data);
                    } else {
                        showAlert('danger', response.message || 'Failed to load data');
                    }
                },
                error: function() {
                    showAlert('danger', 'Error loading pullback data');
                }
            });
        }

        var pullbackDataMap = {};
        var dataTables = {};

        var statusTabs = [
            { key: 'to_be_picked',      label: 'To Be Picked',                    bodyId: 'pullbackTableBodyToBePicked',       tableId: 'pullbackTableToBePicked',       countId: 'pullbackToBePickedCount' },
            { key: 'to_be_dispatched',   label: 'To Be Dispatched',                bodyId: 'pullbackTableBodyToBeDispatched',   tableId: 'pullbackTableToBeDispatched',   countId: 'pullbackToBeDispatchedCount' },
            { key: 'in_transit',         label: 'In transit',                       bodyId: 'pullbackTableBodyInTransit',        tableId: 'pullbackTableInTransit',        countId: 'pullbackInTransitCount' },
            { key: 'pending_inventory',  label: 'Pending Submission To Inventory',  bodyId: 'pullbackTableBodyPendingInventory', tableId: 'pullbackTablePendingInventory', countId: 'pullbackPendingInventoryCount' },
            { key: 'qc_pending',         label: 'QC Pending',                       bodyId: 'pullbackTableBodyQcPending',        tableId: 'pullbackTableQcPending',        countId: 'pullbackQcPendingCount' },
            { key: 'qc_done',            label: 'QC Done',                          bodyId: 'pullbackTableBodyQcDone',           tableId: 'pullbackTableQcDone',           countId: 'pullbackQcDoneCount' }
        ];

        function renderTable(data) {
            destroyTables();
            pullbackDataMap = {};

            var allRows = data || [];

            // Group rows by resolved status key
            var grouped = {};
            statusTabs.forEach(function(t) { grouped[t.key] = []; });
            allRows.forEach(function(item) {
                var key = resolvePickupStatus(item).key;
                if (grouped[key]) grouped[key].push(item);
            });

            // Render All table
            renderTableBody('#pullbackTableBodyAll', allRows);
            initTable('#pullbackTableAll');
            $('#recordCount').text(allRows.length + ' records');
            $('#pullbackAllCount').text(allRows.length);

            // Render each status table
            statusTabs.forEach(function(t) {
                renderTableBody('#' + t.bodyId, grouped[t.key]);
                initTable('#' + t.tableId);
                $('#' + t.countId).text(grouped[t.key].length);
            });

            setTimeout(function() {
                Object.keys(dataTables).forEach(function(key) {
                    if (dataTables[key]) dataTables[key].columns.adjust().draw(false);
                });
            }, 0);
        }

        function renderTableBody(selector, rows) {
            var tbody = $(selector);
            tbody.empty();

            if (!rows || rows.length === 0) {
                tbody.append('<tr><td colspan="7" class="text-center text-muted py-4"><i class="fas fa-inbox fa-2x mb-2 d-block"></i>No records found.</td></tr>');
                return;
            }

            rows.forEach(function(item) {
                pullbackDataMap[item.id] = item;
                var pickedByBadge = getPickedByBadge(item.pickedBy);
                var pickupStatusInfo = getPickupStatusInfo(item);

                var row = '<tr>' +
                    '<td class="text-600 text-primary-d2">REQ-' + item.replacementReqId + '</td>' +
                    '<td>' +
                        '<div class="text-95 text-600">' + (item.clientName || '-') + '</div>' +
                        '<div class="text-80 text-grey-m1">' + (item.location || '') + '</div>' +
                    '</td>' +
                    '<td><code class="text-dark-m2">' + (item.pSerialNo || '-') + '</code></td>' +
                    '<td title="' + (item.printerModelName || '-') + '"><span class="badge bgc-secondary-l2 text-secondary-d2 border-1 brc-secondary-m3 radius-1 text-truncate d-inline-block" style="max-width:100%;font-size:0.7rem;">' + (item.printerModelName || '-') + '</span></td>' +
                    '<td>' + pickedByBadge + '</td>' +
                    '<td>' + pickupStatusInfo + '</td>' +
                    '<td class="text-center">' + getActionButtons(item) + '</td>' +
                    '</tr>';
                tbody.append(row);
            });
        }

        function initTable(selector) {
            dataTables[selector] = $(selector).DataTable({
                order: [[0, 'desc']],
                pageLength: 10,
                autoWidth: false,
                scrollX: true,
                scrollCollapse: true,
                columnDefs: [
                    { orderable: false, targets: [6] },
                    { className: "align-middle", targets: "_all" }
                ],
                drawCallback: function() {
                    $('.dataTables_paginate > .pagination').addClass('pagination-info pagination-spaced');
                }
            });
        }

        function destroyTables() {
            Object.keys(dataTables).forEach(function(key) {
                if (dataTables[key]) {
                    dataTables[key].destroy(true);
                }
            });
            dataTables = {};

            var headers = '<thead class="thead-light"><tr>' +
                '<th>Request ID</th><th>Client</th><th>Serial</th><th>Model</th>' +
                '<th>Pickup By</th><th>Pickup Status</th><th>Actions</th></tr></thead>';

            // Re-create All table
            var allContainer = $('#pullback-all .table-responsive');
            allContainer.empty();
            allContainer.append('<table class="table table-hover table-bordered" id="pullbackTableAll">' +
                headers + '<tbody id="pullbackTableBodyAll"></tbody></table>');

            // Re-create each status table
            statusTabs.forEach(function(t) {
                var paneId = t.tableId.replace('pullbackTable', 'pullback-').toLowerCase().replace(/\s/g, '');
                var container = $('#' + t.tableId).closest('.table-responsive');
                if (container.length === 0) {
                    // fallback: find by tbody id
                    container = $('#' + t.bodyId).closest('.table-responsive');
                }
                if (container.length) {
                    container.empty();
                    container.append('<table class="table table-hover table-bordered" id="' + t.tableId + '">' +
                        headers + '<tbody id="' + t.bodyId + '"></tbody></table>');
                }
            });
        }

        function getPickedByBadge(pickedBy) {
            if (pickedBy === 'ENGINEER') {
                return '<span class="badge badge-md bgc-blue-l2 text-blue-d2 border-1 brc-blue-m3 radius-1">Engineer</span>';
            } else if (pickedBy === 'VENDOR') {
                return '<span class="badge badge-md bgc-orange-l2 text-orange-d2 border-1 brc-orange-m3 radius-1">Vendor</span>';
            }
            return '<span class="badge badge-md bgc-grey-l2 text-grey-d2 border-1 brc-grey-m3 radius-1">' + (pickedBy || '-') + '</span>';
        }

        function getPickupStatusInfo(item) {
            var statusMeta = resolvePickupStatus(item);
            var statusClass = {
                to_be_picked: 'bgc-grey-l2 text-grey-d2 border-1 brc-grey-m3',
                to_be_dispatched: 'bgc-warning-l2 text-warning-d2 border-1 brc-warning-m3',
                in_transit: 'bgc-primary-l2 text-primary-d2 border-1 brc-primary-m3',
                received: 'bgc-success-l2 text-success-d2 border-1 brc-success-m3',
                pending_inventory: 'bgc-orange-l2 text-orange-d2 border-1 brc-orange-m3',
                qc_pending: 'bgc-info-l2 text-info-d2 border-1 brc-info-m3',
                qc_done: 'bgc-success-l2 text-success-d2 border-1 brc-success-m3'
            };

            var html = '<span class="badge badge-md radius-1 ' + (statusClass[statusMeta.key] || statusClass.to_be_picked) + '">' +
                       statusMeta.label + '</span>';

            if (item.consignmentNo || item.dispatchDate || item.pullbackMode) {
                html += ' <button class="btn btn-outline-info btn-xs radius-1 ml-1" title="View Dispatch Details" onclick="showDispatchDetails(' + item.id + ')">' +
                        '<i class="fas fa-info-circle"></i></button>';
            }
            return html;
        }

        function resolvePickupStatus(item) {
            var statusName = ((item.uiStatus || item.statusName || '') + '').trim().toLowerCase();
            if (statusName === 'to be picked') {
                return { key: 'to_be_picked', label: 'To Be Picked' };
            }
            if (statusName === 'to be dispatched') {
                return { key: 'to_be_dispatched', label: 'To Be Dispatched' };
            }
            if (statusName === 'in transit') {
                return { key: 'in_transit', label: 'In transit' };
            }
            if (statusName === 'received') {
                return { key: 'received', label: 'Received' };
            }
            if (statusName === 'pending submission to inventory') {
                return { key: 'pending_inventory', label: 'Pending Submission To Inventory' };
            }
            if (statusName === 'qc pending') {
                return { key: 'qc_pending', label: 'QC Pending' };
            }
            if (statusName === 'qc done') {
                return { key: 'qc_done', label: 'QC Done' };
            }
            if (statusName === 'to_be_picked') {
                return { key: 'to_be_picked', label: 'To Be Picked' };
            }
            if (statusName === 'to_be_dispatched') {
                return { key: 'to_be_dispatched', label: 'To Be Dispatched' };
            }
            if (statusName === 'in_transit') {
                return { key: 'in_transit', label: 'In transit' };
            }
            if (statusName === 'pending_submission_to_inventory') {
                return { key: 'pending_inventory', label: 'Pending Submission To Inventory' };
            }

            // Backward-compatible fallback for old numeric status values.
            if (item.status === 2) {
                return { key: 'qc_done', label: 'QC Done' };
            }
            if (item.status === 1) {
                return { key: 'received', label: 'Received' };
            }
            if (item.status === 3) {
                return { key: 'pending_inventory', label: 'Pending Submission To Inventory' };
            }
            if (item.arrivalDate) {
                return { key: 'received', label: 'Received' };
            }
            if (item.dispatchDate || item.consignmentNo || item.receipt) {
                return { key: 'in_transit', label: 'In transit' };
            }
            if (item.pickedBy || item.pullbackMode) {
                return { key: 'to_be_dispatched', label: 'To Be Dispatched' };
            }
            return { key: 'to_be_picked', label: 'To Be Picked' };
        }

        function getStatusKey(item) {
            return resolvePickupStatus(item).key;
        }

        function getStatusBadge(item) {
            var statusMeta = resolvePickupStatus(item);
            var statusClass = {
                to_be_picked: 'bgc-grey-l2 text-grey-d2 border-1 brc-grey-m3',
                to_be_dispatched: 'bgc-warning-l2 text-warning-d2 border-1 brc-warning-m3',
                in_transit: 'bgc-primary-l2 text-primary-d2 border-1 brc-primary-m3',
                received: 'bgc-success-l2 text-success-d2 border-1 brc-success-m3',
                pending_inventory: 'bgc-danger-l2 text-danger-d2 border-1 brc-danger-m3',
                qc_pending: 'bgc-info-l2 text-info-d2 border-1 brc-info-m3',
                qc_done: 'bgc-success-l2 text-success-d2 border-1 brc-success-m3'
            };
            return '<span class="badge badge-lg radius-1 px-3 ' + (statusClass[statusMeta.key] || statusClass.to_be_picked) + '">' +
                   statusMeta.label + '</span>';
        }

        function getActionButtons(item) {
            var buttons = '<div class="action-buttons">';
            var st = ((item.uiStatus || '') + '').trim().toLowerCase();

            // To Be Picked + Vendor -> Pickup Checklist only
            if (st === 'to be picked' && item.pickedBy === 'VENDOR') {
                buttons += '<button class="btn btn-outline-primary btn-action" title="Pickup Checklist" onclick="showPickupChecklist(' +
                           item.replacementReqId + ', \'' + (item.pSerialNo || '') + '\')">' +
                           '<i class="fas fa-clipboard-list mr-1"></i>Pickup Checklist</button>';
            }
            // To Be Dispatched + Vendor -> Dispatch Details only
            else if (st === 'to be dispatched' && item.pickedBy === 'VENDOR') {
                buttons += '<button class="btn btn-outline-warning btn-action" title="Dispatch Details" onclick="showVendorDispatchForm(' +
                           item.replacementReqId + ', \'' + (item.pSerialNo || '') + '\')">' +
                           '<i class="fas fa-truck mr-1"></i>Dispatch Details</button>';
            }
            // In Transit -> Received only
            else if (st === 'in transit') {
                buttons += '<button class="btn btn-outline-success btn-action" title="Mark Received" onclick="markReceived(' +
                           item.id + ', ' + item.replacementReqId + ', \'' + (item.pSerialNo || '') + '\')">' +
                           '<i class="fas fa-check mr-1"></i>Received</button>';
            }
            // Received -> Received By Inventory only
            else if (st === 'received') {
                buttons += '<button class="btn btn-outline-info btn-action" title="Received By Inventory" onclick="receivedByInventory(' +
                           item.id + ')">' +
                           '<i class="fas fa-warehouse mr-1"></i>Recv by Inv</button>';
            }
            // QC Pending / QC Done / Pending Submission To Inventory -> Update QC only
            else if (st === 'qc pending' || st === 'qc done' || st === 'pending submission to inventory') {
                buttons += '<button class="btn btn-outline-warning btn-action" title="Update QC" onclick="showQcModal(' + item.id + ')">' +
                           '<i class="fas fa-clipboard-check mr-1"></i>Update QC</button>';
            }

            buttons += '</div>';
            return buttons;
        }

        function viewCartridges(id, reqId, serialNo, empty, unused) {
            $('#cartReqId').text('#' + reqId);
            $('#cartSerialNo').text(serialNo);
            $('#emptyCartridges').text(empty);
            $('#unusedCartridges').text(unused);
            $('#cartridgesModal').modal('show');
        }

        function showDispatchDetails(id) {
            var item = pullbackDataMap[id];
            if (!item) return;
            
            $('#dispReqId').text('#' + item.replacementReqId);
            $('#dispSerialNo').text(item.pSerialNo || '-');
            $('#dispPullbackMode').text(item.pullbackMode || '-');
            $('#dispCourierName').text(item.courierName || '-');
            $('#dispConsignmentNo').text(item.consignmentNo || '-');
            $('#dispTransportMode').text(item.transportMode || '-');
            $('#dispDispatchDate').text(item.dispatchDate || '-');
            $('#dispArrivalDate').text(item.arrivalDate || '-');
            $('#dispDestinationBranch').text(item.destinationBranch || '-');
            $('#dispContactPerson').text(item.contactPerson || '-');
            $('#dispContactNumber').text(item.contactNumber || '-');
            $('#dispReceipt').text(item.receipt || '-');
            $('#dispatchModal').modal('show');
        }

        function markReceived(id, reqId, serialNo) {
            $('#receivedPullbackId').val(id);
            $('#recReqId').text('#' + reqId);
            $('#recSerialNo').text(serialNo);
            $('#receivedModal').modal('show');
        }

        function showQcModal(id) {
            var item = pullbackDataMap[id];
            if (!item) return;
            var chk = function(v) { return v == 1 ? '<i class="fas fa-check-circle text-success"></i> Yes' : '<i class="fas fa-times-circle text-danger"></i> No'; };
            $('#qcPullbackId').val(id);
            $('#qcPrinter').html(chk(item.printer));
            $('#qcPowerCable').html(chk(item.powerCable));
            $('#qcLanCable').html(chk(item.lanCable));
            $('#qcTray').html(chk(item.tray));
            $('#qcEmptyCartridge').text(item.emptyCartridge || 0);
            $('#qcUnusedCartridge').text(item.unusedCartridge || 0);
            $('#qcPrinterQc').prop('checked', item.printerQc == 1);
            $('#qcPowerCableQc').prop('checked', item.powerCableQc == 1);
            $('#qcLanCableQc').prop('checked', item.lanCableQc == 1);
            $('#qcTrayQc').prop('checked', item.trayQc == 1);
            $('#qcEmptyCartridgeQc').val(item.emptyCartridgeQc || 0);
            $('#qcUnusedCartridgeQc').val(item.unusedCartridgeQc || 0);
            $('#qcConditionSelect').val('');
            $('#qcDamageDetailsGroup').hide();
            $('#qcDamageDetails').val('').prop('required', false);
            $('#qcComments').val('');
            $('#qcModal').modal('show');
        }

        function receivedByInventory(id) {
            if (!confirm('Are you sure you want to mark this as received by inventory?')) {
                return;
            }
            $.ajax({
                url: contextPath + '/api/pullback/action',
                type: 'POST',
                data: { action: 'receivedByInventory', pullbackId: id },
                success: function(response) {
                    if (response.status === 'SUCCESS') {
                        showAlert('success', response.message);
                        loadPullbackData();
                    } else {
                        showAlert('danger', response.message || 'Operation failed');
                    }
                },
                error: function() {
                    showAlert('danger', 'Error processing request');
                }
            });
        }

        function verifyCartridge(id, reqId, serialNo, empty, unused) {
            $('#verifyPullbackId').val(id);
            $('#verReqId').text('#' + reqId);
            $('#verSerialNo').text(serialNo);
            $('#expectedEmpty').val(empty);
            $('#expectedUnused').val(unused);
            $('#actualEmpty').val(empty);
            $('#actualUnused').val(unused);
            $('#chkPrinter').prop('checked', false);
            $('#chkPowerCable').prop('checked', false);
            $('#chkLanCable').prop('checked', false);
            $('#chkTray').prop('checked', false);
            $('#conditionSelect').val('');
            $('#damageDetailsGroup').hide();
            $('#verifyModal').modal('show');
        }

        function triggerCreditNote(id, reqId, serialNo) {
            $('#cnPullbackId').val(id);
            $('#cnReqId').text('#' + reqId);
            $('#cnSerialNo').text(serialNo);
            $('#creditNoteModal').modal('show');
        }

        $('#receivedForm').on('submit', function(e) {
            e.preventDefault();
            var form = $(this);
            $('#receivedModal').modal('hide');
            setTimeout(function() {
                if (!confirm('Are you sure you want to mark this printer as received?')) {
                    $('#receivedModal').modal('show');
                    return;
                }
                submitAction(form, 'Printer marked as received');
            }, 300);
        });

        $('#verifyForm').on('submit', function(e) {
            e.preventDefault();
            submitAction($(this), 'Verification saved successfully');
        });

        $('#qcForm').on('submit', function(e) {
            e.preventDefault();
            if ($('#qcConditionSelect').val() === 'DAMAGED') {
                alert('Damaged condition selected. This will notify AM for credit note.');
            }
            submitAction($(this), 'QC updated successfully');
        });

        $('#creditNoteForm').on('submit', function(e) {
            e.preventDefault();
            submitAction($(this), 'Credit note workflow initiated');
        });

        function submitAction(form, successMsg) {
            $.ajax({
                url: contextPath + '/api/pullback/action',
                type: 'POST',
                data: form.serialize(),
                success: function(response) {
                    if (response.status === 'SUCCESS') {
                        $('.modal').modal('hide');
                        showAlert('success', successMsg);
                        loadPullbackData();
                    } else {
                        showAlert('danger', response.message || 'Operation failed');
                    }
                },
                error: function() {
                    showAlert('danger', 'Error processing request');
                }
            });
        }

        function showAlert(type, message) {
            var alert = '<div class="alert alert-' + type + ' alert-dismissible fade show">' +
                        '<i class="fas fa-' + (type === 'success' ? 'check-circle' : 'exclamation-triangle') + '"></i> ' + 
                        message +
                        '<button type="button" class="close" data-dismiss="alert">&times;</button></div>';
            $('h2').first().after(alert);
            setTimeout(function() { $('.alert-dismissible').alert('close'); }, 5000);
        }

        function showPickupChecklist(reqId, serialNo) {
            $('#pickupReqId').val(reqId);
            $('#pickupSerialNo').val(serialNo);
            $('#pickupReqIdDisplay').text('REQ-' + reqId);
            $('#pickupSerialNoDisplay').text(serialNo);
            $('#pickupPrinter').prop('checked', false);
            $('#pickupPowerCable').prop('checked', false);
            $('#pickupLanCable').prop('checked', false);
            $('#pickupTray').prop('checked', false);
            $('#pickupEmptyCartridges').val('');
            $('#pickupUnusedCartridge').val('');
            $('#pickupChecklistModal').modal('show');
        }

        $('#pickupChecklistForm').on('submit', function(e) {
            e.preventDefault();

            var emptyVal = parseInt($('#pickupEmptyCartridges').val());
            var unusedVal = parseInt($('#pickupUnusedCartridge').val());

            if (isNaN(emptyVal) || emptyVal < 0) {
                alert('Empty Cartridges must be a valid number >= 0');
                return;
            }
            if (isNaN(unusedVal) || unusedVal < 0) {
                alert('Unused Cartridges must be a valid number >= 0');
                return;
            }

            var payload = {
                replacementRequestId: $('#pickupReqId').val(),
                status: [{
                    printerSerialNo: $('#pickupSerialNo').val(),
                    printer: $('#pickupPrinter').is(':checked'),
                    powerCable: $('#pickupPowerCable').is(':checked'),
                    lanCable: $('#pickupLanCable').is(':checked'),
                    tray: $('#pickupTray').is(':checked'),
                    emptyCartridges: emptyVal,
                    unusedCartridge: unusedVal
                }]
            };

            $.ajax({
                url: contextPath + '/pickup/checklist',
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify(payload),
                success: function(response) {
                    $('#pickupChecklistModal').modal('hide');
                    if (response.status === 'SUCCESS') {
                        showAlert('success', 'Pickup checklist submitted successfully');
                        loadPullbackData();
                    } else {
                        var msg = 'Checklist submission failed';
                        if (response.details && response.details.length > 0) {
                            msg = response.details.map(function(d) { return (d.printerSerialNo || '') + ': ' + d.message; }).join(', ');
                        }
                        showAlert('danger', msg);
                    }
                },
                error: function() {
                    showAlert('danger', 'Error submitting pickup checklist');
                }
            });
        });

        function showVendorDispatchForm(reqId, serialNo) {
            $('#vdReqId').val(reqId);
            $('#vdSerialNo').val(serialNo);
            $('#vdReqIdDisplay').text('REQ-' + reqId);
            $('#vdSerialNoDisplay').text(serialNo);
            $('#vdMode').val('COURIER');
            $('#vdCourierName').val('');
            $('#vdConsignmentNo').val('');
            $('#vdTransportMode').val('');
            $('#vdContactPerson').val('');
            $('#vdContactNumber').val('');
            $('#vdComments').val('');
            $('#vdDispatchDate').val('');
            $('#vdArrivalDate').val('');
            $('#vdReceipt').val('');
            $('#vdDestinationBranch').val('');
            setVendorDispatchModeUI();
            $('#vendorDispatchModal').modal('show');
        }

        function setVendorDispatchModeUI() {
            var mode = $('#vdMode').val();
            var isCourier = mode === 'COURIER';

            $('#vdCourierSection').toggle(isCourier);
            $('#vdTransportSection').toggle(!isCourier);

            $('#vdCourierName').prop('required', isCourier);
            $('#vdConsignmentNo').prop('required', isCourier);
            $('#vdDestinationBranch').prop('required', isCourier);

            $('#vdTransportMode').prop('required', !isCourier);
            $('#vdContactPerson').prop('required', !isCourier);
            $('#vdContactNumber').prop('required', !isCourier);
            $('#vdComments').prop('required', !isCourier);
        }

        $('#vdMode').on('change', setVendorDispatchModeUI);

        $('#vendorDispatchForm').on('submit', function(e) {
            e.preventDefault();

            var receiptFile = $('#vdReceipt')[0].files[0];
            if (!receiptFile) {
                showAlert('danger', 'Receipt file is required');
                return;
            }

            var mode = $('#vdMode').val();
            var payload = new FormData();
            payload.append('replacementReqId', parseInt($('#vdReqId').val()));
            payload.append('pSerialNo', $('#vdSerialNo').val());
            payload.append('pullbackMode', mode);
            payload.append('dispatchDate', $('#vdDispatchDate').val());
            payload.append('arrivalDate', $('#vdArrivalDate').val());
            payload.append('receipt', receiptFile);
            if (mode === 'COURIER') {
                payload.append('courierName', $('#vdCourierName').val());
                payload.append('consignmentNo', $('#vdConsignmentNo').val());
                payload.append('destinationBranch', $('#vdDestinationBranch').val());
            } else {
                payload.append('transportMode', $('#vdTransportMode').val());
                payload.append('contactPerson', $('#vdContactPerson').val());
                payload.append('contactNumber', $('#vdContactNumber').val());
                payload.append('comments', $('#vdComments').val());
            }

            $.ajax({
                url: contextPath + '/api/pullback',
                type: 'POST',
                processData: false,
                contentType: false,
                data: payload,
                success: function(response) {
                    $('#vendorDispatchModal').modal('hide');
                    if (response.status === 'SUCCESS') {
                        showAlert('success', 'Dispatch details submitted successfully');
                        loadPullbackData();
                    } else {
                        showAlert('danger', response.message || 'Dispatch submission failed');
                    }
                },
                error: function() {
                    showAlert('danger', 'Error submitting dispatch details');
                }
            });
        });

        function clearFilters() {
            window.location.href = window.location.pathname;
        }
    </script>

<style>
    .step-indicator {
        opacity: 0.4;
        transition: all 0.3s ease;
    }

    .step-indicator.active {
        opacity: 1;
    }

    .step-number {
        width: 40px;
        height: 40px;
        border-radius: 50%;
        background-color: #6c757d;
        color: white;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        font-weight: bold;
        margin-bottom: 5px;
    }

    .step-indicator.active .step-number {
        background-color: #007bff;
        box-shadow: 0 0 10px rgba(0, 123, 255, 0.5);
    }

    .main-content-inner,
    .page-content {
        overflow-x: hidden;
        min-width: 0;
    }

    #filterCard,
    #recordsCard {
        overflow: visible;
    }

    .tab-content .table-responsive {
        overflow-x: auto;
        -webkit-overflow-scrolling: touch;
        width: 100%;
        max-width: 100%;
    }

    #recordsCard .card-body {
        overflow-x: auto;
        max-width: 100%;
    }

    #recordsCard .dataTables_wrapper {
        overflow-x: auto;
        width: 100% !important;
        max-width: 100%;
    }

    #pullbackTabContent .table {
        width: 100% !important;
        table-layout: fixed;
    }

    #pullbackTabContent .table td,
    #pullbackTabContent .table th {
        vertical-align: middle;
        word-break: break-word;
    }

    #pullbackTabContent .table td:last-child {
        overflow: visible;
    }

    .action-buttons {
        display: flex;
        gap: 0.2rem;
        align-items: center;
        flex-wrap: wrap;
        justify-content: center;
        min-width: 120px;
    }

    .action-buttons .btn-action {
        padding: 0.2rem 0.4rem;
        font-size: 0.7rem;
        white-space: nowrap;
    }

    .location-section {
        animation: fadeIn 0.5s ease;
    }

    #cartridgesModal .modal-content {
        border: 0;
        border-radius: 0.8rem;
        box-shadow: 0 12px 30px rgba(0, 0, 0, 0.16);
        overflow: hidden;
    }

    #cartridgesModal .modal-header {
        border-bottom: 0;
    }

    .cart-meta-grid {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 0.75rem;
    }

    .cart-meta-item {
        background: #f8f9fa;
        border: 1px solid #e9ecef;
        border-radius: 0.5rem;
        padding: 0.65rem 0.8rem;
    }

    .cart-count-grid {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 0.8rem;
    }

    .cart-count-card {
        border-radius: 0.65rem;
        padding: 0.8rem;
        border: 1px solid;
    }

    .cart-count-card.empty {
        background: #fff8e6;
        border-color: #ffe4a6;
        color: #9a6700;
    }

    .cart-count-card.unused {
        background: #ecfdf3;
        border-color: #bbf7d0;
        color: #166534;
    }

    .cart-count-head {
        font-size: 0.85rem;
        font-weight: 600;
        display: flex;
        align-items: center;
    }

    .cart-count-value {
        font-size: 1.7rem;
        font-weight: 700;
        line-height: 1.1;
        margin-top: 0.35rem;
    }

    @media (max-width: 576px) {
        .cart-meta-grid,
        .cart-count-grid {
            grid-template-columns: 1fr;
        }
    }

    @keyframes fadeIn {
        from { opacity: 0; transform: translateY(20px); }
        to { opacity: 1; transform: translateY(0); }
    }
</style>
