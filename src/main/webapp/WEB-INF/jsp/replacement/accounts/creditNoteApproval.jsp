<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="Credit Note Approval - Accounts" scope="request"/>
<%@ include file="../common/header.jsp" %>
<%@ include file="../common/sidebar.jsp" %>

<style>
    /* Fix DataTables alignment with Ace sidebar */
    #creditNotesTable, #approvedTable {
        width: 100% !important;
        table-layout: auto;
    }
    .amount-display {
        font-weight: 600;
        color: #28a745;
    }
    .file-upload-area {
        border: 2px dashed #dee2e6;
        border-radius: 8px;
        padding: 30px;
        text-align: center;
        cursor: pointer;
        transition: all 0.3s;
    }
    .file-upload-area:hover {
        border-color: #007bff;
        background: #f8f9fa;
    }
    .file-upload-area.has-file {
        border-color: #28a745;
        background: #d4edda;
    }
</style>

<div class="main-content-inner">
    <div class="page-content">

        <div class="card shadow-sm">
            <div class="card-header bg-white d-flex justify-content-between align-items-center py-3">
                <h5 class="mb-0">
                    <i class="fas fa-file-invoice-dollar text-primary mr-2"></i>Credit Note Approval Queue
                </h5>
                <span class="badge badge-info">${fn:length(creditNotes)} pending</span>
            </div>
            <div class="card-body">
                <!-- Info Alert -->
                <div class="alert alert-info d-flex align-items-start">
                    <i class="fas fa-info-circle mr-3 mt-1"></i>
                    <div>
                        This queue shows credit note requests approved by Account Managers pending final processing.
                        Upload the credit note document to complete the workflow.
                    </div>
                </div>

                <!-- Error/Success -->
                <c:if test="${not empty error}">
                    <div class="alert alert-danger alert-dismissible fade show">
                        <i class="fas fa-exclamation-triangle"></i> ${error}
                        <button type="button" class="close" data-dismiss="alert">&times;</button>
                    </div>
                </c:if>
                <c:if test="${not empty success}">
                    <div class="alert alert-success alert-dismissible fade show">
                        <i class="fas fa-check-circle"></i> ${success}
                        <button type="button" class="close" data-dismiss="alert">&times;</button>
                    </div>
                </c:if>

                <!-- Pending Table -->
                <c:if test="${empty creditNotes}">
                    <div class="text-center py-5">
                        <i class="fas fa-inbox fa-3x text-muted mb-3"></i>
                        <h5 class="text-muted">No Pending Credit Notes</h5>
                        <p class="text-muted">No credit notes awaiting approval.</p>
                    </div>
                </c:if>

                <c:if test="${not empty creditNotes}">
                    <h6 class="font-weight-bold mb-3"><i class="fas fa-clock text-warning mr-1"></i>Pending Processing</h6>
                    <div class="table-responsive">
                        <table class="table table-striped table-hover table-sm" id="creditNotesTable">
                            <thead class="thead-light">
                                <tr>
                                    <th>#</th>
                                    <th>Client</th>
                                    <th>Location</th>
                                    <th>Items</th>
                                    <th>Amount</th>
                                    <th>AM Comments</th>
                                    <th>Requested By</th>
                                    <th>Date</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${creditNotes}" var="cn">
                                    <tr>
                                        <td><strong>#${cn.replacementRequestId}</strong></td>
                                        <td>${cn.clientName}</td>
                                        <td>${cn.location}</td>
                                        <td>Printer (${cn.printerCount})</td>
                                        <td class="amount-display">
                                            <fmt:formatNumber value="${cn.totalAmount}" type="currency" currencySymbol="₹"/>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty cn.amComments}">
                                                    <span title="${cn.amComments}">${fn:substring(cn.amComments, 0, 50)}${fn:length(cn.amComments) > 50 ? '...' : ''}</span>
                                                </c:when>
                                                <c:otherwise>-</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>${cn.requesterName}</td>
                                        <td>
                                            <fmt:formatDate value="${cn.creationDateTime}" pattern="dd-MMM-yyyy"/>
                                        </td>
                                        <td>
                                            <button class="btn btn-primary btn-sm"
                                                    onclick="openUploadModal(${cn.replacementRequestId}, '${cn.clientName}', '${cn.location}', ${cn.printerCount}, ${cn.totalAmount}, '${cn.requesterName}')">
                                                <i class="fas fa-upload mr-1"></i> Upload
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

        <!-- Recently Issued Card -->
        <div class="card shadow-sm mt-4">
            <div class="card-header bg-white d-flex justify-content-between align-items-center py-3">
                <h5 class="mb-0">
                    <i class="fas fa-check-circle text-success mr-2"></i>Recently Issued Credit Notes
                </h5>
            </div>
            <div class="card-body">
                <c:if test="${empty approvedCreditNotes}">
                    <div class="text-center py-4">
                        <i class="fas fa-inbox fa-2x text-muted mb-2"></i>
                        <p class="text-muted mb-0">No recently issued credit notes.</p>
                    </div>
                </c:if>

                <c:if test="${not empty approvedCreditNotes}">
                    <div class="table-responsive">
                        <table class="table table-striped table-hover table-sm" id="approvedTable">
                            <thead class="thead-light">
                                <tr>
                                    <th>Credit Note #</th>
                                    <th>Replacement ID</th>
                                    <th>Client</th>
                                    <th>Amount</th>
                                    <th>Issued Date</th>
                                    <th>Document</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${approvedCreditNotes}" var="cn">
                                    <tr>
                                        <td><strong>${cn.creditNoteNumber}</strong></td>
                                        <td>#${cn.replacementRequestId}</td>
                                        <td>${cn.clientName}</td>
                                        <td class="amount-display">
                                            <fmt:formatNumber value="${cn.totalAmount}" type="currency" currencySymbol="₹"/>
                                        </td>
                                        <td>
                                            <fmt:formatDate value="${cn.issuedDate}" pattern="dd-MMM-yyyy"/>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty cn.documentPath}">
                                                    <a href="${pageContext.request.contextPath}/${cn.documentPath}"
                                                       class="btn btn-link btn-sm p-0" target="_blank">
                                                        <i class="fas fa-download"></i> Download
                                                    </a>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="text-muted">-</span>
                                                </c:otherwise>
                                            </c:choose>
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

<%@ include file="../common/footer.jsp" %>

<!-- Upload Credit Note Modal (keep exactly as is) -->
    <div class="modal fade" id="uploadModal" tabindex="-1">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header bg-primary text-white">
                    <h5 class="modal-title"><i class="fas fa-upload mr-2"></i>Upload Credit Note</h5>
                    <button type="button" class="close text-white" data-dismiss="modal">&times;</button>
                </div>
                <form id="uploadForm" enctype="multipart/form-data">
                    <input type="hidden" name="action" value="approve">
                    <input type="hidden" name="reqId" id="modalReqId">
                    <input type="hidden" name="location" id="modalLocation">
                    <div class="modal-body">
                        <div class="card mb-4" style="background: #f8f9fa;">
                            <div class="card-body">
                                <div class="row">
                                    <div class="col-md-6">
                                        <small class="text-muted">Client:</small>
                                        <div><strong id="modalClientName">-</strong></div>
                                    </div>
                                    <div class="col-md-6">
                                        <small class="text-muted">Amount:</small>
                                        <div class="amount-display" id="modalAmount">₹0</div>
                                    </div>
                                    <div class="col-md-6 mt-2">
                                        <small class="text-muted">Items:</small>
                                        <div id="modalItems">Printer (0)</div>
                                    </div>
                                    <div class="col-md-6 mt-2">
                                        <small class="text-muted">Requested By:</small>
                                        <div id="modalRequester">-</div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="font-weight-bold">Credit Note Number <span class="text-danger">*</span></label>
                            <input type="text" class="form-control" name="creditNoteNumber" id="creditNoteNumber"
                                   placeholder="e.g., CN-2024-0001" required>
                        </div>
                        <div class="form-group">
                            <label class="font-weight-bold">Upload Credit Note Document <span class="text-danger">*</span></label>
                            <div class="file-upload-area" id="fileUploadArea" onclick="$('#creditNoteFile').click()">
                                <i class="fas fa-cloud-upload-alt fa-3x text-muted mb-3"></i>
                                <p class="mb-0"><strong>Click to upload</strong> or drag and drop</p>
                                <small class="text-muted">PDF, DOC (Max 10MB)</small>
                                <div id="selectedFileName" class="mt-2 text-success" style="display:none;"></div>
                            </div>
                            <input type="file" id="creditNoteFile" name="creditNoteFile"
                                   accept=".pdf,.doc,.docx" style="display:none;" required>
                        </div>
                        <div class="form-group">
                            <label class="font-weight-bold">Comments (Optional)</label>
                            <textarea class="form-control" name="comments" id="approvalComments" rows="2"
                                      placeholder="Add any notes..."></textarea>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                        <button type="submit" class="btn btn-success">
                            <i class="fas fa-check mr-1"></i> Issue Credit Note
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <script>
        $(document).ready(function() {
            $('#creditNotesTable').DataTable({
                order: [[7, 'desc']],
                pageLength: 10,
                autoWidth: false
            });

            $('#approvedTable').DataTable({
                order: [[4, 'desc']],
                pageLength: 5,
                autoWidth: false
            });
        });

        function openUploadModal(reqId, clientName, location, printerCount, totalAmount, requesterName) {
            $('#modalReqId').val(reqId);
            $('#modalLocation').val(location);
            $('#modalClientName').text(clientName);
            $('#modalAmount').text('₹' + totalAmount.toLocaleString('en-IN'));
            $('#modalItems').text('Printer (' + printerCount + ')');
            $('#modalRequester').text(requesterName);
            $('#creditNoteNumber').val('');
            $('#approvalComments').val('');
            $('#creditNoteFile').val('');
            $('#selectedFileName').hide();
            $('#fileUploadArea').removeClass('has-file');

            $('#uploadModal').modal('show');
        }

        $('#creditNoteFile').on('change', function() {
            var fileName = $(this).val().split('\\').pop();
            if (fileName) {
                $('#selectedFileName').text('Selected: ' + fileName).show();
                $('#fileUploadArea').addClass('has-file');
            } else {
                $('#selectedFileName').hide();
                $('#fileUploadArea').removeClass('has-file');
            }
        });

        $('#uploadForm').on('submit', function(e) {
            e.preventDefault();

            var creditNoteNumber = $('#creditNoteNumber').val().trim();
            var creditNoteFile = $('#creditNoteFile')[0].files[0];

            if (!creditNoteNumber) {
                showAppAlert('Please enter a Credit Note Number', 'warning');
                return;
            }

            if (!creditNoteFile) {
                showAppAlert('Please upload a Credit Note document', 'warning');
                return;
            }

            var formData = new FormData(this);

            $.ajax({
                url: '${pageContext.request.contextPath}/views/replacement/accounts/creditNoteApproval',
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                success: function(data) {
                    if (data.success) {
                        showAppAlert('Credit note issued successfully!', 'success');
                        setTimeout(function() { location.reload(); }, 10000);
                    } else {
                        showAppAlert('Error: ' + (data.message || 'Failed to issue credit note'), 'danger');
                    }
                },
                error: function() {
                    showAppAlert('Error issuing credit note', 'danger');
                }
            });
        });
    </script>
