// ============================================================================
// AM COMMERCIALS - PROPER FLOW
// Click "Commercials" → One popup with ALL printer details in table
// Each row: Printer info, TL comments, View/Add buttons, Comments field
// ============================================================================

let currentRequestId = null;

/**
 * Show commercials popup when clicking "Commercials" button
 */
function showCommercialsPopup(reqId) {
    console.log('Loading commercials for request #' + reqId);

    currentRequestId = reqId;

    // Show loading state
    $('#commercialsPopup').modal('show');
    $('#commercialsBody').html('<div class="text-center py-5"><i class="fas fa-spinner fa-spin fa-3x"></i><p class="mt-3">Loading...</p></div>');

    // Fetch data via AJAX
    $.ajax({
        url: contextPath + '/views/replacement/am/getCommercialDetails',
        method: 'GET',
        data: { reqId: reqId },
        dataType: 'json',
        success: function(response) {
            if (response.success && response.data) {
                renderCommercialsTable(response.data);
            } else {
                showError(response.message || 'Failed to load data');
            }
        },
        error: function(xhr, status, error) {
            console.error('Error loading commercials:', error);
            showError('Network error: ' + error);
        }
    });
}

/**
 * Render commercials table with all printer details
 */
function renderCommercialsTable(data) {
    const request = data.request;
    const printers = data.printers;

    let html = `
        <!-- Request Header -->
        <div class="bg-primary text-white p-3 mb-3 rounded">
            <div class="row">
                <div class="col-md-3">
                    <strong>Request ID:</strong> #${request.id}<br>
                    <strong>Client:</strong> ${request.clientName}
                </div>
                <div class="col-md-3">
                    <strong>Requester:</strong> ${request.requesterName}<br>
                    <strong>Role:</strong> ${request.requesterRole || 'N/A'}
                </div>
                <div class="col-md-3">
                    <strong>Account Manager:</strong> ${request.accountManager}<br>
                    <strong>City:</strong> ${request.city || 'N/A'}
                </div>
                <div class="col-md-3">
                    <strong>Total Printers:</strong> ${printers.length}<br>
                    <strong>Location:</strong> ${request.location || 'N/A'}
                </div>
            </div>
        </div>

        <!-- TL Comments -->
        <div class="alert alert-info mb-3">
            <strong><i class="fas fa-comment"></i> TL Comments:</strong><br>
            ${request.tlComments || 'No comments from TL'}
        </div>

        <!-- Printers Table -->
        <div class="table-responsive">
            <table class="table table-bordered table-sm" id="printersTable">
                <thead class="thead-dark">
                    <tr>
                        <th width="3%">#</th>
                        <th width="12%">Existing Printer</th>
                        <th width="8%">Serial No</th>
                        <th width="12%">New Model<br><small>(TL Recommended)</small></th>
                        <th width="7%">City</th>
                        <th width="10%">Location</th>
                        <th width="10%">Commercial</th>
                        <th width="18%">Actions</th>
                        <th width="20%">AM Comments</th>
                    </tr>
                </thead>
                <tbody>
    `;

    printers.forEach((printer, index) => {
        const hasCommercial = printer.hasCommercial;
        const rowClass = hasCommercial ? 'table-success' : '';

        html += `
            <tr class="${rowClass}" data-printer-id="${printer.id}">
                <!-- # -->
                <td class="text-center align-middle">
                    <strong>${index + 1}</strong>
                </td>

                <!-- Existing Printer -->
                <td class="align-middle">
                    <strong>${printer.existingModel}</strong>
                </td>

                <!-- Serial No -->
                <td class="align-middle">
                    <code>${printer.serial}</code>
                </td>

                <!-- New Model (Editable) -->
                <td class="align-middle">
                    <input type="text" 
                           class="form-control form-control-sm new-model-input" 
                           id="newModel_${printer.id}"
                           value="${printer.newModel}"
                           data-printer-id="${printer.id}"
                           placeholder="Enter new model">
                </td>

                <!-- City -->
                <td class="align-middle">
                    ${printer.city}
                </td>

                <!-- Location -->
                <td class="align-middle">
                    <small>${printer.location}</small>
                </td>

                <!-- Commercial Status -->
                <td class="text-center align-middle">
                    ${hasCommercial ? `
                        <span class="badge badge-success badge-pill">
                            <i class="fas fa-check-circle"></i> Added
                        </span>
                        <br>
                        <small class="text-muted">
                            Cost: ₹${formatNumber(printer.existingCost)} → ₹${formatNumber(printer.newCost)}
                        </small>
                    ` : `
                        <span class="badge badge-warning badge-pill">
                            <i class="fas fa-clock"></i> Pending
                        </span>
                    `}
                </td>

                <!-- Actions -->
                <td class="text-center align-middle">
                    ${hasCommercial ? `
                        <button class="btn btn-info btn-sm btn-block mb-1" 
                                onclick="viewCommercial(${printer.id})">
                            <i class="fas fa-eye"></i> View
                        </button>
                        <button class="btn btn-warning btn-sm btn-block" 
                                onclick="addEditCommercial(${printer.id}, '${printer.existingModel}', '${printer.serial}', true)">
                            <i class="fas fa-edit"></i> Edit
                        </button>
                    ` : `
                        <button class="btn btn-primary btn-sm btn-block" 
                                onclick="addEditCommercial(${printer.id}, '${printer.existingModel}', '${printer.serial}', false)">
                            <i class="fas fa-plus"></i> Add Commercial
                        </button>
                    `}
                </td>

                <!-- AM Comments (Per Printer) -->
                <td class="align-middle">
                    <textarea class="form-control form-control-sm am-comments-input" 
                              id="amComments_${printer.id}"
                              rows="2"
                              placeholder="Enter your comments for this printer"
                              data-printer-id="${printer.id}">${printer.amComments || ''}</textarea>
                </td>
            </tr>
        `;
    });

    html += `
                </tbody>
            </table>
        </div>
    `;

    $('#commercialsBody').html(html);
}

/**
 * Show error message
 */
function showError(message) {
    $('#commercialsBody').html(`
        <div class="alert alert-danger">
            <i class="fas fa-exclamation-triangle"></i> ${message}
        </div>
    `);
}

// ============================================================================
// ADD/EDIT COMMERCIAL - Opens separate modal
// ============================================================================

/**
 * Add or Edit commercial
 */
function addEditCommercial(printerId, existingModel, serial, isEdit) {
    console.log((isEdit ? 'Editing' : 'Adding') + ' commercial for printer #' + printerId);

    // Get new model from input
    const newModel = $('#newModel_' + printerId).val();

    // Set modal title
    $('#commercialModalLabel').html(
        isEdit ? '<i class="fas fa-edit"></i> Edit Commercial Details' 
               : '<i class="fas fa-plus"></i> Add Commercial Details'
    );

    // Store data
    $('#commercialModal').data('printerId', printerId);
    $('#commercialModal').data('isEdit', isEdit);

    // Show printer info
    $('#commercialPrinterInfo').html(`
        <div class="row">
            <div class="col-md-6 bg-light p-2 rounded">
                <strong>Existing:</strong> ${existingModel}<br>
                <strong>Serial:</strong> <code>${serial}</code>
            </div>
            <div class="col-md-6 bg-light p-2 rounded">
                <strong>New Model:</strong> <span class="text-success">${newModel}</span>
            </div>
        </div>
    `);

    // Reset form
    $('#commercialForm')[0].reset();
    $('#savingsDisplay').html('');

    if (isEdit) {
        // Load existing data
        $.ajax({
            url: contextPath + '/views/replacement/am/getCommercial',
            method: 'GET',
            data: { printerId: printerId },
            dataType: 'json',
            success: function(response) {
                if (response.success && response.data) {
                    $('#existingCost').val(response.data.existingCost || '');
                    $('#newCost').val(response.data.newCost || '');
                    $('#existingRental').val(response.data.existingRental || '');
                    $('#newRental').val(response.data.newRental || '');
                    $('#commercialJustification').val(response.data.justification || '');
                    calculateSavings();
                }
            }
        });
    }

    // Show modal
    $('#commercialModal').modal('show');
}

/**
 * Save commercial
 */
function saveCommercial() {
    const printerId = $('#commercialModal').data('printerId');
    const newModel = $('#newModel_' + printerId).val();

    const existingCost = parseFloat($('#existingCost').val());
    const newCost = parseFloat($('#newCost').val());
    const existingRental = parseFloat($('#existingRental').val());
    const newRental = parseFloat($('#newRental').val());
    const justification = $('#commercialJustification').val().trim();

    // Validation
    if (!newModel) {
        showAppAlert('Please enter the new model name', 'warning');
        return;
    }

    if (!existingCost || !newCost || !existingRental || !newRental) {
        showAppAlert('Please fill all cost and rental fields', 'warning');
        return;
    }

    if (!justification || justification.length < 20) {
        showAppAlert('Please provide detailed justification (minimum 20 characters)', 'warning');
        return;
    }

    // Show loading
    const btn = $('#saveCommercialBtn');
    const originalHtml = btn.html();
    btn.html('<i class="fas fa-spinner fa-spin"></i> Saving...').prop('disabled', true);

    $.ajax({
        url: contextPath + '/views/replacement/am/saveCommercial',
        method: 'POST',
        data: {
            printerId: printerId,
            reqId: currentRequestId,
            newModel: newModel,
            existingCost: existingCost,
            newCost: newCost,
            existingRental: existingRental,
            newRental: newRental,
            commercialJustification: justification
        },
        dataType: 'json',
        success: function(response) {
            if (response.success) {
                $('#commercialModal').modal('hide');
                showAppAlert(response.message, 'success');

                // Reload the commercials popup
                showCommercialsPopup(currentRequestId);
            } else {
                showAppAlert(response.message, 'danger');
                btn.html(originalHtml).prop('disabled', false);
            }
        },
        error: function(xhr, status, error) {
            showAppAlert('Network error: ' + error, 'danger');
            btn.html(originalHtml).prop('disabled', false);
        }
    });
}

/**
 * Calculate savings
 */
function calculateSavings() {
    const existingCost = parseFloat($('#existingCost').val()) || 0;
    const newCost = parseFloat($('#newCost').val()) || 0;
    const existingRental = parseFloat($('#existingRental').val()) || 0;
    const newRental = parseFloat($('#newRental').val()) || 0;

    const costDiff = newCost - existingCost;
    const rentalDiff = newRental - existingRental;
    const annualRentalDiff = rentalDiff * 12;

    let html = '<div class="mt-3 p-3 bg-light border rounded">';
    html += '<h6><i class="fas fa-calculator"></i> Cost Analysis</h6>';
    html += '<div class="row">';

    // Cost difference
    html += '<div class="col-md-4 text-center">';
    html += '<p class="mb-1"><strong>Cost Difference</strong></p>';
    if (costDiff > 0) {
        html += `<h5 class="text-danger">+₹${formatNumber(costDiff)}</h5>`;
        html += '<small class="text-muted">Higher</small>';
    } else if (costDiff < 0) {
        html += `<h5 class="text-success">-₹${formatNumber(Math.abs(costDiff))}</h5>`;
        html += '<small class="text-muted">Savings</small>';
    } else {
        html += '<h5 class="text-muted">₹0</h5>';
        html += '<small class="text-muted">Same</small>';
    }
    html += '</div>';

    // Monthly rental
    html += '<div class="col-md-4 text-center">';
    html += '<p class="mb-1"><strong>Monthly Rental</strong></p>';
    if (rentalDiff > 0) {
        html += `<h5 class="text-danger">+₹${formatNumber(rentalDiff)}</h5>`;
        html += '<small class="text-muted">Higher</small>';
    } else if (rentalDiff < 0) {
        html += `<h5 class="text-success">-₹${formatNumber(Math.abs(rentalDiff))}</h5>`;
        html += '<small class="text-muted">Savings/mo</small>';
    } else {
        html += '<h5 class="text-muted">₹0</h5>';
        html += '<small class="text-muted">Same</small>';
    }
    html += '</div>';

    // Annual impact
    html += '<div class="col-md-4 text-center">';
    html += '<p class="mb-1"><strong>Annual Impact</strong></p>';
    if (annualRentalDiff > 0) {
        html += `<h5 class="text-danger">+₹${formatNumber(annualRentalDiff)}</h5>`;
        html += '<small class="text-muted">Annual cost</small>';
    } else if (annualRentalDiff < 0) {
        html += `<h5 class="text-success">-₹${formatNumber(Math.abs(annualRentalDiff))}</h5>`;
        html += '<small class="text-muted">Annual savings!</small>';
    } else {
        html += '<h5 class="text-muted">₹0</h5>';
        html += '<small class="text-muted">No change</small>';
    }
    html += '</div>';

    html += '</div></div>';

    $('#savingsDisplay').html(html);
}

/**
 * View commercial (read-only)
 */
function viewCommercial(printerId) {
    $.ajax({
        url: contextPath + '/views/replacement/am/getCommercial',
        method: 'GET',
        data: { printerId: printerId },
        dataType: 'json',
        success: function(response) {
            if (response.success && response.data) {
                const data = response.data;

                let html = `
                    <div class="row mb-3">
                        <div class="col-md-6">
                            <div class="card">
                                <div class="card-header bg-primary text-white">
                                    <strong>Existing Printer</strong>
                                </div>
                                <div class="card-body">
                                    <p><strong>Cost:</strong> ₹${formatNumber(data.existingCost)}</p>
                                    <p class="mb-0"><strong>Monthly Rental:</strong> ₹${formatNumber(data.existingRental)}</p>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="card">
                                <div class="card-header bg-success text-white">
                                    <strong>New Printer</strong>
                                </div>
                                <div class="card-body">
                                    <p><strong>Cost:</strong> ₹${formatNumber(data.newCost)}</p>
                                    <p class="mb-0"><strong>Monthly Rental:</strong> ₹${formatNumber(data.newRental)}</p>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="alert alert-info">
                        <strong><i class="fas fa-comment"></i> Justification:</strong><br>
                        ${data.justification || 'N/A'}
                    </div>
                `;

                $('#commercialViewBody').html(html);
                $('#commercialViewModal').modal('show');
            }
        }
    });
}

// ============================================================================
// SUBMIT ACTION
// ============================================================================

/**
 * Submit all commercials with AM action
 */
function submitCommercialsAction() {
    // Collect all new models and AM comments
    const printers = [];
    $('.new-model-input').each(function() {
        const printerId = $(this).data('printer-id');
        const newModel = $(this).val();
        const amComments = $('#amComments_' + printerId).val();

        printers.push({
            printerId: printerId,
            newModel: newModel,
            amComments: amComments
        });
    });

    console.log('Opening action modal for request #' + currentRequestId);

    // Store data
    $('#amActionModal').data('reqId', currentRequestId);
    $('#amActionModal').data('printers', printers);

    // Reset form
    $('#amActionForm')[0].reset();
    $('#amForwardToGroup').hide();
    $('#amRejectReasonGroup').hide();

    $('#amActionModal').modal('show');
}

/**
 * Handle action type change
 */
function handleActionTypeChange() {
    const actionType = $('#amActionType').val();
    $('#amForwardToGroup').toggle(actionType === 'FORWARD');
    $('#amRejectReasonGroup').toggle(actionType === 'REJECT');
}

/**
 * Submit final action
 */
function submitFinalAction() {
    const reqId = $('#amActionModal').data('reqId');
    const printers = $('#amActionModal').data('printers');
    const actionType = $('#amActionType').val();
    const amGeneralComments = $('#amGeneralComments').val().trim();
    const forwardTo = $('#amForwardTo').val();
    const rejectReason = $('#amRejectReason').val().trim();

    // Validation
    if (!actionType) {
        showAppAlert('Please select an action type', 'warning');
        return;
    }

    if (!amGeneralComments) {
        showAppAlert('Please enter your general comments', 'warning');
        return;
    }

    if (actionType === 'FORWARD' && !forwardTo) {
        showAppAlert('Please select who to forward to', 'warning');
        return;
    }

    if (actionType === 'REJECT' && !rejectReason) {
        showAppAlert('Please provide rejection reason', 'warning');
        return;
    }

    // Confirm
    let confirmMsg = actionType === 'SUBMIT' ? '✅ Submit for final approval?' 
                   : actionType === 'FORWARD' ? '📤 Forward to ' + forwardTo + '?'
                   : '❌ Reject this request?';

    if (!confirm(confirmMsg)) {
        return;
    }

    // Show loading
    const btn = $('#submitFinalActionBtn');
    const originalHtml = btn.html();
    btn.html('<i class="fas fa-spinner fa-spin"></i> Processing...').prop('disabled', true);

    $.ajax({
        url: contextPath + '/views/replacement/am/action',
        method: 'POST',
        data: {
            reqId: reqId,
            printers: JSON.stringify(printers),
            actionType: actionType,
            amGeneralComments: amGeneralComments,
            forwardTo: forwardTo,
            rejectReason: rejectReason
        },
        dataType: 'json',
        success: function(response) {
            if (response.success) {
                showAppAlert(response.message, 'success');
                $('#amActionModal').modal('hide');
                $('#commercialsPopup').modal('hide');

                setTimeout(function() {
                    location.reload();
                }, 10000);
            } else {
                showAppAlert(response.message, 'danger');
                btn.html(originalHtml).prop('disabled', false);
            }
        },
        error: function(xhr, status, error) {
            showAppAlert('Network error: ' + error, 'danger');
            btn.html(originalHtml).prop('disabled', false);
        }
    });
}

// ============================================================================
// UTILITY
// ============================================================================

function formatNumber(num) {
    if (!num) return '0.00';
    return parseFloat(num).toLocaleString('en-IN', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

const contextPath = '${pageContext.request.contextPath}';
