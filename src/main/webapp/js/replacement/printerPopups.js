// ============================================================================
// PRINTER DETAILS POPUP - Shows all printers for a request
// ============================================================================

/**
 * Show printer details popup when clicking "X Printers" button
 */
function showPrintersPopup(reqId) {
    console.log('Loading printer details for request #' + reqId);

    // Show loading state
    $('#printerDetailsModal').modal('show');
    $('#printerDetailsBody').html('<div class="text-center py-5"><i class="fas fa-spinner fa-spin fa-3x"></i><p class="mt-3">Loading printer details...</p></div>');

    // Fetch printer details via AJAX
    $.ajax({
        url: contextPath + '/views/replacement/tl/getPrinterDetails',
        method: 'GET',
        data: { reqId: reqId },
        dataType: 'json',
        success: function(response) {
            if (response.success && response.data) {
                renderPrinterDetails(response.data);
            } else {
                showPrinterError(response.message || 'Failed to load printer details');
            }
        },
        error: function(xhr, status, error) {
            console.error('Error loading printer details:', error);
            showPrinterError('Network error: ' + error);
        }
    });
}

/**
 * Render printer details in modal
 */
function renderPrinterDetails(data) {
    const request = data.request;
    const printers = data.printers;

    let html = `
        <div class="mb-4">
            <h5 class="border-bottom pb-2">Request Information</h5>
            <div class="row">
                <div class="col-md-6">
                    <p><strong>Request ID:</strong> #${request.id}</p>
                    <p><strong>Client:</strong> ${request.clientName} (${request.clientId})</p>
                    <p><strong>Requester:</strong> ${request.requesterName}</p>
                </div>
                <div class="col-md-6">
                    <p><strong>Account Manager:</strong> ${request.accountManager}</p>
                    <p><strong>City:</strong> ${request.city || 'N/A'}</p>
                    <p><strong>Location:</strong> ${request.location || 'N/A'}</p>
                </div>
            </div>
        </div>

        <h5 class="border-bottom pb-2 mb-3">Printers to Replace (${printers.length})</h5>
    `;

    printers.forEach((printer, index) => {
        html += `
            <div class="card mb-3 printer-card" data-printer-id="${printer.id}">
                <div class="card-header bg-dark text-white d-flex justify-content-between align-items-center">
                    <span><i class="fas fa-print"></i> Printer ${index + 1}</span>
                    <button class="btn btn-sm btn-info" onclick="showPrinterHistory(${printer.agrProdId}, '${printer.existingModel}', '${printer.serial}')">
                        <i class="fas fa-history"></i> View History
                    </button>
                </div>
                <div class="card-body">
                    <div class="row">
                        <div class="col-md-6">
                            <h6 class="text-primary">Current Printer</h6>
                            <p class="mb-1"><strong>Model:</strong> ${printer.existingModel}</p>
                            <p class="mb-1"><strong>Serial No:</strong> ${printer.serial}</p>
                            <p class="mb-1"><strong>Installation Date:</strong> ${printer.installationDate || 'N/A'}</p>
                        </div>
                        <div class="col-md-6">
                            <h6 class="text-success">New Printer Recommendation</h6>
                            <div class="input-group">
                                <input type="text" 
                                       class="form-control new-model-input" 
                                       id="newModel_${printer.id}"
                                       value="${printer.newModel || ''}"
                                       placeholder="Enter new model">
                                <div class="input-group-append">
                                    <button class="btn btn-primary" 
                                            onclick="updateNewModel(${printer.id}, ${request.id})">
                                        <i class="fas fa-save"></i> Save
                                    </button>
                                </div>
                            </div>
                            ${printer.newModel ? `<small class="text-muted">Last updated: ${printer.newModelUpdatedAt || 'N/A'}</small>` : ''}
                        </div>
                    </div>
                    <div class="row mt-3">
                        <div class="col-12">
                            <h6>Comments</h6>
                            <textarea class="form-control" rows="2" readonly>${printer.comments || 'No comments'}</textarea>
                        </div>
                    </div>
                </div>
            </div>
        `;
    });

    $('#printerDetailsBody').html(html);
}

/**
 * Show error in printer details modal
 */
function showPrinterError(message) {
    $('#printerDetailsBody').html(`
        <div class="alert alert-danger">
            <i class="fas fa-exclamation-triangle"></i> ${message}
        </div>
    `);
}

/**
 * Update new model recommendation
 */
function updateNewModel(printerId, reqId) {
    const newModel = $('#newModel_' + printerId).val().trim();

    if (!newModel) {
        showAppAlert('Please enter a new model name', 'warning');
        return;
    }

    console.log('Updating new model for printer #' + printerId + ': ' + newModel);

    // Show loading
    const btn = event.target.closest('button');
    const originalHtml = btn.innerHTML;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';
    btn.disabled = true;

    $.ajax({
        url: contextPath + '/views/replacement/tl/updateNewModel',
        method: 'POST',
        data: {
            printerId: printerId,
            reqId: reqId,
            newModel: newModel
        },
        dataType: 'json',
        success: function(response) {
            if (response.success) {
                // Show success message
                showToast('Success', 'New model updated successfully', 'success');

                // Reload printer details
                showPrintersPopup(reqId);
            } else {
                showAppAlert('Error: ' + response.message, 'danger');
                btn.innerHTML = originalHtml;
                btn.disabled = false;
            }
        },
        error: function(xhr, status, error) {
            showAppAlert('Network error: ' + error, 'danger');
            btn.innerHTML = originalHtml;
            btn.disabled = false;
        }
    });
}

// ============================================================================
// PRINTER HISTORY POPUP - Shows detailed history for one printer
// ============================================================================

/**
 * Show printer history popup
 */
function showPrinterHistory(agrProdId, modelName, serial) {
    console.log('Loading printer history for AGR_PROD_ID: ' + agrProdId);

    // Set header
    $('#printerHistoryModalLabel').html(`
        <i class="fas fa-history"></i> Printer History - ${modelName} (${serial})
    `);

    // Show loading state
    $('#printerHistoryModal').modal('show');
    $('#printerHistoryBody').html('<div class="text-center py-5"><i class="fas fa-spinner fa-spin fa-3x"></i><p class="mt-3">Loading printer history...</p></div>');

    // Fetch printer history via AJAX
    $.ajax({
        url: contextPath + '/views/replacement/tl/getPrinterHistory',
        method: 'GET',
        data: { agrProdId: agrProdId },
        dataType: 'json',
        success: function(response) {
            if (response.success && response.data) {
                renderPrinterHistory(response.data);
            } else {
                showHistoryError(response.message || 'Failed to load printer history');
            }
        },
        error: function(xhr, status, error) {
            console.error('Error loading printer history:', error);
            showHistoryError('Network error: ' + error);
        }
    });
}

/**
 * Render printer history in modal
 */
function renderPrinterHistory(data) {
    const printer = data.printer;
    const serviceCalls = data.serviceCalls;
    const pageCount = data.pageCount || [];

    let html = `
        <div class="row mb-4">
            <div class="col-md-6">
                <div class="card">
                    <div class="card-header bg-primary text-white">
                        <i class="fas fa-print"></i> Printer Information
                    </div>
                    <div class="card-body">
                        <p class="mb-2"><strong>Model:</strong> ${printer.modelName}</p>
                        <p class="mb-2"><strong>Serial No:</strong> ${printer.serial}</p>
                        <p class="mb-2"><strong>Installation Date:</strong> ${printer.installationDate}</p>
                        <p class="mb-2"><strong>Client:</strong> ${printer.clientName}</p>
                        <p class="mb-2"><strong>City:</strong> ${printer.city}</p>
                        <p class="mb-0"><strong>Location:</strong> ${printer.location}</p>
                    </div>
                </div>
            </div>
            <div class="col-md-6">
                <div class="card">
                    <div class="card-header bg-warning text-dark">
                        <i class="fas fa-wrench"></i> Service Calls (Last 6 Months)
                    </div>
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-center mb-3">
                            <div>
                                <h3 class="mb-0">${serviceCalls.count}</h3>
                                <small class="text-muted">Total Calls</small>
                            </div>
                            <div>
                                <h3 class="mb-0 text-danger">${serviceCalls.open}</h3>
                                <small class="text-muted">Open Calls</small>
                            </div>
                        </div>
                        <button class="btn btn-sm btn-outline-primary btn-block" onclick="exportServiceCalls(${printer.agrProdId})">
                            <i class="fas fa-download"></i> Export Service Call Data
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <div class="card">
            <div class="card-header bg-success text-white">
                <i class="fas fa-chart-line"></i> Monthly Page Count (Last 6 Months)
            </div>
            <div class="card-body">
    `;

    if (pageCount && pageCount.length > 0) {
        html += `
            <div class="table-responsive">
                <table class="table table-sm table-bordered">
                    <thead class="thead-light">
                        <tr>
                            <th>Month</th>
                            <th class="text-right">Pages Printed</th>
                            <th class="text-right">Avg. Per Day</th>
                        </tr>
                    </thead>
                    <tbody>
        `;

        pageCount.forEach(month => {
            html += `
                <tr>
                    <td>${month.monthYear}</td>
                    <td class="text-right">${month.pageCount.toLocaleString()}</td>
                    <td class="text-right">${month.avgPerDay}</td>
                </tr>
            `;
        });

        html += `
                    </tbody>
                </table>
            </div>
            <canvas id="pageCountChart" height="80"></canvas>
        `;
    } else {
        html += '<p class="text-muted">No page count data available</p>';
    }

    html += `
            </div>
        </div>
    `;

    $('#printerHistoryBody').html(html);

    // Draw chart if data available
    if (pageCount && pageCount.length > 0) {
        drawPageCountChart(pageCount);
    }
}

/**
 * Draw page count chart
 */
function drawPageCountChart(pageCount) {
    const ctx = document.getElementById('pageCountChart');
    if (!ctx) return;

    const labels = pageCount.map(m => m.monthYear);
    const data = pageCount.map(m => m.pageCount);

    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Pages Printed',
                data: data,
                backgroundColor: 'rgba(40, 167, 69, 0.6)',
                borderColor: 'rgba(40, 167, 69, 1)',
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) {
                            return value.toLocaleString();
                        }
                    }
                }
            }
        }
    });
}

/**
 * Show error in printer history modal
 */
function showHistoryError(message) {
    $('#printerHistoryBody').html(`
        <div class="alert alert-danger">
            <i class="fas fa-exclamation-triangle"></i> ${message}
        </div>
    `);
}

/**
 * Export service calls data
 */
function exportServiceCalls(agrProdId) {
    window.location.href = contextPath + '/views/replacement/tl/exportServiceCalls?agrProdId=' + agrProdId;
}

/**
 * Show toast notification
 */
function showToast(title, message, type) {
    // Using Bootstrap toast or simple alert
    showAppAlert(title + ': ' + message, 'danger');
}

// Context path for AJAX calls
const contextPath = '${pageContext.request.contextPath}';
