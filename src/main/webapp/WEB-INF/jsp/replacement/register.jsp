<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="pageTitle" value="Replacement Register" scope="request"/>
<%@ include file="common/header.jsp" %>
<%@ include file="common/sidebar.jsp" %>
<div class="main-content-inner">
    <div class="page-content">

<style>
    /* Register Page Styles */
    /* Row highlighting */
    .row-pending {
        background-color: inherit !important;
    }
    .row-locked {
        background-color: inherit !important;
    }
    
    /* Badge Styles */
    .badge-signed {
        background-color: #28a745;
        color: white;
    }
    .badge-pending {
        background-color: #ffc107;
        color: #212529;
    }
    
    /* Modal Info Grid */
    .info-grid {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 1rem;
        margin-bottom: 1rem;
    }
    .info-item {
        padding: 0.75rem;
        background: #f8f9fa;
        border-radius: 0.375rem;
    }
    .info-item .label {
        font-size: 0.75rem;
        color: #6c757d;
        margin-bottom: 0.25rem;
    }
    .info-item .value {
        font-weight: 600;
        color: #212529;
    }
    
    /* File Upload */
    .file-upload-zone {
        border: 2px dashed #dee2e6;
        border-radius: 0.375rem;
        padding: 2rem;
        text-align: center;
        cursor: pointer;
        transition: border-color 0.15s ease-in-out;
    }
    .file-upload-zone:hover {
        border-color: #007bff;
    }
    .file-upload-zone.dragover {
        border-color: #28a745;
        background-color: rgba(40, 167, 69, 0.1);
    }
    .file-upload-zone i {
        font-size: 2rem;
        color: #6c757d;
        margin-bottom: 0.5rem;
    }
</style>


    <div class="card shadow-sm">
        <div class="card-header bg-white d-flex justify-content-between align-items-center py-3">
            <h5 class="mb-0">
                <i class="fas fa-book text-primary mr-2"></i>Replacement Register
            </h5>
            <div>
                <button class="btn btn-success btn-sm mr-2" onclick="exportToCSV()">
                    <i class="fas fa-file-csv"></i> Export CSV
                </button>
                <button class="btn btn-danger btn-sm" onclick="exportToPDF()">
                    <i class="fas fa-file-pdf"></i> Export PDF
                </button>
            </div>
        </div>
        <div class="card-body">
            <%-- Info Alert --%>
            <div class="alert alert-info d-flex align-items-start">
                <i class="fas fa-info-circle mr-3 mt-1"></i>
                <div>
                    <strong>Document Control:</strong> Once a signed replacement letter is uploaded, 
                    all editing capabilities for that record and its associated child records are permanently 
                    disabled to ensure data immutability.
                </div>
            </div>

            <%-- Filter Section --%>
            <form method="get" action="${pageContext.request.contextPath}/views/replacement/register" class="filter-section">
                <div class="row">
                    <div class="col-md-3">
                        <label class="small font-weight-bold mb-1">Date Range</label>
                        <input type="date" name="fromDate" class="form-control form-control-sm" 
                               value="${filterFromDate}">
                    </div>
                    <div class="col-md-3">
                        <label class="small font-weight-bold mb-1">To Date</label>
                        <input type="date" name="toDate" class="form-control form-control-sm" 
                               value="${filterToDate}">
                    </div>
                    <div class="col-md-2">
                        <label class="small font-weight-bold mb-1">Client</label>
                        <select name="client" class="form-control form-control-sm">
                            <option value="">All Clients</option>
                            <c:forEach items="${clients}" var="client">
                                <option value="${client[0]}" ${filterClient == client[0] ? 'selected' : ''}>
                                    ${fn:escapeXml(client[1])}
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="col-md-2">
                        <label class="small font-weight-bold mb-1">Status</label>
                        <select name="status" class="form-control form-control-sm">
                            <option value="">All Status</option>
                            <option value="pending" ${filterStatus == 'pending' ? 'selected' : ''}>Pending Signature</option>
                            <option value="signed" ${filterStatus == 'signed' ? 'selected' : ''}>Signed & Locked</option>
                        </select>
                    </div>
                    <div class="col-md-2 d-flex align-items-end">
                        <button type="submit" class="btn btn-primary btn-sm mr-2">
                            <i class="fas fa-filter"></i> Apply
                        </button>
                        <a href="${pageContext.request.contextPath}/views/replacement/register" class="btn btn-outline-secondary btn-sm">
                            <i class="fas fa-times"></i> Clear
                        </a>
                    </div>
                </div>
            </form>

            <%-- Success/Error Messages --%>
            <c:if test="${param.success != null}">
                <div class="alert alert-success alert-dismissible fade show">
                    <i class="fas fa-check-circle"></i> ${fn:escapeXml(param.success)}
                    <button type="button" class="close" data-dismiss="alert">&times;</button>
                </div>
            </c:if>
            <c:if test="${error != null}">
                <div class="alert alert-danger alert-dismissible fade show">
                    <i class="fas fa-exclamation-triangle"></i> ${fn:escapeXml(error)}
                    <button type="button" class="close" data-dismiss="alert">&times;</button>
                </div>
            </c:if>

            <%-- Register Table --%>
            <c:choose>
                <c:when test="${empty requests}">
                    <div class="text-center py-5">
                        <i class="fas fa-inbox fa-3x text-muted mb-3"></i>
                        <h5 class="text-muted">No Records Found</h5>
                        <p class="text-muted">No replacement letters match your filter criteria.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="table-responsive">
                        <table class="table table-striped table-hover table-sm" id="registerTable">
                            <thead class="thead-light">
                                <tr>
                                    <th>Letter Ref</th>
                                    <th>Replacement ID</th>
                                    <th>Client</th>
                                    <th class="text-center">Printers</th>
                                    <th>Generation Date</th>
                                    <th>Current Status</th>
                                    <th>Signed Copy</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${requests}" var="req">
                                    <tr class="${req.locked ? 'row-locked' : 'row-pending'}">
                                        <td><strong>${fn:escapeXml(req.letterRef)}</strong></td>
                                        <td>REQ-${req.id}</td>
                                        <td>${fn:escapeXml(req.clientName)}</td>
                                        <td class="text-center">${req.printerCount}</td>
                                        <td>
                                            <fmt:formatDate value="${req.generationDate}" pattern="dd-MMM-yyyy"/>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${req.locked}">
                                                    <span class="badge badge-signed">
                                                        <i class="fas fa-lock mr-1"></i>Signed & Locked
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge badge-pending">
                                                        <i class="fas fa-clock mr-1"></i>Pending Signature
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${req.locked}">
                                                    <a href="${pageContext.request.contextPath}/views/replacement/register?action=download&id=${req.id}"
                                                       class="btn btn-link btn-sm p-0">
                                                        <i class="fas fa-download"></i> Download
                                                    </a>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="text-muted small">Not uploaded</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <div class="action-buttons">
                                                <button class="btn btn-outline-primary btn-action" 
                                                        onclick="openViewModal(${req.id})">
                                                    <i class="fas fa-eye"></i> View
                                                </button>
                                                <c:if test="${!req.locked && (req.currentStage == 'STG11_CREDIT_NOTE_BILLING' || req.currentStage == 'STG12_CLOSURE') && (sessionScope.isCRO || sessionScope.isAccountBillingUser)}">
                                                    <button class="btn btn-primary btn-action" 
                                                            onclick="openUploadModal(${req.id}, '${fn:escapeXml(req.letterRef)}')">
                                                        <i class="fas fa-upload"></i> Upload
                                                    </button>
                                                    <button class="btn btn-success btn-action review-freeze-btn" 
                                                            id="reviewFreezeBtn_${req.id}"
                                                            onclick="openReviewFreezeModal(${req.id}, '${fn:escapeXml(req.letterRef)}')"
                                                            style="display: none;">
                                                        <i class="fas fa-check-circle"></i> Review & Freeze
                                                    </button>
                                                </c:if>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                    
                    <%-- Pagination Info --%>
                    <div class="d-flex justify-content-between align-items-center mt-3">
                        <div class="text-muted small">
                            Showing <strong>${fn:length(requests)}</strong> entries
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<%-- View Letter Modal --%>
<div class="modal fade" id="viewLetterModal" tabindex="-1" role="dialog">
    <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
            <div class="modal-header bg-primary text-white">
                <h5 class="modal-title">
                    <i class="fas fa-file-alt mr-2"></i>Replacement Letter - <span id="viewLetterRef">R-XXXXX</span>
                </h5>
                <button type="button" class="close text-white" data-dismiss="modal">&times;</button>
            </div>
            <div class="modal-body">
                <div id="viewModalLoading" class="text-center py-4">
                    <i class="fas fa-spinner fa-spin fa-2x"></i>
                    <p class="mt-2">Loading details...</p>
                </div>
                <div id="viewModalContent" style="display: none;">
                    <%-- Info Grid --%>
                    <div class="info-grid">
                        <div class="info-item">
                            <div class="label">Client</div>
                            <div class="value" id="viewClientName">-</div>
                        </div>
                        <div class="info-item">
                            <div class="label">Replacement ID</div>
                            <div class="value" id="viewReplacementId">-</div>
                        </div>
                        <div class="info-item">
                            <div class="label">Generated Date</div>
                            <div class="value" id="viewGenerationDate">-</div>
                        </div>
                        <div class="info-item">
                            <div class="label">Status</div>
                            <div class="value" id="viewStatus">-</div>
                        </div>
                    </div>
                    
                    <%-- Printers Table --%>
                    <h6 class="font-weight-bold mb-3">
                        <i class="fas fa-print mr-2"></i>Printers in Letter
                    </h6>
                    <div class="table-responsive">
                        <table class="table table-bordered table-sm">
                            <thead class="thead-light">
                                <tr>
                                    <th>Old Printer</th>
                                    <th>Old Serial</th>
                                    <th>New Printer</th>
                                    <th>New Serial</th>
                                    <th>Delivery Status</th>
                                </tr>
                            </thead>
                            <tbody id="viewPrintersTable">
                                <!-- Populated dynamically -->
                            </tbody>
                        </table>
                    </div>
                    
                    <%-- Locked Alert --%>
                    <div id="viewLockedAlert" class="alert alert-success d-none mt-3">
                        <i class="fas fa-shield-alt mr-2"></i>
                        <strong>Record Locked:</strong> This record has a signed copy uploaded. All editing is disabled.
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
                <button type="button" class="btn btn-primary d-none" id="viewDownloadBtn" onclick="downloadSignedLetter()">
                    <i class="fas fa-download mr-1"></i>Download Signed Copy
                </button>
            </div>
        </div>
    </div>
</div>

<%-- Upload Signed Document Modal (Step 1) --%>
<div class="modal fade" id="uploadSignedModal" tabindex="-1" role="dialog" data-backdrop="static">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header bg-primary text-white">
                <h5 class="modal-title">
                    <i class="fas fa-upload mr-2"></i>Upload Signed Document
                </h5>
                <button type="button" class="close text-white" data-dismiss="modal">&times;</button>
            </div>
            <form id="uploadForm" enctype="multipart/form-data">
                <div class="modal-body">
                    <input type="hidden" id="uploadReqId" name="id">
                    
                    <div class="alert alert-info">
                        <i class="fas fa-info-circle mr-2"></i>
                        Upload the signed document. You can review and edit before freezing the record.
                    </div>
                    
                    <div class="mb-3 text-center">
                        <strong>Letter Reference:</strong> <span id="uploadLetterRef" class="text-primary">R-XXXXX</span>
                    </div>
                    
                    <div class="form-group">
                        <label class="font-weight-bold">Upload Signed Document <span class="text-danger">*</span></label>
                        <div class="file-upload-zone" id="fileUploadZone" onclick="document.getElementById('signedFile').click()">
                            <input type="file" id="signedFile" name="signedFile" accept=".pdf" style="display: none;" required>
                            <i class="fas fa-cloud-upload-alt"></i>
                            <p class="mb-1"><strong>Click to upload</strong> or drag and drop</p>
                            <small class="text-muted">PDF only (Max 25MB)</small>
                            <div id="selectedFileName" class="text-success mt-2" style="display: none;"></div>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-primary" id="uploadBtn" disabled>
                        <i class="fas fa-upload mr-1"></i>Upload Document
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<%-- Review and Freeze Modal (Step 2) --%>
<div class="modal fade" id="reviewFreezeModal" tabindex="-1" role="dialog" data-backdrop="static">
    <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
            <div class="modal-header bg-success text-white">
                <h5 class="modal-title">
                    <i class="fas fa-check-circle mr-2"></i>Review & Freeze - <span id="reviewLetterRef">R-XXXXX</span>
                </h5>
                <button type="button" class="close text-white" data-dismiss="modal">&times;</button>
            </div>
            <div class="modal-body">
                <input type="hidden" id="reviewReqId">
                
                <div class="alert alert-warning">
                    <i class="fas fa-exclamation-triangle mr-2"></i>
                    <strong>Important:</strong> Please review the uploaded document carefully. 
                    Once you freeze, the record and all associated child records will be permanently locked.
                </div>
                
                <%-- Document Preview Section --%>
                <div class="card mb-3">
                    <div class="card-header bg-light">
                        <h6 class="mb-0"><i class="fas fa-file-pdf mr-2 text-danger"></i>Uploaded Document</h6>
                    </div>
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <span id="reviewFileName" class="font-weight-bold">document.pdf</span>
                                <small class="text-muted ml-2" id="reviewFileSize"></small>
                                <small class="text-muted ml-2">Uploaded: <span id="reviewUploadDate"></span></small>
                            </div>
                            <div>
                                <button type="button" class="btn btn-outline-primary btn-sm" onclick="previewDocument()">
                                    <i class="fas fa-eye mr-1"></i>Preview
                                </button>
                                <button type="button" class="btn btn-outline-secondary btn-sm" onclick="downloadTempDocument()">
                                    <i class="fas fa-download mr-1"></i>Download
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
                
                <%-- PDF Preview Container --%>
                <div id="pdfPreviewContainer" class="mb-3" style="display: none;">
                    <div class="card">
                        <div class="card-header bg-light d-flex justify-content-between align-items-center">
                            <span><i class="fas fa-file-pdf mr-2"></i>Document Preview</span>
                            <button type="button" class="btn btn-sm btn-outline-secondary" onclick="$('#pdfPreviewContainer').slideUp();">
                                <i class="fas fa-times"></i>
                            </button>
                        </div>
                        <div class="card-body p-0">
                            <iframe id="pdfPreviewFrame" style="width: 100%; height: 400px; border: none;"></iframe>
                        </div>
                    </div>
                </div>
                
                <%-- Re-upload Section --%>
                <div class="card border-warning">
                    <div class="card-header bg-warning text-dark">
                        <h6 class="mb-0"><i class="fas fa-edit mr-2"></i>Wrong Document? Upload New</h6>
                    </div>
                    <div class="card-body">
                        <p class="text-muted small mb-2">If the uploaded document is incorrect, you can replace it here before freezing.</p>
                        <div class="input-group">
                            <div class="custom-file">
                                <input type="file" class="custom-file-input" id="replaceFile" accept=".pdf">
                                <label class="custom-file-label" for="replaceFile" id="replaceFileLabel">Choose new file...</label>
                            </div>
                            <div class="input-group-append">
                                <button type="button" class="btn btn-warning" id="replaceUploadBtn" onclick="replaceDocument()" disabled>
                                    <i class="fas fa-sync-alt mr-1"></i>Replace
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
                
                <%-- Confirmation Section --%>
                <div class="mt-3">
                    <div class="form-group">
                        <label class="form-check">
                            <input type="checkbox" class="form-check-input" id="confirmFreeze">
                            <span class="form-check-label">
                                I have reviewed the document and confirm that it is correct. I understand that freezing will permanently lock this record.
                            </span>
                        </label>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-success" id="freezeBtn" onclick="freezeRecord()" disabled>
                    <i class="fas fa-lock mr-1"></i>Freeze Record
                </button>
            </div>
        </div>
    </div>


    </div><%-- /.page-content --%>
</div><%-- /.main-content-inner --%>
<%@ include file="common/footer.jsp" %>

<script>
var contextPath = '${pageContext.request.contextPath}';
var currentViewId = null;
var currentUploadId = null;
var currentUploadLetterRef = null;
var uploadedDocumentInfo = {};

// View Modal
function openViewModal(reqId) {
    currentViewId = reqId;
    $('#viewModalLoading').show();
    $('#viewModalContent').hide();
    $('#viewLetterModal').modal('show');
    
    $.get(contextPath + '/views/replacement/register', {
        action: 'getDetails',
        id: reqId
    }, function(response) {
        if (response.success) {
            populateViewModal(response.data);
        } else {
            alert('Error: ' + response.message);
            $('#viewLetterModal').modal('hide');
        }
    }, 'json').fail(function() {
        alert('Network error. Please try again.');
        $('#viewLetterModal').modal('hide');
    });
}

function populateViewModal(req) {
    $('#viewLetterRef').text(req.letterRef);
    $('#viewClientName').text(req.clientName || '-');
    $('#viewReplacementId').text('REQ-' + req.id);
    $('#viewGenerationDate').text(formatDate(req.generationDate));
    
    if (req.isLocked || req.locked) {
        $('#viewStatus').html('<span class="badge badge-signed"><i class="fas fa-lock mr-1"></i>Signed & Locked</span>');
        $('#viewLockedAlert').removeClass('d-none');
        $('#viewDownloadBtn').removeClass('d-none');
    } else {
        $('#viewStatus').html('<span class="badge badge-pending"><i class="fas fa-clock mr-1"></i>Pending Signature</span>');
        $('#viewLockedAlert').addClass('d-none');
        $('#viewDownloadBtn').addClass('d-none');
    }
    
    // Populate printers table
    var tbody = $('#viewPrintersTable');
    tbody.empty();
    
    if (req.printers && req.printers.length > 0) {
        req.printers.forEach(function(p) {
            var newPrinter = p.newModelName || p.recommendedModelName || p.recommendedModelText || '-';
            var newSerial = p.newSerial ? '<code>' + p.newSerial + '</code>' : '<span class="text-muted">-</span>';
            var deliveryStatus = formatDeliveryStatus(p.deliveryStatus, p.deliveredDate);
            
            var row = '<tr>' +
                '<td>' + (p.existingModelName || '-') + '</td>' +
                '<td><code>' + (p.existingSerial || '-') + '</code></td>' +
                '<td>' + newPrinter + '</td>' +
                '<td>' + newSerial + '</td>' +
                '<td>' + deliveryStatus + '</td>' +
                '</tr>';
            tbody.append(row);
        });
    } else {
        tbody.append('<tr><td colspan="5" class="text-center text-muted">No printers found</td></tr>');
    }
    
    $('#viewModalLoading').hide();
    $('#viewModalContent').show();
}

function downloadSignedLetter() {
    if (currentViewId) {
        window.location.href = contextPath + '/views/replacement/register?action=download&id=' + currentViewId;
    }
}

// Upload Modal (Step 1 - Upload only, no lock)
function openUploadModal(reqId, letterRef) {
    currentUploadId = reqId;
    currentUploadLetterRef = letterRef;
    $('#uploadReqId').val(reqId);
    $('#uploadLetterRef').text(letterRef);
    $('#signedFile').val('');
    $('#selectedFileName').hide().text('');
    $('#uploadBtn').prop('disabled', true);
    $('#uploadSignedModal').modal('show');
}

// File input change handler
$('#signedFile').on('change', function() {
    var fileName = this.files[0] ? this.files[0].name : '';
    if (fileName) {
        $('#selectedFileName').text('Selected: ' + fileName).show();
        $('#uploadBtn').prop('disabled', false);
    } else {
        $('#selectedFileName').hide();
        $('#uploadBtn').prop('disabled', true);
    }
});

// Drag and drop
var dropZone = document.getElementById('fileUploadZone');
if (dropZone) {
    dropZone.addEventListener('dragover', function(e) {
        e.preventDefault();
        this.classList.add('dragover');
    });
    dropZone.addEventListener('dragleave', function(e) {
        e.preventDefault();
        this.classList.remove('dragover');
    });
    dropZone.addEventListener('drop', function(e) {
        e.preventDefault();
        this.classList.remove('dragover');
        var files = e.dataTransfer.files;
        if (files.length > 0) {
            document.getElementById('signedFile').files = files;
            $('#signedFile').trigger('change');
        }
    });
}

// Upload form submission (Step 1 - upload without locking)
$('#uploadForm').on('submit', function(e) {
    e.preventDefault();
    
    var formData = new FormData(this);
    formData.append('action', 'uploadTemp');
    
    $('#uploadBtn').prop('disabled', true).html('<i class="fas fa-spinner fa-spin mr-1"></i>Uploading...');
    
    $.ajax({
        url: contextPath + '/views/replacement/register',
        type: 'POST',
        data: formData,
        processData: false,
        contentType: false,
        success: function(data) {
            if (data.success) {
                uploadedDocumentInfo = {
                    fileName: data.fileName,
                    fileSize: data.fileSize,
                    uploadDate: data.uploadDate
                };
                $('#uploadSignedModal').modal('hide');
                // Show Review & Freeze button
                $('#reviewFreezeBtn_' + currentUploadId).show();
                // Reset upload button
                $('#uploadBtn').prop('disabled', false).html('<i class="fas fa-upload mr-1"></i>Upload Document');
            } else {
                alert('Error: ' + data.message);
                $('#uploadBtn').prop('disabled', false).html('<i class="fas fa-upload mr-1"></i>Upload Document');
            }
        },
        error: function() {
            alert('Network error. Please try again.');
            $('#uploadBtn').prop('disabled', false).html('<i class="fas fa-upload mr-1"></i>Upload Document');
        }
    });
});

// Review & Freeze Modal (Step 2)
function openReviewFreezeModal(reqId, letterRef) {
    currentUploadId = reqId;
    currentUploadLetterRef = letterRef;
    $('#reviewReqId').val(reqId);
    $('#reviewLetterRef').text(letterRef);
    $('#confirmFreeze').prop('checked', false);
    $('#freezeBtn').prop('disabled', true);
    $('#replaceFile').val('');
    $('#replaceFileLabel').text('Choose new file...');
    $('#replaceUploadBtn').prop('disabled', true);
    $('#pdfPreviewContainer').hide();
    
    // Load document info
    $.get(contextPath + '/views/replacement/register', {
        action: 'getTempDocInfo',
        id: reqId
    }, function(data) {
        if (data.success) {
            $('#reviewFileName').text(data.fileName || 'document.pdf');
            $('#reviewFileSize').text(data.fileSize ? '(' + formatFileSize(data.fileSize) + ')' : '');
            $('#reviewUploadDate').text(data.uploadDate || new Date().toLocaleDateString());
        }
    }, 'json');
    
    $('#reviewFreezeModal').modal('show');
}

// Replace file input change
$('#replaceFile').on('change', function() {
    var fileName = this.files[0] ? this.files[0].name : '';
    if (fileName) {
        $('#replaceFileLabel').text(fileName);
        $('#replaceUploadBtn').prop('disabled', false);
    } else {
        $('#replaceFileLabel').text('Choose new file...');
        $('#replaceUploadBtn').prop('disabled', true);
    }
});

// Confirm freeze checkbox
$('#confirmFreeze').on('change', function() {
    $('#freezeBtn').prop('disabled', !$(this).is(':checked'));
});

// Replace document function
function replaceDocument() {
    var formData = new FormData();
    formData.append('action', 'replaceTemp');
    formData.append('id', currentUploadId);
    formData.append('signedFile', $('#replaceFile')[0].files[0]);
    
    $('#replaceUploadBtn').prop('disabled', true).html('<i class="fas fa-spinner fa-spin mr-1"></i>Replacing...');
    
    $.ajax({
        url: contextPath + '/views/replacement/register',
        type: 'POST',
        data: formData,
        processData: false,
        contentType: false,
        success: function(data) {
            if (data.success) {
                $('#reviewFileName').text(data.fileName || 'document.pdf');
                $('#reviewFileSize').text(data.fileSize ? '(' + formatFileSize(data.fileSize) + ')' : '');
                $('#reviewUploadDate').text(new Date().toLocaleDateString());
                $('#replaceFile').val('');
                $('#replaceFileLabel').text('Choose new file...');
                $('#pdfPreviewContainer').hide();
                alert('Document replaced successfully!');
            } else {
                alert('Error: ' + data.message);
            }
            $('#replaceUploadBtn').prop('disabled', false).html('<i class="fas fa-sync-alt mr-1"></i>Replace');
        },
        error: function() {
            alert('Network error. Please try again.');
            $('#replaceUploadBtn').prop('disabled', false).html('<i class="fas fa-sync-alt mr-1"></i>Replace');
        }
    });
}

// Preview document
function previewDocument() {
    var previewUrl = contextPath + '/views/replacement/register?action=previewTemp&id=' + currentUploadId;
    $('#pdfPreviewFrame').attr('src', previewUrl);
    $('#pdfPreviewContainer').slideDown();
}

// Download temp document
function downloadTempDocument() {
    window.location.href = contextPath + '/views/replacement/register?action=downloadTemp&id=' + currentUploadId;
}

// Freeze record (final step)
function freezeRecord() {
    if (!confirm('Are you sure you want to freeze this record?\n\nThis action cannot be undone. The record and all associated data will be permanently locked.')) {
        return;
    }
    
    $('#freezeBtn').prop('disabled', true).html('<i class="fas fa-spinner fa-spin mr-1"></i>Freezing...');
    
    $.ajax({
        url: contextPath + '/views/replacement/register',
        type: 'POST',
        data: {
            action: 'freeze',
            id: currentUploadId
        },
        success: function(data) {
            if (data.success) {
                $.get(contextPath + '/views/replacement/closeFlowTracking.jsp', { id: currentUploadId }, function() {}, 'json');
                alert('Record frozen successfully!\n\nRecord ' + currentUploadLetterRef + ' is now permanently locked.');
                $('#reviewFreezeModal').modal('hide');
                location.reload();
            } else {
                alert('Error: ' + data.message);
                $('#freezeBtn').prop('disabled', false).html('<i class="fas fa-lock mr-1"></i>Freeze Record');
            }
        },
        error: function() {
            alert('Network error. Please try again.');
            $('#freezeBtn').prop('disabled', false).html('<i class="fas fa-lock mr-1"></i>Freeze Record');
        }
    });
}

// Format file size helper
function formatFileSize(bytes) {
    if (bytes < 1024) return bytes + ' B';
    else if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB';
    else return (bytes / 1048576).toFixed(1) + ' MB';
}

// Helper function
function formatDate(timestamp) {
    if (!timestamp) return '-';
    var date = new Date(timestamp);
    var months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    return date.getDate().toString().padStart(2, '0') + '-' + months[date.getMonth()] + '-' + date.getFullYear();
}

// Format delivery status with badge
function formatDeliveryStatus(status, deliveredDate) {
    if (!status) {
        return '<span class="text-muted">-</span>';
    }
    
    var statusLower = status.toLowerCase();
    if (statusLower === 'delivered' || statusLower === 'completed') {
        var dateStr = deliveredDate ? ' ' + deliveredDate : '';
        return '<span class="badge badge-success"><i class="fas fa-check-circle mr-1"></i>Delivered' + dateStr + '</span>';
    } else if (statusLower === 'dispatched' || statusLower === 'in transit') {
        return '<span class="badge badge-info"><i class="fas fa-truck mr-1"></i>Dispatched</span>';
    } else if (statusLower === 'pending') {
        return '<span class="badge badge-warning"><i class="fas fa-clock mr-1"></i>Pending</span>';
    } else {
        return '<span class="badge badge-secondary">' + status + '</span>';
    }
}

// Export to CSV functionality
function exportToCSV() {
    var table = document.getElementById('registerTable');
    if (!table) {
        alert('No data to export');
        return;
    }
    
    var rows = table.querySelectorAll('tbody tr');
    if (rows.length === 0) {
        alert('No data to export');
        return;
    }
    
    var csv = [];
    var headers = ['Letter Ref', 'Replacement ID', 'Client', 'Printers', 'Generation Date', 'Status', 'Signed Copy'];
    csv.push(headers.join(','));

    rows.forEach(function(row) {
        var cols = row.querySelectorAll('td');
        if (cols.length >= 7) {
            var rowData = [
                cols[0].innerText.trim(),
                cols[1].innerText.trim(),
                cols[2].innerText.trim(),
                cols[3].innerText.trim(),
                cols[4].innerText.trim(),
                cols[5].innerText.trim().replace(/\s+/g, ' '),
                cols[6].innerText.trim().replace(/\s+/g, ' ')
            ];
            csv.push(rowData.map(function(val) {
                return '"' + (val || '').replace(/"/g, '""') + '"';
            }).join(','));
        }
    });

    var csvContent = csv.join('\n');
    var blob = new Blob([csvContent], {type: 'text/csv;charset=utf-8;'});
    var link = document.createElement('a');
    var url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', 'replacement_register_' + new Date().toISOString().slice(0, 10) + '.csv');
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}

// Export to PDF functionality
function exportToPDF() {
    var table = document.getElementById('registerTable');
    if (!table) {
        alert('No data to export');
        return;
    }
    
    var printWindow = window.open('', '_blank');
    if (!printWindow) {
        alert('Please allow popups for this site to export PDF');
        return;
    }
    
    // Clone table and remove Actions column
    var tableClone = table.cloneNode(true);
    var headerCells = tableClone.querySelectorAll('thead th');
    var bodyCells = tableClone.querySelectorAll('tbody td:nth-child(8)');
    if (headerCells.length >= 8) {
        headerCells[7].remove();
    }
    bodyCells.forEach(function(cell) {
        cell.remove();
    });
    
    printWindow.document.write('<html><head><title>Replacement Register</title>');
    printWindow.document.write('<style>');
    printWindow.document.write('body { font-family: Arial, sans-serif; padding: 20px; }');
    printWindow.document.write('h1 { font-size: 18px; margin-bottom: 20px; }');
    printWindow.document.write('table { width: 100%; border-collapse: collapse; font-size: 12px; }');
    printWindow.document.write('th, td { border: 1px solid #333; padding: 8px; text-align: left; }');
    printWindow.document.write('th { background-color: #333; color: white; }');
    printWindow.document.write('@media print { body { -webkit-print-color-adjust: exact; print-color-adjust: exact; } }');
    printWindow.document.write('</style></head><body>');
    printWindow.document.write('<h1>Replacement Register - ' + new Date().toLocaleDateString() + '</h1>');
    printWindow.document.write(tableClone.outerHTML);
    printWindow.document.write('</body></html>');
    printWindow.document.close();
    
    setTimeout(function() {
        printWindow.print();
    }, 500);
}
</script>
