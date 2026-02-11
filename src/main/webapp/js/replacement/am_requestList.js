






$(document).ready(function() {
    $('#requestsTable').DataTable({
        "order": [[ 7, "desc" ], [ 5, "desc" ]],
        "pageLength": 25
    });

    // Calculate commercial differences on input
    $('#newCost, #existingCost, #newRental, #existingRental').on('input', function() {
        let newCost = parseFloat($('#newCost').val()) || 0;
        let existingCost = parseFloat($('#existingCost').val()) || 0;
        let newRental = parseFloat($('#newRental').val()) || 0;
        let existingRental = parseFloat($('#existingRental').val()) || 0;

        let costDiff = newCost - existingCost;
        let rentalDiff = newRental - existingRental;

        $('#costDiff').text('₹ ' + costDiff.toFixed(2))
                      .toggleClass('text-success', costDiff < 0)
                      .toggleClass('text-danger', costDiff > 0);

        $('#rentalDiff').text('₹ ' + rentalDiff.toFixed(2))
                        .toggleClass('text-success', rentalDiff < 0)
                        .toggleClass('text-danger', rentalDiff > 0);
    });

    // AM Action type radio button handler
    $('input[name="amActionType"]').change(function() {
        let actionType = $(this).val();

        $('#amForwardToGroup').toggle(actionType === 'FORWARD');
        $('#amRejectReasonGroup').toggle(actionType === 'REJECT');

        $('#amForwardTo').prop('required', actionType === 'FORWARD');
        $('#amRejectReason').prop('required', actionType === 'REJECT');
    });
});

function showCommercialsPopup(reqId) {
    $('#modalReqId').text(reqId);
    $('#commercialsModalBody').html('<div class="text-center"><i class="fas fa-spinner fa-spin fa-3x"></i><br>Loading...</div>');
    $('#commercialsModal').modal('show');

    $.ajax({
        url: '<%= request.getContextPath() %>/views/replacement/am/getCommercialDetails',
        data: { reqId: reqId },
        dataType: 'json',
        success: function(response) {
            if (response.success) {
                renderCommercialDetails(response.data, reqId);
            } else {
                $('#commercialsModalBody').html('<div class="alert alert-danger">' + response.message + '</div>');
            }
        },
        error: function() {
            $('#commercialsModalBody').html('<div class="alert alert-danger">Error loading commercial details</div>');
        }
    });
}

function renderCommercialDetails(data, reqId) {
    let html = '';

    // Request Info
    html += `
    <div class="card mb-3 border-primary">
        <div class="card-header bg-primary text-white">
            <h6 class="mb-0"><i class="fas fa-info-circle"></i> Request Info</h6>
        </div>
        <div class="card-body">
            <div class="row">
                <div class="col-md-4">
                    <strong>Client:</strong> \${data.request.clientName}
                </div>
                <div class="col-md-4">
                    <strong>Requester:</strong> \${data.request.requester}
                </div>
                <div class="col-md-4">
                    <strong>Stage:</strong> 
                    <span class="badge badge-info">\${data.request.currentStage}</span>
                </div>
            </div>
        </div>
    </div>`;

    // Service TL Comments
    if (data.request.tlComments) {
        html += `
        <div class="card mb-3 border-info">
            <div class="card-header bg-info text-white">
                <h6 class="mb-0"><i class="fas fa-comment"></i> Service TL Comments</h6>
            </div>
            <div class="card-body">
                <p class="mb-0">\${data.request.tlComments}</p>
            </div>
        </div>`;
    }

    // Printers with Commercials
    html += `
    <div class="card mb-3">
        <div class="card-header bg-warning text-dark">
            <h6 class="mb-0"><i class="fas fa-print"></i> Printers & Commercials (\${data.printers.length})</h6>
        </div>
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-sm table-hover mb-0">
                    <thead class="thead-light">
                        <tr>
                            <th>#</th>
                            <th>Existing Printer</th>
                            <th>Serial No</th>
                            <th>New Model</th>
                            <th>City</th>
                            <th>Location</th>
                            <th>TL Comments</th>
                            <th>Commercials</th>
                        </tr>
                    </thead>
                    <tbody>`;

    data.printers.forEach(function(printer, idx) {
        let hasCommercial = printer.commercials && printer.commercials.id;

        html += `
        <tr>
            <td><strong>\${idx + 1}</strong></td>
            <td>
                <strong>\${printer.existingModel}</strong><br>
                <small class="text-muted">AGR: \${printer.agrProdId}</small>
            </td>
            <td><code>\${printer.serial}</code></td>
            <td>
                <strong class="text-success">\${printer.newModel || 'Not specified'}</strong><br>
                <small class="text-muted">\${printer.newModelSource || ''}</small>
            </td>
            <td>\${printer.city}</td>
            <td>\${printer.location}</td>
            <td>
                <button class="btn btn-sm btn-info" 
                        onclick="showPrinterComments(\${printer.id})">
                    <i class="fas fa-comment"></i> View
                </button>
            </td>
            <td>`;

        if (hasCommercial) {
            html += `
                <button class="btn btn-sm btn-success" 
                        onclick="viewCommercial(\${printer.id}, \${reqId})">
                    <i class="fas fa-eye"></i> View
                </button>
                <button class="btn btn-sm btn-warning" 
                        onclick="editCommercial(\${printer.id}, \${reqId})">
                    <i class="fas fa-edit"></i> Edit
                </button>`;
        } else {
            html += `
                <button class="btn btn-sm btn-primary" 
                        onclick="addCommercial(\${printer.id}, \${reqId})">
                    <i class="fas fa-plus"></i> Add
                </button>`;
        }

        html += `
            </td>
        </tr>`;
    });

    html += `
                    </tbody>
                </table>
            </div>
        </div>
    </div>`;

    // Action Button
    html += `
    <div class="text-right">
        <button class="btn btn-success btn-lg" onclick="openAMActionModal(\${reqId})">
            <i class="fas fa-tasks"></i> Take Action on Request
        </button>
    </div>`;

    $('#commercialsModalBody').html(html);
}

function addCommercial(printerId, reqId) {
    $('#commercialDetailsModalTitle').html('<i class="fas fa-plus"></i> Add Commercial Details');
    $('#commercialPrinterId').val(printerId);
    $('#commercialReqId').val(reqId);

    // Clear form
    $('#commercialForm')[0].reset();
    $('#costDiff').text('₹ 0.00');
    $('#rentalDiff').text('₹ 0.00');

    // Load printer info
    loadPrinterInfoForCommercial(printerId);

    $('#commercialsModal').modal('hide');
    $('#commercialDetailsModal').modal('show');
}

function viewCommercial(printerId, reqId) {
    $('#commercialDetailsModalTitle').html('<i class="fas fa-eye"></i> View Commercial Details');
    loadCommercialData(printerId, reqId, true);
}

function editCommercial(printerId, reqId) {
    $('#commercialDetailsModalTitle').html('<i class="fas fa-edit"></i> Edit Commercial Details');
    loadCommercialData(printerId, reqId, false);
}

function loadPrinterInfoForCommercial(printerId) {
    $.ajax({
        url: '<%= request.getContextPath() %>/views/replacement/am/getPrinterInfo',
        data: { printerId: printerId },
        dataType: 'json',
        success: function(response) {
            if (response.success) {
                let p = response.data;
                $('#commercialPrinterInfo').html(`
                    <div class="row">
                        <div class="col-md-6">
                            <strong>Existing:</strong> \${p.existingModel} (\${p.serial})
                        </div>
                        <div class="col-md-6">
                            <strong>New:</strong> <span class="text-success">\${p.newModel || 'N/A'}</span>
                        </div>
                    </div>
                `);
            }
        }
    });
}

function loadCommercialData(printerId, reqId, readOnly) {
    $.ajax({
        url: '<%= request.getContextPath() %>/views/replacement/am/getCommercial',
        data: { printerId: printerId },
        dataType: 'json',
        success: function(response) {
            if (response.success) {
                let c = response.data;
                $('#commercialPrinterId').val(printerId);
                $('#commercialReqId').val(reqId);
                $('#existingCost').val(c.existingCost);
                $('#newCost').val(c.newCost);
                $('#existingRental').val(c.existingRental);
                $('#newRental').val(c.newRental);
                $('#commercialJustification').val(c.justification);

                // Calculate diffs
                $('#newCost, #existingCost').trigger('input');

                if (readOnly) {
                    $('#commercialForm :input').prop('disabled', true);
                    $('#saveCommercialBtn').hide();
                }

                loadPrinterInfoForCommercial(printerId);
                $('#commercialsModal').modal('hide');
                $('#commercialDetailsModal').modal('show');
            }
        }
    });
}

$('#commercialForm').submit(function(e) {
    e.preventDefault();

    $('#saveCommercialBtn').prop('disabled', true)
                           .html('<i class="fas fa-spinner fa-spin"></i> Saving...');

    $.post(
        '<%= request.getContextPath() %>/views/replacement/am/saveCommercial',
        $(this).serialize(),
        function(response) {
            if (response.success) {
                alert('✅ Commercial details saved successfully!');
                $('#commercialDetailsModal').modal('hide');

                // Reload commercials popup
                let reqId = $('#commercialReqId').val();
                showCommercialsPopup(reqId);
            } else {
                alert('❌ Error: ' + response.message);
            }
            $('#saveCommercialBtn').prop('disabled', false)
                                   .html('<i class="fas fa-save"></i> Save Commercial Details');
        },
        'json'
    ).fail(function() {
        alert('❌ Network error');
        $('#saveCommercialBtn').prop('disabled', false)
                               .html('<i class="fas fa-save"></i> Save Commercial Details');
    });

    return false;
});

function openAMActionModal(reqId) {
    $('#amActionReqId').text(reqId);
    $('#amActionFormReqId').val(reqId);

    // Load commercials summary
    $.ajax({
        url: '<%= request.getContextPath() %>/views/replacement/am/getCommercialsSummary',
        data: { reqId: reqId },
        dataType: 'json',
        success: function(response) {
            if (response.success) {
                let html = '<ul class="list-group">';
                let allHaveCommercials = true;

                response.data.forEach(function(p) {
                    let hasCommercial = p.hasCommercial;
                    if (!hasCommercial) allHaveCommercials = false;

                    html += `
                    <li class="list-group-item \${hasCommercial ? '' : 'list-group-item-warning'}">
                        <div class="row">
                            <div class="col-md-6">
                                <strong>\${p.existingModel}</strong> → <strong class="text-success">\${p.newModel}</strong>
                            </div>
                            <div class="col-md-6 text-right">
                                \${hasCommercial ? 
                                    '<span class="badge badge-success">✓ Commercials Added</span>' : 
                                    '<span class="badge badge-danger">✗ Commercials Missing</span>'}
                            </div>
                        </div>
                    </li>`;
                });
                html += '</ul>';

                if (!allHaveCommercials) {
                    html = '<div class="alert alert-danger"><i class="fas fa-exclamation-triangle"></i> Some printers do not have commercials added yet!</div>' + html;
                }

                $('#amCommercialsSummary').html(html);

                // Load TL comments
                if (response.tlComments) {
                    $('#tlCommentsDisplay').html('<p class="mb-0">' + response.tlComments + '</p>');
                }
            }
        }
    });

    $('#commercialsModal').modal('hide');
    $('#amActionModal').modal('show');
}

$('#amActionForm').submit(function(e) {
    e.preventDefault();

    let actionType = $('input[name="amActionType"]:checked').val();

    if (!actionType) {
        alert('Please select an action');
        return false;
    }

    if (actionType === 'REJECT') {
        if (!confirm('Are you sure you want to REJECT this entire request?')) {
            return false;
        }
    }

    $('#amActionSubmitBtn').prop('disabled', true)
                           .html('<i class="fas fa-spinner fa-spin"></i> Processing...');

    $.post(
        '<%= request.getContextPath() %>/views/replacement/am/action',
        $(this).serialize(),
        function(response) {
            if (response.success) {
                alert('✅ Action completed successfully!');
                location.reload();
            } else {
                alert('❌ Error: ' + response.message);
                $('#amActionSubmitBtn').prop('disabled', false)
                                       .html('<i class="fas fa-paper-plane"></i> Submit Action');
            }
        },
        'json'
    ).fail(function() {
        alert('❌ Network error');
        $('#amActionSubmitBtn').prop('disabled', false)
                               .html('<i class="fas fa-paper-plane"></i> Submit Action');
    });

    return false;
});
