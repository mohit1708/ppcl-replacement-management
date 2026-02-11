
const API_BASE = (typeof contextPath !== 'undefined' ? contextPath : '') + '/views/replacement/courier-pincode';
let currentPage = 1;
let pageSize = 10;
let totalPages = 1;
let couriers = [];

// ============================================================================
// INITIALIZE
// ============================================================================
$(document).ready(function() {
    console.log('Courier Pincode Mapping - Loaded');
    loadCouriers();
    loadMappings();

    // Auto-filter on courier dropdown change
    $('#filterCourier').on('change', function() {
        currentPage = 1;
        loadMappings();
    });

    // Auto-filter on pincode input with debounce
    let pincodeTimer;
    $('#filterPincode').on('input', function() {
        clearTimeout(pincodeTimer);
        pincodeTimer = setTimeout(function() {
            currentPage = 1;
            loadMappings();
        }, 400); // 400ms delay after typing stops
    });
});

// ============================================================================
// LOAD COURIERS
// ============================================================================
function loadCouriers() {
    $.ajax({
        url: API_BASE + '/couriers',
        method: 'GET',
        dataType: 'json',
        success: function(response) {
            if (response.success && response.data) {
                couriers = response.data;
                populateCourierDropdowns();
            }
        },
        error: function(xhr, status, error) {
            console.error('Error loading couriers:', error);
        }
    });
}

function populateCourierDropdowns() {
    let options = '<option value="">-- Select Courier --</option>';
    couriers.forEach(function(c) {
        options += '<option value="' + c.id + '">' + escapeHtml(c.name) + '</option>';
    });
    $('#manualCourier, #bulkCourier, #editCourier').html(options);

    let filterOptions = '<option value="">All Couriers</option>';
    couriers.forEach(function(c) {
        filterOptions += '<option value="' + escapeHtml(c.name) + '">' + escapeHtml(c.name) + '</option>';
    });
    $('#filterCourier').html(filterOptions);
}

// ============================================================================
// LOAD MAPPINGS
// ============================================================================
function loadMappings() {
    const courierName = $('#filterCourier').val();
    const pincode = $('#filterPincode').val();

    $.ajax({
        url: API_BASE + '/mappings',
        method: 'GET',
        data: {
            courierName: courierName,
            pincode: pincode,
            page: currentPage,
            pageSize: pageSize
        },
        dataType: 'json',
        success: function(response) {
            if (response.success && response.data) {
                renderMappings(response.data.mappings);
                totalPages = response.data.totalPages;
                renderPagination(response.data);
                $('#totalRecords').text(response.data.totalCount + ' records');
            } else {
                showEmptyTable();
            }
        },
        error: function(xhr, status, error) {
            console.error('Error loading mappings:', error);
            showEmptyTable('Error loading data');
        }
    });
}

function renderMappings(mappings) {
    if (!mappings || mappings.length === 0) {
        showEmptyTable();
        return;
    }

    let html = '';
    let startNum = (currentPage - 1) * pageSize;

    mappings.forEach(function(m, index) {
        // Handle status as number (1/0) or string ('ACTIVE'/'INACTIVE')
        const statusValue = getStatusText(m.status);
        const isActive = statusValue === 'ACTIVE';
        const statusClass = isActive ? 'success' : 'danger';
        const statusIcon = isActive ? 'check-circle' : 'times-circle';
        // Handle pincode as number or string
        const pincodeStr = String(m.pincode || '');

        html += '<tr>';
        html += '<td>' + (startNum + index + 1) + '</td>';
        html += '<td><strong>' + escapeHtml(m.courierName || '') + '</strong></td>';
        html += '<td><code>' + pincodeStr + '</code></td>';
        html += '<td>' + escapeHtml(m.city || '-') + '</td>';
        html += '<td>' + escapeHtml(m.state || '-') + '</td>';
        html += '<td>' + escapeHtml(m.region || '-') + '</td>';
        html += '<td>';
        html += '<span class="badge badge-' + statusClass + ' status-badge" onclick="showToggleConfirm(' + m.id + ', \'' + statusValue + '\', \'' + pincodeStr + '\')">';
        html += '<i class="fas fa-' + statusIcon + '"></i> ' + statusValue;
        html += '</span>';
        html += '</td>';
        html += '<td>';
        html += '<button class="btn btn-sm btn-warning" onclick="openEditModal(' + m.id + ')">';
        html += '<i class="fas fa-edit"></i> Edit';
        html += '</button>';
        html += '</td>';
        html += '</tr>';
    });

    $('#mappingsBody').html(html);
}

function showEmptyTable(message) {
    const msg = message || 'No mappings found';
    $('#mappingsBody').html('<tr><td colspan="8" class="text-center py-5"><i class="fas fa-inbox fa-3x text-muted"></i><p class="text-muted mt-2">' + msg + '</p></td></tr>');
    $('#pagination').html('');
    $('#totalRecords').text('0 records');
}

function renderPagination(data) {
    if (data.totalPages <= 1) {
        $('#pagination').html('');
        return;
    }

    let html = '';

    // Previous
    const prevDisabled = currentPage === 1 ? 'disabled' : '';
    html += '<li class="page-item ' + prevDisabled + '"><a class="page-link" href="#" onclick="goToPage(' + (currentPage - 1) + '); return false;"><i class="fas fa-chevron-left"></i></a></li>';

    // Pages
    let startPage = Math.max(1, currentPage - 2);
    let endPage = Math.min(data.totalPages, currentPage + 2);

    if (startPage > 1) {
        html += '<li class="page-item"><a class="page-link" href="#" onclick="goToPage(1); return false;">1</a></li>';
        if (startPage > 2) html += '<li class="page-item disabled"><span class="page-link">...</span></li>';
    }

    for (let i = startPage; i <= endPage; i++) {
        const activeClass = i === currentPage ? 'active' : '';
        html += '<li class="page-item ' + activeClass + '"><a class="page-link" href="#" onclick="goToPage(' + i + '); return false;">' + i + '</a></li>';
    }

    if (endPage < data.totalPages) {
        if (endPage < data.totalPages - 1) html += '<li class="page-item disabled"><span class="page-link">...</span></li>';
        html += '<li class="page-item"><a class="page-link" href="#" onclick="goToPage(' + data.totalPages + '); return false;">' + data.totalPages + '</a></li>';
    }

    // Next
    const nextDisabled = currentPage === data.totalPages ? 'disabled' : '';
    html += '<li class="page-item ' + nextDisabled + '"><a class="page-link" href="#" onclick="goToPage(' + (currentPage + 1) + '); return false;"><i class="fas fa-chevron-right"></i></a></li>';

    $('#pagination').html(html);
}

function goToPage(page) {
    if (page < 1 || page > totalPages) return;
    currentPage = page;
    loadMappings();
}

// ============================================================================
// FILTERS
// ============================================================================
function applyFilters() {
    currentPage = 1;
    loadMappings();
}

function clearFilters() {
    $('#filterCourier').val('');
    $('#filterPincode').val('');
    currentPage = 1;
    loadMappings();
}

// ============================================================================
// ADD MAPPING
// ============================================================================
function submitAdd() {
    const activeTab = $('#manualTab').hasClass('active') ? 'manual' : 'bulk';

    if (activeTab === 'manual') {
        submitManualAdd();
    } else {
        submitBulkUpload();
    }
}

function submitManualAdd() {
    const courierId = $('#manualCourier').val();
    const pincodes = $('#manualPincodes').val().trim();

    // Clear previous error styling
    $('#manualCourier').removeClass('is-invalid');
    $('#manualPincodes').removeClass('is-invalid');
    $('#manualResult').html('');

    let hasError = false;
    let errorMessages = [];

    if (!courierId) {
        $('#manualCourier').addClass('is-invalid');
        errorMessages.push('Please select a courier');
        hasError = true;
    }
    if (!pincodes) {
        $('#manualPincodes').addClass('is-invalid');
        errorMessages.push('Please enter at least one pincode');
        hasError = true;
    } else {
        // Validate pincode format
        const pincodeArr = pincodes.split(',').map(p => p.trim()).filter(p => p);
        const invalidPincodes = pincodeArr.filter(p => !/^\d{6}$/.test(p));
        if (invalidPincodes.length > 0) {
            $('#manualPincodes').addClass('is-invalid');
            errorMessages.push('Invalid pincodes: ' + invalidPincodes.join(', ') + '. Each pincode must be exactly 6 digits.');
            hasError = true;
        }
    }

    if (hasError) {
        showValidationError('manualResult', errorMessages);
        return;
    }

    $('#submitAddBtn').html('<i class="fas fa-spinner fa-spin"></i> Processing...').prop('disabled', true);

    $.ajax({
        url: API_BASE + '/add',
        method: 'POST',
        data: {
            courierId: courierId,
            pincodes: pincodes
        },
        dataType: 'json',
        success: function(response) {
            $('#submitAddBtn').html('<i class="fas fa-save"></i> Add Mapping').prop('disabled', false);
            showAddResult(response, 'manual');
            if (response.success && !response.partial) {
                setTimeout(function() {
                    $('#addMappingModal').modal('hide');
                    resetAddForm();
                    loadMappings();
                }, 2000);
            } else if (response.success) {
                loadMappings();
            }
        },
        error: function(xhr, status, error) {
            $('#submitAddBtn').html('<i class="fas fa-save"></i> Add Mapping').prop('disabled', false);
            alert('Error: ' + error);
        }
    });
}

function submitBulkUpload() {
    const courierId = $('#bulkCourier').val();
    const fileInput = document.getElementById('bulkFile');

    // Clear previous error styling
    $('#bulkCourier').removeClass('is-invalid');
    $('#bulkResult').html('');

    let hasError = false;
    let errorMessages = [];

    if (!courierId) {
        $('#bulkCourier').addClass('is-invalid');
        errorMessages.push('Please select a courier');
        hasError = true;
    }
    if (!fileInput.files || fileInput.files.length === 0) {
        errorMessages.push('Please select an Excel file (.xlsx) to upload');
        hasError = true;
    } else {
        const fileName = fileInput.files[0].name.toLowerCase();
        if (!fileName.endsWith('.xlsx') && !fileName.endsWith('.xls')) {
            errorMessages.push('Invalid file format. Please upload an Excel file (.xlsx or .xls)');
            hasError = true;
        }
    }

    if (hasError) {
        showValidationError('bulkResult', errorMessages);
        return;
    }

    const formData = new FormData();
    formData.append('courierId', courierId);
    formData.append('file', fileInput.files[0]);

    $('#submitAddBtn').html('<i class="fas fa-spinner fa-spin"></i> Uploading...').prop('disabled', true);

    $.ajax({
        url: API_BASE + '/upload',
        method: 'POST',
        data: formData,
        processData: false,
        contentType: false,
        dataType: 'json',
        success: function(response) {
            $('#submitAddBtn').html('<i class="fas fa-save"></i> Add Mapping').prop('disabled', false);
            showAddResult(response, 'bulk');
            if (response.success && !response.partial) {
                setTimeout(function() {
                    $('#addMappingModal').modal('hide');
                    resetAddForm();
                    loadMappings();
                }, 2000);
            } else if (response.success) {
                loadMappings();
            }
        },
        error: function(xhr, status, error) {
            $('#submitAddBtn').html('<i class="fas fa-save"></i> Add Mapping').prop('disabled', false);
            alert('Error: ' + error);
        }
    });
}

function showAddResult(response, type) {
    const targetId = type === 'manual' ? '#manualResult' : '#bulkResult';
    let html = '';

    if (response.success && !response.partial) {
        html = '<div class="result-success mt-3">';
        html += '<i class="fas fa-check-circle text-success"></i> ';
        html += '<strong>' + escapeHtml(response.message) + '</strong>';
        html += '<p class="mb-0 mt-2">' + response.data.successCount + ' pincode(s) added successfully</p>';
        html += '</div>';
    } else if (response.partial) {
        html = '<div class="result-partial mt-3">';
        html += '<i class="fas fa-exclamation-triangle text-warning"></i> ';
        html += '<strong>' + escapeHtml(response.message) + '</strong>';
        html += '<div class="row mt-2"><div class="col-6 text-center"><h4 class="text-success">' + response.data.successCount + '</h4><small>Success</small></div>';
        html += '<div class="col-6 text-center"><h4 class="text-danger">' + response.data.failureCount + '</h4><small>Failed</small></div></div>';
        html += renderFailureList(response.data.failureList);
        html += '</div>';
    } else {
        html = '<div class="result-error mt-3">';
        html += '<i class="fas fa-times-circle text-danger"></i> ';
        html += '<strong>' + escapeHtml(response.message) + '</strong>';
        if (response.data && response.data.failureList) {
            html += renderFailureList(response.data.failureList);
        }
        html += '</div>';
    }

    $(targetId).html(html);
}

function renderFailureList(failures) {
    if (!failures || failures.length === 0) return '';

    let html = '<div class="failure-list mt-3"><h6 class="text-danger"><i class="fas fa-list"></i> Failed Pincodes:</h6>';
    failures.forEach(function(f) {
        const pincodeStr = String(f.pincode || '');
        html += '<div class="failure-item"><strong>' + pincodeStr + '</strong> - <span class="text-danger">' + escapeHtml(f.errorMessage) + '</span></div>';
    });
    html += '</div>';
    return html;
}

function handleFileSelect(input) {
    if (input.files && input.files[0]) {
        $('#selectedFileName').text('Selected: ' + input.files[0].name).show();
    }
}

function resetAddForm() {
    $('#manualCourier').val('').removeClass('is-invalid');
    $('#manualPincodes').val('').removeClass('is-invalid');
    $('#bulkCourier').val('').removeClass('is-invalid');
    $('#bulkFile').val('');
    $('#manualResult').html('');
    $('#bulkResult').html('');
    $('#selectedFileName').hide();
}

// ============================================================================
// EDIT MAPPING
// ============================================================================
function openEditModal(mappingId) {
    $.ajax({
        url: API_BASE + '/getMappingById',
        method: 'GET',
        data: { id: mappingId },
        dataType: 'json',
        success: function(response) {
            if (response.success && response.data) {
                const m = response.data;
                $('#editMappingId').val(m.id);
                $('#editPincode').val(m.pincode);
                $('#editCourier').val(m.courierId);
                $('#editCity').val(m.city || '');
                $('#editState').val(m.state || '');
                $('#editRegion').val(m.region || '');
                $('#editMappingModal').modal('show');
            } else {
                alert('Error loading mapping');
            }
        },
        error: function(xhr, status, error) {
            alert('Error: ' + error);
        }
    });
}

function submitEdit() {
    const mappingId = $('#editMappingId').val();
    const courierId = $('#editCourier').val();

    // Clear previous error styling
    $('#editCourier').removeClass('is-invalid');

    if (!courierId) {
        $('#editCourier').addClass('is-invalid');
        showToast('Validation Error', 'Please select a courier', 'error');
        return;
    }

    $.ajax({
        url: API_BASE + '/edit',
        method: 'POST',
        data: {
            mappingId: mappingId,
            courierId: courierId,
            city: $('#editCity').val(),
            state: $('#editState').val(),
            region: $('#editRegion').val()
        },
        dataType: 'json',
        success: function(response) {
            if (response.success) {
                $('#editMappingModal').modal('hide');
                //alert('Mapping updated successfully');
                loadMappings();
            } else {
                alert('Error: ' + response.message);
            }
        },
        error: function(xhr, status, error) {
            alert('Error: ' + error);
        }
    });
}

// ============================================================================
// STATUS TOGGLE
// ============================================================================
function showToggleConfirm(mappingId, currentStatus, pincode) {
    const newStatus = currentStatus === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    const action = newStatus === 'ACTIVE' ? 'enable' : 'disable';

    $('#toggleMappingId').val(mappingId);
    $('#toggleNewStatus').val(newStatus);
    $('#confirmStatusMessage').html('Are you sure you want to <strong>' + action + '</strong> the mapping for pincode <strong>' + escapeHtml(pincode) + '</strong>?');
    $('#confirmStatusModal').modal('show');
}

function confirmToggleStatus() {
    const mappingId = $('#toggleMappingId').val();
    const newStatus = $('#toggleNewStatus').val();

    $.ajax({
        url: API_BASE + '/toggleStatus',
        method: 'POST',
        data: {
            mappingId: mappingId,
            newStatus: newStatus
        },
        dataType: 'json',
        success: function(response) {
            $('#confirmStatusModal').modal('hide');
            if (response.success) {
                loadMappings();
            } else {
                alert('Error: ' + response.message);
            }
        },
        error: function(xhr, status, error) {
            alert('Error: ' + error);
        }
    });
}

// ============================================================================
// EXPORT
// ============================================================================
function exportData(format) {
    const courierName = $('#filterCourier').val();
    const pincode = $('#filterPincode').val();

    let url = API_BASE + '/export?format=' + format;
    if (courierName) url += '&courierName=' + encodeURIComponent(courierName);
    if (pincode) url += '&pincode=' + encodeURIComponent(pincode);

    window.location.href = url;
}

// ============================================================================
// UTILITIES
// ============================================================================
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Convert status value (number or string) to display text
function getStatusText(status) {
    if (status === 1 || status === '1') return 'ACTIVE';
    if (status === 0 || status === '0') return 'INACTIVE';
    if (status === 'ACTIVE' || status === 'INACTIVE') return status;
    return status == 1 ? 'ACTIVE' : 'INACTIVE';
}

function showValidationError(targetId, messages) {
    let html = '<div class="result-error mt-3">';
    html += '<i class="fas fa-exclamation-circle text-danger"></i> ';
    html += '<strong>Validation Error</strong>';
    html += '<ul class="mb-0 mt-2">';
    messages.forEach(function(msg) {
        html += '<li>' + escapeHtml(msg) + '</li>';
    });
    html += '</ul>';
    html += '</div>';
    $('#' + targetId).html(html);
}

function showToast(title, message, type) {
    const alertClass = type === 'error' ? 'alert-danger' : (type === 'success' ? 'alert-success' : 'alert-warning');
    const icon = type === 'error' ? 'times-circle' : (type === 'success' ? 'check-circle' : 'exclamation-triangle');

    // Create toast container if not exists
    if ($('#toastContainer').length === 0) {
        $('body').append('<div id="toastContainer" style="position: fixed; top: 20px; right: 20px; z-index: 9999;"></div>');
    }

    const toastHtml = '<div class="alert ' + alertClass + ' alert-dismissible fade show" role="alert">' +
        '<i class="fas fa-' + icon + ' mr-2"></i>' +
        '<strong>' + escapeHtml(title) + ':</strong> ' + escapeHtml(message) +
        '<button type="button" class="close" data-dismiss="alert"><span>&times;</span></button>' +
        '</div>';

    const $toast = $(toastHtml);
    $('#toastContainer').append($toast);

    // Auto-dismiss after 5 seconds
    setTimeout(function() {
        $toast.alert('close');
    }, 5000);
}

// Reset form when modal closes
$('#addMappingModal').on('hidden.bs.modal', function() {
    resetAddForm();
});
