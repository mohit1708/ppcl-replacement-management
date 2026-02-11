










var contextPath = '${pageContext.request.contextPath}';

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
    }, function(data) {
        if (data.success) {
            populateEditForm(data);
        } else {
            showEditError(data.message || 'Failed to load request details');
        }
    }, 'json').fail(function() {
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
    reasons.forEach(function(r) {
        var selected = (r.id == req.reasonId) ? 'selected' : '';
        reasonSelect.append('<option value="' + r.id + '" ' + selected + '>' + escapeHtml(r.name) + '</option>');
    });
    
    // Sign-In Location
    $('#editSignInBranchId').val(req.signInBranchId || '');
    $('#editSignInLocation').val(req.signInLocation || '-');
    
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
    printers.forEach(function(p) {
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
    $.get(contextPath + '/views/replacement/request', { action: 'getAllPrinterModels' }, function(response) {
        // Handle wrapped response: {success: true, data: [...]}
        var models = response.data || response;
        if (models && models.length > 0) {
            $('.printer-new-model').each(function() {
                var select = $(this);
                var printerId = select.data('printer-id');
                var printer = printers.find(function(p) { return p.id == printerId; });
                
                models.forEach(function(m) {
                    var selected = (printer && printer.newModelId == m.id) ? 'selected' : '';
                    select.append('<option value="' + m.id + '" ' + selected + '>' + escapeHtml(m.modelName) + '</option>');
                });
            });
        } else {
            console.warn('No printer models loaded:', response);
        }
    }, 'json').fail(function(xhr, status, error) {
        console.error('Failed to load printer models:', error);
    });
}

function showEditError(msg) {
    $('#editModalLoading').hide();
    $('#editRequestForm').hide();
    $('#editModalErrorMsg').text(msg);
    $('#editModalError').show();
}

function saveEditRequest() {
    var formData = {
        action: 'update',
        reqId: $('#editReqId').val(),
        replacementType: $('#editReplacementType').val(),
        reasonId: $('#editReasonId').val(),
        comments: $('#editComments').val(),
        contactName: $('#editContactName').val(),
        contactNumber: $('#editContactNumber').val(),
        contactEmail: $('#editContactEmail').val(),
        printers: []
    };
    
    // Collect printer updates
    $('.printer-new-model').each(function() {
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
        success: function(resp) {
            if (resp.success) {
                $('#editRequestModal').modal('hide');
                alert('✅ Request updated successfully!');
                location.reload();
            } else {
                alert('❌ Failed: ' + (resp.message || 'Unknown error'));
                $('#btnSaveEdit').prop('disabled', false).html('<i class="fas fa-save"></i> Save Changes');
            }
        },
        error: function() {
            alert('❌ Network error. Please try again.');
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
    var msg = "Send reminder to " + (ownerName || "the stage owner") + " for " + (stageName || "current stage") + "?";
    if (!confirm(msg)) {
        return;
    }

    $.post(
        contextPath + '/views/replacement/request',
        { action: 'remind', reqId: reqId },
        function (resp) {
            if (resp.success) {
                alert("✅ Reminder sent successfully!");
            } else {
                alert("❌ Failed: " + (resp.message || "Unknown error"));
            }
        },
        'json'
    ).fail(function() {
        alert("❌ Network error. Please try again.");
    });
}

$(document).ready(function() {
    // Initialize DataTables if available and table has data
    var table;
    if ($.fn.DataTable && $('#requestsTable tbody tr').length > 0) {
        table = $('#requestsTable').DataTable({
            "order": [[ 3, "desc" ]], // Sort by Requested Date
            "pageLength": 25,
            "language": {
                "emptyTable": "No requests found",
                "search": "Search:"
            },
            "columnDefs": [
                { "orderable": false, "targets": [7] } // Actions column
            ]
        });
    }

    // Custom filter function
    function applyFilters() {
        var status = $('#filterStatus').val();
        var stage = $('#filterStage').val();
        var dateFrom = $('#filterDateFrom').val();
        var dateTo = $('#filterDateTo').val();

        $('#requestsTable tbody tr').each(function() {
            var $row = $(this);
            var show = true;

            if (status && $row.data('status') !== status) {
                show = false;
            }
            if (stage && $row.data('stage') !== stage) {
                show = false;
            }
            if (dateFrom) {
                var rowDate = $row.data('date');
                if (rowDate < dateFrom) show = false;
            }
            if (dateTo) {
                var rowDate = $row.data('date');
                if (rowDate > dateTo) show = false;
            }

            $row.toggle(show);
        });

        // If using DataTables, redraw
        if (table) {
            table.draw();
        }
    }

    // Apply filters button
    $('#btnApplyFilters').on('click', function() {
        applyFilters();
    });

    // Clear filters button
    $('#btnClearFilters').on('click', function() {
        $('#filterStatus').val('');
        $('#filterStage').val('');
        $('#filterDateFrom').val('');
        $('#filterDateTo').val('');
        $('#requestsTable tbody tr').show();
        if (table) {
            table.search('').columns().search('').draw();
        }
    });

    // Apply filters on Enter key
    $('.filter-section input, .filter-section select').on('keypress', function(e) {
        if (e.which === 13) {
            applyFilters();
        }
    });
});
