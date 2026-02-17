// AM Manager Module JavaScript

let currentReqId = null;

/**
 * View full replacement request
 */
function viewFullRequest(reqId) {
    currentReqId = reqId;
    $('#fullRequestModal').modal('show');

    $.ajax({
        url: '/views/replacement/ammanager/getFullRequest',
        method: 'GET',
        data: { reqId: reqId },
        success: function(response) {
            if (response.success) {
                displayFullRequest(response.data);
            } else {
                showAppAlert(response.message, 'danger');
            }
        },
        error: function() {
            showAppAlert('Failed to load request details', 'danger');
        }
    });
}

/**
 * Display full request details in modal
 */
function displayFullRequest(data) {
    let html = '';

    // Request Info
    html += '<div class="card mb-3">';
    html += '<div class="card-header bg-info text-white"><strong>Request Information</strong></div>';
    html += '<div class="card-body">';
    html += '<div class="row">';
    html += '<div class="col-md-6"><strong>Client:</strong> ' + data.request.clientName + ' (' + data.request.clientId + ')</div>';
    html += '<div class="col-md-6"><strong>Location:</strong> ' + data.request.city + ', ' + data.request.branch + '</div>';
    html += '<div class="col-md-6"><strong>Requester:</strong> ' + data.request.requesterName + ' (' + data.request.requesterRole + ')</div>';
    html += '<div class="col-md-6"><strong>Current Stage:</strong> ' + data.request.currentStage + '</div>';
    html += '</div>';

    if (data.request.tlComments) {
        html += '<div class="mt-3"><strong>TL Comments:</strong><br>';
        html += '<div class="alert alert-warning">' + data.request.tlComments + '</div>';
        html += '</div>';
    }

    html += '</div></div>';

    // Printers Table
    html += '<div class="card">';
    html += '<div class="card-header bg-primary text-white"><strong>Printers for Replacement</strong></div>';
    html += '<div class="card-body">';
    html += '<table class="table table-bordered">';
    html += '<thead class="thead-light">';
    html += '<tr>';
    html += '<th>Serial</th>';
    html += '<th>Existing Model</th>';
    html += '<th>TL Recommended</th>';
    html += '<th>Select Final Model</th>';
    html += '<th>Location</th>';
    html += '</tr>';
    html += '</thead>';
    html += '<tbody>';

    data.printers.forEach(function(printer) {
        html += '<tr>';
        html += '<td>' + printer.serial + '</td>';
        html += '<td>' + printer.existingModel + '</td>';
        html += '<td><span class="badge badge-info">' + (printer.recommendedModel || 'N/A') + '</span></td>';
        html += '<td>';
        html += '<input type="text" class="form-control" ';
        html += 'id="model_' + printer.id + '" ';
        html += 'value="' + (printer.recommendedModel || '') + '" ';
        html += 'placeholder="Enter final model" ';
        html += 'onchange="updateRecommendedModel(' + printer.id + ', this.value)">';
        html += '</td>';
        html += '<td>' + printer.city + ', ' + printer.location + '</td>';
        html += '</tr>';
    });

    html += '</tbody>';
    html += '</table>';
    html += '</div></div>';

    // Comments Section
    html += '<div class="card mt-3">';
    html += '<div class="card-header bg-secondary text-white"><strong>AM Manager Comments</strong></div>';
    html += '<div class="card-body">';
    html += '<textarea class="form-control" id="amManagerComments" rows="4" ';
    html += 'placeholder="Enter your comments and recommendations"></textarea>';
    html += '</div></div>';

    $('#fullRequestBody').html(html);
}

/**
 * View current commercials (last 6 months billing)
 */
function viewCurrentCommercials(reqId) {
    $('#currentCommercialsModal').modal('show');

    $.ajax({
        url: '/views/replacement/ammanager/getCurrentCommercials',
        method: 'GET',
        data: { reqId: reqId },
        success: function(response) {
            if (response.success) {
                displayCurrentCommercials(response.data);
            } else {
                showAppAlert(response.message, 'danger');
            }
        },
        error: function() {
            showAppAlert('Failed to load commercials', 'danger');
        }
    });
}

/**
 * Display current commercials and billing
 */
function displayCurrentCommercials(commercials) {
    let html = '';

    if (commercials.length === 0) {
        html = '<div class="alert alert-info">No commercial data available</div>';
    } else {
        html += '<table class="table table-bordered">';
        html += '<thead class="thead-dark">';
        html += '<tr>';
        html += '<th rowspan="2">Serial</th>';
        html += '<th rowspan="2">Model</th>';
        html += '<th rowspan="2">Agreement</th>';
        html += '<th colspan="2">Current Rates</th>';
        html += '<th colspan="6">Last 6 Months Billing</th>';
        html += '<th rowspan="2">Total</th>';
        html += '</tr>';
        html += '<tr>';
        html += '<th>Black</th><th>Color</th>';
        html += '<th>M-1</th><th>M-2</th><th>M-3</th><th>M-4</th><th>M-5</th><th>M-6</th>';
        html += '</tr>';
        html += '</thead>';
        html += '<tbody>';

        commercials.forEach(function(c) {
            html += '<tr>';
            html += '<td>' + c.serial + '</td>';
            html += '<td>' + c.modelName + '</td>';
            html += '<td>' + c.agreementNo + '</td>';
            html += '<td>₹' + (c.blackRate || 0).toFixed(2) + '</td>';
            html += '<td>₹' + (c.colorRate || 0).toFixed(2) + '</td>';
            html += '<td>₹' + (c.month1Amt || 0).toFixed(2) + '</td>';
            html += '<td>₹' + (c.month2Amt || 0).toFixed(2) + '</td>';
            html += '<td>₹' + (c.month3Amt || 0).toFixed(2) + '</td>';
            html += '<td>₹' + (c.month4Amt || 0).toFixed(2) + '</td>';
            html += '<td>₹' + (c.month5Amt || 0).toFixed(2) + '</td>';
            html += '<td>₹' + (c.month6Amt || 0).toFixed(2) + '</td>';
            html += '<td><strong>₹' + (c.total6Months || 0).toFixed(2) + '</strong></td>';
            html += '</tr>';
        });

        html += '</tbody>';
        html += '</table>';
    }

    $('#currentCommercialsBody').html(html);
}

/**
 * Update recommended printer model
 */
function updateRecommendedModel(printerId, recommendedModel) {
    $.ajax({
        url: '/views/replacement/ammanager/updateRecommendedModel',
        method: 'POST',
        data: {
            printerId: printerId,
            recommendedModel: recommendedModel
        },
        success: function(response) {
            if (response.success) {
                console.log('Model updated');
            }
        }
    });
}

/**
 * Submit AM Manager request
 */
function submitAMManagerRequest() {
    let comments = $('#amManagerComments').val().trim();

    if (!comments) {
        showAppAlert('Please enter comments before submitting', 'warning');
        return;
    }

    if (!confirm('Are you sure you want to submit this request to AM for commercials?')) {
        return;
    }

    $.ajax({
        url: '/views/replacement/ammanager/submitRequest',
        method: 'POST',
        data: {
            reqId: currentReqId,
            comments: comments
        },
        success: function(response) {
            if (response.success) {
                showAppAlert(response.message, 'success');
                $('#fullRequestModal').modal('hide');
                setTimeout(function() { location.reload(); }, 10000);
            } else {
                showAppAlert(response.message, 'danger');
            }
        },
        error: function() {
            showAppAlert('Failed to submit request', 'danger');
        }
    });
}

/**
 * Clear filters
 */
function clearFilters() {
    window.location.href = '/views/replacement/ammanager/requestList';
}
