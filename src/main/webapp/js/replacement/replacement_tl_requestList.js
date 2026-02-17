










var contextPath = '${pageContext.request.contextPath}';
var currentAgrProdId = null;
var allPrinterModels = []; // Global array to store printer models

$(document).ready(function() {
    <c:if test="${!hasError && !empty requests}">
    $('#requestsTable').DataTable({
        "order": [[5, "desc"]],
        "pageLength": 25,
        "columnDefs": [
            { "orderable": false, "targets": [4, 6] }
        ]
    });
    </c:if>
    
    // Load filter data
    loadFilterData();
});

function loadFilterData() {
    $.get(contextPath + '/views/replacement/tl/getFilterData', function(resp) {
        if (resp.success && resp.data) {
            // Populate Requesters dropdown
            var requesters = resp.data.requesters || [];
            var reqSelect = $('#filterRequester');
            requesters.forEach(function(r) {
                reqSelect.append('<option value="' + escapeHtml(r.userId) + '">' + escapeHtml(r.name) + '</option>');
            });
            
            // Populate AM Managers dropdown
            var amManagers = resp.data.amManagers || [];
            var amSelect = $('#filterAM');
            amManagers.forEach(function(m) {
                amSelect.append('<option value="' + escapeHtml(m.userId) + '">' + escapeHtml(m.name) + '</option>');
            });
            
            // Store printer models globally for Action Modal dropdowns
            allPrinterModels = resp.data.printerModels || [];
        }
    }, 'json');
}

// Build printer model dropdown options HTML
function buildPrinterModelOptions(selectedModelId, selectedModelText) {
    var options = '<option value="">-- Select Model --</option>';
    allPrinterModels.forEach(function(m) {
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
    
    $.get(contextPath + '/views/replacement/tl/getPrinterDetails', { reqId: reqId }, function(resp) {
        if (resp.success && resp.data) {
            var printers = resp.data.printers || [];
            var html = '<table class="table table-bordered table-sm">';
            html += '<thead class="thead-light"><tr>';
            html += '<th>Existing Printer</th><th>Serial No.</th><th>New Printer Model</th>';
            html += '<th>City / Location</th><th>Comments</th><th>Printer History</th>';
            html += '</tr></thead><tbody>';
            
            printers.forEach(function(p) {
                html += '<tr>';
                html += '<td><strong>' + escapeHtml(p.existingModel || '-') + '</strong></td>';
                html += '<td><code>' + escapeHtml(p.serial || '-') + '</code></td>';
                html += '<td>' + escapeHtml(p.newModelName || p.newModelText || '-') + '</td>';
                html += '<td>' + escapeHtml(p.location || '') + ', ' + escapeHtml(p.city || '') + '</td>';
                html += '<td>' + escapeHtml(p.comments || '-') + '</td>';
                html += '<td><button class="btn btn-outline-secondary btn-sm" onclick="showPrinterHistory(' + p.agrProdId + ', \'' + escapeHtml(p.existingModel || '') + '\', \'' + escapeHtml(p.serial || '') + '\')">';
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
function showPrinterHistory(agrProdId, modelName, serial) {
    currentAgrProdId = agrProdId;
    $('#historyPrinterName').text(modelName + ' (' + serial + ')');
    $('#printerHistoryContent').html('<div class="text-center py-4"><i class="fas fa-spinner fa-spin fa-2x"></i><p>Loading history...</p></div>');
    $('#printerHistoryModal').modal('show');
    
    $.get(contextPath + '/views/replacement/tl/getPrinterHistory', { agrProdId: agrProdId }, function(resp) {
        if (resp.success && resp.data) {
            var data = resp.data;
            var printer = data.printer || {};
            var pageCounts = data.pageCounts || [];
            var serviceCalls = data.serviceCalls || [];
            
            var html = '';
            
            // Monthly Page Counts
            html += '<div class="mb-4">';
            html += '<h6><i class="fas fa-file-alt text-primary"></i> Monthly Page Count (Last 6 Months)</h6>';
            html += '<div class="commercial-grid">';
            pageCounts.forEach(function(pc) {
                html += '<div class="month-box">';
                html += '<div class="small text-muted">' + escapeHtml(pc.month) + '</div>';
                html += '<div class="font-weight-bold">' + formatNumber(pc.count) + '</div>';
                html += '</div>';
            });
            html += '</div>';
            html += '<div class="small text-muted mt-2">6-Month Average: ' + formatNumber(data.avgMonthlyPages) + ' pages/month | Total: ' + formatNumber(data.totalPages) + ' pages</div>';
            html += '</div>';
            
            // Installation Details
            html += '<div class="mb-4">';
            html += '<h6><i class="fas fa-info-circle text-primary"></i> Installation Details</h6>';
            html += '<div class="row">';
            html += '<div class="col-md-4"><div class="p-3 bg-light rounded"><small class="text-muted">Installation Date</small><div class="font-weight-bold">' + escapeHtml(printer.installationDate || '-') + '</div></div></div>';
            html += '<div class="col-md-4"><div class="p-3 bg-light rounded"><small class="text-muted">Client</small><div class="font-weight-bold">' + escapeHtml(printer.clientName || '-') + '</div></div></div>';
            html += '<div class="col-md-4"><div class="p-3 bg-light rounded"><small class="text-muted">Location</small><div class="font-weight-bold">' + escapeHtml(printer.location || '') + ', ' + escapeHtml(printer.city || '') + '</div></div></div>';
            html += '</div></div>';
            
            // Service Calls
            html += '<div>';
            html += '<h6><i class="fas fa-wrench text-primary"></i> Service Calls (Last 6 Months)</h6>';
            if (serviceCalls.length > 0) {
                html += '<table class="table table-sm table-bordered">';
                html += '<thead class="thead-light"><tr><th>Call ID</th><th>Date</th><th>Issue</th><th>Engineer</th><th>Status</th></tr></thead>';
                html += '<tbody>';
                serviceCalls.forEach(function(sc) {
                    html += '<tr>';
                    html += '<td>SVC-' + sc.callId + '</td>';
                    html += '<td>' + escapeHtml(sc.callDate || '-') + '</td>';
                    html += '<td>' + escapeHtml(sc.problem || '-') + '</td>';
                    html += '<td>' + escapeHtml(sc.engineer || '-') + '</td>';
                    html += '<td><span class="badge badge-success">Closed</span></td>';
                    html += '</tr>';
                });
                html += '</tbody></table>';
            } else {
                html += '<p class="text-muted">No service calls in the last 6 months</p>';
            }
            html += '<div class="small text-muted">Total: ' + serviceCalls.length + ' service calls in last 6 months</div>';
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
    
    $.get(contextPath + '/views/replacement/tl/getPrinterDetails', { reqId: reqId }, function(resp) {
        if (resp.success && resp.data) {
            var req = resp.data.request || {};
            var printers = resp.data.printers || [];
            
            $('#actionRequester').text(req.requester || '-');
            $('#actionAM').text(req.accountManager || '-');
            $('#actionReason').text(req.reasonName || '-');
            
            // Build printers section
            var html = '';
            printers.forEach(function(p, idx) {
                html += '<div class="printer-card">';
                html += '<div class="card-header">';
                html += '<span><span class="badge badge-primary mr-2">Printer ' + (idx + 1) + '</span>';
                html += escapeHtml(p.existingModel || '') + ' → <span class="text-primary">' + escapeHtml(p.newModelName || p.newModelText || 'TBD') + '</span></span>';
                html += '<div>';
                html += '<button class="btn btn-outline-secondary btn-sm mr-2" onclick="showPrinterHistory(' + p.agrProdId + ', \'' + escapeHtml(p.existingModel || '') + '\', \'' + escapeHtml(p.serial || '') + '\')">';
                html += '<i class="fas fa-history"></i> Printer History</button>';
                html += '<span class="text-muted">' + escapeHtml(p.location || '') + ', ' + escapeHtml(p.city || '') + '</span>';
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
            showAppAlert('Failed to load request details', 'danger');
            $('#actionModal').modal('hide');
        }
    }, 'json');
}

function approveAndForward() {
    // First save all printer recommendations
    var reqId = $('#actionReqId').val();
    var printerUpdates = [];
    var hasError = false;
    
    // Collect all printer recommendations
    $('.printer-rec-model').each(function() {
        var printerId = $(this).data('printer-id');
        var newModelId = $(this).val();
        var comments = $('.printer-comments[data-printer-id="' + printerId + '"]').val();
        
        if (!newModelId) {
            hasError = true;
            showAppAlert('Please select a recommended printer model for all printers', 'warning');
            return false;
        }
        
        printerUpdates.push({
            printerId: printerId,
            newModelId: newModelId,
            comments: comments
        });
    });
    
    if (hasError) return;
    
    if (!confirm('Save recommendations and forward to AM Manager for commercial review?')) {
        return;
    }
    
    // Save each printer recommendation
    var savePromises = printerUpdates.map(function(p) {
        return $.post(contextPath + '/views/replacement/tl/updateRecommendation', {
            printerId: p.printerId,
            reqId: reqId,
            newModelId: p.newModelId,
            comments: p.comments
        });
    });
    
    // Wait for all updates, then approve
    $.when.apply($, savePromises).done(function() {
        var actionComments = $('#actionComments').val();
        
        $.post(contextPath + '/views/replacement/tl/action', {
            reqId: reqId,
            actionType: 'APPROVE',
            comments: actionComments
        }, function(resp) {
            if (resp.success) {
                showAppAlert('Recommendations saved and ' + resp.message, 'success');
                $('#actionModal').modal('hide');
                setTimeout(function() { location.reload(); }, 10000);
            } else {
                showAppAlert((resp.message || 'Failed'), 'danger');
            }
        }, 'json');
    }).fail(function() {
        showAppAlert('Failed to save recommendations', 'danger');
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
        showAppAlert('Please provide rejection comments', 'warning');
        return;
    }
    
    var reqId = $('#actionReqId').val();
    var fullReason = reason ? reason + ': ' + comments : comments;
    
    $.post(contextPath + '/views/replacement/tl/action', {
        reqId: reqId,
        actionType: 'REJECT',
        comments: fullReason
    }, function(resp) {
        if (resp.success) {
            showAppAlert(resp.message, 'success');
            $('#rejectModal').modal('hide');
            $('#actionModal').modal('hide');
            setTimeout(function() { location.reload(); }, 10000);
        } else {
            showAppAlert((resp.message || 'Failed'), 'danger');
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
        showAppAlert('Please fill all required fields', 'warning');
        return;
    }
    
    var reqId = $('#actionReqId').val();
    
    $.post(contextPath + '/views/replacement/tl/action', {
        reqId: reqId,
        actionType: 'FORWARD',
        forwardTo: forwardTo,
        comments: comments
    }, function(resp) {
        if (resp.success) {
            showAppAlert(resp.message, 'success');
            $('#forwardModal').modal('hide');
            $('#actionModal').modal('hide');
            setTimeout(function() { location.reload(); }, 10000);
        } else {
            showAppAlert((resp.message || 'Failed'), 'danger');
        }
    }, 'json');
}

function exportServiceCalls() {
    if (currentAgrProdId) {
        window.open(contextPath + '/views/replacement/tl/exportServiceCalls?agrProdId=' + currentAgrProdId, '_blank');
    }
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
$('#btnApplyFilters').click(function() {
    var requester = $('#filterRequester').val();
    var am = $('#filterAM').val();
    var status = $('#filterStatus').val();
    
    $('#requestsTable tbody tr').each(function() {
        var row = $(this);
        var showRow = true;
        
        if (requester && row.data('requester') !== requester) showRow = false;
        if (am && row.data('am') !== am) showRow = false;
        if (status && row.data('status') !== status) showRow = false;
        
        row.toggle(showRow);
    });
});

$('#btnClearFilters').click(function() {
    $('#filterDate').val('');
    $('#filterRequester').val('');
    $('#filterAM').val('');
    $('#filterStatus').val('');
    $('#requestsTable tbody tr').show();
});
