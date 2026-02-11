<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Courier - Pincode Mapping" scope="request"/>
<%@ include file="../common/header.jsp" %>
<%@ include file="../common/sidebar.jsp" %>

    <div class="page-header">
        <h1 class="page-title text-primary-d2">
            Courier - Pincode Mapping
        </h1>
        <div class="page-header-actions">
            <button class="btn btn-primary btn-bold radius-1" data-toggle="modal" data-target="#addMappingModal">
                <i class="fas fa-plus mr-1"></i> Add Mapping
            </button>
        </div>
    </div>

    <!-- Filter Section -->
    <div class="card acard mb-4 shadow-sm">
        <div class="card-body">
            <div class="row align-items-end">
                <div class="col-12 col-md-3">
                    <label class="text-90 text-600 mb-1">Courier Name</label>
                    <select class="form-control" id="filterCourier">
                        <option value="">All Couriers</option>
                    </select>
                </div>
                <div class="col-12 col-md-3">
                    <label class="text-90 text-600 mb-1">Pincode</label>
                    <input type="text" class="form-control" id="filterPincode" placeholder="Search pincode..." maxlength="6">
                </div>
                <div class="col-12 col-md-2 mt-2 mt-md-0">
                    <button class="btn btn-outline-secondary btn-block" onclick="clearFilters()">
                        <i class="fas fa-times mr-1"></i> Clear
                    </button>
                </div>
                <div class="col-12 col-md-4 mt-2 mt-md-0 text-md-right">
                    <button class="btn btn-outline-success btn-bold px-3 mr-2" onclick="exportData('csv')">
                        <i class="fas fa-file-csv mr-1"></i> CSV
                    </button>
                    <button class="btn btn-outline-danger btn-bold px-3" onclick="exportData('pdf')">
                        <i class="fas fa-file-pdf mr-1"></i> PDF
                    </button>
                </div>
            </div>
        </div>
    </div>

    <!-- Data Table -->
    <div class="card acard shadow-sm">
        <div class="card-header bgc-primary-d1">
            <h5 class="card-title text-white text-110">
                <i class="fas fa-table mr-1"></i> Pincode Mappings
                <span class="badge badge-light ml-2 text-dark" id="totalRecords">0 records</span>
            </h5>
        </div>
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-striped-primary table-borderless border-0 mb-0" id="mappingsTable">
                    <thead>
                        <tr class="bgc-primary-l4 text-dark-tp3">
                            <th>#</th>
                            <th>Courier Name</th>
                            <th>Pincode</th>
                            <th>City</th>
                            <th>State</th>
                            <th>Region</th>
                            <th>Status</th>
                            <th class="text-center">Actions</th>
                        </tr>
                    </thead>
                    <tbody id="mappingsBody">
                        <tr>
                            <td colspan="8" class="text-center py-5">
                                <i class="fas fa-spinner fa-spin fa-2x text-primary-m2"></i>
                                <p class="mt-2 text-grey-m1">Loading...</p>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
                        <!-- Pagination -->
                        <nav>
                            <ul class="pagination justify-content-center" id="pagination"></ul>
                        </nav>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>

<!-- Add Mapping Modal -->
<div class="modal fade" id="addMappingModal" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header py-2 bgc-primary-tp1 border-0  radius-t-1">
                <h5 class="modal-title text-white-tp1 text-110 pl-2 font-bolder"><i class="fas fa-plus-circle"></i> Add Pincode Mapping</h5>
                <button type="button" class="position-tr btn btn-xs btn-outline-white btn-h-yellow btn-a-yellow mt-1px mr-1px btn-brc-tp" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true" class="text-150">&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <ul class="nav nav-tabs mb-3">
                    <li class="nav-item">
                        <a class="nav-link active" id="manualTab" data-toggle="tab" href="#manualAdd">
                            <i class="fas fa-keyboard"></i> Manual Add
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" id="bulkTab" data-toggle="tab" href="#bulkUpload">
                            <i class="fas fa-file-upload"></i> Bulk Upload
                        </a>
                    </li>
                </ul>
                <div class="tab-content">
                    <!-- Manual Add -->
                    <div class="tab-pane fade show active" id="manualAdd">
                        <div class="form-group">
                            <label><strong>Select Courier <span class="text-danger">*</span></strong></label>
                            <select class="form-control" id="manualCourier" required>
                                <option value="">-- Select Courier --</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label><strong>Pincodes <span class="text-danger">*</span></strong></label>
                            <textarea class="form-control" id="manualPincodes" rows="4" 
                                placeholder="Enter comma-separated pincodes (e.g., 400001, 400002, 400003)"></textarea>
                            <small class="text-muted">Each pincode must be exactly 6 digits</small>
                        </div>
                        <div id="manualResult"></div>
                    </div>
                    <!-- Bulk Upload -->
                    <div class="tab-pane fade" id="bulkUpload">
                        <div class="form-group">
                            <label><strong>Select Courier <span class="text-danger">*</span></strong></label>
                            <select class="form-control" id="bulkCourier" required>
                                <option value="">-- Select Courier --</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label><strong>Upload Excel File <span class="text-danger">*</span></strong></label>
                            <div class="upload-zone" onclick="document.getElementById('bulkFile').click()">
                                <i class="fas fa-cloud-upload-alt mb-2"></i>
                                <p class="mb-1">Click to upload or drag and drop</p>
                                <small class="text-muted">Excel file (.xlsx) with pincodes in first column</small>
                                <p id="selectedFileName" class="text-primary mt-2 mb-0" style="display:none;"></p>
                            </div>
                            <input type="file" id="bulkFile" accept=".xlsx,.xls" style="display:none;" onchange="handleFileSelect(this)">
                        </div>
                        <div class="text-center mb-3">
                            <a href="${pageContext.request.contextPath}/views/replacement/courier-pincode/template" class="btn btn-outline-primary btn-sm">
                                <i class="fas fa-download"></i> Download Sample Template
                            </a>
                        </div>
                        <div id="bulkResult"></div>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-primary" id="submitAddBtn" onclick="submitAdd()">
                    <i class="fas fa-save"></i> Add Mapping
                </button>
            </div>
        </div>
    </div>
</div>

<!-- Edit Modal -->
<div class="modal fade" id="editMappingModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header py-2 bgc-warning-tp1 border-0  radius-t-1">
                <h5 class="modal-title text-white-tp1 text-110 pl-2 font-bolder"><i class="fas fa-edit"></i> Edit Mapping</h5>
                <button type="button" class="position-tr btn btn-xs btn-outline-white btn-h-yellow btn-a-yellow mt-1px mr-1px btn-brc-tp" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true" class="text-150">&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <input type="hidden" id="editMappingId">
                <div class="form-group">
                    <label><strong>Pincode</strong></label>
                    <input type="text" class="form-control" id="editPincode" readonly>
                    <small class="text-muted">Pincode cannot be changed</small>
                </div>
                <div class="form-group">
                    <label><strong>Courier <span class="text-danger">*</span></strong></label>
                    <select class="form-control" id="editCourier" required>
                        <option value="">-- Select Courier --</option>
                    </select>
                </div>
                <div class="row">
                    <div class="col-md-6">
                        <div class="form-group">
                            <label><strong>City</strong></label>
                            <input type="text" class="form-control" id="editCity">
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="form-group">
                            <label><strong>State</strong></label>
                            <input type="text" class="form-control" id="editState">
                        </div>
                    </div>
                </div>
                <div class="form-group">
                    <label><strong>Region</strong></label>
                    <input type="text" class="form-control" id="editRegion">
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-warning" onclick="submitEdit()">
                    <i class="fas fa-save"></i> Save Changes
                </button>
            </div>
        </div>
    </div>
</div>

<!-- Confirm Status Modal -->
<div class="modal fade" id="confirmStatusModal" tabindex="-1">
    <div class="modal-dialog modal-sm">
        <div class="modal-content">
            <div class="modal-header py-2 bgc-secondary-tp1 border-0  radius-t-1">
                <h5 class="modal-title text-white-tp1 text-110 pl-2 font-bolder"><i class="fas fa-file-import"></i> Bulk Upload Result</h5>
                <button type="button" class="position-tr btn btn-xs btn-outline-white btn-h-yellow btn-a-yellow mt-1px mr-1px btn-brc-tp" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true" class="text-150">&times;</span>
                </button>
            </div>
            <div class="modal-body text-center">
                <input type="hidden" id="toggleMappingId">
                <input type="hidden" id="toggleNewStatus">
                <p id="confirmStatusMessage"></p>
            </div>
            <div class="modal-footer justify-content-center">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-danger" onclick="confirmToggleStatus()">Confirm</button>
            </div>
        </div>
    </div>
</div>

<script>
    var contextPath = '<%= request.getContextPath() %>';
</script>
<script src="<%= request.getContextPath() %>/js/replacement/courier_pincodeMappingPage.js"></script>

<%@ include file="../common/footer.jsp" %>
